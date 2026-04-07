package com.zju.lease.web.app.service;

import com.zju.lease.model.entity.ChatConversation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zju.lease.web.app.vo.chat.ChatConversationVo;

import java.util.List;

/**
* @author Chen
* @description 针对表【chat_conversation(会话表)】的数据库操作Service
*/
public interface ChatConversationService extends IService<ChatConversation> {

    List<ChatConversation> listConversationsByUserId(Long userId);

    ChatConversation getConversationByTwoUsers(Long userId1, Long userId2);

    ChatConversation getOrCreateConversation(Long userId1, Long userId2);

    List<ChatConversationVo> listConversationVosByUserId(Long userId);
}
