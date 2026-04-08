package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.model.entity.ChatConversationRead;
import com.zju.lease.web.app.mapper.ChatConversationReadMapper;
import com.zju.lease.web.app.service.ChatConversationReadService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author Chen
* @description 针对表【chat_conversation_read(会话已读状态表)】的数据库操作Service实现
*/
@Service
public class ChatConversationReadServiceImpl extends ServiceImpl<ChatConversationReadMapper, ChatConversationRead>
    implements ChatConversationReadService {

    @Override
    @Async
    public void incrementUnreadAsync(Long userId, Long conversationId) {
        ChatConversationRead record = baseMapper.selectByUserIdAndConversationId(userId, conversationId);
        if (record == null) {
            // 记录不存在，创建新记录，未读数为1
            record = new ChatConversationRead();
            record.setUserId(userId);
            record.setConversationId(conversationId);
            record.setUnreadCount(1);
            record.setLastReadTime(new Date());
            baseMapper.insert(record);
        } else {
            // 已存在，未读数+1
            record.setUnreadCount(record.getUnreadCount() + 1);
            baseMapper.updateById(record);
        }
    }

    @Override
    public void markAsRead(Long userId, Long conversationId) {
        ChatConversationRead record = baseMapper.selectByUserIdAndConversationId(userId, conversationId);
        if (record != null) {
            record.setUnreadCount(0);
            record.setLastReadTime(new Date());
            baseMapper.updateById(record);
        } else {
            // 如果记录不存在，创建一个已读记录
            record = new ChatConversationRead();
            record.setUserId(userId);
            record.setConversationId(conversationId);
            record.setUnreadCount(0);
            record.setLastReadTime(new Date());
            baseMapper.insert(record);
        }
    }
}
