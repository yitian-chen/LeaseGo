package com.zju.lease.web.admin.controller.system;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zju.lease.common.result.Result;
import com.zju.lease.model.entity.SystemUser;
import com.zju.lease.model.enums.BaseStatus;
import com.zju.lease.web.admin.service.SystemUserService;
import com.zju.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.zju.lease.web.admin.vo.system.user.SystemUserQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "后台用户信息管理")
@RestController
@RequestMapping("/admin/system/user")
public class SystemUserController {

    @Autowired
    private SystemUserService service;

    @Operation(summary = "根据条件分页查询后台用户列表")
    @GetMapping("page")
    public Result<IPage<SystemUserItemVo>> page(@RequestParam long current, @RequestParam long size, SystemUserQueryVo queryVo) {
        Page<SystemUser> page = new Page<>(current, size);
        IPage<SystemUserItemVo> result = service.pageSystemUser(page, queryVo);
        return Result.ok(result);
    }

    @Operation(summary = "根据ID查询后台用户信息")
    @GetMapping("getById")
    public Result<SystemUserItemVo> getById(@RequestParam Long id) {
        SystemUserItemVo result = service.getSystemUserById(id);
        return Result.ok(result );
    }

    @Operation(summary = "保存或更新后台用户信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody SystemUser systemUser) {
        // 密码需要处理，进行MD5加密
        if (systemUser.getPassword() != null) {
            systemUser.setPassword(DigestUtils.md5Hex(systemUser.getPassword()));
        }
        // 如果传入null，则Mybatis Plus会直接忽略，不会修改这个字段
        service.saveOrUpdate(systemUser);
        return Result.ok();
    }

    @Operation(summary = "判断后台用户名是否可用")
    @GetMapping("isUserNameAvailable")
    public Result<Boolean> isUsernameExists(@RequestParam String username) {
        return Result.ok();
    }

    @DeleteMapping("deleteById")
    @Operation(summary = "根据ID删除后台用户信息")
    public Result removeById(@RequestParam Long id) {
        return Result.ok();
    }

    @Operation(summary = "根据ID修改后台用户状态")
    @PostMapping("updateStatusByUserId")
    public Result updateStatusByUserId(@RequestParam Long id, @RequestParam BaseStatus status) {
        return Result.ok();
    }
}
