package com.zju.lease.auth.vo.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员登录请求")
public class AdminLoginVo {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "图形验证码key")
    private String captchaKey;

    @Schema(description = "图形验证码")
    private String captchaCode;
}
