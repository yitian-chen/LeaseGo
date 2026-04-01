package com.zju.lease.web.app.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "图形验证码响应")
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaVo {
    @Schema(description = "验证码图片Base64")
    private String image;

    @Schema(description = "验证码Key")
    private String key;
}