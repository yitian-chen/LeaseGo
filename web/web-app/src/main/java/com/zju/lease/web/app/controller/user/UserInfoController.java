package com.zju.lease.web.app.controller.user;

import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.web.app.service.LoginService;
import com.zju.lease.web.app.service.UserInfoService;
import com.zju.lease.web.app.vo.user.UserSearchVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/app/user")
@Tag(name = "用户信息")
public class UserInfoController {

    @Autowired
    private LoginService service;

    @Autowired
    private UserInfoService userInfoService;

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

    @GetMapping("/search")
    @Operation(summary = "搜索用户")
    public Result<List<UserSearchVo>> searchUsers(@RequestParam String keyword) {
        Long currentUserId = LoginUserHolder.getLoginUser().getUserId();
        List<UserInfo> users = userInfoService.searchUsers(keyword, currentUserId);

        List<UserSearchVo> result = new ArrayList<>();
        for (UserInfo user : users) {
            UserSearchVo vo = new UserSearchVo();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setPhone(user.getPhone());
            vo.setAvatarUrl(user.getAvatarUrl());
            result.add(vo);
        }
        return Result.ok(result);
    }
}
