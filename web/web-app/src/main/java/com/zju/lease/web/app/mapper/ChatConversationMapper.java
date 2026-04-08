package com.zju.lease.web.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zju.lease.model.entity.ChatConversation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Chen
* @description 针对表【chat_conversation(会话表)】的数据库操作Mapper
*/
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    @Select("SELECT * FROM chat_conversation WHERE (user_id1 = #{userId} OR user_id2 = #{userId}) AND is_deleted = 0 ORDER BY update_time DESC")
    List<ChatConversation> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM chat_conversation WHERE ((user_id1 = #{userId1} AND user_id2 = #{userId2}) OR (user_id1 = #{userId2} AND user_id2 = #{userId1})) AND is_deleted = 0 LIMIT 1")
    ChatConversation selectByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Select("SELECT c.id, c.user_id1, c.user_id2, c.create_time, c.update_time, c.is_deleted, " +
            "IFNULL(r.unread_count, 0) as unread_count " +
            "FROM chat_conversation c " +
            "LEFT JOIN chat_conversation_read r ON c.id = r.conversation_id AND r.user_id = #{userId} " +
            "WHERE (c.user_id1 = #{userId} OR c.user_id2 = #{userId}) AND c.is_deleted = 0 " +
            "ORDER BY c.update_time DESC")
    List<ChatConversation> selectWithUnreadCount(@Param("userId") Long userId);
}
