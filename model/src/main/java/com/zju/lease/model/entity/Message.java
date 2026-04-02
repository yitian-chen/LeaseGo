package com.zju.lease.model.entity;

import lombok.Data;

@Data
public class Message {
    private Long fromId;
    private String toName;
    private String toId;
    private String message;
}
