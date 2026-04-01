package com.zju.lease.web.app.service;

import com.zju.lease.web.app.vo.user.CaptchaVo;
import com.zju.lease.web.app.vo.user.LoginVo;
import com.zju.lease.web.app.vo.user.UserInfoVo;

public interface LoginService {
    void getCode(String phone) throws Exception;

    String login(LoginVo loginVo);

    UserInfoVo getLoginUserById(Long userId);

    CaptchaVo getCaptcha();

    void updateNickname(Long userId, String nickname);
}
