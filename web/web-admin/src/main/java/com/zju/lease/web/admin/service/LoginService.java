package com.zju.lease.web.admin.service;

import com.zju.lease.web.admin.vo.login.CaptchaVo;
import com.zju.lease.web.admin.vo.login.LoginVo;

public interface LoginService {

    CaptchaVo getCaptcha();

    String login(LoginVo loginVo);
}
