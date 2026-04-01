package com.zju.lease.web.app.controller.login;


import com.zju.lease.common.login.LoginUser;
import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import com.zju.lease.web.app.service.LoginService;
import com.zju.lease.web.app.vo.user.CaptchaVo;
import com.zju.lease.web.app.vo.user.LoginVo;
import com.zju.lease.web.app.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")
public class LoginController {

    @Autowired
    private LoginService service;

    @GetMapping("login/getCode")
    @Operation(summary = "获取短信验证码")
    public Result getCode(@RequestParam String phone) throws Exception {
        service.getCode(phone);
        return Result.ok();
    }

    @GetMapping("login/captcha")
    @Operation(summary = "获取图形验证码")
    public Result<CaptchaVo> getCaptcha() {
        CaptchaVo result = service.getCaptcha();
        return Result.ok(result);
    }

    @PostMapping("login")
    @Operation(summary = "登录(支持短信和密码)")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String token = service.login(loginVo);
        return Result.ok(token);
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        UserInfoVo info = service.getLoginUserById(userId);
        return Result.ok(info);
    }

    @PostMapping("updateNickname")
    @Operation(summary = "修改用户名")
    public Result updateNickname(@RequestParam String nickname) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        service.updateNickname(userId, nickname);
        return Result.ok();
    }
}

