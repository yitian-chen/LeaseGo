package com.zju.lease.agent.controller.agent;

import com.zju.lease.agent.service.ApartmentDataIngestor;
import com.zju.lease.agent.service.ApartmentSearchAgent;
import com.zju.lease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "AI智能搜索房间")
    @PostMapping("/search")
    public Result<String> search(@RequestParam String query) {
        log.info("Agent search query: {}", query);
        String response = apartmentSearchAgent.search(query);
        return Result.ok(response);
    }

    @Operation(summary = "手动触发房间数据重新索引")
    @PostMapping("/admin/reindex")
    public Result<String> reindex(@RequestParam(required = false) Long roomId) {
        if (roomId != null) {
            apartmentDataIngestor.ingestRoomAsync(roomId);
            return Result.ok("Room " + roomId + " re-index triggered.");
        } else {
            apartmentDataIngestor.fullReindex();
            return Result.ok("Full re-index triggered.");
        }
    }
}
