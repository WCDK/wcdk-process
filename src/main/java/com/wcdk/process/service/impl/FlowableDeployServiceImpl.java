package com.wcdk.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wcdk.process.common.json.JSON;
import com.wcdk.process.common.json.JsonArray;
import com.wcdk.process.common.json.JsonObject;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ProcessActionButtonResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDiagramEdgeResponse;
import com.wcdk.process.dto.ProcessDiagramNodeResponse;
import com.wcdk.process.dto.ProcessDiagramWaypointResponse;
import com.wcdk.process.dto.ProcessFormFieldResponse;
import com.wcdk.process.dto.ProcessFormOptionResponse;
import com.wcdk.process.dto.ProcessFormTableCellResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import com.wcdk.process.dto.ProcessDefinitionUpdateRequest;
import com.wcdk.process.entity.WcdkProcessClient;
import com.wcdk.process.entity.WcdkProcessClientProcess;
import com.wcdk.process.entity.WcdkProcessDefinitionMeta;
import com.wcdk.process.entity.WcdkProcessForm;
import com.wcdk.process.entity.WcdkProcessFormBinding;
import com.wcdk.process.mapper.WcdkProcessClientMapper;
import com.wcdk.process.mapper.WcdkProcessClientProcessMapper;
import com.wcdk.process.mapper.WcdkProcessDefinitionMetaMapper;
import com.wcdk.process.mapper.WcdkProcessFormBindingMapper;
import com.wcdk.process.mapper.WcdkProcessFormMapper;
import com.wcdk.process.service.FlowableDeployService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowElementsContainer;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableDeployServiceImpl implements FlowableDeployService {

    private static final String START_EVENT = "StartEvent";

    private static final String USER_TASK = "UserTask";

    private static final int DEPLOYMENT_NAME_MIN_LENGTH = 4;

    private static final int DEPLOYMENT_NAME_MAX_LENGTH = 50;

    private static final Pattern DEPLOYMENT_NAME_PATTERN =
//            Pattern.compile("^(?=.*[A-Za-z0-9一-龥])[A-Za-z0-9一-龥._\\-()（）]{4,50}$");
    Pattern.compile("^(?=.*[A-Za-z0-9\\u4e00-\\u9fa5])[A-Za-z0-9\\u4e00-\\u9fa5._\\-()（）]{4,50}$");


    private final RepositoryService repositoryService;

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    private final WcdkProcessClientProcessMapper wcdkProcessClientProcessMapper;

    private final WcdkProcessClientMapper wcdkProcessClientMapper;

    private final WcdkProcessDefinitionMetaMapper wcdkProcessDefinitionMetaMapper;

    private final WcdkProcessFormBindingMapper wcdkProcessFormBindingMapper;

    private final WcdkProcessFormMapper wcdkProcessFormMapper;

    @Override
    public DeploymentResponse deployProcess(String deploymentName, String category, String clientId, String processBeanName, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("部署文件不能为空");
        }
        String validDeploymentName = validateDeploymentName(deploymentName);
        String validClientId = normalizeOptionalValue(clientId);
        String validProcessBeanName = normalizeOptionalValue(processBeanName);
        log.info("开始部署流程文件，流程名称：{}，文件名：{}", validDeploymentName, file.getOriginalFilename());
        try {
            byte[] fileBytes = file.getBytes();
            BpmnModel bpmnModel = parseBpmnModel(fileBytes);
            validateBpmnModel(bpmnModel);
            validateProcessDefinitionUnique(bpmnModel);
            String resourceName = resolveDeploymentResourceName(file.getOriginalFilename());
            byte[] deployBytes = new BpmnXMLConverter().convertToXML(bpmnModel, StandardCharsets.UTF_8.name());
            Deployment deployment = repositoryService.createDeployment()
                    .name(validDeploymentName)
                    .category(category)
                    .addBytes(resourceName, deployBytes)
                    .deploy();
            log.info("流程部署成功，部署ID：{}", deployment.getId());
            initDeploymentDefinitionMeta(deployment.getId());
            bindDeploymentProcessDefinitions(deployment.getId(), validClientId, validProcessBeanName);
            return buildDeploymentResponse(deployment);
        } catch (IOException ex) {
            log.error("读取流程部署文件失败", ex);
            throw new IllegalArgumentException("读取流程部署文件失败");
        } catch (XMLStreamException ex) {
            log.error("解析 BPMN 文件失败", ex);
            throw new IllegalArgumentException("解析 BPMN 文件失败");
        }
    }

    @Override
    public List<DeploymentResponse> listDeployment(String deploymentName, String category, String clientId) {
        List<Deployment> deployments = repositoryService.createDeploymentQuery()
                .orderByDeploymentTime()
                .desc()
                .list();
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        Map<String, List<ProcessDefinition>> deploymentDefinitionMap = processDefinitions.stream()
                .collect(Collectors.groupingBy(ProcessDefinition::getDeploymentId));
        Map<String, List<WcdkProcessClientProcess>> bindingMap = listProcessBindingMap(processDefinitions);
        Map<String, String> clientNameMap = listClientNameMap(bindingMap);
        Map<String, Integer> invalidStatusMap = listInvalidStatusMap(processDefinitions);
        return deployments.stream()
                .map(deployment -> buildDeploymentResponse(
                        deployment,
                        deploymentDefinitionMap.getOrDefault(deployment.getId(), List.of()),
                        bindingMap,
                        clientNameMap,
                        invalidStatusMap
                ))
                .filter(deployment -> matchesDeploymentName(deployment, deploymentName))
                .filter(deployment -> matchesCategory(deployment, category))
                .filter(deployment -> matchesClient(deployment, clientId))
                .toList();
    }

    @Override
    public List<ProcessDefinitionResponse> listProcessDefinition() {
        Map<String, String> deploymentCategoryMap = repositoryService.createDeploymentQuery()
                .list()
                .stream()
                .collect(Collectors.toMap(Deployment::getId, Deployment::getCategory, (left, right) -> right));
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        Map<String, List<WcdkProcessClientProcess>> bindingMap = listProcessBindingMap(processDefinitions);
        Map<String, String> clientNameMap = listClientNameMap(bindingMap);
        Map<String, Integer> invalidStatusMap = listInvalidStatusMap(processDefinitions);
        return processDefinitions.stream()
                .map(processDefinition -> buildProcessDefinitionResponse(
                        processDefinition,
                        deploymentCategoryMap.get(processDefinition.getDeploymentId()),
                        bindingMap.getOrDefault(processDefinition.getId(), List.of()),
                        clientNameMap,
                        invalidStatusMap.getOrDefault(processDefinition.getId(), 0)
                ))
                .toList();
    }

    @Override
    public ProcessDefinitionDetailResponse getProcessDefinitionDetail(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            throw new IllegalArgumentException("流程定义ID不能为空");
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        if (processDefinition == null) {
            throw new IllegalArgumentException("未查询到对应流程定义，流程定义ID：" + processDefinitionId);
        }
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(processDefinition.getDeploymentId())
                .singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Process process = bpmnModel == null ? null : bpmnModel.getMainProcess();
        Map<String, String> nodeParentMap = process == null ? Map.of() : buildFlowNodeParentMap(process, "");
        List<ProcessDiagramNodeResponse> nodes = process == null ? List.of() : collectFlowNodes(process)
                .stream()
                .sorted(Comparator
                        .comparing((FlowNode node) -> resolveNodeX(bpmnModel, node.getId()))
                        .thenComparing(node -> resolveNodeY(bpmnModel, node.getId())))
                .map(node -> buildNodeResponse(bpmnModel, node, nodeParentMap.get(node.getId())))
                .toList();
        List<ProcessDiagramEdgeResponse> sequenceFlows = process == null ? List.of() : collectSequenceFlows(process)
                .stream()
                .map(sequenceFlow -> buildEdgeResponse(bpmnModel, sequenceFlow))
                .toList();
        fillNodeBoundForms(nodes, processDefinition);
        String bpmnXml = readBpmnXml(processDefinition);
        DynamicPageSchema dynamicPageSchema = buildDynamicPageSchema(bpmnXml, nodes, processDefinition.getKey());
        long userTaskCount = nodes.stream()
                .filter(node -> USER_TASK.equals(node.getElementType()))
                .count();
        return ProcessDefinitionDetailResponse.builder()
                .processDefinitionId(processDefinition.getId())
                .processDefinitionKey(processDefinition.getKey())
                .processDefinitionName(processDefinition.getName())
                .category(deployment == null ? null : deployment.getCategory())
                .version(processDefinition.getVersion())
                .deploymentId(processDefinition.getDeploymentId())
                .deploymentName(deployment == null ? null : deployment.getName())
                .resourceName(processDefinition.getResourceName())
                .suspended(processDefinition.isSuspended())
                .nodeCount(nodes.size())
                .userTaskCount((int) userTaskCount)
                .sequenceFlowCount(sequenceFlows.size())
                .bpmnXml(bpmnXml)
                .formFields(dynamicPageSchema.formFields())
                .actionButtons(dynamicPageSchema.actionButtons())
                .nodes(nodes)
                .sequenceFlows(sequenceFlows)
                .build();
    }

    private void fillNodeBoundForms(List<ProcessDiagramNodeResponse> nodes, ProcessDefinition processDefinition) {
        if (nodes == null || nodes.isEmpty() || processDefinition == null || !StringUtils.hasText(processDefinition.getId())) {
            return;
        }
        List<WcdkProcessFormBinding> bindings = wcdkProcessFormBindingMapper.selectList(new LambdaQueryWrapper<WcdkProcessFormBinding>()
                .eq(WcdkProcessFormBinding::getProcessDefinitionId, processDefinition.getId())
                .eq(WcdkProcessFormBinding::getStatus, 1)
                .orderByDesc(WcdkProcessFormBinding::getUpdateTime));
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        List<Long> formIds = bindings.stream()
                .map(WcdkProcessFormBinding::getFormId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (formIds.isEmpty()) {
            return;
        }
        Map<Long, WcdkProcessForm> formMap = wcdkProcessFormMapper.selectBatchIds(formIds).stream()
                .filter(form -> form != null && Objects.equals(form.getStatus(), 1))
                .collect(Collectors.toMap(WcdkProcessForm::getId, form -> form, (left, right) -> right));
        if (formMap.isEmpty()) {
            return;
        }
        Map<String, List<Map<String, Object>>> nodeFormMap = new LinkedHashMap<>();
        for (WcdkProcessFormBinding binding : bindings) {
            if (binding == null || !StringUtils.hasText(binding.getTaskDefinitionKey())) {
                continue;
            }
            WcdkProcessForm form = formMap.get(binding.getFormId());
            if (form == null) {
                continue;
            }
            nodeFormMap.computeIfAbsent(binding.getTaskDefinitionKey(), key -> new ArrayList<>())
                    .add(buildNodeBoundForm(binding, form));
        }
        for (ProcessDiagramNodeResponse node : nodes) {
            List<Map<String, Object>> boundForms = nodeFormMap.get(node.getElementId());
            if (boundForms == null || boundForms.isEmpty()) {
                continue;
            }
            List<String> formKeys = boundForms.stream()
                    .map(form -> Objects.toString(form.get("formKey"), ""))
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toList();
            node.setBoundForms(boundForms);
            node.setBoundFormKeys(formKeys);
            node.setFormKey(String.join(",", formKeys));
        }
    }

    private Map<String, Object> buildNodeBoundForm(WcdkProcessFormBinding binding, WcdkProcessForm form) {
        Map<String, Object> result = new LinkedHashMap<>();
        Object schema = parseBoundFormSchema(form.getFormSchemaJson());
        result.put("formId", form.getId());
        result.put("formKey", form.getFormKey());
        result.put("formName", form.getFormName());
        result.put("formVersion", form.getFormVersion());
        result.put("processNodeId", binding.getTaskDefinitionKey());
        result.put("fieldCount", schema instanceof List<?> ? ((List<?>) schema).size() : 0);
        result.put("schema", schema);
        return result;
    }

    private Object parseBoundFormSchema(String schemaJson) {
        if (!StringUtils.hasText(schemaJson)) {
            return List.of();
        }
        try {
            Object schema = JSON.parse(schemaJson);
            return schema == null ? List.of() : schema;
        } catch (Exception ex) {
            log.warn("Failed to parse bound form schema. schemaJson={}", schemaJson, ex);
            return List.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessDefinitionDetailResponse updateProcessDefinition(ProcessDefinitionUpdateRequest request) {
        if (request == null || !StringUtils.hasText(request.getProcessDefinitionId())) {
            throw new IllegalArgumentException("流程定义ID不能为空");
        }
        if (!StringUtils.hasText(request.getBpmnXml())) {
            throw new IllegalArgumentException("BPMN XML 内容不能为空");
        }
        String oldProcessDefinitionId = request.getProcessDefinitionId().trim();
        ProcessDefinition oldProcessDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(oldProcessDefinitionId)
                .singleResult();
        if (oldProcessDefinition == null) {
            throw new IllegalArgumentException("未查询到对应流程定义，流程定义ID：" + oldProcessDefinitionId);
        }
        String sourceDeploymentId = StringUtils.hasText(request.getDeploymentId())
                ? request.getDeploymentId().trim()
                : oldProcessDefinition.getDeploymentId();
        Deployment oldDeployment = repositoryService.createDeploymentQuery()
                .deploymentId(sourceDeploymentId)
                .singleResult();
        if (oldDeployment == null) {
            throw new IllegalArgumentException("未查询到对应部署，部署ID：" + sourceDeploymentId);
        }
        try {
            BpmnModel bpmnModel = parseBpmnModel(request.getBpmnXml().getBytes(StandardCharsets.UTF_8));
            validateBpmnModel(bpmnModel);
            validateUpdateProcessDefinitionKey(bpmnModel, oldProcessDefinition);
            String resourceName = StringUtils.hasText(oldProcessDefinition.getResourceName())
                    ? oldProcessDefinition.getResourceName()
                    : bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
            byte[] deployBytes = new BpmnXMLConverter().convertToXML(bpmnModel, StandardCharsets.UTF_8.name());
            Deployment newDeployment = repositoryService.createDeployment()
                    .name(oldDeployment.getName())
                    .category(oldDeployment.getCategory())
                    .addBytes(resourceName, deployBytes)
                    .deploy();
            ProcessDefinition newProcessDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(newDeployment.getId())
                    .processDefinitionKey(oldProcessDefinition.getKey())
                    .singleResult();
            if (newProcessDefinition == null) {
                throw new IllegalArgumentException("流程修改后未生成新版本流程定义");
            }
            markProcessDefinitionInvalid(oldProcessDefinition.getId());
            ensureProcessDefinitionMeta(newProcessDefinition, 0);
            copyProcessClientBindings(oldProcessDefinition.getId(), newProcessDefinition);
            return getProcessDefinitionDetail(newProcessDefinition.getId());
        } catch (XMLStreamException ex) {
            log.error("修改流程定义时解析 BPMN 失败", ex);
            throw new IllegalArgumentException("解析 BPMN 文件失败");
        }
    }

    @Override
    public void updateDeploymentBinding(String deploymentId, String clientId, String processBeanName) {
        if (!StringUtils.hasText(deploymentId)) {
            throw new IllegalArgumentException("部署ID不能为空");
        }
        String validClientId = normalizeRequiredValue(clientId, "客户端不能为空");
        String validProcessBeanName = normalizeRequiredValue(processBeanName, "流程处理器不能为空");
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId.trim())
                .singleResult();
        if (deployment == null) {
            throw new IllegalArgumentException("未查询到对应部署，部署ID：" + deploymentId);
        }
        List<String> processBeanNames = wcdkProcessClientRegistryService.listProcessBeanNameByClientId(validClientId);
        if (!processBeanNames.contains(validProcessBeanName)) {
            throw new IllegalArgumentException("当前客户端未注册该流程处理器");
        }
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .list();
        if (processDefinitions.isEmpty()) {
            throw new IllegalArgumentException("当前部署未关联流程定义");
        }
        processDefinitions.forEach(processDefinition -> wcdkProcessClientRegistryService.bindProcessDefinition(
                validClientId,
                processDefinition.getId(),
                validProcessBeanName,
                processDefinition.getName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeployment(String deploymentId, Boolean cascade) {
        if (!StringUtils.hasText(deploymentId)) {
            throw new IllegalArgumentException("部署ID不能为空");
        }
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId)
                .singleResult();
        if (deployment == null) {
            throw new IllegalArgumentException("未查询到对应部署，部署ID：" + deploymentId);
        }
        log.info("开始删除流程部署，部署ID：{}，级联删除：{}", deploymentId, cascade);
        List<String> processDefinitionIds = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .list()
                .stream()
                .map(ProcessDefinition::getId)
                .toList();
        wcdkProcessFormBindingMapper.delete(new LambdaQueryWrapper<WcdkProcessFormBinding>()
                .eq(WcdkProcessFormBinding::getDeploymentId, deploymentId)
                .or(!processDefinitionIds.isEmpty(), wrapper -> wrapper.in(WcdkProcessFormBinding::getProcessDefinitionId, processDefinitionIds)));
        repositoryService.deleteDeployment(deploymentId, Boolean.TRUE.equals(cascade));
    }

    private String validateDeploymentName(String deploymentName) {
        if (!StringUtils.hasText(deploymentName)) {
            throw new IllegalArgumentException("流程名称不能为空");
        }
        String trimmedDeploymentName = deploymentName.trim();
        if (trimmedDeploymentName.length() < DEPLOYMENT_NAME_MIN_LENGTH
                || trimmedDeploymentName.length() > DEPLOYMENT_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("流程名称长度必须为4到50个字符");
        }
        if (!DEPLOYMENT_NAME_PATTERN.matcher(trimmedDeploymentName).matches()) {
            throw new IllegalArgumentException("流程名称仅支持中文、字母、数字、点、减号、下划线和中英文括号");
        }
        return trimmedDeploymentName;
    }

    private boolean matchesDeploymentName(DeploymentResponse deployment, String deploymentName) {
        return !StringUtils.hasText(deploymentName) || containsIgnoreCase(deployment.getDeploymentName(), deploymentName);
    }

    private boolean matchesCategory(DeploymentResponse deployment, String category) {
        return !StringUtils.hasText(category) || containsIgnoreCase(deployment.getCategory(), category);
    }

    private boolean matchesClient(DeploymentResponse deployment, String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return true;
        }
        String keyword = clientId.trim().toLowerCase(Locale.ROOT);
        return deployment.getClientIds().stream()
                .anyMatch(value -> containsIgnoreCase(value, keyword))
                || deployment.getClientNames().stream()
                .anyMatch(value -> containsIgnoreCase(value, keyword));
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private String normalizeOptionalValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeRequiredValue(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void bindDeploymentProcessDefinitions(String deploymentId, String clientId, String processBeanName) {
        if (!StringUtils.hasText(deploymentId) || !StringUtils.hasText(clientId) || !StringUtils.hasText(processBeanName)) {
            return;
        }
        repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .list()
                .forEach(processDefinition -> wcdkProcessClientRegistryService.bindProcessDefinition(
                        clientId,
                        processDefinition.getId(),
                        processBeanName,
                        processDefinition.getName()));
    }

    private BpmnModel parseBpmnModel(byte[] fileBytes) throws XMLStreamException {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("流程文件内容不能为空");
        }
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream, StandardCharsets.UTF_8.name());
            return new BpmnXMLConverter().convertToBpmnModel(streamReader);
        } catch (IOException ex) {
            throw new IllegalArgumentException("读取 BPMN 文件失败", ex);
        }
    }

    private void validateBpmnModel(BpmnModel bpmnModel) {
        if (bpmnModel == null) {
            throw new IllegalArgumentException("流程文件解析后为空");
        }
        Process mainProcess = bpmnModel.getMainProcess();
        if (mainProcess == null || !StringUtils.hasText(mainProcess.getId())) {
            throw new IllegalArgumentException("流程文件缺少主流程定义");
        }
        boolean hasFlowNode = mainProcess.getFlowElements()
                .stream()
                .anyMatch(FlowNode.class::isInstance);
        if (!hasFlowNode) {
            throw new IllegalArgumentException("流程文件中未找到可绘制到画布的节点");
        }
        validateParallelJoinBeforeUserTask(mainProcess);
    }

    private void validateParallelJoinBeforeUserTask(Process mainProcess) {
        Map<String, FlowNode> nodeMap = collectFlowNodes(mainProcess).stream()
                .filter(node -> StringUtils.hasText(node.getId()))
                .collect(Collectors.toMap(FlowNode::getId, node -> node, (left, right) -> left, LinkedHashMap::new));
        List<SequenceFlow> sequenceFlows = collectSequenceFlows(mainProcess);
        Map<String, List<SequenceFlow>> incomingFlowMap = sequenceFlows.stream()
                .filter(sequenceFlow -> StringUtils.hasText(sequenceFlow.getTargetRef()))
                .collect(Collectors.groupingBy(SequenceFlow::getTargetRef, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<SequenceFlow>> outgoingFlowMap = sequenceFlows.stream()
                .filter(sequenceFlow -> StringUtils.hasText(sequenceFlow.getSourceRef()))
                .collect(Collectors.groupingBy(SequenceFlow::getSourceRef, LinkedHashMap::new, Collectors.toList()));
        for (FlowNode targetNode : nodeMap.values()) {
            List<SequenceFlow> incomingFlows = incomingFlowMap.getOrDefault(targetNode.getId(), List.of());
            if (!isUserTaskNode(targetNode) || incomingFlows.size() <= 1) {
                continue;
            }
            if (hasCommonParallelSplitAncestor(incomingFlows, nodeMap, incomingFlowMap, outgoingFlowMap)) {
                throw new IllegalArgumentException("并行任务进入【" + resolveFlowNodeName(targetNode)
                        + "】前必须先使用并行网关汇聚，避免任一并行分支通过后提前生成下一审批任务");
            }
        }
    }

    private boolean hasCommonParallelSplitAncestor(List<SequenceFlow> incomingFlows,
                                                   Map<String, FlowNode> nodeMap,
                                                   Map<String, List<SequenceFlow>> incomingFlowMap,
                                                   Map<String, List<SequenceFlow>> outgoingFlowMap) {
        Set<String> commonAncestorIds = null;
        for (SequenceFlow incomingFlow : incomingFlows) {
            Set<String> ancestorIds = new java.util.LinkedHashSet<>();
            collectParallelSplitAncestorIds(incomingFlow.getSourceRef(), nodeMap, incomingFlowMap, outgoingFlowMap,
                    ancestorIds, new java.util.LinkedHashSet<>());
            if (commonAncestorIds == null) {
                commonAncestorIds = ancestorIds;
            } else {
                commonAncestorIds.retainAll(ancestorIds);
            }
            if (commonAncestorIds == null || commonAncestorIds.isEmpty()) {
                return false;
            }
        }
        return commonAncestorIds != null && !commonAncestorIds.isEmpty();
    }

    private void collectParallelSplitAncestorIds(String nodeId,
                                                 Map<String, FlowNode> nodeMap,
                                                 Map<String, List<SequenceFlow>> incomingFlowMap,
                                                 Map<String, List<SequenceFlow>> outgoingFlowMap,
                                                 Set<String> ancestorIds,
                                                 Set<String> visitedNodeIds) {
        if (!StringUtils.hasText(nodeId) || !visitedNodeIds.add(nodeId)) {
            return;
        }
        FlowNode node = nodeMap.get(nodeId);
        if (isParallelGatewayNode(node) && outgoingFlowMap.getOrDefault(nodeId, List.of()).size() > 1) {
            ancestorIds.add(nodeId);
        }
        for (SequenceFlow incomingFlow : incomingFlowMap.getOrDefault(nodeId, List.of())) {
            collectParallelSplitAncestorIds(incomingFlow.getSourceRef(), nodeMap, incomingFlowMap, outgoingFlowMap,
                    ancestorIds, visitedNodeIds);
        }
    }

    private boolean isUserTaskNode(FlowNode node) {
        return node != null && USER_TASK.equals(node.getClass().getSimpleName());
    }

    private boolean isParallelGatewayNode(FlowNode node) {
        return node != null && "ParallelGateway".equals(node.getClass().getSimpleName());
    }

    private String resolveFlowNodeName(FlowNode node) {
        if (node == null) {
            return "";
        }
        return StringUtils.hasText(node.getName()) ? node.getName() : node.getId();
    }

    private void validateProcessDefinitionUnique(BpmnModel bpmnModel) {
        Process mainProcess = bpmnModel.getMainProcess();
        String processDefinitionKey = mainProcess.getId().trim();
        ProcessDefinition existingProcessDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
        if (existingProcessDefinition != null) {
            throw new IllegalArgumentException("流程定义标识已存在，请修改 BPMN 中的 process id 后重新部署，process id：" + processDefinitionKey);
        }
    }

    private void validateUpdateProcessDefinitionKey(BpmnModel bpmnModel, ProcessDefinition oldProcessDefinition) {
        Process mainProcess = bpmnModel.getMainProcess();
        String newProcessDefinitionKey = mainProcess.getId().trim();
        if (!Objects.equals(newProcessDefinitionKey, oldProcessDefinition.getKey())) {
            throw new IllegalArgumentException("流程定义标识必须与原流程一致，当前process id："
                    + newProcessDefinitionKey + "，原process id：" + oldProcessDefinition.getKey());
        }
    }

    private void copyProcessClientBindings(String oldProcessDefinitionId, ProcessDefinition newProcessDefinition) {
        if (!StringUtils.hasText(oldProcessDefinitionId) || newProcessDefinition == null) {
            return;
        }
        List<WcdkProcessClientProcess> oldBindings = wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, oldProcessDefinitionId));
        if (oldBindings.isEmpty()) {
            return;
        }
        wcdkProcessClientProcessMapper.delete(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, newProcessDefinition.getId()));
        LocalDateTime now = LocalDateTime.now();
        oldBindings.stream()
                .filter(binding -> StringUtils.hasText(binding.getClientId()))
                .filter(binding -> StringUtils.hasText(binding.getProcessBeanName()))
                .forEach(binding -> wcdkProcessClientProcessMapper.insert(WcdkProcessClientProcess.builder()
                        .clientId(binding.getClientId().trim())
                        .processBeanName(binding.getProcessBeanName().trim())
                        .processDefinitionId(newProcessDefinition.getId())
                        .processName(StringUtils.hasText(newProcessDefinition.getName()) ? newProcessDefinition.getName() : binding.getProcessName())
                        .excuteParam(binding.getExcuteParam())
                        .createTime(now)
                        .build()));
    }

    private String resolveDeploymentResourceName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "process-upload.bpmn20.xml";
        }
        String trimmedName = originalFilename.trim();
        String lowerCaseName = trimmedName.toLowerCase(Locale.ROOT);
        if (lowerCaseName.endsWith(".bpmn20.xml")) {
            return trimmedName;
        }
        if (lowerCaseName.endsWith(".bpmn")) {
            return trimmedName.substring(0, trimmedName.length() - 5) + ".bpmn20.xml";
        }
        if (lowerCaseName.endsWith(".xml")) {
            return trimmedName.substring(0, trimmedName.length() - 4) + ".bpmn20.xml";
        }
        return trimmedName + ".bpmn20.xml";
    }

    private DeploymentResponse buildDeploymentResponse(Deployment deployment) {
        return DeploymentResponse.builder()
                .deploymentId(deployment.getId())
                .deploymentName(deployment.getName())
                .fileName(resolveDeploymentFileName(deployment.getId()))
                .category(deployment.getCategory())
                .deployTime(deployment.getDeploymentTime())
                .invalidStatus(0)
                .clientIds(List.of())
                .clientNames(List.of())
                .processBeanNames(List.of())
                .build();
    }

    private DeploymentResponse buildDeploymentResponse(Deployment deployment,
                                                       List<ProcessDefinition> processDefinitions,
                                                       Map<String, List<WcdkProcessClientProcess>> bindingMap,
                                                       Map<String, String> clientNameMap,
                                                       Map<String, Integer> invalidStatusMap) {
        List<WcdkProcessClientProcess> bindings = (processDefinitions == null ? List.<ProcessDefinition>of() : processDefinitions)
                .stream()
                .flatMap(processDefinition -> bindingMap.getOrDefault(processDefinition.getId(), List.of()).stream())
                .toList();
        Integer invalidStatus = resolveDeploymentInvalidStatus(processDefinitions, invalidStatusMap);
        List<String> clientIds = bindings.stream()
                .map(WcdkProcessClientProcess::getClientId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        List<String> clientNames = clientIds.stream()
                .map(clientId -> StringUtils.hasText(clientNameMap.get(clientId)) ? clientNameMap.get(clientId) : clientId)
                .toList();
        List<String> processBeanNames = bindings.stream()
                .map(WcdkProcessClientProcess::getProcessBeanName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        return DeploymentResponse.builder()
                .deploymentId(deployment.getId())
                .deploymentName(deployment.getName())
                .fileName(resolveDeploymentFileName(deployment.getId()))
                .category(deployment.getCategory())
                .deployTime(deployment.getDeploymentTime())
                .invalidStatus(invalidStatus)
                .clientIds(clientIds)
                .clientNames(clientNames)
                .processBeanNames(processBeanNames)
                .build();
    }

    private String resolveDeploymentFileName(String deploymentId) {
        if (!StringUtils.hasText(deploymentId)) {
            return null;
        }
        List<String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
        if (resourceNames == null || resourceNames.isEmpty()) {
            return null;
        }
        for (String resourceName : resourceNames) {
            if (StringUtils.hasText(resourceName)
                    && resourceName.toLowerCase(Locale.ROOT).endsWith(".bpmn20.xml")) {
                return resourceName;
            }
        }
        return resourceNames.get(0);
    }

    private ProcessDefinitionResponse buildProcessDefinitionResponse(ProcessDefinition processDefinition,
                                                                     String category,
                                                                     List<WcdkProcessClientProcess> bindings,
                                                                     Map<String, String> clientNameMap,
                                                                     Integer invalidStatus) {
        List<WcdkProcessClientProcess> bindingRows = bindings == null ? List.of() : bindings;
        List<String> clientIds = bindingRows.stream()
                .map(WcdkProcessClientProcess::getClientId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        List<String> clientNames = clientIds.stream()
                .map(clientId -> StringUtils.hasText(clientNameMap.get(clientId)) ? clientNameMap.get(clientId) : clientId)
                .toList();
        List<String> processBeanNames = bindingRows.stream()
                .map(WcdkProcessClientProcess::getProcessBeanName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        return ProcessDefinitionResponse.builder()
                .processDefinitionId(processDefinition.getId())
                .processDefinitionKey(processDefinition.getKey())
                .processDefinitionName(processDefinition.getName())
                .category(category)
                .version(processDefinition.getVersion())
                .deploymentId(processDefinition.getDeploymentId())
                .resourceName(processDefinition.getResourceName())
                .suspended(processDefinition.isSuspended())
                .invalidStatus(invalidStatus == null ? 0 : invalidStatus)
                .clientIds(clientIds)
                .clientNames(clientNames)
                .processBeanNames(processBeanNames)
                .build();
    }

    private Map<String, List<WcdkProcessClientProcess>> listProcessBindingMap(List<ProcessDefinition> processDefinitions) {
        if (processDefinitions == null || processDefinitions.isEmpty()) {
            return Map.of();
        }
        List<String> processDefinitionIds = processDefinitions.stream()
                .map(ProcessDefinition::getId)
                .filter(StringUtils::hasText)
                .toList();
        if (processDefinitionIds.isEmpty()) {
            return Map.of();
        }
        return wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                        .in(WcdkProcessClientProcess::getProcessDefinitionId, processDefinitionIds))
                .stream()
                .collect(Collectors.groupingBy(WcdkProcessClientProcess::getProcessDefinitionId));
    }

    private Map<String, Integer> listInvalidStatusMap(List<ProcessDefinition> processDefinitions) {
        if (processDefinitions == null || processDefinitions.isEmpty()) {
            return Map.of();
        }
        List<String> processDefinitionIds = processDefinitions.stream()
                .map(ProcessDefinition::getId)
                .filter(StringUtils::hasText)
                .toList();
        if (processDefinitionIds.isEmpty()) {
            return Map.of();
        }
        return wcdkProcessDefinitionMetaMapper.selectList(new LambdaQueryWrapper<WcdkProcessDefinitionMeta>()
                        .in(WcdkProcessDefinitionMeta::getProcessDefinitionId, processDefinitionIds))
                .stream()
                .filter(meta -> StringUtils.hasText(meta.getProcessDefinitionId()))
                .collect(Collectors.toMap(
                        WcdkProcessDefinitionMeta::getProcessDefinitionId,
                        meta -> meta.getInvalidStatus() == null ? 0 : meta.getInvalidStatus(),
                        (left, right) -> right
                ));
    }

    private Integer resolveDeploymentInvalidStatus(List<ProcessDefinition> processDefinitions,
                                                   Map<String, Integer> invalidStatusMap) {
        List<ProcessDefinition> definitions = processDefinitions == null ? List.of() : processDefinitions;
        if (definitions.isEmpty()) {
            return 0;
        }
        boolean hasEffective = definitions.stream()
                .anyMatch(processDefinition -> invalidStatusMap == null
                        || invalidStatusMap.getOrDefault(processDefinition.getId(), 0) == 0);
        return hasEffective ? 0 : 1;
    }

    private void initDeploymentDefinitionMeta(String deploymentId) {
        if (!StringUtils.hasText(deploymentId)) {
            return;
        }
        repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .list()
                .forEach(processDefinition -> ensureProcessDefinitionMeta(processDefinition, 0));
    }

    private void ensureProcessDefinitionMeta(ProcessDefinition processDefinition, Integer invalidStatus) {
        if (processDefinition == null || !StringUtils.hasText(processDefinition.getId())) {
            return;
        }
        WcdkProcessDefinitionMeta meta = wcdkProcessDefinitionMetaMapper.selectOne(new LambdaQueryWrapper<WcdkProcessDefinitionMeta>()
                .eq(WcdkProcessDefinitionMeta::getProcessDefinitionId, processDefinition.getId()));
        LocalDateTime now = LocalDateTime.now();
        if (meta == null) {
            wcdkProcessDefinitionMetaMapper.insert(WcdkProcessDefinitionMeta.builder()
                    .processDefinitionId(processDefinition.getId())
                    .processDefinitionKey(processDefinition.getKey())
                    .processDefinitionVersion(processDefinition.getVersion())
                    .deploymentId(processDefinition.getDeploymentId())
                    .invalidStatus(invalidStatus == null ? 0 : invalidStatus)
                    .createTime(now)
                    .updateTime(now)
                    .build());
            return;
        }
        meta.setProcessDefinitionKey(processDefinition.getKey());
        meta.setProcessDefinitionVersion(processDefinition.getVersion());
        meta.setDeploymentId(processDefinition.getDeploymentId());
        meta.setInvalidStatus(invalidStatus == null ? 0 : invalidStatus);
        meta.setUpdateTime(now);
        wcdkProcessDefinitionMetaMapper.updateById(meta);
    }

    private void markProcessDefinitionInvalid(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return;
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        if (processDefinition != null) {
            ensureProcessDefinitionMeta(processDefinition, 1);
            return;
        }
        WcdkProcessDefinitionMeta meta = wcdkProcessDefinitionMetaMapper.selectOne(new LambdaQueryWrapper<WcdkProcessDefinitionMeta>()
                .eq(WcdkProcessDefinitionMeta::getProcessDefinitionId, processDefinitionId.trim()));
        if (meta != null) {
            meta.setInvalidStatus(1);
            meta.setUpdateTime(LocalDateTime.now());
            wcdkProcessDefinitionMetaMapper.updateById(meta);
        }
    }

    private Map<String, String> listClientNameMap(Map<String, List<WcdkProcessClientProcess>> bindingMap) {
        if (bindingMap == null || bindingMap.isEmpty()) {
            return Map.of();
        }
        List<String> clientIds = bindingMap.values()
                .stream()
                .flatMap(List::stream)
                .map(WcdkProcessClientProcess::getClientId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (clientIds.isEmpty()) {
            return Map.of();
        }
        return wcdkProcessClientMapper.selectList(new LambdaQueryWrapper<WcdkProcessClient>()
                        .in(WcdkProcessClient::getClientId, clientIds))
                .stream()
                .collect(Collectors.toMap(
                        WcdkProcessClient::getClientId,
                        client -> StringUtils.hasText(client.getClientName()) ? client.getClientName() : client.getClientId(),
                        (left, right) -> right
                ));
    }

    private ProcessDiagramNodeResponse buildNodeResponse(BpmnModel bpmnModel, FlowNode node, String parentId) {
        GraphicInfo graphicInfo = bpmnModel == null ? null : bpmnModel.getGraphicInfo(node.getId());
        return ProcessDiagramNodeResponse.builder()
                .elementId(node.getId())
                .elementName(node.getName())
                .elementType(node.getClass().getSimpleName())
                .parentId(StringUtils.hasText(parentId) ? parentId : null)
                .documentation(StringUtils.hasText(node.getDocumentation()) ? node.getDocumentation() : null)
                .defaultFlowId(node instanceof Gateway ? ((Gateway) node).getDefaultFlow() : null)
                .x(graphicInfo == null ? null : graphicInfo.getX())
                .y(graphicInfo == null ? null : graphicInfo.getY())
                .width(graphicInfo == null ? null : graphicInfo.getWidth())
                .height(graphicInfo == null ? null : graphicInfo.getHeight())
                .incomingCount(node.getIncomingFlows() == null ? 0 : node.getIncomingFlows().size())
                .outgoingCount(node.getOutgoingFlows() == null ? 0 : node.getOutgoingFlows().size())
                .build();
    }

    private ProcessDiagramEdgeResponse buildEdgeResponse(BpmnModel bpmnModel, SequenceFlow sequenceFlow) {
        List<ProcessDiagramWaypointResponse> waypoints = bpmnModel == null ? List.of() : bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId())
                .stream()
                .map(graphicInfo -> ProcessDiagramWaypointResponse.builder()
                        .x(graphicInfo.getX())
                        .y(graphicInfo.getY())
                        .build())
                .toList();
        return ProcessDiagramEdgeResponse.builder()
                .elementId(sequenceFlow.getId())
                .elementName(sequenceFlow.getName())
                .sourceRef(sequenceFlow.getSourceRef())
                .targetRef(sequenceFlow.getTargetRef())
                .conditionExpression(sequenceFlow.getConditionExpression())
                .waypoints(waypoints)
                .build();
    }

    private String readBpmnXml(ProcessDefinition processDefinition) {
        try (InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException ex) {
            log.error("读取流程定义 XML 失败，流程定义ID：{}", processDefinition.getId(), ex);
            throw new IllegalArgumentException("读取流程定义 XML 失败");
        }
    }

    private Double resolveNodeX(BpmnModel bpmnModel, String nodeId) {
        GraphicInfo graphicInfo = bpmnModel == null ? null : bpmnModel.getGraphicInfo(nodeId);
        return graphicInfo == null ? Double.MAX_VALUE : graphicInfo.getX();
    }

    private Double resolveNodeY(BpmnModel bpmnModel, String nodeId) {
        GraphicInfo graphicInfo = bpmnModel == null ? null : bpmnModel.getGraphicInfo(nodeId);
        return graphicInfo == null ? Double.MAX_VALUE : graphicInfo.getY();
    }

    private List<FlowNode> collectFlowNodes(FlowElementsContainer container) {
        List<FlowNode> nodes = new ArrayList<>();
        if (container == null || container.getFlowElements() == null) {
            return nodes;
        }
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof FlowNode) {
                nodes.add((FlowNode) flowElement);
            }
            if (flowElement instanceof FlowElementsContainer) {
                nodes.addAll(collectFlowNodes((FlowElementsContainer) flowElement));
            }
        }
        return nodes;
    }

    private Map<String, String> buildFlowNodeParentMap(FlowElementsContainer container, String parentId) {
        Map<String, String> result = new LinkedHashMap<>();
        if (container == null || container.getFlowElements() == null) {
            return result;
        }
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof FlowNode) {
                result.put(((FlowNode) flowElement).getId(), parentId);
            }
            if (flowElement instanceof FlowElementsContainer) {
                result.putAll(buildFlowNodeParentMap((FlowElementsContainer) flowElement, flowElement.getId()));
            }
        }
        return result;
    }

    private List<SequenceFlow> collectSequenceFlows(FlowElementsContainer container) {
        List<SequenceFlow> sequenceFlows = new ArrayList<>();
        if (container == null || container.getFlowElements() == null) {
            return sequenceFlows;
        }
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof SequenceFlow) {
                sequenceFlows.add((SequenceFlow) flowElement);
            }
            if (flowElement instanceof FlowElementsContainer) {
                sequenceFlows.addAll(collectSequenceFlows((FlowElementsContainer) flowElement));
            }
        }
        return sequenceFlows;
    }

    private DynamicPageSchema buildDynamicPageSchema(String bpmnXml,
                                                     List<ProcessDiagramNodeResponse> nodes,
                                                     String processDefinitionKey) {
        if (!StringUtils.hasText(bpmnXml)) {
            return buildDefaultDynamicPageSchema(nodes, processDefinitionKey);
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(bpmnXml)));
            document.getDocumentElement().normalize();
            DynamicPageSchema schemaFromDocumentation = resolveSchemaFromDocumentation(document, nodes);
            if (schemaFromDocumentation != null) {
                return schemaFromDocumentation;
            }
            List<ProcessFormFieldResponse> formFields = resolveFieldsFromFlowableForm(document, nodes);
            if (!formFields.isEmpty()) {
                return new DynamicPageSchema(formFields, buildDefaultActionButtons(nodes));
            }
        } catch (Exception ex) {
            log.warn("Failed to parse BPMN page schema, fallback to default render. processDefinitionKey={}", processDefinitionKey, ex);
        }
        return buildDefaultDynamicPageSchema(nodes, processDefinitionKey);
    }

    private DynamicPageSchema resolveSchemaFromDocumentation(Document document, List<ProcessDiagramNodeResponse> nodes) {
        List<Element> candidates = new ArrayList<>();
        Element processElement = findFirstElement(document, "process");
        if (processElement != null) {
            candidates.add(processElement);
        }
        candidates.addAll(findElements(document, "startEvent"));
        candidates.addAll(findElements(document, "userTask"));
        for (Element candidate : candidates) {
            String documentation = readDirectChildText(candidate, "documentation");
            if (!StringUtils.hasText(documentation)) {
                continue;
            }
            JsonObject schemaJson = parseDocumentationSchema(documentation);
            if (schemaJson == null) {
                continue;
            }
            List<ProcessFormFieldResponse> formFields = parseFormFields(schemaJson.getJSONArray("formFields"), candidate, nodes);
            List<ProcessActionButtonResponse> actionButtons = parseActionButtons(schemaJson.getJSONArray("buttons"), nodes);
            if (formFields.isEmpty()) {
                continue;
            }
            if (actionButtons.isEmpty()) {
                actionButtons = buildDefaultActionButtons(nodes);
            }
            return new DynamicPageSchema(formFields, actionButtons);
        }
        return null;
    }

    private JsonObject parseDocumentationSchema(String documentation) {
        String trimmed = documentation == null ? null : documentation.trim();
        if (!StringUtils.hasText(trimmed) || (!trimmed.startsWith("{") && !trimmed.startsWith("["))) {
            return null;
        }
        try {
            if (trimmed.startsWith("[")) {
                return new JsonObject(Map.of("formFields", JSON.parseArray(trimmed)));
            }
            return JSON.parseObject(trimmed);
        } catch (Exception ex) {
            log.debug("Documentation is not dynamic page schema JSON, ignored. content={}", documentation, ex);
            return null;
        }
    }

    private List<ProcessFormFieldResponse> resolveFieldsFromFlowableForm(Document document,
                                                                         List<ProcessDiagramNodeResponse> nodes) {
        List<Element> targetNodes = new ArrayList<>();
        targetNodes.addAll(findElements(document, "startEvent"));
        targetNodes.addAll(findElements(document, "userTask"));
        Map<String, ProcessFormFieldResponse> fieldMap = new LinkedHashMap<>();
        int sortOrder = 0;
        for (Element targetNode : targetNodes) {
            List<Element> fieldElements = new ArrayList<>();
            fieldElements.addAll(findDescendants(targetNode, "formProperty"));
            fieldElements.addAll(findDescendants(targetNode, "formField"));
            for (Element fieldElement : fieldElements) {
                ProcessFormFieldResponse field = buildFieldFromElement(fieldElement, targetNode, nodes, sortOrder);
                if (field != null && !fieldMap.containsKey(field.getFieldKey())) {
                    fieldMap.put(field.getFieldKey(), field);
                    sortOrder += 1;
                }
            }
        }
        return new ArrayList<>(fieldMap.values());
    }

    private ProcessFormFieldResponse buildFieldFromElement(Element fieldElement,
                                                           Element ownerElement,
                                                           List<ProcessDiagramNodeResponse> nodes,
                                                           int sortOrder) {
        String fieldKey = firstNonBlank(resolveAttribute(fieldElement, "id"), resolveAttribute(fieldElement, "variable"));
        if (!StringUtils.hasText(fieldKey)) {
            return null;
        }
        String label = firstNonBlank(resolveAttribute(fieldElement, "name"), fieldKey);
        String rawType = firstNonBlank(resolveAttribute(fieldElement, "type"), "string");
        String normalizedDataType = normalizeDataType(rawType);
        String componentType = normalizeComponentType(rawType, hasChild(fieldElement, "value"));
        String placeholder = resolveAttribute(fieldElement, "placeholder");
        String defaultValue = firstNonBlank(resolveAttribute(fieldElement, "default"), resolveAttribute(fieldElement, "defaultValue"));
        String requiredValue = firstNonBlank(resolveAttribute(fieldElement, "required"), resolveAttribute(fieldElement, "isRequired"));
        Integer rows = "textarea".equals(componentType) ? 4 : null;
        List<ProcessFormOptionResponse> options = parseFieldOptions(fieldElement);
        String ownerNodeId = resolveAttribute(ownerElement, "id");
        ProcessDiagramNodeResponse sourceNode = findNode(nodes, ownerNodeId);
        return ProcessFormFieldResponse.builder()
                .fieldKey(fieldKey)
                .label(label)
                .componentType(componentType)
                .dataType(normalizedDataType)
                .placeholder(StringUtils.hasText(placeholder) ? placeholder : buildDefaultPlaceholder(label, componentType))
                .required("true".equalsIgnoreCase(requiredValue))
                .readOnly(Boolean.FALSE)
                .defaultValue(defaultValue)
                .rows(rows)
                .sortOrder(sortOrder)
                .sourceNodeId(ownerNodeId)
                .sourceNodeName(sourceNode == null ? null : sourceNode.getElementName())
                .options(options)
                .build();
    }

    private List<ProcessFormFieldResponse> parseFormFields(JsonArray formFields,
                                                           Element ownerElement,
                                                           List<ProcessDiagramNodeResponse> nodes) {
        if (formFields == null || formFields.isEmpty()) {
            return List.of();
        }
        List<ProcessFormFieldResponse> results = new ArrayList<>();
        String ownerNodeId = ownerElement == null ? null : resolveAttribute(ownerElement, "id");
        ProcessDiagramNodeResponse sourceNode = findNode(nodes, ownerNodeId);
        for (int index = 0; index < formFields.size(); index += 1) {
            JsonObject field = formFields.getJsonObject(index);
            if (field == null) {
                continue;
            }
            String componentType = normalizeComponentType(firstNonBlank(field.getString("componentType"), field.getString("type")), field.containsKey("options"));
            if ("group".equals(componentType)) {
                results.addAll(parseFormFields(field.getJSONArray("children"), ownerElement, nodes));
                continue;
            }
            if ("button".equals(componentType)) {
                continue;
            }
            String fieldKey = firstNonBlank(field.getString("fieldKey"), field.getString("bindField"), field.getString("prop"), field.getString("id"));
            if (!StringUtils.hasText(fieldKey)) {
                continue;
            }
            String label = firstNonBlank(field.getString("label"), fieldKey);
            results.add(ProcessFormFieldResponse.builder()
                    .fieldKey(fieldKey)
                    .label(label)
                    .componentType(componentType)
                    .dataType(normalizeDataType(field.getString("dataType")))
                    .placeholder(firstNonBlank(field.getString("placeholder"), buildDefaultPlaceholder(label, componentType)))
                    .required(!Boolean.FALSE.equals(field.getBoolean("required")))
                    .readOnly(Boolean.TRUE.equals(field.getBoolean("readOnly")))
                    .defaultValue(field.getString("defaultValue"))
                    .rows(field.getIntValue("rows") > 0 ? field.getIntValue("rows") : ("textarea".equals(componentType) ? 4 : null))
                    .tableRows(field.getIntValue("tableRows") > 0 ? field.getIntValue("tableRows") : null)
                    .tableColumns(field.getIntValue("tableColumns") > 0 ? field.getIntValue("tableColumns") : null)
                    .sortOrder(field.getIntValue("sortOrder") > 0 ? field.getIntValue("sortOrder") : index)
                    .sourceNodeId(ownerNodeId)
                    .sourceNodeName(sourceNode == null ? null : sourceNode.getElementName())
                    .options(parseFieldOptions(field.getJSONArray("options")))
                    .children(parseTableChildren(field.getJSONArray("children"), ownerNodeId, sourceNode))
                    .build());
        }
        return results;
    }

    private List<ProcessFormTableCellResponse> parseTableChildren(JsonArray children,
                                                                  String ownerNodeId,
                                                                  ProcessDiagramNodeResponse sourceNode) {
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        List<ProcessFormTableCellResponse> results = new ArrayList<>();
        for (int index = 0; index < children.size(); index += 1) {
            JsonObject cell = children.getJsonObject(index);
            if (cell == null) {
                continue;
            }
            results.add(ProcessFormTableCellResponse.builder()
                    .row(cell.getIntValue("row"))
                    .column(cell.getIntValue("column"))
                    .fields(parseTableCellFields(cell.getJSONArray("fields"), ownerNodeId, sourceNode))
                    .build());
        }
        return results;
    }

    private List<ProcessFormFieldResponse> parseTableCellFields(JsonArray fields,
                                                                String ownerNodeId,
                                                                ProcessDiagramNodeResponse sourceNode) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }
        List<ProcessFormFieldResponse> results = new ArrayList<>();
        for (int index = 0; index < fields.size(); index += 1) {
            JsonObject field = fields.getJsonObject(index);
            if (field == null) {
                continue;
            }
            String componentType = normalizeComponentType(firstNonBlank(field.getString("componentType"), field.getString("type")), field.containsKey("options"));
            if ("group".equals(componentType)) {
                results.addAll(parseTableCellFields(field.getJSONArray("children"), ownerNodeId, sourceNode));
                continue;
            }
            if ("button".equals(componentType) || "table".equals(componentType)) {
                continue;
            }
            String fieldKey = firstNonBlank(field.getString("fieldKey"), field.getString("bindField"), field.getString("prop"), field.getString("id"));
            if (!StringUtils.hasText(fieldKey)) {
                continue;
            }
            String label = firstNonBlank(field.getString("label"), fieldKey);
            results.add(ProcessFormFieldResponse.builder()
                    .fieldKey(fieldKey)
                    .label(label)
                    .componentType(componentType)
                    .dataType(normalizeDataType(field.getString("dataType")))
                    .placeholder(firstNonBlank(field.getString("placeholder"), buildDefaultPlaceholder(label, componentType)))
                    .required(!Boolean.FALSE.equals(field.getBoolean("required")))
                    .readOnly(Boolean.TRUE.equals(field.getBoolean("readOnly")))
                    .defaultValue(field.getString("defaultValue"))
                    .rows(field.getIntValue("rows") > 0 ? field.getIntValue("rows") : ("textarea".equals(componentType) ? 4 : null))
                    .sortOrder(field.getIntValue("sortOrder") > 0 ? field.getIntValue("sortOrder") : index)
                    .sourceNodeId(ownerNodeId)
                    .sourceNodeName(sourceNode == null ? null : sourceNode.getElementName())
                    .options(parseFieldOptions(field.getJSONArray("options")))
                    .build());
        }
        return results;
    }

    private List<ProcessActionButtonResponse> parseActionButtons(JsonArray buttons, List<ProcessDiagramNodeResponse> nodes) {
        if (buttons == null || buttons.isEmpty()) {
            return List.of();
        }
        List<ProcessActionButtonResponse> results = new ArrayList<>();
        for (int index = 0; index < buttons.size(); index += 1) {
            JsonObject button = buttons.getJsonObject(index);
            if (button == null) {
                continue;
            }
            String actionKey = firstNonBlank(button.getString("actionKey"), button.getString("key"));
            if (!StringUtils.hasText(actionKey)) {
                continue;
            }
            results.add(ProcessActionButtonResponse.builder()
                    .actionKey(actionKey)
                    .label(firstNonBlank(button.getString("label"), resolveDefaultButtonLabel(actionKey)))
                    .buttonType(firstNonBlank(button.getString("buttonType"), resolveDefaultButtonType(actionKey)))
                    .submit(button.containsKey("submit") ? button.getBoolean("submit") : resolveDefaultSubmitFlag(actionKey))
                    .sortOrder(button.getIntValue("sortOrder") > 0 ? button.getIntValue("sortOrder") : index)
                    .build());
        }
        return results.isEmpty() ? buildDefaultActionButtons(nodes) : results;
    }

    private List<ProcessActionButtonResponse> buildDefaultActionButtons(List<ProcessDiagramNodeResponse> nodes) {
        boolean hasStartEvent = nodes.stream().anyMatch(node -> START_EVENT.equals(node.getElementType()));
        boolean hasUserTask = nodes.stream().anyMatch(node -> USER_TASK.equals(node.getElementType()));
        List<ProcessActionButtonResponse> results = new ArrayList<>();
        results.add(ProcessActionButtonResponse.builder()
                .actionKey("createDraft")
                .label(hasUserTask ? "保存草稿" : "创建流程申请")
                .buttonType("primary")
                .submit(Boolean.FALSE)
                .sortOrder(0)
                .build());
        if (hasStartEvent) {
            results.add(ProcessActionButtonResponse.builder()
                    .actionKey("createAndSubmit")
                    .label(hasUserTask ? "创建并发起" : "立即发起")
                    .buttonType("success")
                    .submit(Boolean.TRUE)
                    .sortOrder(1)
                    .build());
        }
        results.add(ProcessActionButtonResponse.builder()
                .actionKey("reset")
                .label("重置")
                .buttonType("default")
                .submit(Boolean.FALSE)
                .sortOrder(2)
                .build());
        return results;
    }

    private DynamicPageSchema buildDefaultDynamicPageSchema(List<ProcessDiagramNodeResponse> nodes, String processDefinitionKey) {
        return new DynamicPageSchema(List.of(), buildDefaultActionButtons(nodes));
    }

    private String resolveDefaultButtonLabel(String actionKey) {
        if ("createDraft".equals(actionKey)) {
            return "保存草稿";
        }
        if ("createAndSubmit".equals(actionKey)) {
            return "创建并发起";
        }
        if ("reset".equals(actionKey)) {
            return "重置";
        }
        return actionKey;
    }

    private String resolveDefaultButtonType(String actionKey) {
        if ("createAndSubmit".equals(actionKey)) {
            return "success";
        }
        if ("reset".equals(actionKey)) {
            return "default";
        }
        return "primary";
    }

    private Boolean resolveDefaultSubmitFlag(String actionKey) {
        return "createAndSubmit".equals(actionKey);
    }

    private String normalizeDataType(String rawType) {
        if (!StringUtils.hasText(rawType)) {
            return "string";
        }
        String normalized = rawType.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("long") || normalized.contains("int") || normalized.contains("number")) {
            return "integer";
        }
        if (normalized.contains("double") || normalized.contains("float") || normalized.contains("decimal")) {
            return "number";
        }
        if (normalized.contains("bool")) {
            return "boolean";
        }
        return "string";
    }

    private String normalizeComponentType(String rawType, boolean hasOptions) {
        if (!StringUtils.hasText(rawType)) {
            return hasOptions ? "select" : "input";
        }
        String normalized = rawType.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("textarea")) {
            return "textarea";
        }
        if (normalized.contains("table")) {
            return "table";
        }
        if (normalized.contains("group")) {
            return "group";
        }
        if (normalized.contains("button")) {
            return "button";
        }
        if (normalized.contains("radio")) {
            return "radio";
        }
        if (normalized.contains("switch")) {
            return "switch";
        }
        if (normalized.contains("checkbox") || "boolean".equals(normalized)) {
            return "checkbox";
        }
        if (normalized.contains("select") || normalized.contains("enum") || hasOptions) {
            return "select";
        }
        if (normalized.contains("date")) {
            return "date";
        }
        if (normalized.contains("int") || normalized.contains("long") || normalized.contains("number") || normalized.contains("decimal")) {
            return "number";
        }
        return "input";
    }

    private List<ProcessFormOptionResponse> parseFieldOptions(Element fieldElement) {
        List<Element> valueElements = findDescendants(fieldElement, "value");
        if (valueElements.isEmpty()) {
            valueElements = findDescendants(fieldElement, "item");
        }
        List<ProcessFormOptionResponse> results = new ArrayList<>();
        for (Element valueElement : valueElements) {
            String optionValue = firstNonBlank(resolveAttribute(valueElement, "id"), resolveAttribute(valueElement, "value"));
            String optionLabel = firstNonBlank(resolveAttribute(valueElement, "name"), readText(valueElement), optionValue);
            if (StringUtils.hasText(optionValue)) {
                results.add(ProcessFormOptionResponse.builder()
                        .label(optionLabel)
                        .value(optionValue)
                        .build());
            }
        }
        return results;
    }

    private List<ProcessFormOptionResponse> parseFieldOptions(JsonArray options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        List<ProcessFormOptionResponse> results = new ArrayList<>();
        for (int index = 0; index < options.size(); index += 1) {
            JsonObject option = options.getJSONObject(index);
            if (option == null) {
                continue;
            }
            String value = firstNonBlank(option.getString("value"), option.getString("id"));
            if (!StringUtils.hasText(value)) {
                continue;
            }
            results.add(ProcessFormOptionResponse.builder()
                    .label(firstNonBlank(option.getString("label"), option.getString("name"), value))
                    .value(value)
                    .build());
        }
        return results;
    }

    private ProcessDiagramNodeResponse findNode(List<ProcessDiagramNodeResponse> nodes, String nodeId) {
        if (!StringUtils.hasText(nodeId)) {
            return null;
        }
        for (ProcessDiagramNodeResponse node : nodes) {
            if (Objects.equals(nodeId, node.getElementId())) {
                return node;
            }
        }
        return null;
    }

    private List<Element> findElements(Document document, String tagName) {
        return findDescendants(document.getDocumentElement(), tagName);
    }

    private Element findFirstElement(Document document, String tagName) {
        List<Element> elements = findElements(document, tagName);
        return elements.isEmpty() ? null : elements.get(0);
    }

    private List<Element> findDescendants(Element root, String tagName) {
        List<Element> results = new ArrayList<>();
        if (root == null) {
            return results;
        }
        NodeList children = root.getElementsByTagName("*");
        for (int index = 0; index < children.getLength(); index += 1) {
            Node node = children.item(index);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (tagName.equalsIgnoreCase(localName(element))) {
                    results.add(element);
                }
            }
        }
        return results;
    }

    private boolean hasChild(Element root, String tagName) {
        return !findDescendants(root, tagName).isEmpty();
    }

    private String readDirectChildText(Element root, String tagName) {
        if (root == null) {
            return null;
        }
        NodeList children = root.getChildNodes();
        for (int index = 0; index < children.getLength(); index += 1) {
            Node node = children.item(index);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (tagName.equalsIgnoreCase(localName(element))) {
                    return readText(element);
                }
            }
        }
        return null;
    }

    private String readText(Element element) {
        return element == null ? null : element.getTextContent();
    }

    private String resolveAttribute(Element element, String attributeName) {
        if (element == null || !StringUtils.hasText(attributeName)) {
            return null;
        }
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        NamedNodeMap attributes = element.getAttributes();
        for (int index = 0; index < attributes.getLength(); index += 1) {
            Node attribute = attributes.item(index);
            String currentName = localName(attribute);
            if (attributeName.equalsIgnoreCase(currentName)) {
                return attribute.getNodeValue();
            }
        }
        return null;
    }

    private String localName(Node node) {
        if (node == null) {
            return null;
        }
        String localName = node.getLocalName();
        if (StringUtils.hasText(localName)) {
            return localName;
        }
        String nodeName = node.getNodeName();
        if (!StringUtils.hasText(nodeName)) {
            return null;
        }
        int index = nodeName.indexOf(':');
        return index >= 0 ? nodeName.substring(index + 1) : nodeName;
    }

    private String buildDefaultPlaceholder(String label, String componentType) {
        if ("select".equals(componentType) || "radio".equals(componentType) || "date".equals(componentType)) {
            return "请选择" + label;
        }
        return "请输入" + label;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private record DynamicPageSchema(List<ProcessFormFieldResponse> formFields,
                                     List<ProcessActionButtonResponse> actionButtons) {
    }
}


