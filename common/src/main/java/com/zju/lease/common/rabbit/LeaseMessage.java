package com.zju.lease.common.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租约到期消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseMessage {
    private Long leaseId;
    private Long roomId;
    private String phone;
    private String name;
    private LocalDateTime expiredAt;
}
