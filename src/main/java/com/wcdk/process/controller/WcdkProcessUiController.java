package com.wcdk.process.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.entity.DeploymentEntity;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ResourceEntity;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.repository.DeploymentRepository;
import com.wcdk.process.repository.ProcessDefinitionRepository;
import com.wcdk.process.repository.ResourceRepository;
import com.wcdk.process.repository.TaskRepository;
import com.wcdk.process.support.BpmnGraphSupport;
import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * wcdk-process-ui 流程管理兼容接口。
 *
 * @auther WCDK
 * @date 2026/8/11
 * @version 1.0
 */
@RestController
@RequestMapping("/wcdk_process")
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class WcdkProcessUiController {

    private static final String TENANT_ID = "default";
    private static final String MODEL_PREFIX = "UI_MODEL:";
    private static final String FORM_PREFIX = "UI_FORM:";

    private final DeploymentRepository deploymentRepository;
    private final ProcessDefinitionRepository definitionRepository;
    private final ResourceRepository resourceRepository;
    private final TaskRepository taskRepository;
    private final BpmnGraphSupport bpmnGraphSupport;
    private final ObjectMapper objectMapper;

    @GetMapping("/deploy/list")
    public Mono<ApiResponse<List<Map<String, Object>>>> listDeployments(
            @RequestParam(required = false) String deploymentName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String clientId) {
        return deploymentRepository.findAll()
                .filter(this::isWorkflowDeployment)
                .filter(item -> contains(item.getName(), deploymentName))
                .filter(item -> contains(item.getCategory(), category))
                .filter(item -> contains(string(readMetadata(item.getDescription()).get("clientId")), clientId))
                .concatMap(this::deploymentView)
                .collectList()
                .map(ApiResponse::success);
    }

    @GetMapping("/deploy/definition/list")
    public Mono<ApiResponse<List<Map<String, Object>>>> listDefinitions() {
        return definitionRepository.findAll()
                .map(this::definitionView)
                .collectList()
                .map(ApiResponse::success);
    }

    @GetMapping("/deploy/definition/{id}")
    public Mono<ApiResponse<Map<String, Object>>> definitionDetail(@PathVariable String id) {
        return definitionRepository.selectById(id)
                .flatMap(definition -> resourceRepository.findByDeploymentId(definition.getDeploymentId())
                        .filter(resource -> "BPMN".equals(resource.getResourceType())
                                || definition.getResourceName() != null
                                && definition.getResourceName().equals(resource.getName()))
                        .next()
                        .map(resource -> {
                            Map<String, Object> detail = definitionDetailView(definition);
                            detail.put("bpmnXml", resource.getContent());
                            return detail;
                        })
                        .defaultIfEmpty(definitionDetailView(definition)))
                .map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "流程定义不存在")));
    }

    @PutMapping("/deploy/definition/{id}")
    public Mono<ApiResponse<Map<String, Object>>> updateDefinition(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        String xml = string(payload.get("bpmnXml"));
        if (xml.isBlank()) {
            return Mono.just(ApiResponse.error(400, "BPMN 内容不能为空"));
        }
        BpmnGraphSupport.ParsedBpmn parsed;
        try {
            parsed = bpmnGraphSupport.parse(xml);
        } catch (IllegalArgumentException error) {
            return Mono.just(ApiResponse.error(400, error.getMessage()));
        }
        return definitionRepository.selectById(id)
                .flatMap(definition -> {
                    if (parsed.processKey() != null && !parsed.processKey().isBlank()) {
                        definition.setKey(parsed.processKey());
                    }
                    if (parsed.processName() != null && !parsed.processName().isBlank()) {
                        definition.setName(parsed.processName());
                    }
                    definition.setGraphJson(bpmnGraphSupport.toGraphJson(parsed.graph()));
                    definition.setUpdatedAt(Instant.now());
                    Mono<Void> saveResource = resourceRepository.findByDeploymentId(definition.getDeploymentId())
                            .filter(resource -> "BPMN".equals(resource.getResourceType())
                                    || definition.getResourceName() != null
                                && definition.getResourceName().equals(resource.getName()))
                            .next()
                            .flatMap(resource -> {
                                resource.setContent(xml);
                                return resourceRepository.updateById(resource).thenReturn(true);
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                ResourceEntity resource = modelResourceEntity(
                                        definition.getDeploymentId(), definition.getResourceName(), xml);
                                resource.setResourceType("BPMN");
                                return resourceRepository.insert(resource).thenReturn(true);
                            }))
                            .then();
                    return saveResource
                            .then(definitionRepository.updateById(definition))
                            .thenReturn(definitionDetailView(definition));
                })
                .map(detail -> ApiResponse.success("流程定义更新成功", detail))
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "流程定义不存在")));
    }

    @PostMapping(value = "/deploy/process", consumes = "multipart/form-data")
    public Mono<ApiResponse<Map<String, Object>>> deployProcess(
            @RequestPart("deploymentName") String deploymentName,
            @RequestPart(value = "category", required = false) String category,
            @RequestPart(value = "clientId", required = false) String clientId,
            @RequestPart(value = "processBeanName", required = false) String processBeanName,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("file") MultipartFile file) {
        return Mono.fromCallable(() -> new String(file.getBytes(), StandardCharsets.UTF_8))
                .flatMap(xml -> createDeployment(
                        deploymentName, category, description, xml, file.getOriginalFilename(), "UI_UPLOAD",
                        clientId, processBeanName))
                .map(ApiResponse::success)
                .onErrorResume(IllegalArgumentException.class,
                        error -> Mono.just(ApiResponse.error(400, error.getMessage())));
    }

    @DeleteMapping("/deploy")
    public Mono<ApiResponse<Boolean>> deleteDeployment(
            @RequestParam String deploymentId,
            @RequestParam(defaultValue = "true") boolean cascade) {
        Mono<Void> definitions = definitionRepository.findAll()
                .filter(item -> deploymentId.equals(item.getDeploymentId()))
                .concatMap(item -> definitionRepository.deleteById(item.getId()))
                .then();
        Mono<Void> resources = resourceRepository.findByDeploymentId(deploymentId)
                .concatMap(item -> resourceRepository.deleteById(item.getId()))
                .then();
        return definitions.then(resources)
                .then(deploymentRepository.deleteById(deploymentId))
                .thenReturn(ApiResponse.success("部署删除成功", true));
    }

    @PutMapping("/deploy/{id}/binding")
    public Mono<ApiResponse<Map<String, Object>>> updateBinding(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        return deploymentRepository.selectById(id)
                .flatMap(deployment -> {
                    Map<String, Object> metadata = readMetadata(deployment.getDescription());
                    metadata.put("description", payload.getOrDefault("description", metadata.get("description")));
                    metadata.put("clientId", payload.get("clientId"));
                    metadata.put("processBeanName", payload.get("processBeanName"));
                    deployment.setDescription(writeMetadata(metadata));
                    deployment.setUpdatedAt(Instant.now());
                    return deploymentRepository.updateById(deployment).then(deploymentView(deployment));
                })
                .map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "部署不存在")));
    }

    @GetMapping("/deploy/client/list")
    public Mono<ApiResponse<Map<String, Object>>> listDeployClients(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Mono.just(ApiResponse.success(page(List.of(), 0, pageNum, pageSize)));
    }

    @GetMapping("/deploy/client/{id}/process-bean/list")
    public Mono<ApiResponse<List<Map<String, Object>>>> listProcessBeans(@PathVariable String id) {
        return Mono.just(ApiResponse.success(List.of()));
    }

    @GetMapping("/model/list")
    public Mono<ApiResponse<List<Map<String, Object>>>> listModels(
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String modelKey,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean deployed) {
        return deploymentRepository.findAll()
                .filter(this::isModel)
                .filter(item -> contains(item.getName(), modelName))
                .filter(item -> contains(modelKey(item), modelKey))
                .filter(item -> contains(item.getCategory(), category))
                .concatMap(this::modelView)
                .filter(item -> deployed == null || deployed.equals(item.get("deployed")))
                .collectList()
                .map(ApiResponse::success);
    }

    @PostMapping("/model")
    public Mono<ApiResponse<Map<String, Object>>> createModel(@RequestBody Map<String, Object> payload) {
        return saveModel(null, payload).map(ApiResponse::success)
                .onErrorResume(IllegalArgumentException.class,
                        error -> Mono.just(ApiResponse.error(400, error.getMessage())));
    }

    @PutMapping("/model/{id}")
    public Mono<ApiResponse<Map<String, Object>>> updateModel(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        return saveModel(id, payload).map(ApiResponse::success)
                .onErrorResume(IllegalArgumentException.class,
                        error -> Mono.just(ApiResponse.error(400, error.getMessage())));
    }

    @DeleteMapping("/model/{id}")
    public Mono<ApiResponse<Boolean>> deleteModel(@PathVariable String id) {
        return resourceRepository.findByDeploymentId(id)
                .concatMap(resource -> resourceRepository.deleteById(resource.getId()))
                .then(deploymentRepository.deleteById(id))
                .thenReturn(ApiResponse.success("模型删除成功", true));
    }

    @GetMapping("/model/{id}/xml")
    public Mono<ApiResponse<String>> modelXml(@PathVariable String id) {
        return modelResource(id)
                .map(ResourceEntity::getContent)
                .map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "模型不存在")));
    }

    @PostMapping("/model/{id}/deploy")
    public Mono<ApiResponse<Map<String, Object>>> deployModel(
            @PathVariable String id,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String processBeanName) {
        return Mono.zip(deploymentRepository.selectById(id), modelResource(id))
                .flatMap(tuple -> {
                    DeploymentEntity model = tuple.getT1();
                    return createDeployment(model.getName(), model.getCategory(), null,
                            tuple.getT2().getContent(), tuple.getT2().getName(), "UI_MODEL_DEPLOY:" + id,
                            clientId, processBeanName);
                })
                .map(ApiResponse::success)
                .switchIfEmpty(Mono.just(ApiResponse.error(404, "模型不存在")))
                .onErrorResume(IllegalArgumentException.class,
                        error -> Mono.just(ApiResponse.error(400, error.getMessage())));
    }

    @GetMapping("/process/task/list")
    public Mono<ApiResponse<List<Map<String, Object>>>> listTasks(
            @RequestParam(required = false) String assignee) {
        Flux<TaskEntity> tasks = assignee == null || assignee.isBlank()
                ? taskRepository.findAll() : taskRepository.findByAssignee(assignee);
        return tasks.map(this::taskView).collectList().map(ApiResponse::success);
    }

    @DeleteMapping("/process/task/{id}")
    public Mono<ApiResponse<Boolean>> deleteTask(@PathVariable String id) {
        return taskRepository.deleteById(id)
                .thenReturn(ApiResponse.success("任务删除成功", true));
    }

    private Mono<Map<String, Object>> saveModel(String id, Map<String, Object> payload) {
        String name = string(payload.get("modelName"));
        String key = string(payload.get("modelKey"));
        String xml = string(payload.get("bpmnXml"));
        if (name.isBlank() || key.isBlank() || xml.isBlank()) {
            return Mono.error(new IllegalArgumentException("模型名称、模型标识和 BPMN 内容不能为空"));
        }
        bpmnGraphSupport.parse(xml);
        if (id == null) {
            String modelId = UUID.randomUUID().toString();
            Instant now = Instant.now();
            DeploymentEntity model = new DeploymentEntity();
            model.setId(modelId);
            model.setTenantId(TENANT_ID);
            model.setName(name);
            model.setCategory(string(payload.get("category")));
            model.setSourceSystem(MODEL_PREFIX + key);
            model.setDeploymentTime(now);
            model.setVersion(1);
            model.setCreatedAt(now);
            model.setUpdatedAt(now);
            ResourceEntity resource = modelResourceEntity(modelId, key + ".bpmn", xml);
            return deploymentRepository.insert(model)
                    .then(resourceRepository.insert(resource))
                    .then(modelView(model));
        }
        return deploymentRepository.selectById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("模型不存在")))
                .flatMap(model -> {
                    model.setName(name);
                    model.setCategory(string(payload.get("category")));
                    model.setSourceSystem(MODEL_PREFIX + key);
                    model.setVersion(model.getVersion() == null ? 1 : model.getVersion() + 1);
                    model.setUpdatedAt(Instant.now());
                    return modelResource(id)
                            .flatMap(resource -> {
                                resource.setName(key + ".bpmn");
                                resource.setContent(xml);
                                return deploymentRepository.updateById(model)
                                        .then(resourceRepository.updateById(resource))
                                        .then(modelView(model));
                            })
                            .switchIfEmpty(Mono.defer(() -> deploymentRepository.updateById(model)
                                    .then(resourceRepository.insert(modelResourceEntity(id, key + ".bpmn", xml)))
                                    .then(modelView(model))));
                });
    }

    private Mono<Map<String, Object>> createDeployment(
            String name, String category, String description, String xml, String fileName,
            String source, String clientId, String processBeanName) {
        BpmnGraphSupport.ParsedBpmn parsed = bpmnGraphSupport.parse(xml);
        String key = parsed.processKey() == null || parsed.processKey().isBlank()
                ? "process_" + UUID.randomUUID().toString().replace("-", "") : parsed.processKey();
        String deploymentId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("description", description);
        metadata.put("fileName", fileName);
        metadata.put("clientId", clientId);
        metadata.put("processBeanName", processBeanName);

        DeploymentEntity deployment = new DeploymentEntity();
        deployment.setId(deploymentId);
        deployment.setTenantId(TENANT_ID);
        deployment.setName(name);
        deployment.setCategory(category);
        deployment.setDescription(writeMetadata(metadata));
        deployment.setDeploymentTime(now);
        deployment.setSourceSystem(source);
        deployment.setVersion(1);
        deployment.setCreatedAt(now);
        deployment.setUpdatedAt(now);

        ResourceEntity resource = modelResourceEntity(deploymentId, fileName, xml);
        resource.setResourceType("BPMN");
        return definitionRepository.findByTenantIdAndKey(TENANT_ID, key).count()
                .flatMap(count -> {
                    ProcessDefinitionEntity definition = new ProcessDefinitionEntity();
                    definition.setId(UUID.randomUUID().toString());
                    definition.setTenantId(TENANT_ID);
                    definition.setKey(key);
                    definition.setName(name == null || name.isBlank() ? parsed.processName() : name);
                    definition.setVersion((int) (count + 1));
                    definition.setCategory(category);
                    definition.setDescription(description);
                    definition.setDeploymentId(deploymentId);
                    definition.setResourceName(fileName);
                    definition.setGraphJson(bpmnGraphSupport.toGraphJson(parsed.graph()));
                    definition.setSuspended(0);
                    definition.setCreatedAt(now);
                    definition.setUpdatedAt(now);
                    return deploymentRepository.insert(deployment)
                            .then(resourceRepository.insert(resource))
                            .then(definitionRepository.insert(definition))
                            .then(deploymentView(deployment));
                });
    }

    private Mono<Map<String, Object>> deploymentView(DeploymentEntity deployment) {
        Map<String, Object> metadata = readMetadata(deployment.getDescription());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("deploymentId", deployment.getId());
        view.put("deploymentName", deployment.getName());
        view.put("fileName", metadata.getOrDefault("fileName", "-"));
        view.put("category", deployment.getCategory());
        view.put("description", metadata.get("description"));
        view.put("clientIds", listValue(metadata.get("clientId")));
        view.put("clientNames", listValue(metadata.get("clientId")));
        view.put("processBeanNames", listValue(metadata.get("processBeanName")));
        view.put("invalidStatus", 0);
        view.put("deployTime", deployment.getDeploymentTime());
        return Mono.just(view);
    }

    private Map<String, Object> definitionView(ProcessDefinitionEntity definition) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("processDefinitionId", definition.getId());
        view.put("processDefinitionKey", definition.getKey());
        view.put("processDefinitionName", definition.getName());
        view.put("version", definition.getVersion());
        view.put("category", definition.getCategory());
        view.put("description", definition.getDescription());
        view.put("deploymentId", definition.getDeploymentId());
        view.put("resourceName", definition.getResourceName());
        view.put("suspended", Integer.valueOf(1).equals(definition.getSuspended()));
        view.put("invalidStatus", 0);
        return view;
    }

    private Map<String, Object> definitionDetailView(ProcessDefinitionEntity definition) {
        Map<String, Object> view = definitionView(definition);
        try {
            com.wcdk.process.execution.ProcessGraph graph =
                    objectMapper.readValue(definition.getGraphJson(), com.wcdk.process.execution.ProcessGraph.class);
            List<Map<String, Object>> nodes = graph.getNodes().values().stream().map(node -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("elementId", node.getId());
                item.put("elementType", toElementType(node.getNodeType()));
                item.put("elementName", node.getName());
                item.putAll(node.getProperties());
                return item;
            }).toList();
            List<Map<String, Object>> flows = graph.getEdges().values().stream().map(edge -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("elementId", edge.getId());
                item.put("sourceRef", edge.getSourceNodeId());
                item.put("targetRef", edge.getTargetNodeId());
                item.put("conditionExpression", edge.getConditionExpression());
                return item;
            }).toList();
            view.put("nodes", nodes);
            view.put("sequenceFlows", flows);
        } catch (Exception ignored) {
            view.put("nodes", List.of());
            view.put("sequenceFlows", List.of());
        }
        return view;
    }

    private Mono<Map<String, Object>> modelView(DeploymentEntity model) {
        return deploymentRepository.findAll()
                .filter(deployment -> ("UI_MODEL_DEPLOY:" + model.getId())
                        .equals(deployment.getSourceSystem()))
                .next()
                .map(deployment -> modelView(model, deployment.getId()))
                .defaultIfEmpty(modelView(model, null));
    }

    private Map<String, Object> modelView(DeploymentEntity model, String deploymentId) {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("modelId", model.getId());
                    view.put("modelName", model.getName());
                    view.put("modelKey", modelKey(model));
                    view.put("category", model.getCategory());
                    view.put("version", model.getVersion());
                    view.put("deployed", deploymentId != null);
                    view.put("deploymentId", deploymentId);
                    view.put("lastUpdateTime", model.getUpdatedAt());
                    return view;
    }

    private Mono<ResourceEntity> modelResource(String deploymentId) {
        return resourceRepository.findByDeploymentId(deploymentId).next();
    }

    private ResourceEntity modelResourceEntity(String deploymentId, String name, String content) {
        ResourceEntity resource = new ResourceEntity();
        resource.setId(UUID.randomUUID().toString());
        resource.setDeploymentId(deploymentId);
        resource.setTenantId(TENANT_ID);
        resource.setName(name == null || name.isBlank() ? "process.bpmn" : name);
        resource.setResourceType("MODEL");
        resource.setContent(content);
        resource.setCreatedAt(Instant.now());
        return resource;
    }

    private Map<String, Object> taskView(TaskEntity task) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("taskId", task.getId());
        view.put("taskName", task.getName());
        view.put("assignee", task.getAssignee());
        view.put("owner", task.getOwner());
        view.put("status", task.getState());
        view.put("processInstanceId", task.getProcessInstanceId());
        view.put("processDefinitionId", task.getProcessDefinitionId());
        view.put("taskDefinitionKey", task.getTaskDefinitionKey());
        view.put("createTime", task.getCreateTime());
        view.put("dueTime", task.getDueTime());
        return view;
    }

    private boolean isWorkflowDeployment(DeploymentEntity item) {
        return !isModel(item) && (item.getSourceSystem() == null || !item.getSourceSystem().startsWith(FORM_PREFIX));
    }

    private boolean isModel(DeploymentEntity item) {
        return item.getSourceSystem() != null && item.getSourceSystem().startsWith(MODEL_PREFIX);
    }

    private String modelKey(DeploymentEntity item) {
        return isModel(item) ? item.getSourceSystem().substring(MODEL_PREFIX.length()) : "";
    }

    private boolean contains(String value, String filter) {
        return filter == null || filter.isBlank()
                || value != null && value.toLowerCase().contains(filter.toLowerCase());
    }

    private String string(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private List<String> listValue(Object value) {
        String text = string(value);
        return text.isBlank() ? List.of() : List.of(text);
    }

    private Map<String, Object> readMetadata(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() { });
        } catch (Exception ignored) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("description", value);
            return result;
        }
    }

    private String writeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception exception) {
            throw new IllegalArgumentException("部署信息序列化失败", exception);
        }
    }

    private String toElementType(com.wcdk.process.enums.NodeType type) {
        if (type == null) {
            return "";
        }
        String[] parts = type.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return result.toString();
    }

    private Map<String, Object> page(List<?> records, long total, int pageNum, int pageSize) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return data;
    }
}