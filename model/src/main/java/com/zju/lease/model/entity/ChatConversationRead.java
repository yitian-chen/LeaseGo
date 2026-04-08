package com.zju.lease.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatConversationRead extends BaseEntity implements Serializable {

    @TableField("user_id")
    private Long userId;

    @TableField("conversation_id")
    private Long conversationId;

    @TableField("last_read_time")
    private Date lastReadTime;

    @TableField("unread_count")
    private Integer unreadCount;
}
