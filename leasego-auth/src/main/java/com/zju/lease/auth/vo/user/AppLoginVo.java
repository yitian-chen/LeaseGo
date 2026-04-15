package com.zju.lease.auth.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "App登录请求")
public class AppLoginVo {

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "短信验证码")
    private String code;

    @Schema(description = "图形验证码key")
    private String captchaKey;

    @Schema(description = "图形验证码")
    private String captchaCode;

    @Schema(description = "登录策略: 1-短信登录, 2-密码登录")
    private Integer strategy;

    @Schema(description = "密码(新用户或修改密码时)")
    private String password;
}
