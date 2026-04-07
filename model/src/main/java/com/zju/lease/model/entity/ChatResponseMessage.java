package com.zju.lease.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseMessage {
    private Boolean system;   // 是否为系统消息
    private Long fromId;      // 发送者 ID (系统消息可为 null)
    private String fromName;  // 发送者昵称 (系统消息填 "系统")
    private Object message;   // 消息内容 (私聊为 String，系统广播为 List)
}