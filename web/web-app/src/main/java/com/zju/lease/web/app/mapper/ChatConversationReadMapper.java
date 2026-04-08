package com.zju.lease.web.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.ChatConversationRead;
import org.apache.ibatis.annotations.Param;

/**
* @author Chen
* @description 针对表【chat_conversation_read(会话已读状态表)】的数据库操作Mapper
*/
public interface ChatConversationReadMapper extends BaseMapper<ChatConversationRead> {

    /**
     * 查询指定用户和会话的已读记录
     */
    ChatConversationRead selectByUserIdAndConversationId(@Param("userId") Long userId, @Param("conversationId") Long conversationId);
}
