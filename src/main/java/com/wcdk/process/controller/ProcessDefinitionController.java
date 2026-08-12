package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.repository.ProcessDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/definition")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessDefinitionController {

    private final ProcessDefinitionRepository processDefinitionRepository;

    @GetMapping("/list")
    public Mono<ApiResponse<Flux<ProcessDefinitionResponse>>> listDefinitions(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String key) {
        Flux<ProcessDefinitionEntity> flux;
        if (tenantId != null && !tenantId.isBlank() && key != null && !key.isBlank()) {
            flux = processDefinitionRepository.findByTenantIdAndKey(tenantId, key);
        } else {
            flux = processDefinitionRepository.findAll();
        }
        return Mono.just(ApiResponse.success(flux.map(this::toResponse)));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<ProcessDefinitionResponse>> getDefinition(@PathVariable String id) {
        return processDefinitionRepository.selectById(id)
                .map(pd -> ApiResponse.success(toResponse(pd)))
                .defaultIfEmpty(ApiResponse.error(404, "Process definition not found"));
    }

    @GetMapping("/latest")
    public Mono<ApiResponse<ProcessDefinitionResponse>> getLatestDefinition(
            @RequestParam String tenantId,
            @RequestParam String key) {
        return processDefinitionRepository.findByTenantIdAndKey(tenantId, key)
                .sort((a, b) -> Integer.compare(b.getVersion(), a.getVersion()))
                .next()
                .map(pd -> ApiResponse.success(toResponse(pd)))
                .defaultIfEmpty(ApiResponse.error(404, "Process definition not found"));
    }

    @PostMapping("/{id}/suspend")
    public Mono<ApiResponse<Void>> suspendDefinition(@PathVariable String id) {
        return processDefinitionRepository.selectById(id)
                .flatMap(pd -> {
                    pd.setSuspended(1);
                    pd.setUpdatedAt(java.time.Instant.now());
                    return processDefinitionRepository.updateById(pd);
                })
                .then(Mono.just(ApiResponse.success(null)));
    }

    @PostMapping("/{id}/activate")
    public Mono<ApiResponse<Void>> activateDefinition(@PathVariable String id) {
        return processDefinitionRepository.selectById(id)
                .flatMap(pd -> {
                    pd.setSuspended(0);
                    pd.setUpdatedAt(java.time.Instant.now());
                    return processDefinitionRepository.updateById(pd);
                })
                .then(Mono.just(ApiResponse.success(null)));
    }

    private ProcessDefinitionResponse toResponse(ProcessDefinitionEntity pd) {
        return ProcessDefinitionResponse.builder()
                .id(pd.getId())
                .tenantId(pd.getTenantId())
                .key(pd.getKey())
                .name(pd.getName())
                .version(pd.getVersion())
                .category(pd.getCategory())
                .description(pd.getDescription())
                .deploymentId(pd.getDeploymentId())
                .resourceName(pd.getResourceName())
                .diagramResourceName(pd.getDiagramResourceName())
                .suspended(pd.getSuspended())
                .createdAt(pd.getCreatedAt())
                .updatedAt(pd.getUpdatedAt())
                .build();
    }
}