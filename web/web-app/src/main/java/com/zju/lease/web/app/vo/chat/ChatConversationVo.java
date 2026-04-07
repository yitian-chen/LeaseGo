package com.zju.lease.web.app.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "会话信息")
public class ChatConversationVo {

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "对方用户ID")
    private Long otherUserId;

    @Schema(description = "对方用户昵称")
    private String otherUserName;

    @Schema(description = "最后一条消息")
    private String lastMessage;

    @Schema(description = "最后消息时间")
    private Date lastMessageTime;
}
