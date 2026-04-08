package com.zju.lease.web.app.service;

import com.zju.lease.model.entity.ChatConversationRead;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Chen
* @description 针对表【chat_conversation_read(会话已读状态表)】的数据库操作Service
*/
public interface ChatConversationReadService extends IService<ChatConversationRead> {

    /**
     * 异步增加未读计数
     */
    void incrementUnreadAsync(Long userId, Long conversationId);

    /**
     * 标记会话已读
     */
    void markAsRead(Long userId, Long conversationId);
}
