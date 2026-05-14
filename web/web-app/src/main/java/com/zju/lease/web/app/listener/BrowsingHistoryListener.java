package com.zju.lease.web.app.listener;

import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.zju.lease.web.app.service.BrowsingHistoryService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrowsingHistoryListener {

    private final BrowsingHistoryService browsingHistoryService;

    @RabbitListener(queues = RabbitMQConfig.HISTORY_RECORD_QUEUE, ackMode = "MANUAL")
    public void onHistoryRecord(Map<String, Object> msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        Long userId = toLong(msg.get("userId"));
        Long roomId = toLong(msg.get("roomId"));
        try {
            browsingHistoryService.saveHistory(userId, roomId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to save browsing history: userId={}, roomId={}", userId, roomId, e);
            channel.basicNack(tag, false, true);
        }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
