package com.zju.lease.auth.service.impl;

import com.wf.captcha.SpecCaptcha;
import com.zju.lease.auth.feign.UpmsFeignClient;
import com.zju.lease.auth.service.AdminLoginService;
import com.zju.lease.auth.vo.login.CaptchaVo;
import com.zju.lease.auth.vo.login.AdminLoginVo;
import com.zju.lease.auth.vo.system.SystemUserInfoVo;
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
public class AdminLoginServiceImpl implements AdminLoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UpmsFeignClient upmsFeignClient;

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        String code = specCaptcha.text().toLowerCase();
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();

        stringRedisTemplate.opsForValue().set(key, code, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);

        return new CaptchaVo(specCaptcha.toBase64(), key);
    }

    @Override
    public String login(AdminLoginVo loginVo) {
        if (loginVo.getCaptchaCode() == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }

        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        if (!code.equals(loginVo.getCaptchaCode().toLowerCase())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        // 调用UPMS服务验证用户名密码
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", loginVo.getUsername());
        credentials.put("password", loginVo.getPassword());

        Result<Map<String, Object>> result = upmsFeignClient.verifyAdmin(credentials);

        if (result.getCode() != 200) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        Map<String, Object> data = result.getData();
        Long userId = ((Number) data.get("userId")).longValue();
        String username = (String) data.get("username");

        return JwtUtil.createToken(userId, username);
    }

    @Override
    public SystemUserInfoVo getLoginUserInfoById(Long userId) {
        Result<SystemUserInfoVo> result = upmsFeignClient.getAdminInfo(userId);
        if (result.getCode() == 200) {
            return result.getData();
        }
        return new SystemUserInfoVo();
    }
}
