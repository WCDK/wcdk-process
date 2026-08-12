package com.wcdk.process.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.engine.ReactiveRuntimeService;
import com.wcdk.process.engine.ReactiveTaskService;
import com.wcdk.process.entity.DeploymentEntity;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.entity.ResourceEntity;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.DeploymentRepository;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.ProcessDefinitionRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import com.wcdk.process.repository.ResourceRepository;
import com.wcdk.process.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * wcdk-process-ui 流程业务、表单、概览及客户端接口。
 *
 * @auther WCDK
 * @date 2026/8/11
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessUiBusinessController {

    private static final String TENANT_ID = "default";
    private static final String FORM_PREFIX = "UI_FORM:";

    private final DeploymentRepository deploymentRepository;
    private final ProcessDefinitionRepository definitionRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ResourceRepository resourceRepository;
    private final TaskRepository taskRepository;
    private final ExecutionRepository executionRepository;
    private final ReactiveRuntimeService runtimeService;
    private final ReactiveTaskService taskService;
    private final ObjectMapper objectMapper;

    @GetMapping("/home/overview")
    public Mono<ApiResponse<Map<String, Long>>> overview() {
        Mono<Long> definitions = definitionRepository.findAll().count();
        Mono<Long> models = deploymentRepository.findAll()
                .filter(item -> item.getSourceSystem() != null
                        && item.getSourceSystem().startsWith("UI_MODEL:"))
                .count();
        Mono<Long> processes = processInstanceRepository.findAll().count();
        Mono<Long> tasks = taskRepository.findAll().count();
        return Mono.zip(definitions, models, processes, tasks)
                .map(tuple -> {
                    Map<String, Long> result = new LinkedHashMap<>();
                    result.put("definitionCount", tuple.getT1());
                    result.put("modelCount", tuple.getT2());
                    result.put("processCount", tuple.getT3());
                    result.put("taskCount", tuple.getT4());
                    return ApiResponse.success(result);
                });
    }

    @PostMapping("/process/request")
    public Mono<ApiResponse<Map<String, Object>>> createProcess(@RequestBody Map<String, Object> payload) {
        String definitionId = string(payload.get("processDefinitionId"));
        return definitionRepository.selectById(definitionId)
                .flatMap(definition -> {
                    String businessTitle = string(payload.get("businessTitle"));
                    String businessKey = businessTitle.isBlank() ? UUID.randomUUID().toString() : businessTitle;
                    String starter = string(payload.get("starter"));
                    if (starter.isBlank()) {
                        starter = "admin";
                    }
                    Map<String, Object> variables = new LinkedHashMap<>(payload);
                    variables.putIfAbsent("taskName", payload.get("taskName"));
                    variables.putIfAbsent("businessTitle", businessTitle);
                    return runtimeService.startProcessByKey(
                            definition.getTenantId(), definition.getKey(), businessKey, starter, variables);
                })
                .map(this::processView)
                .map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "流程定义不存在")));
    }

    @GetMapping("/process/request/list")
    public Mono<ApiResponse<Map<String, Object>>> listProcesses(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String processNo,
            @RequestParam(required = false) String starter,
            @RequestParam(required = false) String businessTitle,
            @RequestParam(required = false) String processDefinitionKey,
            @RequestParam(required = false) String status) {
        int safePage = Math.max(1, pageNum);
        int safeSize = Math.max(1, Math.min(pageSize, 500));
        return processInstanceRepository.findAll()
                .filter(item -> contains(item.getId(), processNo))
                .filter(item -> contains(item.getStarter(), starter))
                .filter(item -> contains(item.getBusinessKey(), businessTitle))
                .filter(item -> contains(item.getProcessDefinitionKey(), processDefinitionKey))
                .filter(item -> contains(item.getStatus(), status))
                .map(this::processView)
                .collectList()
                .map(all -> {
                    int from = Math.min((safePage - 1) * safeSize, all.size());
                    int to = Math.min(from + safeSize, all.size());
                    Map<String, Object> page = new LinkedHashMap<>();
                    page.put("records", all.subList(from, to));
                    page.put("total", (long) all.size());
                    page.put("pageNum", safePage);
                    page.put("pageSize", safeSize);
                    return ApiResponse.success(page);
                });
    }

    @PostMapping("/process/request/{id}/submit")
    public Mono<ApiResponse<Map<String, Object>>> submitProcess(@PathVariable String id) {
        return processInstanceRepository.selectById(id)
                .map(this::processView)
                .map(data -> ApiResponse.success("流程已提交", data))
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "流程实例不存在")));
    }

    @DeleteMapping("/process/request/{id}")
    public Mono<ApiResponse<Boolean>> deleteProcess(@PathVariable String id) {
        Mono<Void> tasks = taskRepository.findByProcessInstanceId(id)
                .concatMap(item -> taskRepository.deleteById(item.getId()))
                .then();
        Mono<Void> executions = executionRepository.findByProcessInstanceId(id)
                .concatMap(item -> executionRepository.deleteById(item.getId()))
                .then();
        return tasks.then(executions)
                .then(processInstanceRepository.deleteById(id))
                .thenReturn(ApiResponse.success("流程实例删除成功", true));
    }

    @PostMapping("/process/request/approve")
    public Mono<ApiResponse<Map<String, Object>>> approve(@RequestBody Map<String, Object> payload) {
        String taskId = string(payload.get("taskId"));
        String assignee = string(payload.get("assignee"));
        if (assignee.isBlank()) {
            assignee = "admin";
        }
        return taskService.completeTask(taskId, assignee, payload)
                .map(this::taskView)
                .map(data -> ApiResponse.success("任务处理成功", data))
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "任务不存在")));
    }

    @GetMapping("/process/request/{id}/diagram")
    public Mono<ApiResponse<Map<String, Object>>> processDiagram(@PathVariable String id) {
        return processInstanceRepository.selectById(id)
                .flatMap(instance -> definitionRepository.selectById(instance.getProcessDefinitionId()))
                .map(this::definitionDiagram)
                .map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "流程图不存在")));
    }

    @PostMapping("/process/form")
    public Mono<ApiResponse<Map<String, Object>>> saveForm(@RequestBody Map<String, Object> payload) {
        String key = string(payload.get("formKey"));
        String name = string(payload.get("formName"));
        if (key.isBlank() || name.isBlank()) {
            return Mono.just(ApiResponse.error(400, "表单名称和表单标识不能为空"));
        }
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        DeploymentEntity form = new DeploymentEntity();
        form.setId(id);
        form.setTenantId(TENANT_ID);
        form.setName(name);
        form.setSourceSystem(FORM_PREFIX + key);
        form.setDeploymentTime(now);
        form.setVersion(1);
        form.setCreatedAt(now);
        form.setUpdatedAt(now);
        ResourceEntity resource = new ResourceEntity();
        resource.setId(UUID.randomUUID().toString());
        resource.setDeploymentId(id);
        resource.setTenantId(TENANT_ID);
        resource.setName(key + ".json");
        resource.setResourceType("FORM");
        resource.setContent(writeJson(payload.getOrDefault("schema", List.of())));
        resource.setCreatedAt(now);
        return deploymentRepository.insert(form)
                .then(resourceRepository.insert(resource))
                .thenReturn(ApiResponse.success(formView(form, payload.get("schema"))));
    }

    @GetMapping("/process/form/list")
    public Mono<ApiResponse<Map<String, Object>>> listForms(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String formKey) {
        int safePage = Math.max(pageNum, 1);
        int safeSize = Math.max(1, Math.min(pageSize, 500));
        return deploymentRepository.findAll()
                .filter(item -> item.getSourceSystem() != null && item.getSourceSystem().startsWith(FORM_PREFIX))
                .filter(item -> contains(item.getName(), formName))
                .filter(item -> contains(item.getSourceSystem().substring(FORM_PREFIX.length()), formKey))
                .concatMap(form -> resourceRepository.findByDeploymentId(form.getId()).next()
                        .map(resource -> formView(form, readJsonList(resource.getContent())))
                        .defaultIfEmpty(formView(form, List.of())))
                .collectList()
                .map(all -> {
                    int from = Math.min((safePage - 1) * safeSize, all.size());
                    int to = Math.min(from + safeSize, all.size());
                    Map<String, Object> page = new LinkedHashMap<>();
                    page.put("records", all.subList(from, to));
                    page.put("total", (long) all.size());
                    page.put("pageNum", safePage);
                    page.put("pageSize", safeSize);
                    return ApiResponse.success(page);
                });
    }

    @DeleteMapping("/process/form/{id}")
    public Mono<ApiResponse<Boolean>> deleteForm(@PathVariable String id) {
        return resourceRepository.findByDeploymentId(id)
                .concatMap(resource -> resourceRepository.deleteById(resource.getId()))
                .then(deploymentRepository.deleteById(id))
                .thenReturn(ApiResponse.success("表单删除成功", true));
    }

    @GetMapping({"/wcdk/process/client/list", "/wcdk_process/client/list"})
    public Mono<ApiResponse<Map<String, Object>>> listClients(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("records", List.of());
        page.put("total", 0L);
        page.put("pageNum", pageNum);
        page.put("pageSize", pageSize);
        return Mono.just(ApiResponse.success(page));
    }

    @PostMapping({"/wcdk/process/client/{id}/detect", "/wcdk_process/client/{id}/detect"})
    public Mono<ApiResponse<Map<String, Object>>> detectClient(@PathVariable String id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("clientId", id);
        result.put("online", false);
        result.put("message", "未发现已注册的流程客户端");
        return Mono.just(ApiResponse.success(result));
    }

    @DeleteMapping({"/wcdk/process/client/{id}", "/wcdk_process/client/{id}"})
    public Mono<ApiResponse<Boolean>> deleteClient(@PathVariable String id) {
        return Mono.just(ApiResponse.success("客户端已移除", true));
    }

    private Map<String, Object> processView(ProcessInstanceEntity instance) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", instance.getId());
        view.put("processRequestId", instance.getId());
        view.put("processNo", instance.getId());
        view.put("processInstanceId", instance.getId());
        view.put("processDefinitionId", instance.getProcessDefinitionId());
        view.put("processDefinitionKey", instance.getProcessDefinitionKey());
        view.put("businessTitle", instance.getBusinessKey());
        view.put("taskName", instance.getBusinessKey());
        view.put("starter", instance.getStarter());
        view.put("status", "RUNNING".equals(instance.getStatus()) ? "PROCESSING" : instance.getStatus());
        view.put("startTime", instance.getStartTime());
        view.put("endTime", instance.getEndTime());
        return view;
    }

    private Map<String, Object> taskView(TaskEntity task) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("taskId", task.getId());
        view.put("taskName", task.getName());
        view.put("assignee", task.getAssignee());
        view.put("status", task.getState());
        view.put("processInstanceId", task.getProcessInstanceId());
        view.put("completeTime", task.getCompleteTime());
        return view;
    }

    private Map<String, Object> definitionDiagram(ProcessDefinitionEntity definition) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processDefinitionId", definition.getId());
        result.put("processDefinitionKey", definition.getKey());
        result.put("processDefinitionName", definition.getName());
        try {
            ProcessGraph graph = objectMapper.readValue(definition.getGraphJson(), ProcessGraph.class);
            result.put("nodes", graph.getNodes().values().stream().map(node -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("elementId", node.getId());
                item.put("elementType", node.getNodeType() == null ? "" : node.getNodeType().name());
                item.put("elementName", node.getName());
                item.putAll(node.getProperties());
                return item;
            }).toList());
            result.put("sequenceFlows", graph.getEdges().values().stream().map(edge -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("elementId", edge.getId());
                item.put("sourceRef", edge.getSourceNodeId());
                item.put("targetRef", edge.getTargetNodeId());
                item.put("conditionExpression", edge.getConditionExpression());
                return item;
            }).toList());
        } catch (Exception ignored) {
            result.put("nodes", List.of());
            result.put("sequenceFlows", List.of());
        }
        return result;
    }

    private Map<String, Object> formView(DeploymentEntity form, Object schema) {
        List<?> fields = schema instanceof List<?> list ? list : List.of();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", form.getId());
        view.put("formName", form.getName());
        view.put("formKey", form.getSourceSystem().substring(FORM_PREFIX.length()));
        view.put("fieldCount", fields.size());
        view.put("schema", fields);
        view.put("updateTime", form.getUpdatedAt());
        return view;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("表单结构序列化失败", exception);
        }
    }

    private List<?> readJsonList(String value) {
        try {
            return objectMapper.readValue(value, List.class);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private boolean contains(String value, String filter) {
        return filter == null || filter.isBlank()
                || value != null && value.toLowerCase().contains(filter.toLowerCase());
    }

    private String string(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}