package com.zju.lease.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wf.captcha.SpecCaptcha;
import com.zju.lease.auth.feign.UpmsFeignClient;
import com.zju.lease.auth.service.AppLoginService;
import com.zju.lease.auth.service.SmsService;
import com.zju.lease.auth.vo.user.CaptchaVo;
import com.zju.lease.auth.vo.user.AppLoginVo;
import com.zju.lease.auth.vo.user.UserInfoVo;
import com.zju.lease.common.constant.RedisConstant;
import com.zju.lease.common.exception.LeaseException;
import com.zju.lease.common.result.Result;
import com.zju.lease.common.result.ResultCodeEnum;
import com.zju.lease.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AppLoginServiceImpl implements AppLoginService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UpmsFeignClient upmsFeignClient;

    @Override
    public void getCode(String phone) throws Exception {
        String key = RedisConstant.APP_LOGIN_PREFIX + phone;

        Boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_CODE_TTL_SEC - ttl < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC) {
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }

        String code = smsService.sendCode(phone);
        redisTemplate.opsForValue().set(key, code, RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        String code = specCaptcha.text().toLowerCase();
        String key = RedisConstant.APP_LOGIN_CAPTCHA_PREFIX + UUID.randomUUID();

        redisTemplate.opsForValue().set(key, code, RedisConstant.APP_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        return new CaptchaVo(specCaptcha.toBase64(), key);
    }

    @Override
    public String login(AppLoginVo loginVo) {
        if (loginVo.getPhone() == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        // 验证图形验证码
        if (loginVo.getCaptchaCode() == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }

        String captchaCode = redisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if (captchaCode == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        if (!captchaCode.equals(loginVo.getCaptchaCode().toLowerCase())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        // 根据策略分发逻辑
        if (loginVo.getStrategy() == 1) {
            return loginBySmsCode(loginVo);
        } else if (loginVo.getStrategy() == 2) {
            return loginByPassword(loginVo);
        } else {
            throw new LeaseException(ResultCodeEnum.APP_INVALID_LOGIN_STRATAGY);
        }
    }

    private String loginBySmsCode(AppLoginVo loginVo) {
        if (loginVo.getCode() == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        String key = RedisConstant.APP_LOGIN_PREFIX + loginVo.getPhone();
        String code = redisTemplate.opsForValue().get(key);

        if (code == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 通过UPMS查询用户
        Map<String, String> query = new HashMap<>();
        query.put("phone", loginVo.getPhone());
        Result<Map<String, Object>> userResult = upmsFeignClient.verifyAdmin(query);

        Long userId;
        String nickname;

        if (userResult.getCode() != 200 || userResult.getData() == null) {
            // 新用户，需要创建
            if (StringUtils.isBlank(loginVo.getPassword())) {
                throw new LeaseException(ResultCodeEnum.APP_PASSWORD_REQUIRED_FOR_NEW);
            }

            Map<String, Object> newUser = new HashMap<>();
            newUser.put("phone", loginVo.getPhone());
            newUser.put("password", loginVo.getPassword());
            newUser.put("nickname", "用户-" + loginVo.getPhone().substring(7));

            Result<Long> createResult = upmsFeignClient.createUser(newUser);
            userId = createResult.getData();
            nickname = "用户-" + loginVo.getPhone().substring(7);
        } else {
            Map<String, Object> userData = userResult.getData();
            userId = ((Number) userData.get("id")).longValue();
            nickname = (String) userData.get("nickname");

            // 检查账号状态
            Integer status = (Integer) userData.get("status");
            if (status != 1) {
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }

            // 如果提供了新密码，更新
            if (StringUtils.isNotBlank(loginVo.getPassword())) {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("id", userId);
                updateData.put("password", loginVo.getPassword());
                upmsFeignClient.updateUser(updateData);
            }
        }

        // 清理redis验证码
        redisTemplate.delete(key);
        return JwtUtil.createToken(userId, nickname);
    }

    private String loginByPassword(AppLoginVo loginVo) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("phone", loginVo.getPhone());
        credentials.put("password", loginVo.getPassword());

        Result<Map<String, Object>> result = upmsFeignClient.verifyAdmin(credentials);

        if (result.getCode() != 200) {
            throw new LeaseException(ResultCodeEnum.APP_PHONE_UNSIGNED_UP);
        }

        Map<String, Object> userData = result.getData();
        Long userId = ((Number) userData.get("id")).longValue();
        String nickname = (String) userData.get("nickname");

        // 清理图形验证码
        redisTemplate.delete(loginVo.getCaptchaKey());
        return JwtUtil.createToken(userId, nickname);
    }

    @Override
    public UserInfoVo getUserInfoById(Long userId) {
        Result<UserInfoVo> result = upmsFeignClient.getUserInfo(userId);
        if (result.getCode() == 200 && result.getData() != null) {
            return result.getData();
        }
        return new UserInfoVo();
    }
}
