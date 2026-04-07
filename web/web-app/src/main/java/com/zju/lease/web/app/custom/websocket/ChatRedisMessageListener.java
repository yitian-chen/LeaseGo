package com.zju.lease.web.app.custom.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zju.lease.model.entity.RedisChatMessage;
import com.zju.lease.model.entity.ChatResponseMessage;
import jakarta.websocket.Session;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChatRedisMessageListener implements MessageListener {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());

        try {
            if ("chat_msg_channel".equals(channel)) {
                // 使用 Jackson 反序列化，与 RedisTemplate 的 GenericJackson2JsonRedisSerializer 保持一致
                RedisChatMessage redisMsg = objectMapper.readValue(message.getBody(), RedisChatMessage.class);
                Long toId = redisMsg.getToId();

                Session targetSession = ChatEndpoint.onlineUsers.get(toId);
                if (targetSession != null && targetSession.isOpen()) {
                    // 使用 Jackson 序列化响应
                    String responseJson = objectMapper.writeValueAsString(new ChatResponseMessage(
                            false,
                            redisMsg.getFromId(),
                            redisMsg.getFromName(),
                            redisMsg.getMessage()
                    ));
                    try {
                        targetSession.getBasicRemote().sendText(responseJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if ("chat_sys_channel".equals(channel)) {
                // 系统广播：直接将 body 转为字符串转发
                String body = new String(message.getBody());
                ChatEndpoint.onlineUsers.values().forEach(session -> {
                    if (session.isOpen()) {
                        try {
                            session.getBasicRemote().sendText(body);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
