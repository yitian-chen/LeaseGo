package com.zju.lease.agent.controller.agent;

import com.zju.lease.agent.service.ApartmentDataIngestor;
import com.zju.lease.agent.service.ApartmentSearchAgent;
import com.zju.lease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI助手")
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentSearchController {

    private final ApartmentSearchAgent apartmentSearchAgent;
    private final ApartmentDataIngestor apartmentDataIngestor;

    @Operation(summary = "AI智能搜索房间",
            description = "用户输入自然语言描述租房需求，AI分析后推荐最匹配的房源。例如：\"我想在西湖区找个2000左右的朝南房间\"")
    @PostMapping("/search")
    public Result<String> search(
            @Parameter(description = "自然语言查询，如「找个西湖区2000元以内朝南带独卫的房间」", required = true)
            @RequestParam String query) {
        log.info("Agent search query: {}", query);
        String response = apartmentSearchAgent.search(query);
        return Result.ok(response);
    }

    @Operation(summary = "手动触发房间数据重新索引",
            description = "将数据库中的房间数据同步到Redis向量索引。不传roomId时触发全量重建")
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
