package com.zju.lease.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomSearchTool {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Tool("搜索匹配用户需求的公寓房间。输入一个自然语言查询描述，返回匹配的房间信息列表。")
    public String searchRooms(String query) {
        try {
            log.info("Searching rooms with query: {}", query);
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)
                    .minScore(0.5)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
            List<EmbeddingMatch<TextSegment>> matches = result.matches();

            if (matches.isEmpty()) {
                return "未找到匹配的房间。建议用户尝试调整搜索条件，如扩大预算范围、更换区域或减少配套设施要求。";
            }

            return matches.stream()
                    .map(match -> {
                        TextSegment segment = match.embedded();
                        return String.format(
                                "[房间ID:%s | 公寓ID:%s | 租金:%s元/月 | 相似度:%.2f]%n%s",
                                segment.metadata().getString("roomId"),
                                segment.metadata().getString("apartmentId"),
                                segment.metadata().getString("rent"),
                                match.score(),
                                segment.text()
                        );
                    })
                    .collect(Collectors.joining("\n\n---\n\n"));

        } catch (Exception e) {
            log.error("Room search failed for query: {}", query, e);
            return "搜索服务暂时不可用，请稍后再试。";
        }
    }
}
