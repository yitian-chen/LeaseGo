package com.zju.lease.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatConversation extends BaseEntity implements Serializable {

    @TableField("user_id1")
    private Long userId1;

    @TableField("user_id2")
    private Long userId2;

    @TableField(exist = false)
    private Long otherUserId;

    @TableField(exist = false)
    private String otherUserName;

    @TableField(exist = false)
    private String lastMessage;

    @TableField(exist = false)
    private Date lastMessageTime;
}
