package com.zju.lease.web.app.custom.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zju.lease.model.entity.Message;
import com.zju.lease.model.entity.RedisChatMessage;
import com.zju.lease.model.entity.ChatResponseMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/app/chat", configurator = AuthWebSocketConfigurator.class)
@Component
public class ChatEndpoint {

    public static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    private static RedisTemplate<String, Object> redisTemplate;
    private static StringRedisTemplate stringRedisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setRedisTemplate(@Qualifier("myRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        ChatEndpoint.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        ChatEndpoint.stringRedisTemplate = stringRedisTemplate;
    }

    private String currentUserName;
    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.currentUserName = (String) config.getUserProperties().get("username");
        this.currentUserId = (Long) config.getUserProperties().get("userId");
        if (this.currentUserName == null || this.currentUserId == null) return;

        onlineUsers.put(this.currentUserId, session);

        // 🌟 核心改动：使用 Hash 存储，映射 userId -> userName
        redisTemplate.opsForHash().put("chat:online_users", this.currentUserId.toString(), this.currentUserName);
        broadcastOnlineStatus();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Message msg = objectMapper.readValue(message, Message.class);

            System.out.println("【1. 收到前端消息】尝试发给 toId: " + msg.getToId() + ", 内容: " + msg.getMessage());

            // 发送到 Redis 频道，交由集群分发
            RedisChatMessage redisMsg = new RedisChatMessage();
            redisMsg.setFromId(this.currentUserId);
            redisMsg.setFromName(this.currentUserName);
            redisMsg.setToId(msg.getToId());
            redisMsg.setMessage(msg.getMessage());
            // 直接发送对象，由 RedisTemplate 的 GenericJackson2JsonRedisSerializer 统一处理序列化
            redisTemplate.convertAndSend("chat_msg_channel", redisMsg);

            // 私聊消息发给自己一份 (用于前端回显)
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
            // 从 Hash 中移除
            redisTemplate.opsForHash().delete("chat:online_users", this.currentUserId.toString());
            broadcastOnlineStatus();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void broadcastOnlineStatus() {
        // 从 Hash 获取所有在线用户
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("chat:online_users");

        // 构造前端需要的对象列表 [{userId: 1, nickname: "chen"}, ...]
        List<Map<String, Object>> onlineList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Map<String, Object> userNode = new HashMap<>();
            userNode.put("userId", Long.valueOf(entry.getKey().toString()));
            userNode.put("nickname", entry.getValue().toString());
            onlineList.add(userNode);
        }

        // 封装为标准系统响应并广播
        ChatResponseMessage sysMsg = new ChatResponseMessage(true, null, "系统", onlineList);
        try {
            // 使用 StringRedisTemplate 发送纯 JSON 字符串，不包含类型信息
            stringRedisTemplate.convertAndSend("chat_sys_channel", objectMapper.writeValueAsString(sysMsg));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}