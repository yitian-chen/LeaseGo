package com.zju.lease.common.websocket.pojo;

import lombok.Data;

@Data
public class Message {
    private String toName;
    private String message;
}
