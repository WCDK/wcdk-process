package com.wcdk.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.common.json.JSON;
import com.wcdk.process.constant.ProcessStatusConstant;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDiagramNodeResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import com.wcdk.process.dto.ProcessTaskInfoResponse;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.entity.ProcessRequest;
import com.wcdk.process.entity.WcdkProcessClientProcess;
import com.wcdk.process.entity.WcdkProcessForm;
import com.wcdk.process.entity.WcdkProcessFormBinding;
import com.wcdk.process.mapper.ProcessRequestMapper;
import com.wcdk.process.mapper.WcdkProcessClientProcessMapper;
import com.wcdk.process.mapper.WcdkProcessFormBindingMapper;
import com.wcdk.process.mapper.WcdkProcessFormMapper;
import com.wcdk.process.service.FlowableDeployService;
import com.wcdk.process.service.ProcessRequestService;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowElementsContainer;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Slf4j
@Service
public class ProcessRequestServiceImpl extends ServiceImpl<ProcessRequestMapper, ProcessRequest> implements ProcessRequestService {

    private static final String APPROVED_VARIABLE_NAME = "approved";

    private static final String APPROVAL_RESULT_VARIABLE_NAME = "nodeApprovalResult";

    private static final String APPROVAL_RESULT_TEXT_VARIABLE_NAME = "nodeApprovalResultText";

    private static final String APPROVAL_COMMENT_VARIABLE_NAME = "nodeApprovalComment";

    private static final String APPROVAL_FORM_DATA_VARIABLE_NAME = "approvalFormData";

    private static final String PROCESS_BEAN_NAME = "processBeanName";

    private static final String USER_TASK_ACTIVITY_TYPE = "userTask";

    private static final String REJECT_COMMENT_TYPE = "驳回";

    private static final String APPROVAL_ACTION_REJECT = "REJECT";

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    private final FlowableDeployService flowableDeployService;

    private final WcdkProcessClientCallbackService wcdkProcessClientCallbackService;

    private final WcdkProcessClientProcessMapper wcdkProcessClientProcessMapper;

    private final WcdkProcessFormBindingMapper formBindingMapper;

    private final WcdkProcessFormMapper formMapper;

