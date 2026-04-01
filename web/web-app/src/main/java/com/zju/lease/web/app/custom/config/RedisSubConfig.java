package com.zju.lease.web.app.custom.config;

import com.zju.lease.web.app.custom.websocket.ChatRedisMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ChatRedisMessageListener chatRedisMessageListener) {
            
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 订阅私聊频道和系统广播频道
        container.addMessageListener(chatRedisMessageListener, new ChannelTopic("chat_msg_channel"));
        container.addMessageListener(chatRedisMessageListener, new ChannelTopic("chat_sys_channel"));
        
        return container;
    }
}