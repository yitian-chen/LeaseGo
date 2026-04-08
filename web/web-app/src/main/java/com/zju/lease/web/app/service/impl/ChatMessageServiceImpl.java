package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.model.entity.ChatConversation;
import com.zju.lease.model.entity.ChatMessage;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.web.app.mapper.ChatConversationMapper;
import com.zju.lease.web.app.mapper.ChatMessageMapper;
import com.zju.lease.web.app.mapper.UserInfoMapper;
import com.zju.lease.web.app.service.ChatMessageService;
import com.zju.lease.web.app.vo.chat.ChatHistoryVo;
import com.zju.lease.web.app.vo.chat.ChatMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Chen
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    @Async
    public void saveMessageAsync(Long conversationId, Long fromId, String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId);
        chatMessage.setFromId(fromId);
        chatMessage.setMessage(message);
        chatMessageMapper.insert(chatMessage);
    }

    @Override
    public ChatHistoryVo listMessagesByUsers(Long userId1, Long userId2) {
        ChatConversation conversation = chatConversationMapper.selectByTwoUsers(userId1, userId2);
        if (conversation == null) {
            return new ChatHistoryVo();
        }

        List<ChatMessage> messages = chatMessageMapper.selectByConversationId(conversation.getId());

        // 收集所有涉及的用户 ID（发送者 + 当前用户）
        Map<Long, String> userAvatars = new HashMap<>();
        userAvatars.put(userId1, null);  // 占位，等会查
        for (ChatMessage msg : messages) {
            userAvatars.putIfAbsent(msg.getFromId(), null);
        }

        // 批量查询用户头像
        for (Long userId : userAvatars.keySet()) {
            UserInfo user = userInfoMapper.selectById(userId);
            if (user != null) {
                userAvatars.put(userId, user.getAvatarUrl());
            }
        }

        // 构建消息列表（不包含头像URL，由前端根据 userAvatars 自行映射）
        List<ChatMessageVo> messageList = new ArrayList<>();
        for (ChatMessage msg : messages) {
            ChatMessageVo vo = new ChatMessageVo();
            vo.setId(msg.getId());
            vo.setFromId(msg.getFromId());
            vo.setFromName(userAvatars.get(msg.getFromId()) != null
                    ? userInfoMapper.selectById(msg.getFromId()).getNickname()
                    : "未知用户");
            vo.setMessage(msg.getMessage());
            vo.setCreateTime(msg.getCreateTime());
            vo.setFromMe(msg.getFromId().equals(userId1));
            messageList.add(vo);
        }

        // 构建结果
        ChatHistoryVo result = new ChatHistoryVo();
        result.setMessages(messageList);
        result.setUserAvatars(userAvatars);
        return result;
    }
}
