package com.zju.lease.auth.controller;

import com.zju.lease.auth.service.AdminLoginService;
import com.zju.lease.auth.vo.login.AdminLoginVo;
import com.zju.lease.auth.vo.login.CaptchaVo;
import com.zju.lease.auth.vo.system.SystemUserInfoVo;
import com.zju.lease.common.login.LoginUser;
import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员登录")
@RestController
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private AdminLoginService adminLoginService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("login/captcha")
    public Result<CaptchaVo> getCaptcha() {
        CaptchaVo result = adminLoginService.getCaptcha();
        return Result.ok(result);
    }

    @Operation(summary = "管理员登录")
    @PostMapping("login")
    public Result<String> login(@RequestBody AdminLoginVo loginVo) {
        String jwt = adminLoginService.login(loginVo);
        return Result.ok(jwt);
    }

    @Operation(summary = "获取登录用户信息")
    @GetMapping("info")
    public Result<SystemUserInfoVo> info() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        SystemUserInfoVo info = adminLoginService.getLoginUserInfoById(userId);
        return Result.ok(info);
    }
}
