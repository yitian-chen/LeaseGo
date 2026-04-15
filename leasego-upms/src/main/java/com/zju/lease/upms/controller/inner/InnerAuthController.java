package com.zju.lease.upms.controller.inner;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zju.lease.common.exception.LeaseException;
import com.zju.lease.common.result.Result;
import com.zju.lease.common.result.ResultCodeEnum;
import com.zju.lease.model.entity.SystemUser;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.model.enums.BaseStatus;
import com.zju.lease.upms.mapper.SystemUserMapper;
import com.zju.lease.upms.mapper.UserInfoMapper;
import com.zju.lease.upms.vo.SystemUserInfoVo;
import com.zju.lease.upms.vo.UserInfoVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "内部接口-认证服务调用")
@RestController
@RequestMapping("/inner")
public class InnerAuthController {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @PostMapping("/admin/verify")
    public Result<Map<String, Object>> verifyAdmin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        SystemUser systemUser = systemUserMapper.selectOneByUsername(username);

        if (systemUser == null) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR.getCode(),
                    ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR.getMessage());
        }

        if (systemUser.getStatus() == BaseStatus.DISABLE) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR.getCode(),
                    ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR.getMessage());
        }

        if (!systemUser.getPassword().equals(DigestUtils.md5Hex(password))) {
            return Result.fail(ResultCodeEnum.ADMIN_ACCOUNT_ERROR.getCode(),
                    ResultCodeEnum.ADMIN_ACCOUNT_ERROR.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", systemUser.getId());
        data.put("username", systemUser.getUsername());
        data.put("name", systemUser.getName());
        return Result.ok(data);
    }

    @GetMapping("/admin/info/{id}")
    public Result<SystemUserInfoVo> getAdminInfo(@PathVariable("id") Long id) {
        SystemUser systemUser = systemUserMapper.selectById(id);
        if (systemUser == null) {
            return Result.ok(new SystemUserInfoVo());
        }
        SystemUserInfoVo vo = new SystemUserInfoVo();
        vo.setName(systemUser.getName());
        vo.setAvatarUrl(systemUser.getAvatarUrl());
        return Result.ok(vo);
    }

    @GetMapping("/user/info/{id}")
    public Result<UserInfoVo> getUserInfo(@PathVariable("id") Long id) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        if (userInfo == null) {
            return Result.ok(new UserInfoVo());
        }
        UserInfoVo vo = new UserInfoVo();
        vo.setNickname(userInfo.getNickname());
        vo.setAvatarUrl(userInfo.getAvatarUrl());
        return Result.ok(vo);
    }

    @PostMapping("/user/create")
    public Result<Long> createUser(@RequestBody Map<String, Object> userData) {
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone((String) userData.get("phone"));
        userInfo.setPassword(DigestUtils.md5Hex((String) userData.get("password")));
        userInfo.setNickname((String) userData.get("nickname"));
        userInfo.setStatus(BaseStatus.ENABLE);
        userInfoMapper.insert(userInfo);
        return Result.ok(userInfo.getId());
    }

    @PostMapping("/user/update")
    public Result<Void> updateUser(@RequestBody Map<String, Object> userData) {
        Long id = ((Number) userData.get("id")).longValue();
        UserInfo userInfo = userInfoMapper.selectById(id);
        if (userInfo == null) {
            throw new LeaseException(ResultCodeEnum.APP_PHONE_UNSIGNED_UP);
        }

        if (userData.containsKey("password")) {
            userInfo.setPassword(DigestUtils.md5Hex((String) userData.get("password")));
        }
        if (userData.containsKey("nickname")) {
            userInfo.setNickname((String) userData.get("nickname"));
        }
        userInfoMapper.updateById(userInfo);
        return Result.ok();
    }

    @PostMapping("/user/verifyPassword")
    public Result<Boolean> verifyPassword(@RequestBody Map<String, String> credentials) {
        String phone = credentials.get("phone");
        String password = credentials.get("password");

        UserInfo userInfo = userInfoMapper.selectOneByPhone(phone);
        if (userInfo == null) {
            return Result.ok(false);
        }

        if (userInfo.getStatus() == BaseStatus.DISABLE) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        if (StringUtils.isBlank(userInfo.getPassword())) {
            return Result.fail(ResultCodeEnum.APP_PASSWORD_NOT_SET.getCode(),
                    ResultCodeEnum.APP_PASSWORD_NOT_SET.getMessage());
        }

        boolean valid = userInfo.getPassword().equals(DigestUtils.md5Hex(password));
        return Result.ok(valid);
    }
}
