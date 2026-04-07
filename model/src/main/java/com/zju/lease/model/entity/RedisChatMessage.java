package com.zju.lease.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RedisChatMessage {
    private Long toId;
    private Long fromId;
    private String fromName; // 发送者用户名
    private String message;  // 消息内容
}