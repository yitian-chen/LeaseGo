package com.zju.lease.agent.config;

import com.zju.lease.agent.service.ApartmentSearchAgent;
import com.zju.lease.agent.tool.RoomSearchTool;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
public class AgentServiceConfig {

    @Value("${langchain4j.dashscope.embedding-model.api-key}")
    private String dashscopeApiKey;

    @Value("${langchain4j.dashscope.embedding-model.model-name:text-embedding-v4}")
    private String embeddingModelName;

    @Value("${minimax.api-key}")
    private String minimaxApiKey;

    @Value("${minimax.base-url}")
    private String minimaxBaseUrl;

    @Value("${minimax.model-name:abab6.5s-chat}")
    private String chatModelName;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Creating QwenEmbeddingModel: {}", embeddingModelName);
        return QwenEmbeddingModel.builder()
                .apiKey(dashscopeApiKey)
                .modelName(embeddingModelName)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Creating RedisEmbeddingStore: {}:{}", redisHost, redisPort);
        return RedisEmbeddingStore.builder()
                .host(redisHost)
                .port(redisPort)
                .indexName("apartment-room-embeddings")
                .dimension(1024)
                .prefix("lease:emb:")
                .metadataFieldsName(List.of("roomId", "apartmentId", "rent"))
                .build();
    }

    @Bean
    public ChatModel chatModel() {
        log.info("Creating OpenAiChatModel for MiniMax: baseUrl={}, model={}", minimaxBaseUrl, chatModelName);
        return OpenAiChatModel.builder()
                .baseUrl(minimaxBaseUrl)
                .apiKey(minimaxApiKey)
                .modelName(chatModelName)
                .temperature(0.3)
                .maxTokens(2048)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public ApartmentSearchAgent apartmentSearchAgent(
            ChatModel chatModel,
            RoomSearchTool roomSearchTool) {
        return AiServices.builder(ApartmentSearchAgent.class)
                .chatModel(chatModel)
                .tools(roomSearchTool)
                .build();
    }
}
