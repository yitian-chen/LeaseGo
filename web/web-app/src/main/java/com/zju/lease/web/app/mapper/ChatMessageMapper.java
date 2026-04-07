package com.zju.lease.web.app.mapper;

import com.zju.lease.model.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Chen
* @description 针对表【chat_message(聊天消息表)】的数据库操作Mapper
*/
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND is_deleted = 0 ORDER BY create_time ASC")
    List<ChatMessage> selectByConversationId(@Param("conversationId") Long conversationId);

    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT 1")
    ChatMessage selectLastMessageByConversationId(@Param("conversationId") Long conversationId);
}
