package com.zju.lease.web.app.controller.chat;

import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import com.zju.lease.model.entity.ChatConversation;
import com.zju.lease.web.app.service.ChatConversationReadService;
import com.zju.lease.web.app.service.ChatConversationService;
import com.zju.lease.web.app.service.ChatMessageService;
import com.zju.lease.web.app.vo.chat.ChatConversationVo;
import com.zju.lease.web.app.vo.chat.ChatHistoryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app/chat")
@Tag(name = "聊天室")
public class ChatController {

    @Autowired
    private ChatConversationService chatConversationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatConversationReadService chatConversationReadService;

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<List<ChatConversationVo>> listConversations() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        List<ChatConversationVo> list = chatConversationService.listConversationVosByUserId(userId);
        return Result.ok(list);
    }

    @Operation(summary = "获取与某用户的聊天记录")
    @GetMapping("/conversations/{userId}")
    public Result<ChatHistoryVo> getMessages(@PathVariable Long userId) {
        Long currentUserId = LoginUserHolder.getLoginUser().getUserId();
        ChatHistoryVo history = chatMessageService.listMessagesByUsers(currentUserId, userId);
        // 进入聊天页面，自动标记该会话已读
        ChatConversation conversation = chatConversationService.getConversationByTwoUsers(currentUserId, userId);
        if (conversation != null) {
            chatConversationReadService.markAsRead(currentUserId, conversation.getId());
        }
        return Result.ok(history);
    }

    @Operation(summary = "标记与某用户的会话已读")
    @PostMapping("/conversations/{userId}/read")
    public Result<Void> markAsRead(@PathVariable Long userId) {
        Long currentUserId = LoginUserHolder.getLoginUser().getUserId();
        ChatConversation conversation = chatConversationService.getConversationByTwoUsers(currentUserId, userId);
        if (conversation != null) {
            chatConversationReadService.markAsRead(currentUserId, conversation.getId());
        }
        return Result.ok();
    }
}

// TODO: 增加房源与房东的联系与用户的身份标识
