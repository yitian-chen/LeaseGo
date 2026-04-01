package com.zju.lease.common.websocket;

import com.alibaba.fastjson2.JSON;
import com.zju.lease.common.config.GetHttpSessionConfig;
import com.zju.lease.common.login.LoginUserHolder;
import com.zju.lease.common.utils.MessageUtils;
import com.zju.lease.common.websocket.pojo.Message;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/app/chat", configurator = GetHttpSessionConfig.class)
@Component
public class ChatEndpoint {

    // 此处的 Session 即一个用户的 WebSocket 连接通道，可以通过它进行服务器与客户端的信息传递
    private static final Map<String, Session> onlineUsers = new ConcurrentHashMap<>();

    // 此处的 HttpSession 即用户登陆时创建的状态会话，用于身份认证。
    private HttpSession httpSession;

    // @OnOpen 注解指的是建立 websocket 连接后，此方法被调用，有点类似 Arduino 里面的类似方法
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String userName = LoginUserHolder.getLoginUser().getUserName();
        onlineUsers.put(userName, session);

        // 广播通知所有用户，此用户上线
        String message = MessageUtils.getMessage(true, null, MessageUtils.getOnlineUsersMessage(getFriends()));
        broadcastAllUsers(message);
    }

    private Set<String> getFriends() {
        return onlineUsers.keySet();
    }

    private void broadcastAllUsers(String message) {
        // 用 entrySet 把 Map 变成方便遍历的 key + value pair Set，方便用 for 遍历
        Set<Map.Entry<String, Session>> entries = onlineUsers.entrySet();
        try {
            for (Map.Entry<String, Session> entry : entries) {
                Session session = entry.getValue();
                // 用 Session 类的 getBasicRemote() 方法来发送同步消息
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 浏览器向客户端发送消息时该方法会被调用，即私聊
    @OnMessage
    public void onMessage(String message, Session session) {
        // 调用 parseObject() 方法将传来的 JSON 格式的 message 装填进格式相对应的 Message 类中，方便后续调用
        Message msg = JSON.parseObject(message, Message.class);
        String toName = msg.getToName();
        String tempMessage = msg.getMessage();

        // 获取消息接收方的 Session
        Session targetSession = onlineUsers.get(toName);
        if (targetSession != null) {
            String currentUserName = LoginUserHolder.getLoginUser().getUserName();
            MessageUtils.getOnlineUsersMessage(Set.of(currentUserName + ": " + tempMessage));

            try {
                // 发送给目标用户
                targetSession.getBasicRemote().sendText(
                        "{\"system\": false, \"fromName\": \"" + currentUserName
                                + "\", \"message\": \"" + tempMessage + "\"}"
                );

                // 发送给当前用户自己
                session.getBasicRemote().sendText(
                        "{\"system\": false, \"fromName\": \"" + currentUserName
                                + "\", \"message\": \"" + tempMessage + "\"}"
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        String userName = LoginUserHolder.getLoginUser().getUserName();
        if (userName != null) {
            // 此处调用 Map 的 remove() 方法，移除这对 K - V pair ，同时返回 value
            Session removeSession = onlineUsers.remove(userName);
            if (removeSession != null) {
                try {
                    removeSession.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 通知其他用户
        String message = MessageUtils.getOnlineUsersMessage(getFriends());
        broadcastAllUsers(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}
