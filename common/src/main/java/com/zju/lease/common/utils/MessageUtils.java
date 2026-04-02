package com.zju.lease.common.utils;

import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageUtils {
    public static String getMessage(boolean isSystemMessage, String fromName, Object message) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        if (isSystemMessage) {
            sb.append("\"system\": true,");
            sb.append("\"fromName\": null,");
            sb.append("\"message\": ").append("\"").append(message.toString()).append("\"");
        } else {
            sb.append("\"system\": false,");
            sb.append("\"fromName\": ").append(fromName).append("\",");
            sb.append("\"message\": ").append("\"").append(message.toString()).append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

    public static String getOnlineUsersMessage(Set<Object> onlineUsers) {
        Map<String, Object> result = new HashMap<>();
        result.put("system", true);
        result.put("fromName", "系统");
        result.put("message", onlineUsers); // 返回当前在线的 ID 列表
        return JSON.toJSONString(result);
    }
}
