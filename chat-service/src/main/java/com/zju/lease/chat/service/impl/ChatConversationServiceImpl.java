package com.zju.lease.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.model.entity.ChatConversation;
import com.zju.lease.model.entity.ChatMessage;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.chat.mapper.ChatConversationMapper;
import com.zju.lease.chat.mapper.ChatMessageMapper;
import com.zju.lease.chat.mapper.UserInfoMapper;
import com.zju.lease.chat.service.ChatConversationService;
import com.zju.lease.chat.vo.chat.ChatConversationVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation>
    implements ChatConversationService {

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<ChatConversation> listConversationsByUserId(Long userId) {
        return chatConversationMapper.selectByUserId(userId);
    }

    @Override
    public ChatConversation getConversationByTwoUsers(Long userId1, Long userId2) {
        return chatConversationMapper.selectByTwoUsers(userId1, userId2);
    }

    @Override
    public ChatConversation getOrCreateConversation(Long userId1, Long userId2) {
        // 先查缓存（无锁快速路径）
        ChatConversation conversation = chatConversationMapper.selectByTwoUsers(userId1, userId2);
        if (conversation != null) {
            return conversation;
        }

        // 分布式锁避免 TOCTOU 竞态创建重复会话
        long minId = Math.min(userId1, userId2);
        long maxId = Math.max(userId1, userId2);
        String lockKey = "lock:conv:" + minId + "-" + maxId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, java.util.concurrent.TimeUnit.SECONDS)) {
                // 双重检查
                conversation = chatConversationMapper.selectByTwoUsers(userId1, userId2);
                if (conversation != null) {
                    return conversation;
                }
                conversation = new ChatConversation();
                conversation.setUserId1(minId);
                conversation.setUserId2(maxId);
                chatConversationMapper.insert(conversation);
                return conversation;
            } else {
                log.warn("Failed to acquire lock for conversation {}-{}", minId, maxId);
                return chatConversationMapper.selectByTwoUsers(userId1, userId2);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<ChatConversationVo> listConversationVosByUserId(Long userId) {
        List<ChatConversation> conversations = chatConversationMapper.selectWithUnreadCount(userId);
        List<ChatConversationVo> result = new ArrayList<>();

        for (ChatConversation conversation : conversations) {
            ChatConversationVo vo = new ChatConversationVo();
            vo.setConversationId(conversation.getId());

            Long otherUserId = conversation.getUserId1().equals(userId)
                    ? conversation.getUserId2()
                    : conversation.getUserId1();
            vo.setOtherUserId(otherUserId);

            UserInfo otherUser = userInfoMapper.selectById(otherUserId);
            if (otherUser != null) {
                vo.setOtherUserName(otherUser.getNickname());
            }

            ChatMessage lastMsg = chatMessageMapper.selectLastMessageByConversationId(conversation.getId());
            if (lastMsg != null) {
                vo.setLastMessage(lastMsg.getMessage());
                vo.setLastMessageTime(lastMsg.getCreateTime());
            }

            if (otherUser != null) {
                vo.setAvatarUrl(otherUser.getAvatarUrl());
            }

            vo.setUnreadCount(conversation.getUnreadCount() != null ? conversation.getUnreadCount() : 0);

            result.add(vo);
        }
        return result;
    }
}
