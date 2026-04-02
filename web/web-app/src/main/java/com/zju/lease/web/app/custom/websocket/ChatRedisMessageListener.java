package com.zju.lease.web.app.custom.websocket;

import com.alibaba.fastjson2.JSON;
import com.zju.lease.model.entity.RedisChatMessage;
import jakarta.websocket.Session;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ChatRedisMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        if ("chat_msg_channel".equals(channel)) {
            // 处理私聊消息
            RedisChatMessage redisMsg = JSON.parseObject(body, RedisChatMessage.class);

            Long toId = redisMsg.getToId();

            // 检查接收方是否连接在【当前微服务节点】
            Session targetSession = ChatEndpoint.onlineUsers.get(toId);
            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.getBasicRemote().sendText(
                            "{\"system\": false, \"fromName\": \"" + redisMsg.getFromName()
                                    + "\", \"message\": \"" + redisMsg.getMessage() + "\"}"
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if ("chat_sys_channel".equals(channel)) {
            // 处理系统广播消息（如上下线），群发给本节点连接的所有用户
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
    }
}