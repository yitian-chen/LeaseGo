package com.zju.lease.auth.service;

import com.zju.lease.auth.vo.user.CaptchaVo;
import com.zju.lease.auth.vo.user.AppLoginVo;
import com.zju.lease.auth.vo.user.UserInfoVo;

public interface AppLoginService {
    void getCode(String phone) throws Exception;
    CaptchaVo getCaptcha();
    String login(AppLoginVo loginVo);
    UserInfoVo getUserInfoById(Long userId);
}