    public ProcessRequestServiceImpl(RuntimeService runtimeService,
                                     TaskService taskService,
                                     HistoryService historyService,
                                     RepositoryService repositoryService,
                                     FlowableDeployService flowableDeployService,
                                     WcdkProcessClientCallbackService wcdkProcessClientCallbackService,
                                     WcdkProcessClientProcessMapper wcdkProcessClientProcessMapper,
                                     WcdkProcessFormBindingMapper formBindingMapper,
                                     WcdkProcessFormMapper formMapper) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.flowableDeployService = flowableDeployService;
        this.wcdkProcessClientCallbackService = wcdkProcessClientCallbackService;
        this.wcdkProcessClientProcessMapper = wcdkProcessClientProcessMapper;
        this.formBindingMapper = formBindingMapper;
        this.formMapper = formMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessRequestResponse createProcessRequest(ProcessRequestCreateRequest request) {
        validateCreateRequest(request);
        Map<String, Object> formData = normalizeFormData(request.getFormData());
        if (StringUtils.hasText(request.getProcessBeanName())) {
            formData.put(PROCESS_BEAN_NAME, request.getProcessBeanName().trim());
        }
        LocalDateTime now = LocalDateTime.now();
        ProcessRequest processRequest = ProcessRequest.builder()
                .processNo(generateProcessNo(now))
                .starter(resolveStarter(formData))
                .taskName(request.getTaskName().trim())
                .businessTitle(resolveBusinessTitle(request.getProcessDefinitionKey(), formData))
                .formDataJson(JSON.toJSONString(formData))
                .processDefinitionKey(request.getProcessDefinitionKey().trim())
                .status(Boolean.TRUE.equals(request.getSubmit()) ? ProcessStatusConstant.PROCESSING : ProcessStatusConstant.DRAFT)
                .createTime(now)
                .updateTime(now)
                .build();
        save(processRequest);
        log.info("流程申请创建完成，记录ID={}, 流程单号={}", processRequest.getId(), processRequest.getProcessNo());
        if (Boolean.TRUE.equals(request.getSubmit())) {
            startProcessAndSync(processRequest);
            notifyProcessCallback(processRequest, "PROCESS_SUBMITTED", "流程申请已提交");
            return getProcessRequest(processRequest.getId());
        }
        notifyProcessCallback(processRequest, "PROCESS_CREATED", "流程申请已创建");
        return buildProcessRequestResponse(processRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessRequestResponse submitProcessRequest(Long id) {
        ProcessRequest processRequest = getRequiredProcessRequest(id);
        if (!Objects.equals(ProcessStatusConstant.DRAFT, processRequest.getStatus())) {
            throw new IllegalArgumentException("当前流程申请不是草稿状态，不能重复提交");
        }
        processRequest.setStatus(ProcessStatusConstant.PROCESSING);
        processRequest.setUpdateTime(LocalDateTime.now());
        updateById(processRequest);
        startProcessAndSync(processRequest);
        notifyProcessCallback(processRequest, "PROCESS_SUBMITTED", "流程申请已提交");
        return getProcessRequest(id);
    }

    @Override
    public ProcessRequestResponse getProcessRequest(Long id) {
        return buildProcessRequestResponse(getRequiredProcessRequest(id));
    }

    @Override
    public ProcessDefinitionDetailResponse getProcessRequestDiagramDetail(Long id) {
        ProcessRequest processRequest = getRequiredProcessRequest(id);
        String processDefinitionId = resolveProcessDefinitionId(processRequest);
        if (!StringUtils.hasText(processDefinitionId)) {
            throw new IllegalArgumentException("未查询到对应流程定义，无法展示流程图");
        }
        ProcessDefinitionDetailResponse detailResponse = flowableDeployService.getProcessDefinitionDetail(processDefinitionId);
        return ProcessDefinitionDetailResponse.builder()
                .processDefinitionId(detailResponse.getProcessDefinitionId())
                .processDefinitionKey(detailResponse.getProcessDefinitionKey())
                .processDefinitionName(detailResponse.getProcessDefinitionName())
                .category(detailResponse.getCategory())
                .version(detailResponse.getVersion())
                .deploymentId(detailResponse.getDeploymentId())
                .deploymentName(detailResponse.getDeploymentName())
                .resourceName(detailResponse.getResourceName())
                .suspended(detailResponse.getSuspended())
                .nodeCount(detailResponse.getNodeCount())
                .userTaskCount(detailResponse.getUserTaskCount())
                .sequenceFlowCount(detailResponse.getSequenceFlowCount())
                .bpmnXml(detailResponse.getBpmnXml())
                .formFields(detailResponse.getFormFields())
                .actionButtons(detailResponse.getActionButtons())
                .nodes(resolveApprovalResultNodes(detailResponse.getNodes(), processRequest))
                .sequenceFlows(detailResponse.getSequenceFlows())
                .activeNodeIds(resolveActiveNodeIds(processRequest))
                .build();
    }

    @Override
    public PageResponse<ProcessRequestResponse> listProcessRequest(long pageNum,
                                                                   long pageSize,
                                                                   String processNo,
                                                                   String starter,
                                                                   String businessTitle,
                                                                   String category,
                                                                   String processDefinitionKey,
                                                                   String status) {
        long safePageNum = Math.max(pageNum, 1L);
        long safePageSize = Math.max(pageSize, 1L);
        Set<String> categoryDefinitionKeys = resolveDefinitionKeysByCategory(category);
        if (StringUtils.hasText(category) && categoryDefinitionKeys.isEmpty()) {
            return new PageResponse<>(0L, safePageNum, safePageSize, List.of());
        }
        Page<ProcessRequest> page = lambdaQuery()
                .like(StringUtils.hasText(processNo), ProcessRequest::getProcessNo, processNo == null ? null : processNo.trim())
                .like(StringUtils.hasText(starter), ProcessRequest::getStarter, starter == null ? null : starter.trim())
                .like(StringUtils.hasText(businessTitle), ProcessRequest::getBusinessTitle, businessTitle == null ? null : businessTitle.trim())
                .in(StringUtils.hasText(category), ProcessRequest::getProcessDefinitionKey, categoryDefinitionKeys)
                .eq(StringUtils.hasText(processDefinitionKey), ProcessRequest::getProcessDefinitionKey, processDefinitionKey == null ? null : processDefinitionKey.trim())
                .eq(StringUtils.hasText(status), ProcessRequest::getStatus, status == null ? null : status.trim())
                .orderByDesc(ProcessRequest::getCreateTime)
                .page(new Page<>(safePageNum, safePageSize));
        return new PageResponse<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords().stream().map(this::buildProcessRequestListResponse).toList()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveProcessRequest(ProcessRequestApproveRequest request) {
        if (!StringUtils.hasText(request.getTaskId())) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        if (request.getApproved() == null) {
            throw new IllegalArgumentException("审批结果不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(request.getTaskId()).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("未找到对应任务，任务ID：" + request.getTaskId());
        }
        ProcessRequest processRequest = getProcessRequestByProcessInstanceId(task.getProcessInstanceId());
        List<ProcessTaskInfoResponse> currentTasks = buildTaskInfoResponses(listActiveTasks(task.getProcessInstanceId()), processRequest.getId());
        validateTaskNotPrematureParallelJoin(task, currentTasks);
        Map<String, Object> approvalFormData = normalizeFormData(request.getFormData());
        mergeApprovalFormData(processRequest, approvalFormData);
        Map<String, Object> variables = new HashMap<>();
        variables.put(APPROVED_VARIABLE_NAME, request.getApproved());
        variables.put("comment", request.getComment());
        variables.putAll(approvalFormData);
        if (!approvalFormData.isEmpty()) {
            variables.put(APPROVAL_FORM_DATA_VARIABLE_NAME, approvalFormData);
        }
        recordTaskApprovalVariables(task, request);
        log.info("处理流程审批任务，taskId={}, approved={}", request.getTaskId(), request.getApproved());
        if (isRejectAction(request)) {
            rejectToPreviousTask(task, variables, request.getComment());
        } else {
            taskService.complete(request.getTaskId(), variables);
        }
        syncProcessRequestState(processRequest);
        List<ProcessTaskInfoResponse> nextTasks = resolveCurrentTaskInfoResponses(processRequest);
        boolean waitingParallelTasks = Boolean.TRUE.equals(request.getApproved())
                && hasUnfinishedParallelTask(currentTasks, nextTasks, request.getTaskId());
        notifyProcessCallback(processRequest,
                resolveApprovalEventType(request, waitingParallelTasks),
                resolveApprovalMessage(request, waitingParallelTasks),
                task,
                request.getApproved(),
                request.getApprovalAction(),
                currentTasks,
                approvalFormData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessRequest(Long id, String deleteReason) {
        ProcessRequest processRequest = getRequiredProcessRequest(id);
        if (Objects.equals(ProcessStatusConstant.PROCESSING, processRequest.getStatus())) {
            throw new IllegalArgumentException("当前流程正在审批中，禁止删除");
        }
        if (StringUtils.hasText(processRequest.getCurrentTaskId())) {
            Task currentTask = taskService.createTaskQuery()
                    .taskId(processRequest.getCurrentTaskId())
                    .singleResult();
            if (currentTask != null) {
                taskService.deleteTask(currentTask.getId(), resolveDeleteReason(deleteReason));
            }
        }
        if (StringUtils.hasText(processRequest.getProcessInstanceId())) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processRequest.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null) {
                runtimeService.deleteProcessInstance(processRequest.getProcessInstanceId(), resolveDeleteReason(deleteReason));
            }
        }
//        notifyProcessCallback(processRequest, "PROCESS_DELETED", "流程申请已删除");
        removeById(id);
        log.info("删除流程申请完成，记录ID={}, 流程实例ID={}", id, processRequest.getProcessInstanceId());
    }

    private void validateCreateRequest(ProcessRequestCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("流程创建请求不能为空");
        }
        if (!StringUtils.hasText(request.getProcessDefinitionKey())) {
            throw new IllegalArgumentException("流程定义标识不能为空");
        }
        if (!StringUtils.hasText(request.getTaskName())) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
    }

    private void startProcessAndSync(ProcessRequest processRequest) {
        Map<String, Object> variables = new HashMap<>(parseFormData(processRequest.getFormDataJson()));
        if (StringUtils.hasText(processRequest.getStarter())) {
            variables.putIfAbsent("starter", processRequest.getStarter());
        }
        if (StringUtils.hasText(processRequest.getTaskName())) {
            variables.putIfAbsent("taskName", processRequest.getTaskName());
        }
        if (StringUtils.hasText(processRequest.getBusinessTitle())) {
            variables.putIfAbsent("businessTitle", processRequest.getBusinessTitle());
        }
        if (StringUtils.hasText(processRequest.getProcessNo())) {
            variables.putIfAbsent("processNo", processRequest.getProcessNo());
        }
        String processBeanName = resolveProcessBeanName(processRequest);
        if (StringUtils.hasText(processBeanName)) {
            variables.putIfAbsent(PROCESS_BEAN_NAME, processBeanName);
        }
        log.info("鍚姩娴佺▼瀹炰緥锛宲rocessNo={}, processDefinitionKey={}", processRequest.getProcessNo(), processRequest.getProcessDefinitionKey());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processRequest.getProcessDefinitionKey(),
                processRequest.getProcessNo(),
                variables
        );
        processRequest.setProcessInstanceId(processInstance.getId());
        syncProcessRequestState(processRequest);
    }

    private void syncProcessRequestState(ProcessRequest processRequest) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processRequest.getProcessInstanceId())
                .singleResult();
        List<Task> currentTasks = listActiveTasks(processRequest.getProcessInstanceId());
        if (processInstance == null) {
            Object approved = getHistoricVariableValue(processRequest.getProcessInstanceId(), APPROVED_VARIABLE_NAME);
            processRequest.setCurrentTaskId(null);
            processRequest.setCurrentTaskName(null);
            if (Boolean.FALSE.equals(approved)) {
                processRequest.setStatus(ProcessStatusConstant.REJECTED);
            } else {
                processRequest.setStatus(ProcessStatusConstant.APPROVED);
            }
        } else if (currentTasks != null && !currentTasks.isEmpty()) {
            Task firstTask = currentTasks.get(0);
            processRequest.setCurrentTaskId(firstTask.getId());
            processRequest.setCurrentTaskName(resolveCurrentTaskName(currentTasks));
            processRequest.setStatus(ProcessStatusConstant.PROCESSING);
        } else {
            processRequest.setCurrentTaskId(null);
            processRequest.setCurrentTaskName(null);
            processRequest.setStatus(ProcessStatusConstant.PROCESSING);
        }
        processRequest.setUpdateTime(LocalDateTime.now());
        updateById(processRequest);
        log.info("同步流程申请状态完成，记录ID={}, 状态={}", processRequest.getId(), processRequest.getStatus());
    }

