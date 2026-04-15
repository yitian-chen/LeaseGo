package com.zju.lease.auth.service;

import com.zju.lease.auth.vo.login.CaptchaVo;
import com.zju.lease.auth.vo.login.AdminLoginVo;
import com.zju.lease.auth.vo.system.SystemUserInfoVo;

public interface AdminLoginService {
    CaptchaVo getCaptcha();
    String login(AdminLoginVo loginVo);
    SystemUserInfoVo getLoginUserInfoById(Long userId);
}
