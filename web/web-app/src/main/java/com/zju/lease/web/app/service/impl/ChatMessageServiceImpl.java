package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.model.entity.ChatConversation;
import com.zju.lease.model.entity.ChatMessage;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.web.app.mapper.ChatConversationMapper;
import com.zju.lease.web.app.mapper.ChatMessageMapper;
import com.zju.lease.web.app.mapper.UserInfoMapper;
import com.zju.lease.web.app.service.ChatMessageService;
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
    public List<ChatMessageVo> listMessagesByUsers(Long userId1, Long userId2) {
        ChatConversation conversation = chatConversationMapper.selectByTwoUsers(userId1, userId2);
        if (conversation == null) {
            return new ArrayList<>();
        }

        List<ChatMessage> messages = chatMessageMapper.selectByConversationId(conversation.getId());

        // Pre-load user info for all users involved
        Map<Long, String> userNameMap = new HashMap<>();
        for (ChatMessage msg : messages) {
            if (!userNameMap.containsKey(msg.getFromId())) {
                UserInfo user = userInfoMapper.selectById(msg.getFromId());
                if (user != null) {
                    userNameMap.put(msg.getFromId(), user.getNickname());
                }
            }
        }

        // Convert to VO
        List<ChatMessageVo> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            ChatMessageVo vo = new ChatMessageVo();
            vo.setId(msg.getId());
            vo.setFromId(msg.getFromId());
            vo.setFromName(userNameMap.getOrDefault(msg.getFromId(), "未知用户"));
            vo.setMessage(msg.getMessage());
            vo.setCreateTime(msg.getCreateTime());
            vo.setFromMe(msg.getFromId().equals(userId1));
            result.add(vo);
        }
        return result;
    }
}