    private Object getHistoricVariableValue(String processInstanceId, String variableName) {
        HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName(variableName)
                .singleResult();
        return historicVariableInstance == null ? null : historicVariableInstance.getValue();
    }

    private String resolveCurrentTaskName(List<Task> currentTasks) {
        return currentTasks.stream()
                .map(Task::getName)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining("、"));
    }

    private List<Task> listActiveTasks(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return List.of();
        }
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .asc()
                .list();
    }

    private List<ProcessTaskInfoResponse> buildTaskInfoResponses(List<Task> tasks, Long processRequestId) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        return tasks.stream()
                .map(task -> buildTaskInfoResponse(task, processRequestId))
                .toList();
    }

    private ProcessTaskInfoResponse buildTaskInfoResponse(Task task, Long processRequestId) {
        return ProcessTaskInfoResponse.builder()
                .taskId(task.getId())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .taskName(task.getName())
                .assignee(task.getAssignee())
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .processRequestId(processRequestId)
                .build();
    }

    private void validateTaskNotPrematureParallelJoin(Task task, List<ProcessTaskInfoResponse> currentTasks) {
        if (task == null || !StringUtils.hasText(task.getProcessDefinitionId())
                || !StringUtils.hasText(task.getTaskDefinitionKey())
                || currentTasks == null || currentTasks.size() <= 1) {
            return;
        }
        boolean hasOtherActiveTask = currentTasks.stream()
                .map(ProcessTaskInfoResponse::getTaskId)
                .filter(StringUtils::hasText)
                .anyMatch(taskId -> !Objects.equals(taskId, task.getId()));
        if (!hasOtherActiveTask) {
            return;
        }
        var bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
            return;
        }
        FlowElement currentElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey(), true);
        if (!(currentElement instanceof FlowNode currentNode)
                || currentNode.getIncomingFlows() == null
                || currentNode.getIncomingFlows().size() <= 1) {
            return;
        }
        List<SequenceFlow> sequenceFlows = collectSequenceFlows(bpmnModel.getMainProcess());
        Map<String, FlowNode> nodeMap = collectFlowNodes(bpmnModel.getMainProcess()).stream()
                .filter(node -> StringUtils.hasText(node.getId()))
                .collect(Collectors.toMap(FlowNode::getId, node -> node, (left, right) -> left, LinkedHashMap::new));
        Map<String, List<SequenceFlow>> incomingFlowMap = sequenceFlows.stream()
                .filter(sequenceFlow -> StringUtils.hasText(sequenceFlow.getTargetRef()))
                .collect(Collectors.groupingBy(SequenceFlow::getTargetRef, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<SequenceFlow>> outgoingFlowMap = sequenceFlows.stream()
                .filter(sequenceFlow -> StringUtils.hasText(sequenceFlow.getSourceRef()))
                .collect(Collectors.groupingBy(SequenceFlow::getSourceRef, LinkedHashMap::new, Collectors.toList()));
        if (hasCommonParallelSplitAncestor(currentNode.getIncomingFlows(), nodeMap, incomingFlowMap, outgoingFlowMap)) {
            throw new IllegalArgumentException("当前任务【" + resolveFlowNodeName(currentNode)
                    + "】存在未完成的并行前置任务，必须等待并行任务全部通过后才能审批");
        }
    }

    private void recordTaskApprovalVariables(Task task, ProcessRequestApproveRequest request) {
        if (task == null || request == null || !StringUtils.hasText(task.getId())) {
            return;
        }
        String result = Boolean.TRUE.equals(request.getApproved()) ? "PASS" : (isRejectAction(request) ? "ROLLBACK" : "REJECT");
        taskService.setVariableLocal(task.getId(), APPROVAL_RESULT_VARIABLE_NAME, result);
        taskService.setVariableLocal(task.getId(), APPROVAL_RESULT_TEXT_VARIABLE_NAME, resolveApprovalResultText(result));
        if (StringUtils.hasText(request.getComment())) {
            taskService.setVariableLocal(task.getId(), APPROVAL_COMMENT_VARIABLE_NAME, request.getComment().trim());
        }
    }

    private List<ProcessDiagramNodeResponse> resolveApprovalResultNodes(List<ProcessDiagramNodeResponse> nodes, ProcessRequest processRequest) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        Map<String, NodeApprovalInfo> approvalInfoMap = resolveNodeApprovalInfoMap(processRequest);
        return nodes.stream()
                .map(node -> copyNodeWithApprovalResult(node, approvalInfoMap.get(node.getElementId())))
                .toList();
    }

    private Map<String, NodeApprovalInfo> resolveNodeApprovalInfoMap(ProcessRequest processRequest) {
        if (processRequest == null || !StringUtils.hasText(processRequest.getProcessInstanceId())) {
            return Map.of();
        }
        Map<String, NodeApprovalInfo> result = new LinkedHashMap<>();
        List<HistoricActivityInstance> historicUserTasks = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processRequest.getProcessInstanceId())
                .activityType(USER_TASK_ACTIVITY_TYPE)
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        for (HistoricActivityInstance historicTask : historicUserTasks) {
            NodeApprovalInfo approvalInfo = resolveHistoricTaskApprovalInfo(historicTask);
            if (approvalInfo != null && StringUtils.hasText(historicTask.getActivityId())) {
                result.put(historicTask.getActivityId(), approvalInfo);
            }
        }
        List<Task> activeTasks = listActiveTasks(processRequest.getProcessInstanceId());
        for (Task activeTask : activeTasks) {
            if (activeTask != null && StringUtils.hasText(activeTask.getTaskDefinitionKey())) {
                result.put(activeTask.getTaskDefinitionKey(), NodeApprovalInfo.processing(activeTask.getAssignee()));
            }
        }
        return result;
    }

    private NodeApprovalInfo resolveHistoricTaskApprovalInfo(HistoricActivityInstance historicTask) {
        if (historicTask == null || !StringUtils.hasText(historicTask.getTaskId())) {
            return null;
        }
        Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                .taskId(historicTask.getTaskId())
                .list()
                .stream()
                .filter(variable -> variable != null && StringUtils.hasText(variable.getVariableName()))
                .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue,
                        (left, right) -> right, LinkedHashMap::new));
        String result = variables.get(APPROVAL_RESULT_VARIABLE_NAME) == null ? null : String.valueOf(variables.get(APPROVAL_RESULT_VARIABLE_NAME));
        if (!StringUtils.hasText(result)) {
            result = "PASS";
        }
        String resultText = variables.get(APPROVAL_RESULT_TEXT_VARIABLE_NAME) == null
                ? resolveApprovalResultText(result)
                : String.valueOf(variables.get(APPROVAL_RESULT_TEXT_VARIABLE_NAME));
        String comment = variables.get(APPROVAL_COMMENT_VARIABLE_NAME) == null ? null : String.valueOf(variables.get(APPROVAL_COMMENT_VARIABLE_NAME));
        return NodeApprovalInfo.builder()
                .approvalResult(result)
                .approvalResultText(resultText)
                .approvalAssignee(historicTask.getAssignee())
                .approvalComment(comment)
                .build();
    }

    private ProcessDiagramNodeResponse copyNodeWithApprovalResult(ProcessDiagramNodeResponse node, NodeApprovalInfo approvalInfo) {
        if (node == null) {
            return null;
        }
        return ProcessDiagramNodeResponse.builder()
                .elementId(node.getElementId())
                .elementName(node.getElementName())
                .elementType(node.getElementType())
                .parentId(node.getParentId())
                .documentation(node.getDocumentation())
                .defaultFlowId(node.getDefaultFlowId())
                .approvalResult(approvalInfo == null ? null : approvalInfo.getApprovalResult())
                .approvalResultText(approvalInfo == null ? null : approvalInfo.getApprovalResultText())
                .approvalAssignee(approvalInfo == null ? null : approvalInfo.getApprovalAssignee())
                .approvalComment(approvalInfo == null ? null : approvalInfo.getApprovalComment())
                .formKey(node.getFormKey())
                .boundFormKeys(node.getBoundFormKeys())
                .boundForms(node.getBoundForms())
                .x(node.getX())
                .y(node.getY())
                .width(node.getWidth())
                .height(node.getHeight())
                .incomingCount(node.getIncomingCount())
                .outgoingCount(node.getOutgoingCount())
                .build();
    }

    private String resolveApprovalResultText(String result) {
        if ("PROCESSING".equals(result)) {
            return "审批中";
        }
        if ("REJECT".equals(result)) {
            return "拒绝";
        }
        if ("ROLLBACK".equals(result)) {
            return "驳回";
        }
        if ("PASS".equals(result)) {
            return "通过";
        }
        return result;
    }

    private boolean hasCommonParallelSplitAncestor(List<SequenceFlow> incomingFlows,
                                                   Map<String, FlowNode> nodeMap,
                                                   Map<String, List<SequenceFlow>> incomingFlowMap,
                                                   Map<String, List<SequenceFlow>> outgoingFlowMap) {
        Set<String> commonAncestorIds = null;
        for (SequenceFlow incomingFlow : incomingFlows) {
            Set<String> ancestorIds = new LinkedHashSet<>();
            collectParallelSplitAncestorIds(incomingFlow.getSourceRef(), nodeMap, incomingFlowMap, outgoingFlowMap,
                    ancestorIds, new LinkedHashSet<>());
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
        if (node != null && "ParallelGateway".equals(node.getClass().getSimpleName())
                && outgoingFlowMap.getOrDefault(nodeId, List.of()).size() > 1) {
            ancestorIds.add(nodeId);
        }
        for (SequenceFlow incomingFlow : incomingFlowMap.getOrDefault(nodeId, List.of())) {
            collectParallelSplitAncestorIds(incomingFlow.getSourceRef(), nodeMap, incomingFlowMap, outgoingFlowMap,
                    ancestorIds, visitedNodeIds);
        }
    }

    private List<FlowNode> collectFlowNodes(FlowElementsContainer container) {
        List<FlowNode> nodes = new java.util.ArrayList<>();
        if (container == null || container.getFlowElements() == null) {
            return nodes;
        }
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof FlowNode flowNode) {
                nodes.add(flowNode);
            }
            if (flowElement instanceof FlowElementsContainer childContainer) {
                nodes.addAll(collectFlowNodes(childContainer));
            }
        }
        return nodes;
    }

    private List<SequenceFlow> collectSequenceFlows(FlowElementsContainer container) {
        List<SequenceFlow> sequenceFlows = new java.util.ArrayList<>();
        if (container == null || container.getFlowElements() == null) {
            return sequenceFlows;
        }
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof SequenceFlow sequenceFlow) {
                sequenceFlows.add(sequenceFlow);
            }
            if (flowElement instanceof FlowElementsContainer childContainer) {
                sequenceFlows.addAll(collectSequenceFlows(childContainer));
            }
        }
        return sequenceFlows;
    }

    private String resolveFlowNodeName(FlowNode node) {
        if (node == null) {
            return "";
        }
        return StringUtils.hasText(node.getName()) ? node.getName() : node.getId();
    }

    private boolean hasUnfinishedParallelTask(List<ProcessTaskInfoResponse> currentTasks,
                                              List<ProcessTaskInfoResponse> nextTasks,
                                              String completedTaskId) {
        if (currentTasks == null || currentTasks.size() <= 1 || nextTasks == null || nextTasks.isEmpty()) {
            return false;
        }
        Set<String> unfinishedTaskIds = nextTasks.stream()
                .map(ProcessTaskInfoResponse::getTaskId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        return currentTasks.stream()
                .map(ProcessTaskInfoResponse::getTaskId)
                .filter(StringUtils::hasText)
                .filter(taskId -> !Objects.equals(taskId, completedTaskId))
                .anyMatch(unfinishedTaskIds::contains);
    }

    private String resolveApprovalEventType(ProcessRequestApproveRequest request, boolean waitingParallelTasks) {
        if (waitingParallelTasks) {
            return "PROCESS_PARALLEL_WAITING";
        }
        return Boolean.TRUE.equals(request.getApproved()) ? "PROCESS_APPROVED" : "PROCESS_REJECTED";
    }

    private String resolveApprovalMessage(ProcessRequestApproveRequest request, boolean waitingParallelTasks) {
        if (waitingParallelTasks) {
            return "并行审批任务已通过，等待其他并行任务全部通过";
        }
        if (Boolean.TRUE.equals(request.getApproved())) {
            return "流程申请已通过";
        }
        return isRejectAction(request) ? "流程任务已驳回至上一节点" : "流程任务审批拒绝";
    }

    private boolean isRejectAction(ProcessRequestApproveRequest request) {
        return request != null && isRejectAction(request.getApprovalAction());
    }

    private boolean isRejectAction(String approvalAction) {
        return StringUtils.hasText(approvalAction)
                && APPROVAL_ACTION_REJECT.equalsIgnoreCase(approvalAction.trim());
    }

    private void rejectToPreviousTask(Task currentTask, Map<String, Object> variables, String comment) {
        List<String> previousTaskDefinitionKeys = resolvePreviousTaskDefinitionKeys(currentTask);
        if (previousTaskDefinitionKeys.isEmpty()) {
            throw new IllegalArgumentException("当前任务没有可回退的上一任务");
        }
        runtimeService.setVariables(currentTask.getProcessInstanceId(), variables);
        if (StringUtils.hasText(comment)) {
            taskService.addComment(currentTask.getId(), currentTask.getProcessInstanceId(), REJECT_COMMENT_TYPE, comment.trim());
        }
        var changeActivityStateBuilder = runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(currentTask.getProcessInstanceId());
        if (previousTaskDefinitionKeys.size() == 1) {
            changeActivityStateBuilder.moveActivityIdTo(currentTask.getTaskDefinitionKey(), previousTaskDefinitionKeys.get(0));
        } else {
            changeActivityStateBuilder.moveSingleActivityIdToActivityIds(currentTask.getTaskDefinitionKey(), previousTaskDefinitionKeys);
        }
        changeActivityStateBuilder.changeState();
        log.info("流程任务驳回完成，taskId={}, currentTaskDefinitionKey={}, previousTaskDefinitionKey={}",
                currentTask.getId(), currentTask.getTaskDefinitionKey(), previousTaskDefinitionKeys);
    }

    private List<String> resolvePreviousTaskDefinitionKeys(Task currentTask) {
        List<String> bpmnPreviousTaskDefinitionKeys = resolveBpmnPreviousTaskDefinitionKeys(currentTask);
        if (!bpmnPreviousTaskDefinitionKeys.isEmpty()) {
            return bpmnPreviousTaskDefinitionKeys;
        }
        String previousTaskDefinitionKey = resolvePreviousTaskDefinitionKey(currentTask);
        return StringUtils.hasText(previousTaskDefinitionKey) ? List.of(previousTaskDefinitionKey) : List.of();
    }

    private List<String> resolveBpmnPreviousTaskDefinitionKeys(Task currentTask) {
        if (currentTask == null || !StringUtils.hasText(currentTask.getProcessDefinitionId())
                || !StringUtils.hasText(currentTask.getTaskDefinitionKey())) {
            return List.of();
        }
        var bpmnModel = repositoryService.getBpmnModel(currentTask.getProcessDefinitionId());
        if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
            return List.of();
        }
        FlowElement currentElement = bpmnModel.getMainProcess().getFlowElement(currentTask.getTaskDefinitionKey(), true);
        if (!(currentElement instanceof FlowNode currentNode) || currentNode.getIncomingFlows() == null) {
            return List.of();
        }
        Map<String, FlowNode> nodeMap = collectFlowNodes(bpmnModel.getMainProcess()).stream()
                .filter(node -> StringUtils.hasText(node.getId()))
                .collect(Collectors.toMap(FlowNode::getId, node -> node, (left, right) -> left, LinkedHashMap::new));
        LinkedHashSet<String> previousTaskDefinitionKeys = new LinkedHashSet<>();
        for (SequenceFlow incomingFlow : currentNode.getIncomingFlows()) {
            collectNearestPreviousUserTaskDefinitionKeys(incomingFlow.getSourceRef(), nodeMap,
                    previousTaskDefinitionKeys, new LinkedHashSet<>());
        }
        return previousTaskDefinitionKeys.stream()
                .filter(taskDefinitionKey -> hasFinishedHistoricTask(currentTask.getProcessInstanceId(), taskDefinitionKey))
                .toList();
    }

    private void collectNearestPreviousUserTaskDefinitionKeys(String nodeId,
                                                             Map<String, FlowNode> nodeMap,
                                                             Set<String> previousTaskDefinitionKeys,
                                                             Set<String> visitedNodeIds) {
        if (!StringUtils.hasText(nodeId) || !visitedNodeIds.add(nodeId)) {
            return;
        }
        FlowNode node = nodeMap.get(nodeId);
        if (node == null) {
            return;
        }
        if ("UserTask".equals(node.getClass().getSimpleName())) {
            previousTaskDefinitionKeys.add(nodeId);
            return;
        }
        if (node.getIncomingFlows() == null || node.getIncomingFlows().isEmpty()) {
            return;
        }
        for (SequenceFlow incomingFlow : node.getIncomingFlows()) {
            collectNearestPreviousUserTaskDefinitionKeys(incomingFlow.getSourceRef(), nodeMap,
                    previousTaskDefinitionKeys, visitedNodeIds);
        }
    }

    private boolean hasFinishedHistoricTask(String processInstanceId, String taskDefinitionKey) {
        if (!StringUtils.hasText(processInstanceId) || !StringUtils.hasText(taskDefinitionKey)) {
            return false;
        }
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityId(taskDefinitionKey)
                .activityType(USER_TASK_ACTIVITY_TYPE)
                .finished()
                .count() > 0;
    }

    private String resolvePreviousTaskDefinitionKey(Task currentTask) {
        if (currentTask == null || !StringUtils.hasText(currentTask.getProcessInstanceId())) {
            return null;
        }
        List<HistoricActivityInstance> historicUserTasks = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(currentTask.getProcessInstanceId())
                .activityType(USER_TASK_ACTIVITY_TYPE)
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .desc()
                .list();
        if (historicUserTasks == null || historicUserTasks.isEmpty()) {
            return null;
        }
        String currentTaskDefinitionKey = currentTask.getTaskDefinitionKey();
        for (HistoricActivityInstance historicUserTask : historicUserTasks) {
            if (historicUserTask == null || !StringUtils.hasText(historicUserTask.getActivityId())) {
                continue;
            }
            if (!Objects.equals(historicUserTask.getActivityId(), currentTaskDefinitionKey)) {
                return historicUserTask.getActivityId();
            }
        }
        return null;
    }

    private ProcessRequest getRequiredProcessRequest(Long id) {
        ProcessRequest processRequest = getById(id);
        if (processRequest == null) {
            throw new IllegalArgumentException("未找到对应流程申请，ID：" + id);
        }
        return processRequest;
    }

    private ProcessRequest getProcessRequestByProcessInstanceId(String processInstanceId) {
        ProcessRequest processRequest = baseMapper.selectByProcessInstanceId(processInstanceId);
        if (processRequest == null) {
            throw new IllegalArgumentException("未找到对应流程申请，流程实例ID：" + processInstanceId);
        }
        return processRequest;
    }

    private ProcessRequestResponse buildProcessRequestResponse(ProcessRequest processRequest) {
        return ProcessRequestResponse.builder()
                .id(processRequest.getId())
                .processNo(processRequest.getProcessNo())
                .starter(processRequest.getStarter())
                .taskName(processRequest.getTaskName())
                .businessTitle(processRequest.getBusinessTitle())
                .formData(parseFormData(processRequest.getFormDataJson()))
                .status(processRequest.getStatus())
                .processInstanceId(processRequest.getProcessInstanceId())
                .currentTaskId(processRequest.getCurrentTaskId())
                .currentTaskName(processRequest.getCurrentTaskName())
                .currentTasks(resolveCurrentTaskInfoResponses(processRequest))
                .processDefinitionKey(processRequest.getProcessDefinitionKey())
                .processDefinitionId(resolveProcessDefinitionId(processRequest))
                .processBeanName(resolveProcessBeanName(processRequest))
                .activeNodeIds(resolveActiveNodeIds(processRequest))
                .createTime(processRequest.getCreateTime())
                .updateTime(processRequest.getUpdateTime())
                .build();
    }

    private ProcessRequestResponse buildProcessRequestListResponse(ProcessRequest processRequest) {
        return ProcessRequestResponse.builder()
                .id(processRequest.getId())
                .processNo(processRequest.getProcessNo())
                .starter(processRequest.getStarter())
                .taskName(processRequest.getTaskName())
                .businessTitle(processRequest.getBusinessTitle())
                .status(processRequest.getStatus())
                .processInstanceId(processRequest.getProcessInstanceId())
                .currentTaskId(processRequest.getCurrentTaskId())
                .currentTaskName(processRequest.getCurrentTaskName())
                .currentTasks(List.of())
                .processDefinitionKey(processRequest.getProcessDefinitionKey())
                .activeNodeIds(List.of())
                .createTime(processRequest.getCreateTime())
                .updateTime(processRequest.getUpdateTime())
                .build();
    }

    private Map<String, Object> normalizeFormData(Map<String, Object> formData) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        if (formData == null) {
            return normalized;
        }
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            if (!StringUtils.hasText(entry.getKey())) {
                continue;
            }
            normalized.put(entry.getKey().trim(), entry.getValue());
        }
        return normalized;
    }

    private void mergeApprovalFormData(ProcessRequest processRequest, Map<String, Object> approvalFormData) {
        if (processRequest == null || approvalFormData == null || approvalFormData.isEmpty()) {
            return;
        }
        Map<String, Object> formData = parseFormData(processRequest.getFormDataJson());
        formData.putAll(approvalFormData);
        processRequest.setFormDataJson(JSON.toJSONString(formData));
        processRequest.setUpdateTime(LocalDateTime.now());
        updateById(processRequest);
    }

    private Map<String, Object> parseFormData(String formDataJson) {
        if (!StringUtils.hasText(formDataJson)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> parsed = JSON.parseObject(formDataJson, MAP_TYPE);
            return parsed == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parsed);
        } catch (Exception ex) {
            log.warn("瑙ｆ瀽琛ㄥ崟鏁版嵁澶辫触锛岃繑鍥炵┖瀵硅薄锛屽師濮嬪€?{}", formDataJson, ex);
            return new LinkedHashMap<>();
        }
    }

    private String resolveStarter(Map<String, Object> formData) {
        String starter = resolveStringValue(formData,
                "starter", "applicant", "applyUser", "applyUserName", "submitter", "creator", "userName");
        return StringUtils.hasText(starter) ? starter : "SYSTEM";
    }

    private String resolveBusinessTitle(String processDefinitionKey, Map<String, Object> formData) {
        String title = resolveStringValue(formData,
                "businessTitle", "title", "subject", "reason", "name", "remark", "description");
        return StringUtils.hasText(title) ? title : processDefinitionKey;
    }

    private List<ProcessTaskInfoResponse> resolveCurrentTaskInfoResponses(ProcessRequest processRequest) {
        if (processRequest == null || !StringUtils.hasText(processRequest.getProcessInstanceId())) {
            return List.of();
        }
        return buildTaskInfoResponses(listActiveTasks(processRequest.getProcessInstanceId()), processRequest.getId());
    }

    private void notifyProcessCallback(ProcessRequest processRequest, String eventType, String message) {
        notifyProcessCallback(processRequest, eventType, message, null, null, null, null, Map.of());
    }

    private void notifyProcessCallback(ProcessRequest processRequest, String eventType, String message, Task completedTask, Boolean approved,
                                       String approvalAction, List<ProcessTaskInfoResponse> currentTasks,
                                       Map<String, Object> approvalFormData) {
        String processBeanName = resolveProcessBeanName(processRequest);
        ProcessRequestResponse response = buildProcessRequestResponse(processRequest);
        if (!StringUtils.hasText(response.getProcessDefinitionId())) {
            return;
        }
        String processDefinitionName = resolveProcessDefinitionName(response.getProcessDefinitionId());
        String currentTaskId = completedTask == null ? response.getCurrentTaskId() : completedTask.getId();
        String currentTaskName = completedTask == null ? response.getCurrentTaskName() : completedTask.getName();
        List<ProcessTaskInfoResponse> callbackCurrentTasks = currentTasks == null ? response.getCurrentTasks() : currentTasks;
        List<ProcessTaskInfoResponse> nextTasks = response.getCurrentTasks();
        String approvalId = completedTask == null ? null : completedTask.getAssignee();
        String approvalName = approvalId;
        String taskApprovalResult = resolveTaskApprovalResult(approved, approvalAction);
        Integer currentApprovalResult = resolveCurrentApprovalResult(approved, approvalAction);
        List<Map<String, Object>> relatedForms = resolveRelatedForms(response.getProcessDefinitionId(), completedTask, response.getFormData());
        Map<String, Object> firstRelatedForm = relatedForms.isEmpty() ? null : relatedForms.get(0);
        Long relatedFormId = firstRelatedForm == null ? null : (Long) firstRelatedForm.get("formId");
        String relatedFormName = firstRelatedForm == null ? null : (String) firstRelatedForm.get("formName");
        WcdkProcessConnectionEvent event = WcdkProcessConnectionEvent.builder()
                .processInstanceId(response.getProcessInstanceId())
                .processDefinitionId(response.getProcessDefinitionId())
                .processDefinitionKey(response.getProcessDefinitionKey())
                .processDefinitionName(processDefinitionName)
                .businessKey(response.getProcessNo())
                .approvalId(approvalId)
                .approvalName(approvalName)
                .currentTaskId(currentTaskId)
                .currentTaskName(currentTaskName)
                .currentTasks(callbackCurrentTasks)
                .taskApproved(approved)
                .taskApprovalResult(taskApprovalResult)
                .currentApprovalResult(currentApprovalResult)
                .approvalFormData(approvalFormData == null ? Map.of() : approvalFormData)
                .relatedFormData(response.getFormData())
                .relatedFormId(relatedFormId)
                .relatedFormName(relatedFormName)
                .relatedForms(relatedForms)
                .nextTaskId(response.getCurrentTaskId())
                .nextTaskName(response.getCurrentTaskName())
                .nextTasks(nextTasks)
                .processBeanName(processBeanName)
                .eventType(eventType)
                .message(message)
                .eventTime(LocalDateTime.now())
                .build();
        wcdkProcessClientCallbackService.callback(event);
    }

    private String resolveTaskApprovalResult(Boolean approved, String approvalAction) {
        if (approved == null) {
            return null;
        }
        if (isRejectAction(approvalAction)) {
            return "驳回";
        }
        return Boolean.TRUE.equals(approved) ? "通过" : "拒绝";
    }

    private Integer resolveCurrentApprovalResult(Boolean approved, String approvalAction) {
        if (approved == null) {
            return null;
        }
        if (Boolean.TRUE.equals(approved)) {
            return 0;
        }
        return isRejectAction(approvalAction) ? 2 : 1;
    }

    private String resolveProcessDefinitionName(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return null;
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        return processDefinition == null ? null : processDefinition.getName();
    }

    private List<Map<String, Object>> resolveRelatedForms(String processDefinitionId, Task completedTask, Map<String, Object> formData) {
        if (!StringUtils.hasText(processDefinitionId) || completedTask == null || !StringUtils.hasText(completedTask.getTaskDefinitionKey())) {
            return List.of();
        }
        List<WcdkProcessFormBinding> bindings = formBindingMapper.selectList(new LambdaQueryWrapper<WcdkProcessFormBinding>()
                .eq(WcdkProcessFormBinding::getProcessDefinitionId, processDefinitionId)
                .eq(WcdkProcessFormBinding::getTaskDefinitionKey, completedTask.getTaskDefinitionKey())
                .eq(WcdkProcessFormBinding::getStatus, 1)
                .orderByDesc(WcdkProcessFormBinding::getUpdateTime));
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        return bindings.stream()
                .map(binding -> buildRelatedForm(binding, formData))
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Object> buildRelatedForm(WcdkProcessFormBinding binding, Map<String, Object> formData) {
        if (binding == null || binding.getFormId() == null) {
            return null;
        }
        WcdkProcessForm form = formMapper.selectById(binding.getFormId());
        if (form == null || !Objects.equals(form.getStatus(), 1)) {
            return null;
        }
        Object schema = parseFormSchema(form.getFormSchemaJson());
        Map<String, Object> relatedForm = new LinkedHashMap<>();
        relatedForm.put("formId", form.getId());
        relatedForm.put("formKey", form.getFormKey());
        relatedForm.put("formName", form.getFormName());
        relatedForm.put("formVersion", form.getFormVersion());
        relatedForm.put("processNodeId", binding.getTaskDefinitionKey());
        relatedForm.put("processNodeName", resolveTaskName(binding.getProcessDefinitionId(), binding.getTaskDefinitionKey()));
        relatedForm.put("fieldCount", schema instanceof List<?> ? ((List<?>) schema).size() : 0);
        relatedForm.put("schema", schema);
        relatedForm.put("formData", formData == null ? Map.of() : formData);
        return relatedForm;
    }

    private Object parseFormSchema(String schemaJson) {
        if (!StringUtils.hasText(schemaJson)) {
            return List.of();
        }
        try {
            Object schema = JSON.parse(schemaJson);
            return schema == null ? List.of() : schema;
        } catch (Exception exception) {
            return List.of();
        }
    }

    private String resolveTaskName(String processDefinitionId, String taskDefinitionKey) {
        if (!StringUtils.hasText(processDefinitionId) || !StringUtils.hasText(taskDefinitionKey)) {
            return taskDefinitionKey;
        }
        try {
            var bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
                return taskDefinitionKey;
            }
            var flowElement = bpmnModel.getMainProcess().getFlowElement(taskDefinitionKey, true);
            return flowElement != null && StringUtils.hasText(flowElement.getName()) ? flowElement.getName() : taskDefinitionKey;
        } catch (Exception exception) {
            return taskDefinitionKey;
        }
    }

    private String resolveProcessDefinitionId(ProcessRequest processRequest) {
        if (processRequest == null) {
            return null;
        }
        if (StringUtils.hasText(processRequest.getProcessInstanceId())) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processRequest.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null) {
                return processInstance.getProcessDefinitionId();
            }
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processRequest.getProcessInstanceId())
                    .singleResult();
            if (historicProcessInstance != null) {
                return historicProcessInstance.getProcessDefinitionId();
            }
        }
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processRequest.getProcessDefinitionKey())
                .latestVersion()
                .singleResult();
        return processDefinition == null ? null : processDefinition.getId();
    }

    private List<String> resolveActiveNodeIds(ProcessRequest processRequest) {
        if (processRequest == null || !StringUtils.hasText(processRequest.getProcessInstanceId())) {
            return List.of();
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processRequest.getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            return List.of();
        }
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processRequest.getProcessInstanceId());
        return activeActivityIds == null ? List.of() : activeActivityIds;
    }

    private String resolveStringValue(Map<String, Object> formData, String... candidateKeys) {
        if (formData == null || formData.isEmpty()) {
            return null;
        }
        for (String candidateKey : candidateKeys) {
            Object value = formData.get(candidateKey);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }

    private String resolveFormProcessBeanName(ProcessRequest processRequest) {
        Map<String, Object> formData = parseFormData(processRequest.getFormDataJson());
        Object value = formData.get(PROCESS_BEAN_NAME);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private String resolveProcessBeanName(ProcessRequest processRequest) {
        if (processRequest == null) {
            return null;
        }
        String formProcessBeanName = resolveFormProcessBeanName(processRequest);
        if (StringUtils.hasText(formProcessBeanName)) {
            return formProcessBeanName;
        }
        if (StringUtils.hasText(processRequest.getProcessInstanceId())) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processRequest.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null) {
                Object runtimeValue = runtimeService.getVariable(processRequest.getProcessInstanceId(), PROCESS_BEAN_NAME);
                if (runtimeValue != null && StringUtils.hasText(String.valueOf(runtimeValue))) {
                    return String.valueOf(runtimeValue).trim();
                }
            }
            HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processRequest.getProcessInstanceId())
                    .variableName(PROCESS_BEAN_NAME)
                    .singleResult();
            if (historicVariableInstance != null && historicVariableInstance.getValue() != null
                    && StringUtils.hasText(String.valueOf(historicVariableInstance.getValue()))) {
                return String.valueOf(historicVariableInstance.getValue()).trim();
            }
        }
        return resolveBoundProcessBeanName(processRequest);
    }

    private String resolveBoundProcessBeanName(ProcessRequest processRequest) {
        String processDefinitionId = resolveProcessDefinitionId(processRequest);
        if (!StringUtils.hasText(processDefinitionId)) {
            return null;
        }
        Page<WcdkProcessClientProcess> page = wcdkProcessClientProcessMapper.selectPage(new Page<>(1, 1), new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, processDefinitionId)
                .isNotNull(WcdkProcessClientProcess::getProcessBeanName));
        WcdkProcessClientProcess binding = page.getRecords().isEmpty() ? null : page.getRecords().get(0);
        if (binding == null || !StringUtils.hasText(binding.getProcessBeanName())) {
            return null;
        }
        return binding.getProcessBeanName().trim();
    }

    private String generateProcessNo(LocalDateTime now) {
        return "PROCESS-" + now.getYear()
                + String.format("%02d", now.getMonthValue())
                + String.format("%02d", now.getDayOfMonth())
                + String.format("%02d", now.getHour())
                + String.format("%02d", now.getMinute())
                + String.format("%02d", now.getSecond())
                + String.format("%03d", now.getNano() / 1_000_000);
    }

    private String resolveDeleteReason(String deleteReason) {
        return StringUtils.hasText(deleteReason) ? deleteReason.trim() : "娴佺▼涓績鎵嬪姩鍒犻櫎";
    }

    private Set<String> resolveDefinitionKeysByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return Set.of();
        }
        String trimmedCategory = category.trim();
        Set<String> deploymentIds = repositoryService.createDeploymentQuery()
                .deploymentCategory(trimmedCategory)
                .list()
                .stream()
                .map(Deployment::getId)
                .collect(Collectors.toSet());
        if (deploymentIds.isEmpty()) {
            return Set.of();
        }
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list()
                .stream()
                .filter(processDefinition -> deploymentIds.contains(processDefinition.getDeploymentId()))
                .map(ProcessDefinition::getKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    @lombok.Data
    @lombok.Builder
    private static class NodeApprovalInfo {

        private String approvalResult;

        private String approvalResultText;

        private String approvalAssignee;

        private String approvalComment;

        private static NodeApprovalInfo processing(String assignee) {
            return NodeApprovalInfo.builder()
                    .approvalResult("PROCESSING")
                    .approvalResultText("审批中")
                    .approvalAssignee(assignee)
                    .build();
        }
    }
}
