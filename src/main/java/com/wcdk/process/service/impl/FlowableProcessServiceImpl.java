package com.wcdk.process.service.impl;

import com.wcdk.process.dto.ProcessInstanceResponse;
import com.wcdk.process.dto.ProcessTaskInfoResponse;
import com.wcdk.process.dto.StartProcessRequest;
import com.wcdk.process.dto.TaskCompleteRequest;
import com.wcdk.process.dto.TaskResponse;
import com.wcdk.process.entity.ProcessRequest;
import com.wcdk.process.mapper.ProcessRequestMapper;
import com.wcdk.process.service.FlowableProcessService;
import lombok.RequiredArgsConstructor;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowElementsContainer;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Flowable 流程实例与任务处理实现。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Service
@RequiredArgsConstructor
public class FlowableProcessServiceImpl implements FlowableProcessService {

    private static final String DEFAULT_PROCESS_DEFINITION_KEY = "leave-process";

    private static final String PROCESS_BEAN_NAME = "processBeanName";

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    private final ProcessRequestMapper processRequestMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstanceResponse startProcess(StartProcessRequest request) {
        validateStartProcessRequest(request);
        Map<String, Object> variables = new HashMap<>();
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }
        if (StringUtils.hasText(request.getStarter())) {
            variables.put("starter", request.getStarter());
        }
        if (StringUtils.hasText(request.getProcessBeanName())) {
            variables.put(PROCESS_BEAN_NAME, request.getProcessBeanName());
        }
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                resolveProcessDefinitionKey(request.getProcessDefinitionKey()),
                request.getBusinessKey(),
                variables
        );
        return buildProcessInstanceResponse(processInstance, request.getProcessBeanName());
    }

    @Override
    public ProcessInstanceResponse getProcessInstance(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance != null) {
            Object processBeanName = runtimeService.getVariable(processInstanceId, PROCESS_BEAN_NAME);
            return buildProcessInstanceResponse(processInstance, processBeanName == null ? null : String.valueOf(processBeanName));
        }
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (historicProcessInstance == null) {
            throw new IllegalArgumentException("未查询到对应流程实例，流程实例ID：" + processInstanceId);
        }
        return ProcessInstanceResponse.builder()
                .processInstanceId(historicProcessInstance.getId())
                .processDefinitionId(historicProcessInstance.getProcessDefinitionId())
                .processDefinitionKey(historicProcessInstance.getProcessDefinitionKey())
                .businessKey(historicProcessInstance.getBusinessKey())
                .processBeanName(getHistoricProcessBeanName(processInstanceId))
                .suspended(false)
                .build();
    }

    @Override
    public List<TaskResponse> listTask(String assignee) {
        var taskQuery = taskService.createTaskQuery().active();
        if (StringUtils.hasText(assignee)) {
            taskQuery.taskAssignee(assignee);
        }
        return taskQuery.orderByTaskCreateTime()
                .desc()
                .list()
                .stream()
                .map(this::buildTaskResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(TaskCompleteRequest request) {
        if (request == null || !StringUtils.hasText(request.getTaskId())) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        Task task = taskService.createTaskQuery()
                .taskId(request.getTaskId())
                .singleResult();
        if (task == null) {
            throw new IllegalArgumentException("未查询到对应任务，任务ID：" + request.getTaskId());
        }
        validateTaskNotPrematureParallelJoin(task);
        Map<String, Object> variables = request.getVariables() == null ? Map.of() : request.getVariables();
        taskService.complete(request.getTaskId(), variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance == null) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (historicProcessInstance == null) {
                throw new IllegalArgumentException("未查询到对应流程实例，流程实例ID：" + processInstanceId);
            }
            throw new IllegalArgumentException("流程实例已结束，不能重复删除");
        }
        runtimeService.deleteProcessInstance(processInstanceId, resolveDeleteReason(deleteReason));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(String taskId, String deleteReason) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new IllegalArgumentException("未查询到对应任务，任务ID：" + taskId);
        }
        if (StringUtils.hasText(task.getProcessInstanceId())) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (processInstance != null) {
                throw new IllegalArgumentException("运行中的流程任务不支持直接删除，请先完成任务或删除流程实例");
            }
        }
        taskService.deleteTask(taskId, resolveDeleteReason(deleteReason));
    }

    private void validateStartProcessRequest(StartProcessRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("启动流程请求不能为空");
        }
        if (!StringUtils.hasText(resolveProcessDefinitionKey(request.getProcessDefinitionKey()))) {
            throw new IllegalArgumentException("流程定义标识不能为空");
        }
    }

    private String resolveProcessDefinitionKey(String processDefinitionKey) {
        return StringUtils.hasText(processDefinitionKey) ? processDefinitionKey : DEFAULT_PROCESS_DEFINITION_KEY;
    }

    private String resolveDeleteReason(String deleteReason) {
        return StringUtils.hasText(deleteReason) ? deleteReason.trim() : "页面手动删除";
    }

    private ProcessInstanceResponse buildProcessInstanceResponse(ProcessInstance processInstance, String processBeanName) {
        return ProcessInstanceResponse.builder()
                .processInstanceId(processInstance.getId())
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .processDefinitionKey(processInstance.getProcessDefinitionKey())
                .businessKey(processInstance.getBusinessKey())
                .processBeanName(processBeanName)
                .suspended(processInstance.isSuspended())
                .build();
    }

    private TaskResponse buildTaskResponse(Task task) {
        ProcessRequest processRequest = getProcessRequest(task.getProcessInstanceId());
        String createdTaskName = processRequest != null && StringUtils.hasText(processRequest.getTaskName())
                ? processRequest.getTaskName()
                : task.getName();
        return TaskResponse.builder()
                .taskId(task.getId())
                .taskName(createdTaskName)
                .currentTaskName(task.getName())
                .parallelTasks(resolveParallelTasks(task.getProcessInstanceId(), processRequest == null ? null : processRequest.getId()))
                .assignee(task.getAssignee())
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionId(task.getProcessDefinitionId())
                .processRequestId(processRequest == null ? null : processRequest.getId())
                .build();
    }

    private List<ProcessTaskInfoResponse> resolveParallelTasks(String processInstanceId, Long processRequestId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return List.of();
        }
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .asc()
                .list()
                .stream()
                .map(task -> ProcessTaskInfoResponse.builder()
                        .taskId(task.getId())
                        .taskDefinitionKey(task.getTaskDefinitionKey())
                        .taskName(task.getName())
                        .assignee(task.getAssignee())
                        .processInstanceId(task.getProcessInstanceId())
                        .processDefinitionId(task.getProcessDefinitionId())
                        .processRequestId(processRequestId)
                        .build())
                .toList();
    }

    private void validateTaskNotPrematureParallelJoin(Task task) {
        if (task == null || !StringUtils.hasText(task.getProcessInstanceId())
                || !StringUtils.hasText(task.getProcessDefinitionId())
                || !StringUtils.hasText(task.getTaskDefinitionKey())) {
            return;
        }
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(task.getProcessInstanceId())
                .active()
                .list();
        boolean hasOtherActiveTask = activeTasks != null && activeTasks.stream()
                .map(Task::getId)
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

    private ProcessRequest getProcessRequest(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return null;
        }
        return processRequestMapper.selectByProcessInstanceId(processInstanceId);
    }

    private String getHistoricProcessBeanName(String processInstanceId) {
        HistoricVariableInstance processBeanName = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName(PROCESS_BEAN_NAME)
                .singleResult();
        if (processBeanName == null || processBeanName.getValue() == null) {
            return null;
        }
        return String.valueOf(processBeanName.getValue());
    }
}
