package com.zju.lease.chat.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zju.lease.common.rabbit.RabbitMQConfig;
import com.zju.lease.model.entity.Message;
import com.zju.lease.model.entity.RedisChatMessage;
import com.zju.lease.model.entity.ChatResponseMessage;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/app/chat", configurator = AuthWebSocketConfigurator.class)
@Component
public class ChatEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ChatEndpoint.class);

    public static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    private static RedisTemplate<String, Object> redisTemplate;
    private static StringRedisTemplate stringRedisTemplate;
    private static RabbitTemplate rabbitTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setRedisTemplate(@Qualifier("myRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        ChatEndpoint.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        ChatEndpoint.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        ChatEndpoint.rabbitTemplate = rabbitTemplate;
    }

    private String currentUserName;
    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        log.info("WebSocket onOpen called");
        this.currentUserName = (String) config.getUserProperties().get("username");
        this.currentUserId = (Long) config.getUserProperties().get("userId");
        log.info("WebSocket onOpen, username: {}, userId: {}", currentUserName, currentUserId);
        if (this.currentUserName == null || this.currentUserId == null) {
            try {
                log.warn("WebSocket authentication failed, closing connection");
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Authentication failed"));
            } catch (IOException ignored) {}
            return;
        }

        log.info("WebSocket connected successfully, userId: {}", this.currentUserId);
        onlineUsers.put(this.currentUserId, session);

        redisTemplate.opsForHash().put("chat:online_users", this.currentUserId.toString(), this.currentUserName);
        broadcastOnlineStatus();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Message msg = objectMapper.readValue(message, Message.class);

            RedisChatMessage redisMsg = new RedisChatMessage();
            redisMsg.setFromId(this.currentUserId);
            redisMsg.setFromName(this.currentUserName);
            redisMsg.setToId(msg.getToId());
            redisMsg.setMessage(msg.getMessage());
            redisTemplate.convertAndSend("chat_msg_channel", redisMsg);

            // RabbitMQ 持久化
            Map<String, Object> persistMsg = new HashMap<>();
            persistMsg.put("fromId", this.currentUserId);
            persistMsg.put("toId", msg.getToId());
            persistMsg.put("message", msg.getMessage());
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.CHAT_PERSIST_KEY, persistMsg);

            ChatResponseMessage echoMsg = new ChatResponseMessage(false, this.currentUserId, this.currentUserName, msg.getMessage());
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(echoMsg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (this.currentUserId != null) {
            onlineUsers.remove(this.currentUserId);
            redisTemplate.opsForHash().delete("chat:online_users", this.currentUserId.toString());
            broadcastOnlineStatus();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void broadcastOnlineStatus() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("chat:online_users");

        List<Map<String, Object>> onlineList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Map<String, Object> userNode = new HashMap<>();
            userNode.put("userId", Long.valueOf(entry.getKey().toString()));
            userNode.put("nickname", entry.getValue().toString());
            onlineList.add(userNode);
        }

        ChatResponseMessage sysMsg = new ChatResponseMessage(true, null, "系统", onlineList);
        try {
            stringRedisTemplate.convertAndSend("chat_sys_channel", objectMapper.writeValueAsString(sysMsg));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
