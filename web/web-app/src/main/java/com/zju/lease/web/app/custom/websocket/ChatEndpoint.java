package com.zju.lease.web.app.custom.websocket;

import com.alibaba.fastjson2.JSON;
import com.zju.lease.common.utils.MessageUtils;
import com.zju.lease.model.entity.Message;
import com.zju.lease.model.entity.RedisChatMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/app/chat", configurator = AuthWebSocketConfigurator.class)
@Component
public class ChatEndpoint {

    // 本地内存仍需保留，但只存放连接到【当前微服务节点】的 Session
    public static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    // 解决 @ServerEndpoint 类中无法直接 @Autowired 注入 Spring Bean 的问题
    private static RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        ChatEndpoint.redisTemplate = redisTemplate;
    }

    // 当前连接的用户名
    private String currentUserName;

    // 当前连接的用户 id
    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 从握手配置器中获取 username 与 id
        this.currentUserName = (String) config.getUserProperties().get("username");
        if (this.currentUserName == null) {
            return;
        }
        this.currentUserId = (Long) config.getUserProperties().get("userId");
        if (this.currentUserId == null) {
            return;
        }

        // 保存到本节点内存
        onlineUsers.put(this.currentUserId, session);

        // 存入 Redis，维护全局在线用户列表
        redisTemplate.opsForSet().add("chat:online_users", this.currentUserId);

        // 广播上线消息（通知其他节点）
        broadcastOnlineStatus();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Message msg = JSON.parseObject(message, Message.class);

        // 构造需要在集群中路由的完整消息体
        RedisChatMessage redisMsg = new RedisChatMessage();
        redisMsg.setFromId(this.currentUserId);
        redisMsg.setFromName(this.currentUserName);
        redisMsg.setToId(msg.getToId());
//        redisMsg.setToName(msg.getToName());
        redisMsg.setMessage(msg.getMessage());

        // 将消息发布到 Redis 的 chat_msg_channel 频道，交由监听器分发
        redisTemplate.convertAndSend("chat_msg_channel", JSON.toJSONString(redisMsg));

        // 私聊消息也发给自己一份，用于前端回显
        try {
            session.getBasicRemote().sendText(
                    "{\"system\": false, \"fromName\": \"" + this.currentUserName
                            + "\", \"message\": \"" + msg.getMessage() + "\"}"
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (this.currentUserName != null) {
            // 从本节点移除
            onlineUsers.remove(this.currentUserId);
            // 从全局 Redis 移除
            redisTemplate.opsForSet().remove("chat:online_users", this.currentUserId);

            // 广播下线消息
            broadcastOnlineStatus();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void broadcastOnlineStatus() {
        Set<Object> allOnlineUsers = redisTemplate.opsForSet().members("chat:online_users");
        String message = MessageUtils.getOnlineUsersMessage(allOnlineUsers);
        // 将系统广播消息发送到专门的广播频道
        redisTemplate.convertAndSend("chat_sys_channel", message);
    }
}