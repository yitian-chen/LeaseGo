package com.zju.lease.web.admin.listener;

import com.zju.lease.common.constant.RedisConstant;
import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomCacheDeleteListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.CACHE_DELETE_QUEUE, ackMode = "MANUAL")
    public void onCacheDelete(Long roomId, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            String key = RedisConstant.APP_ROOM_PREFIX + roomId;
            redisTemplate.delete(key);
            log.info("Delayed cache delete completed: roomId={}", roomId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed delayed cache delete for roomId={}", roomId, e);
            channel.basicNack(tag, false, true); // requeue for retry
        }
    }
}
