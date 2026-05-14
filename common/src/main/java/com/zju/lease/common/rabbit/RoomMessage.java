package com.zju.lease.common.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间变更消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomMessage {
    private Long roomId;
    private String action; // UPDATE, DELETE
}
