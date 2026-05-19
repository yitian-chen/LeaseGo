package com.zju.lease.common.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 统一配置：交换机、队列、绑定关系
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 房间变更 → Agent 重索引 ====================
    public static final String ROOM_EXCHANGE = "lease.room";
    public static final String ROOM_REINDEX_QUEUE = "lease.room.reindex";
    public static final String ROOM_REINDEX_KEY = "room.updated";

    // ==================== 聊天消息持久化 ====================
    public static final String CHAT_EXCHANGE = "lease.chat";
    public static final String CHAT_PERSIST_QUEUE = "lease.chat.persist";
    public static final String CHAT_PERSIST_KEY = "chat.message";

    // ==================== 浏览历史记录 ====================
    public static final String HISTORY_EXCHANGE = "lease.history";
    public static final String HISTORY_RECORD_QUEUE = "lease.history.record";
    public static final String HISTORY_RECORD_KEY = "history.record";

    // ==================== 房间缓存延迟双删 ====================
    public static final String CACHE_DELETE_DELAY_QUEUE = "lease.room.cache.delete.delay";
    public static final String CACHE_DELETE_QUEUE = "lease.room.cache.delete";
    public static final String CACHE_DELETE_DELAY_KEY = "room.cache.delete.delay";
    public static final String CACHE_DELETE_KEY = "room.cache.delete";

    // ==================== 租约到期通知 ====================
    public static final String LEASE_EXCHANGE = "lease.lease";
    public static final String LEASE_EXPIRED_QUEUE = "lease.lease.expired";
    public static final String LEASE_EXPIRED_KEY = "lease.expired";

    // ==================== 交换机 ====================
    @Bean
    public TopicExchange roomExchange() {
        return new TopicExchange(ROOM_EXCHANGE);
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    @Bean
    public TopicExchange historyExchange() {
        return new TopicExchange(HISTORY_EXCHANGE);
    }

    @Bean
    public TopicExchange leaseExchange() {
        return new TopicExchange(LEASE_EXCHANGE);
    }

    // ==================== 队列 ====================
    @Bean
    public Queue roomReindexQueue() {
        return QueueBuilder.durable(ROOM_REINDEX_QUEUE).build();
    }

    @Bean
    public Queue chatPersistQueue() {
        return QueueBuilder.durable(CHAT_PERSIST_QUEUE).build();
    }

    @Bean
    public Queue historyRecordQueue() {
        return QueueBuilder.durable(HISTORY_RECORD_QUEUE).build();
    }

    @Bean
    public Queue leaseExpiredQueue() {
        return QueueBuilder.durable(LEASE_EXPIRED_QUEUE).build();
    }

    @Bean
    public Queue cacheDeleteDelayQueue() {
        return QueueBuilder.durable(CACHE_DELETE_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", ROOM_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CACHE_DELETE_KEY)
                .withArgument("x-message-ttl", 1000)
                .build();
    }

    @Bean
    public Queue cacheDeleteQueue() {
        return QueueBuilder.durable(CACHE_DELETE_QUEUE).build();
    }

    // ==================== 绑定 ====================
    @Bean
    public Binding roomReindexBinding() {
        return BindingBuilder.bind(roomReindexQueue()).to(roomExchange()).with(ROOM_REINDEX_KEY);
    }

    @Bean
    public Binding chatPersistBinding() {
        return BindingBuilder.bind(chatPersistQueue()).to(chatExchange()).with(CHAT_PERSIST_KEY);
    }

    @Bean
    public Binding historyRecordBinding() {
        return BindingBuilder.bind(historyRecordQueue()).to(historyExchange()).with(HISTORY_RECORD_KEY);
    }

    @Bean
    public Binding leaseExpiredBinding() {
        return BindingBuilder.bind(leaseExpiredQueue()).to(leaseExchange()).with(LEASE_EXPIRED_KEY);
    }

    @Bean
    public Binding cacheDeleteDelayBinding() {
        return BindingBuilder.bind(cacheDeleteDelayQueue()).to(roomExchange()).with(CACHE_DELETE_DELAY_KEY);
    }

    @Bean
    public Binding cacheDeleteBinding() {
        return BindingBuilder.bind(cacheDeleteQueue()).to(roomExchange()).with(CACHE_DELETE_KEY);
    }

    // ==================== 消息转换器 ====================
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
