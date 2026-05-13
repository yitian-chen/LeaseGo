package com.zju.lease.agent.controller.agent;

import com.zju.lease.agent.service.ApartmentDataIngestor;
import com.zju.lease.agent.service.ApartmentSearchAgent;
import com.zju.lease.agent.service.RoomSearcher;
import com.zju.lease.agent.vo.AgentSearchVo;
import com.zju.lease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI助手")
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentSearchController {

    private final ApartmentSearchAgent apartmentSearchAgent;
    private final RoomSearcher roomSearcher;
    private final ApartmentDataIngestor apartmentDataIngestor;

    @Operation(summary = "AI智能搜索房间",
            description = "用户输入自然语言描述租房需求，AI分析后推荐最匹配的房源")
    @PostMapping("/search")
    public Result<AgentSearchVo> search(
            @Parameter(description = "自然语言查询，如「找个西湖区2000元以内朝南带独卫的房间」", required = true)
            @RequestParam String query) {
        log.info("Agent search query: {}", query);

        // 1. 向量搜索匹配房间
        RoomSearcher.SearchResult searchResult = roomSearcher.search(query, 5);
        List<AgentSearchVo.RoomItemVo> roomItems = roomSearcher.getRoomItems(searchResult.roomIds());

        // 2. 将用户查询和搜索结果发给 LLM
        String context = searchResult.contextText().isEmpty() ? "无匹配结果" : searchResult.contextText();
        String aiText = apartmentSearchAgent.search(query, context);

        // 3. 过滤 think 标签
        String cleaned = aiText.replaceAll("(?s)<think>.*?</think>", "").trim();

        return Result.ok(new AgentSearchVo(cleaned, roomItems));
    }

    @Operation(summary = "手动触发房间数据重新索引")
    @PostMapping("/admin/reindex")
    public Result<String> reindex(
            @Parameter(description = "房间ID，传值则只重建该房间的索引，不传则全量重建")
            @RequestParam(required = false) Long roomId) {
        if (roomId != null) {
            apartmentDataIngestor.ingestRoomAsync(roomId);
            return Result.ok("房间 " + roomId + " 重新索引已触发");
        } else {
            apartmentDataIngestor.fullReindex();
            return Result.ok("全量重新索引已触发");
        }
    }
}
