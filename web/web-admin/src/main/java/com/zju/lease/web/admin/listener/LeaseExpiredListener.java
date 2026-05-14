package com.zju.lease.web.admin.listener;

import com.zju.lease.common.rabbit.LeaseMessage;
import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class LeaseExpiredListener {

    @RabbitListener(queues = RabbitMQConfig.LEASE_EXPIRED_QUEUE, ackMode = "MANUAL")
    public void onLeaseExpired(LeaseMessage msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Lease expired: leaseId={}, roomId={}, phone={}, name={}",
                msg.getLeaseId(), msg.getRoomId(), msg.getPhone(), msg.getName());
        // TODO: 后续可扩展短信通知、推送通知
        try {
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, true);
        }
    }
}
