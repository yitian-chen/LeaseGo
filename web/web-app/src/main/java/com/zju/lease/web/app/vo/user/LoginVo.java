package com.zju.lease.web.app.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "APP端登录实体")
public class LoginVo {

    @Schema(description = "登录策略: 1-短信验证码登录, 2-密码登录", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer strategy;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "短信验证码 (策略1时必填)")
    private String code;

    @Schema(description = "密码 (策略2时必填; 策略1且为新用户初次登录时必填，用于设置初始密码)")
    private String password;

    @Schema(description = "图形验证码Key (策略2时必填)")
    private String captchaKey;

    @Schema(description = "图形验证码 (策略2时必填)")
    private String captchaCode;
}