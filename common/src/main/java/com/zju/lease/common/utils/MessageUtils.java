package com.zju.lease.common.utils;

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

    public static String getOnlineUsersMessage(Set<String> onlineUsers) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"system\": true,");
        sb.append("\"fromName\": null,");
        sb.append("\"message\": [");
        
        int i = 0;
        for (String user : onlineUsers) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(user).append("\"");
            i++;
        }

        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
}
