package com.zju.lease.auth.feign;

import com.zju.lease.common.result.Result;
import com.zju.lease.auth.vo.system.SystemUserInfoVo;
import com.zju.lease.auth.vo.user.UserInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "leasego-upms", path = "/upms")
public interface UpmsFeignClient {

    @PostMapping("/inner/admin/verify")
    Result<Map<String, Object>> verifyAdmin(@RequestBody Map<String, String> credentials);

    @GetMapping("/inner/admin/info/{id}")
    Result<SystemUserInfoVo> getAdminInfo(@PathVariable("id") Long id);

    @GetMapping("/inner/user/info/{id}")
    Result<UserInfoVo> getUserInfo(@PathVariable("id") Long id);

    @PostMapping("/inner/user/create")
    Result<Long> createUser(@RequestBody Map<String, Object> userData);

    @PostMapping("/inner/user/update")
    Result<Void> updateUser(@RequestBody Map<String, Object> userData);

    @PostMapping("/inner/user/verifyPassword")
    Result<Boolean> verifyPassword(@RequestBody Map<String, String> credentials);
}
