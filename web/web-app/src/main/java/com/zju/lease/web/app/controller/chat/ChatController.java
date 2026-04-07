package com.zju.lease.web.app.controller.chat;

import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.result.Result;
import com.zju.lease.web.app.service.ChatConversationService;
import com.zju.lease.web.app.service.ChatMessageService;
import com.zju.lease.web.app.vo.chat.ChatConversationVo;
import com.zju.lease.web.app.vo.chat.ChatMessageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "获取会话列表")
    @GetMapping("/conversations")
    public Result<List<ChatConversationVo>> listConversations() {
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        List<ChatConversationVo> list = chatConversationService.listConversationVosByUserId(userId);
        return Result.ok(list);
    }

    @Operation(summary = "获取与某用户的聊天记录")
    @GetMapping("/conversations/{userId}")
    public Result<List<ChatMessageVo>> getMessages(@PathVariable Long userId) {
        Long currentUserId = LoginUserHolder.getLoginUser().getUserId();
        List<ChatMessageVo> messages = chatMessageService.listMessagesByUsers(currentUserId, userId);
        return Result.ok(messages);
    }
}
