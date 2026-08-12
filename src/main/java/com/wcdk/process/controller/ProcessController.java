package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.ProcessInstanceResponse;
import com.wcdk.process.dto.StartProcessRequest;
import com.wcdk.process.engine.ReactiveProcessEngine;
import com.wcdk.process.engine.ReactiveRuntimeService;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessController {

    private final ReactiveProcessEngine processEngine;
    private final ProcessInstanceRepository processInstanceRepository;

    @PostMapping("/start")
    public Mono<ApiResponse<ProcessInstanceResponse>> startProcess(@RequestBody StartProcessRequest request) {
        ReactiveRuntimeService runtimeService = processEngine.getRuntimeService();
        return runtimeService.startProcessByKey(
                        request.getProcessDefinitionKey(),
                        request.getBusinessKey(),
                        request.getStarter(),
                        request.getVariables())
                .map(pi -> ApiResponse.success(toResponse(pi)));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<ProcessInstanceResponse>> getProcessInstance(@PathVariable String id) {
        return processInstanceRepository.selectById(id)
                .map(pi -> ApiResponse.success(toResponse(pi)))
                .defaultIfEmpty(ApiResponse.error(404, "Process instance not found"));
    }

    @GetMapping("/list")
    public Mono<ApiResponse<Flux<ProcessInstanceResponse>>> listProcessInstances(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String starter,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Flux<ProcessInstanceEntity> flux;
        if (status != null && !status.isBlank()) {
            flux = processInstanceRepository.findByStatus(status);
        } else if (starter != null && !starter.isBlank()) {
            flux = processInstanceRepository.findByStarter(starter);
        } else {
            flux = processInstanceRepository.findAll();
        }
        return Mono.just(ApiResponse.success(flux.map(this::toResponse)));
    }

    @PostMapping("/{id}/suspend")
    public Mono<ApiResponse<Void>> suspendProcess(@PathVariable String id) {
        return processEngine.getRuntimeService().suspendProcessInstance(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/{id}/activate")
    public Mono<ApiResponse<Void>> activateProcess(@PathVariable String id) {
        return processEngine.getRuntimeService().activateProcessInstance(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/{id}/terminate")
    public Mono<ApiResponse<Void>> terminateProcess(@PathVariable String id) {
        return processEngine.getRuntimeService().terminateProcessInstance(id)
                .then(Mono.just(ApiResponse.success(null)));
    }

    private ProcessInstanceResponse toResponse(ProcessInstanceEntity pi) {
        return ProcessInstanceResponse.builder()
                .id(pi.getId())
                .tenantId(pi.getTenantId())
                .processDefinitionId(pi.getProcessDefinitionId())
                .processDefinitionKey(pi.getProcessDefinitionKey())
                .processDefinitionVersion(pi.getProcessDefinitionVersion())
                .businessKey(pi.getBusinessKey())
                .starter(pi.getStarter())
                .startTime(pi.getStartTime())
                .endTime(pi.getEndTime())
                .durationMs(pi.getDurationMs())
                .status(pi.getStatus())
                .suspensionState(pi.getSuspensionState())
                .revision(pi.getRevision())
                .createdAt(pi.getCreatedAt())
                .updatedAt(pi.getUpdatedAt())
                .build();
    }
}