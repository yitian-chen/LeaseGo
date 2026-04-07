package com.zju.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zju.lease.model.entity.ChatConversation;
import com.zju.lease.model.entity.ChatMessage;
import com.zju.lease.model.entity.UserInfo;
import com.zju.lease.web.app.mapper.ChatConversationMapper;
import com.zju.lease.web.app.mapper.ChatMessageMapper;
import com.zju.lease.web.app.mapper.UserInfoMapper;
import com.zju.lease.web.app.service.ChatConversationService;
import com.zju.lease.web.app.vo.chat.ChatConversationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author Chen
* @description 针对表【chat_conversation(会话表)】的数据库操作Service实现
*/
@Service
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation>
    implements ChatConversationService {

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

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
        ChatConversation conversation = chatConversationMapper.selectByTwoUsers(userId1, userId2);
        if (conversation == null) {
            conversation = new ChatConversation();
            conversation.setUserId1(Math.min(userId1, userId2));
            conversation.setUserId2(Math.max(userId1, userId2));
            chatConversationMapper.insert(conversation);
        }
        return conversation;
    }

    @Override
    public List<ChatConversationVo> listConversationVosByUserId(Long userId) {
        List<ChatConversation> conversations = chatConversationMapper.selectByUserId(userId);
        List<ChatConversationVo> result = new ArrayList<>();

        for (ChatConversation conversation : conversations) {
            ChatConversationVo vo = new ChatConversationVo();
            vo.setConversationId(conversation.getId());

            // Determine the other user
            Long otherUserId = conversation.getUserId1().equals(userId)
                    ? conversation.getUserId2()
                    : conversation.getUserId1();
            vo.setOtherUserId(otherUserId);

            // Get other user's nickname
            UserInfo otherUser = userInfoMapper.selectById(otherUserId);
            if (otherUser != null) {
                vo.setOtherUserName(otherUser.getNickname());
            }

            // Get last message
            ChatMessage lastMsg = chatMessageMapper.selectLastMessageByConversationId(conversation.getId());
            if (lastMsg != null) {
                vo.setLastMessage(lastMsg.getMessage());
                vo.setLastMessageTime(lastMsg.getCreateTime());
            }

            result.add(vo);
        }
        return result;
    }
}
