package com.zju.lease.web.app.mapper;

import com.zju.lease.model.entity.ChatConversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
}
