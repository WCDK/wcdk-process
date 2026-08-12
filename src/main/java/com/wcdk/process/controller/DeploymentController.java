package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.DeployProcessRequest;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.entity.DeploymentEntity;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ResourceEntity;
import com.wcdk.process.repository.DeploymentRepository;
import com.wcdk.process.repository.ProcessDefinitionRepository;
import com.wcdk.process.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/deployment")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class DeploymentController {

    private final DeploymentRepository deploymentRepository;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ResourceRepository resourceRepository;

    @PostMapping("/deploy")
    public Mono<ApiResponse<DeploymentResponse>> deploy(@RequestBody DeployProcessRequest request) {
        String deploymentId = UUID.randomUUID().toString();
        String tenantId = request.getTenantId() != null ? request.getTenantId() : "default";

        DeploymentEntity deployment = new DeploymentEntity();
        deployment.setId(deploymentId);
        deployment.setTenantId(tenantId);
        deployment.setName(request.getName());
        deployment.setCategory(request.getCategory());
        deployment.setDescription(request.getDescription());
        deployment.setDeploymentTime(Instant.now());
        deployment.setSourceSystem("API");
        deployment.setVersion(1);
        deployment.setCreatedAt(Instant.now());
        deployment.setUpdatedAt(Instant.now());

        return deploymentRepository.insert(deployment)
                .then(Mono.defer(() -> {
                    ResourceEntity resource = new ResourceEntity();
                    resource.setId(UUID.randomUUID().toString());
                    resource.setDeploymentId(deploymentId);
                    resource.setTenantId(tenantId);
                    resource.setName(request.getName() != null ? request.getName() + ".bpmn" : "process.bpmn");
                    resource.setResourceType("BPMN");
                    resource.setContent(request.getBpmnXml());
                    resource.setCreatedAt(Instant.now());
                    return resourceRepository.insert(resource);
                }))
                .then(Mono.defer(() -> {
                    return processDefinitionRepository.findByTenantIdAndKey(tenantId, request.getProcessDefinitionKey())
                            .count()
                            .flatMap(count -> {
                                ProcessDefinitionEntity pd = new ProcessDefinitionEntity();
                                pd.setId(UUID.randomUUID().toString());
                                pd.setTenantId(tenantId);
                                pd.setKey(request.getProcessDefinitionKey());
                                pd.setName(request.getName());
                                pd.setVersion((int) (count + 1));
                                pd.setCategory(request.getCategory());
                                pd.setDescription(request.getDescription());
                                pd.setDeploymentId(deploymentId);
                                pd.setResourceName(request.getName() != null ? request.getName() + ".bpmn" : "process.bpmn");
                                pd.setGraphJson(extractGraphJson(request.getBpmnXml()));
                                pd.setSuspended(0);
                                pd.setCreatedAt(Instant.now());
                                pd.setUpdatedAt(Instant.now());
                                return processDefinitionRepository.insert(pd);
                            });
                }))
                .then(Mono.just(ApiResponse.success(toResponse(deployment))));
    }

    @GetMapping("/list")
    public Mono<ApiResponse<Flux<DeploymentResponse>>> listDeployments(
            @RequestParam(required = false) String tenantId) {
        Flux<DeploymentEntity> flux;
        if (tenantId != null && !tenantId.isBlank()) {
            flux = deploymentRepository.findByTenantId(tenantId);
        } else {
            flux = deploymentRepository.findAll();
        }
        return Mono.just(ApiResponse.success(flux.map(this::toResponse)));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<DeploymentResponse>> getDeployment(@PathVariable String id) {
        return deploymentRepository.selectById(id)
                .map(d -> ApiResponse.success(toResponse(d)))
                .defaultIfEmpty(ApiResponse.error(404, "Deployment not found"));
    }

    private String extractGraphJson(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.isBlank()) {
            return null;
        }
        return "{\"bpmnXml\":\"" + bpmnXml.replace("\"", "\\\"").replace("\n", "\\n") + "\"}";
    }

    private DeploymentResponse toResponse(DeploymentEntity d) {
        return DeploymentResponse.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .name(d.getName())
                .category(d.getCategory())
                .description(d.getDescription())
                .deploymentTime(d.getDeploymentTime())
                .sourceSystem(d.getSourceSystem())
                .version(d.getVersion())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}