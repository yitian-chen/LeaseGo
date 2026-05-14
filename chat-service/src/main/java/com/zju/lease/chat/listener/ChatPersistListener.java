package com.zju.lease.chat.listener;

import com.zju.lease.model.entity.ChatConversation;
import com.zju.lease.chat.service.ChatConversationReadService;
import com.zju.lease.chat.service.ChatConversationService;
import com.zju.lease.chat.service.ChatMessageService;
import com.zju.lease.common.rabbit.RabbitMQConfig;
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
public class ChatPersistListener {

    private final ChatMessageService chatMessageService;
    private final ChatConversationReadService chatConversationReadService;
    private final ChatConversationService chatConversationService;

    @RabbitListener(queues = RabbitMQConfig.CHAT_PERSIST_QUEUE, ackMode = "MANUAL")
    public void onChatMessage(Map<String, Object> msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        Long fromId = toLong(msg.get("fromId"));
        Long toId = toLong(msg.get("toId"));
        String message = (String) msg.get("message");

        try {
            ChatConversation conversation = chatConversationService.getOrCreateConversation(fromId, toId);
            chatMessageService.saveMessageAsync(conversation.getId(), fromId, message);
            chatConversationReadService.incrementUnreadAsync(toId, conversation.getId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to persist chat msg", e);
            channel.basicNack(tag, false, true);
        }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
