package com.zju.lease.web.app.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "聊天历史（包含消息列表和用户头像映射）")
public class ChatHistoryVo {

    @Schema(description = "消息列表")
    private List<ChatMessageVo> messages;

    @Schema(description = "用户头像映射，key为用户ID，value为头像URL")
    private Map<Long, String> userAvatars;
}
