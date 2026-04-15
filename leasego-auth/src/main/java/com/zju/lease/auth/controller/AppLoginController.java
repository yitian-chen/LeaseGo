package com.zju.lease.auth.controller;

import com.zju.lease.auth.service.AppLoginService;
import com.zju.lease.auth.vo.user.AppLoginVo;
import com.zju.lease.auth.vo.user.CaptchaVo;
import com.zju.lease.auth.vo.user.UserInfoVo;
import com.zju.lease.common.login.LoginUser;
import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "App用户登录")
@RestController
@RequestMapping("/app")
public class AppLoginController {

    @Autowired
    private AppLoginService appLoginService;

    @Operation(summary = "获取短信验证码")
    @GetMapping("login/getCode")
    public Result<Void> getCode(@RequestParam String phone) throws Exception {
        appLoginService.getCode(phone);
        return Result.ok();
    }

    @Operation(summary = "获取图形验证码")
    @GetMapping("login/captcha")
    public Result<CaptchaVo> getCaptcha() {
        CaptchaVo result = appLoginService.getCaptcha();
        return Result.ok(result);
    }

    @Operation(summary = "登录(支持短信和密码)")
    @PostMapping("login")
    public Result<String> login(@RequestBody AppLoginVo loginVo) {
        String token = appLoginService.login(loginVo);
        return Result.ok(token);
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("info")
    public Result<UserInfoVo> info() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        UserInfoVo info = appLoginService.getUserInfoById(userId);
        return Result.ok(info);
    }
}
