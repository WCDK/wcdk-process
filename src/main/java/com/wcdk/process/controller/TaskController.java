package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.TaskClaimRequest;
import com.wcdk.process.dto.TaskCompleteRequest;
import com.wcdk.process.dto.TaskResponse;
import com.wcdk.process.engine.ReactiveProcessEngine;
import com.wcdk.process.engine.ReactiveTaskService;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class TaskController {

    private final ReactiveProcessEngine processEngine;
    private final TaskRepository taskRepository;

    @PostMapping("/{id}/complete")
    public Mono<ApiResponse<TaskResponse>> completeTask(
            @PathVariable String id,
            @RequestBody(required = false) TaskCompleteRequest request) {
        ReactiveTaskService taskService = processEngine.getTaskService();
        String userId = request != null ? request.getUserId() : null;
        java.util.Map<String, Object> variables = request != null ? request.getVariables() : null;
        return taskService.completeTask(id, userId, variables)
                .map(task -> ApiResponse.success(toResponse(task)));
    }

    @PostMapping("/{id}/claim")
    public Mono<ApiResponse<TaskResponse>> claimTask(
            @PathVariable String id,
            @RequestBody TaskClaimRequest request) {
        ReactiveTaskService taskService = processEngine.getTaskService();
        return taskService.claimTask(id, request.getUserId())
                .map(task -> ApiResponse.success(toResponse(task)));
    }

    @PostMapping("/{id}/unclaim")
    public Mono<ApiResponse<TaskResponse>> unclaimTask(@PathVariable String id) {
        ReactiveTaskService taskService = processEngine.getTaskService();
        return taskService.unclaimTask(id)
                .map(task -> ApiResponse.success(toResponse(task)));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<TaskResponse>> getTask(@PathVariable String id) {
        return taskRepository.selectById(id)
                .map(task -> ApiResponse.success(toResponse(task)))
                .defaultIfEmpty(ApiResponse.error(404, "Task not found"));
    }

    @GetMapping("/list")
    public Mono<ApiResponse<Flux<TaskResponse>>> listTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String processInstanceId,
            @RequestParam(required = false) String state) {
        Flux<TaskEntity> flux;
        if (assignee != null && !assignee.isBlank()) {
            flux = taskRepository.findByAssignee(assignee);
        } else if (processInstanceId != null && !processInstanceId.isBlank()) {
            flux = taskRepository.findByProcessInstanceId(processInstanceId);
        } else if (state != null && !state.isBlank()) {
            flux = taskRepository.findByState(state);
        } else {
            flux = taskRepository.findAll();
        }
        return Mono.just(ApiResponse.success(flux.map(this::toResponse)));
    }

    private TaskResponse toResponse(TaskEntity task) {
        return TaskResponse.builder()
                .id(task.getId())
                .tenantId(task.getTenantId())
                .processInstanceId(task.getProcessInstanceId())
                .executionId(task.getExecutionId())
                .processDefinitionId(task.getProcessDefinitionId())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .name(task.getName())
                .state(task.getState())
                .assignee(task.getAssignee())
                .owner(task.getOwner())
                .priority(task.getPriority())
                .dueTime(task.getDueTime())
                .createTime(task.getCreateTime())
                .claimTime(task.getClaimTime())
                .completeTime(task.getCompleteTime())
                .description(task.getDescription())
                .revision(task.getRevision())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}