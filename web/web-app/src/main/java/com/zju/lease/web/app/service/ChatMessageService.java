package com.zju.lease.web.app.service;

import com.zju.lease.model.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zju.lease.web.app.vo.chat.ChatMessageVo;

import java.util.List;

/**
* @author Chen
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service
*/
public interface ChatMessageService extends IService<ChatMessage> {

    void saveMessageAsync(Long conversationId, Long fromId, String message);

    List<ChatMessageVo> listMessagesByUsers(Long userId1, Long userId2);
}
