package com.zju.lease.web.app.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "聊天消息")
public class ChatMessageVo {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "发送者ID")
    private Long fromId;

    @Schema(description = "发送者昵称")
    private String fromName;

    @Schema(description = "消息内容")
    private String message;

    @Schema(description = "发送时间")
    private Date createTime;

    @Schema(description = "是否来自自己")
    private Boolean fromMe;
}
