package com.zju.lease.model.entity;

import lombok.Data;

@Data
public class RedisChatMessage {
    private String fromName; // 发送者用户名
    private String toName;   // 接收者用户名
    private String message;  // 消息内容
}