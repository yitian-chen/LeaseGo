package com.zju.lease.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends BaseEntity implements Serializable {

    @TableField("conversation_id")
    private Long conversationId;

    @TableField("from_id")
    private Long fromId;

    @TableField("message")
    private String message;

    @TableField(exist = false)
    private String fromName;
}
