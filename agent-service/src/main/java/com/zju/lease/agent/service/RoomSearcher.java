package com.zju.lease.agent.service;

import com.zju.lease.agent.mapper.*;
import com.zju.lease.model.entity.*;
import com.zju.lease.model.enums.ItemType;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomSearcher {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final RoomInfoMapper roomInfoMapper;
    private final ApartmentInfoMapper apartmentInfoMapper;
    private final RoomLabelMapper roomLabelMapper;
    private final GraphInfoMapper graphInfoMapper;

    /**
     * 搜索匹配的房间，返回搜索结果（文本 + 房间ID列表）
     */
    public SearchResult search(String query, int maxResults) {
        try {
            log.info("Searching rooms with query: {}", query);
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(0.5)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
            List<EmbeddingMatch<TextSegment>> matches = result.matches();

            if (matches.isEmpty()) {
                return new SearchResult("", List.of());
            }

            List<Long> roomIds = new ArrayList<>();
            StringBuilder contextText = new StringBuilder();

            for (int i = 0; i < matches.size(); i++) {
                EmbeddingMatch<TextSegment> match = matches.get(i);
                TextSegment segment = match.embedded();
                String roomIdStr = segment.metadata().getString("roomId");
                if (roomIdStr != null) {
                    roomIds.add(Long.parseLong(roomIdStr));
                }
                contextText.append("【匹配结果").append(i + 1).append("】\n");
                contextText.append(segment.text()).append("\n\n");
            }

            return new SearchResult(contextText.toString(), roomIds);

        } catch (Exception e) {
            log.error("Room search failed for query: {}", query, e);
            return new SearchResult("", List.of());
        }
    }

    /**
     * 根据房间ID列表获取完整的房间展示数据
     */
    public List<com.zju.lease.agent.vo.AgentSearchVo.RoomItemVo> getRoomItems(List<Long> roomIds) {
        List<com.zju.lease.agent.vo.AgentSearchVo.RoomItemVo> items = new ArrayList<>();

        for (Long roomId : roomIds) {
            try {
                RoomInfo room = roomInfoMapper.selectById(roomId);
                if (room == null) continue;

                ApartmentInfo apartment = apartmentInfoMapper.selectById(room.getApartmentId());
                List<LabelInfo> labels = roomLabelMapper.selectListByRoomId(roomId);
                List<GraphInfo> images = graphInfoMapper.selectListByRoomId(roomId);

                items.add(new com.zju.lease.agent.vo.AgentSearchVo.RoomItemVo(
                        room.getId(),
                        room.getRoomNumber(),
                        room.getRent(),
                        apartment != null ? apartment.getName() : "",
                        apartment != null ? apartment.getProvinceName() : "",
                        apartment != null ? apartment.getCityName() : "",
                        apartment != null ? apartment.getDistrictName() : "",
                        labels.stream().map(LabelInfo::getName).collect(Collectors.toList()),
                        images.isEmpty() ? null : images.get(0).getUrl()
                ));
            } catch (Exception e) {
                log.warn("Failed to fetch room item {}: {}", roomId, e.getMessage());
            }
        }

        return items;
    }

    public record SearchResult(String contextText, List<Long> roomIds) {}
}
