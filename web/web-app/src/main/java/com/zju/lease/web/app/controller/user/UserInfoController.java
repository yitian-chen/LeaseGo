package com.zju.lease.web.app.controller.user;

import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import com.zju.lease.web.app.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/user")
@Tag(name = "更改用户信息")
public class UserInfoController {

    @Autowired
    private LoginService service;

    @Operation(summary = "更改用户头像")
    @PostMapping("/avatar")
    public Result updateAvatar(@RequestParam String url) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        service.updateAvatar(userId, url);
        return Result.ok();
    }

    @PostMapping("/updateNickname")
    @Operation(summary = "修改用户名")
    public Result updateNickname(@RequestParam String nickname) {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        service.updateNickname(userId, nickname);
        return Result.ok();
    }
}
