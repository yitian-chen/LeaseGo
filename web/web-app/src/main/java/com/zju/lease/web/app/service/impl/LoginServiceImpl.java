package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wf.captcha.SpecCaptcha;
import com.zju.lease.common.constant.RedisConstant;
import com.zju.lease.common.exception.LeaseException;
import com.zju.lease.common.result.ResultCodeEnum;
import com.zju.lease.common.utils.JwtUtil;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.model.enums.BaseStatus;
import com.zju.lease.web.app.mapper.UserInfoMapper;
import com.zju.lease.web.app.service.LoginService;
import com.zju.lease.web.app.service.SmsService;
import com.zju.lease.web.app.vo.user.CaptchaVo;
import com.zju.lease.web.app.vo.user.LoginVo;
import com.zju.lease.web.app.vo.user.UserInfoVo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserInfoMapper userInfoMapper;

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
    public String login(LoginVo loginVo) {
        if (loginVo.getPhone() == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        // 使用自定义方法查询包含密码的用户信息
        UserInfo userInfo = userInfoMapper.selectOneByPhone(loginVo.getPhone());

        // 验证图形验证码
        if (loginVo.getCaptchaCode() == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }

        String code = redisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        if (!code.equals(loginVo.getCaptchaCode().toLowerCase())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        // 根据策略分发逻辑
        if (loginVo.getStrategy() == 1) {
            return loginBySmsCode(loginVo, userInfo);
        } else if (loginVo.getStrategy() == 2) {
            return loginByPassword(loginVo, userInfo);
        } else {
            throw new LeaseException(ResultCodeEnum.APP_INVALID_LOGIN_STRATAGY);
        }
    }

    private String loginBySmsCode(LoginVo loginVo, UserInfo userInfo) {
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

        // 验证通过，如果是新用户
        if (userInfo == null) {
            // 初次登录强制要求传递 password 字段
            if (StringUtils.isBlank(loginVo.getPassword())) {
                throw new LeaseException(ResultCodeEnum.APP_PASSWORD_REQUIRED_FOR_NEW);
            }
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-" + loginVo.getPhone().substring(7));
            userInfo.setPassword(DigestUtils.md5Hex(loginVo.getPassword()));
            userInfoMapper.insert(userInfo);
        } else {
            // 老用户检查状态
            if (userInfo.getStatus() == BaseStatus.DISABLE) {
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
            // 如果老用户之前没设过密码，或者通过短信登录想重置密码，可以开放此逻辑：
            if (StringUtils.isNotBlank(loginVo.getPassword())) {
                userInfo.setPassword(DigestUtils.md5Hex(loginVo.getPassword()));
                userInfoMapper.updateById(userInfo);
            }
        }

        // 登录成功，清理 redis 验证码防止复用
        redisTemplate.delete(key);
        return JwtUtil.createToken(userInfo.getId(), userInfo.getNickname());
    }

    private String loginByPassword(LoginVo loginVo, UserInfo userInfo) {
        // 验证用户及密码
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_PHONE_UNSIGNED_UP);
        }
        if (userInfo.getStatus() == BaseStatus.DISABLE) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }
        if (StringUtils.isBlank(userInfo.getPassword())) {
            throw new LeaseException(ResultCodeEnum.APP_PASSWORD_NOT_SET);
        }
        if (!userInfo.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_ERROR); // 复用或新建密码错误Enum
        }

        // 登录成功，清理 redis 图形验证码
        redisTemplate.delete(loginVo.getCaptchaKey());
        return JwtUtil.createToken(userInfo.getId(), userInfo.getNickname());
    }

    @Override
    public UserInfoVo getLoginUserById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
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
    public void updateNickname(Long userId, String nickname) {
        // 检查非空
        if(StringUtils.isBlank(nickname)){
            throw new LeaseException(ResultCodeEnum.APP_USERNAME_EMPTY);
        }

        // 检查唯一性
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getNickname, nickname);
        Long count = userInfoMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new LeaseException(ResultCodeEnum.APP_USERNAME_EXIST);
        }

        // 更新用户名
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setNickname(nickname);
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public void updateAvatar(Long userId, String url) {
        if (StringUtils.isBlank(url)) {
            throw new LeaseException(ResultCodeEnum.APP_AVATAR_URL_EMPTY);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setAvatarUrl(url);
        userInfoMapper.updateById(userInfo);
    }
}
