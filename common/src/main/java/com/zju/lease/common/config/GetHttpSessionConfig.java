package com.zju.lease.common.config;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class GetHttpSessionConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig serverEndpointConfig,
                                HandshakeRequest request, HandshakeResponse response) {
        // 获取 HttpSession 对象
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        // 保存 httpSession 对象到 ServerEndpointConfig 对象
        // 可以在 ChatEndpoint 类中的 onOpen 方法通过 EndpointConfig 对象获取这里存入的数据
        serverEndpointConfig.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }
}
