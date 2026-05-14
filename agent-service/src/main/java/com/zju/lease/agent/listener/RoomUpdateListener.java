package com.zju.lease.agent.listener;

import com.zju.lease.agent.service.ApartmentDataIngestor;
import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.zju.lease.common.rabbit.RoomMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomUpdateListener {

    private final ApartmentDataIngestor apartmentDataIngestor;

    @RabbitListener(queues = RabbitMQConfig.ROOM_REINDEX_QUEUE, ackMode = "MANUAL")
    public void onRoomUpdate(RoomMessage msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received room update: roomId={}, action={}", msg.getRoomId(), msg.getAction());
        try {
            apartmentDataIngestor.ingestRoom(msg.getRoomId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to reindex room {}", msg.getRoomId(), e);
            channel.basicNack(tag, false, true); // requeue
        }
    }
}
