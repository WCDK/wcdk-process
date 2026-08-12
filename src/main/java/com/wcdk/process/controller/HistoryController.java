package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.history.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class HistoryController {

    private final HistoryProjector historyProjector;
    private final HistoryAuditService historyAuditService;

    @GetMapping("/process/{processInstanceId}")
    public Mono<ApiResponse<ProcessInstanceHistory>> getProcessInstanceHistory(
            @PathVariable String processInstanceId) {
        return historyProjector.getProcessInstanceHistory(processInstanceId)
                .map(h -> ApiResponse.success(h))
                .defaultIfEmpty(ApiResponse.error(404, "Process instance history not found"));
    }

    @GetMapping("/process/{processInstanceId}/activities")
    public Mono<ApiResponse<Flux<ActivityHistory>>> getActivityHistory(
            @PathVariable String processInstanceId) {
        return Mono.just(ApiResponse.success(historyProjector.getActivityHistory(processInstanceId)));
    }

    @GetMapping("/process/{processInstanceId}/tasks")
    public Mono<ApiResponse<Flux<TaskHistory>>> getTaskHistory(
            @PathVariable String processInstanceId) {
        return Mono.just(ApiResponse.success(historyProjector.getTaskHistory(processInstanceId)));
    }

    @GetMapping("/process/{processInstanceId}/variables")
    public Mono<ApiResponse<Flux<VariableHistory>>> getVariableHistory(
            @PathVariable String processInstanceId) {
        return Mono.just(ApiResponse.success(historyProjector.getVariableHistory(processInstanceId)));
    }

    @GetMapping("/process/{processInstanceId}/audit")
    public Mono<ApiResponse<ProcessAuditReport>> getAuditReport(
            @PathVariable String processInstanceId) {
        return historyAuditService.generateAuditReport(processInstanceId)
                .map(report -> ApiResponse.success(report))
                .onErrorResume(e -> Mono.just(ApiResponse.error(404, e.getMessage())));
    }
}