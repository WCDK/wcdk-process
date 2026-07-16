package com.wcdk.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.constant.ProcessStatusConstant;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.entity.ProcessRequest;
import com.wcdk.process.mapper.ProcessRequestMapper;
import com.wcdk.process.service.FlowableDeployService;
import com.wcdk.process.service.ProcessRequestService;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
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

    private static final String PROCESS_BEAN_NAME = "processBeanName";

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    private final FlowableDeployService flowableDeployService;

    private final WcdkProcessClientCallbackService wcdkProcessClientCallbackService;

    public ProcessRequestServiceImpl(RuntimeService runtimeService,
                                     TaskService taskService,
                                     HistoryService historyService,
                                     RepositoryService repositoryService,
                                     FlowableDeployService flowableDeployService,
                                     WcdkProcessClientCallbackService wcdkProcessClientCallbackService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.flowableDeployService = flowableDeployService;
        this.wcdkProcessClientCallbackService = wcdkProcessClientCallbackService;
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
                .nodes(detailResponse.getNodes())
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
                page.getRecords().stream().map(this::buildProcessRequestResponse).toList()
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
        Map<String, Object> variables = new HashMap<>();
        variables.put(APPROVED_VARIABLE_NAME, request.getApproved());
        variables.put("comment", request.getComment());
        log.info("处理流程审批任务，taskId={}, approved={}", request.getTaskId(), request.getApproved());
        taskService.complete(request.getTaskId(), variables);
        syncProcessRequestState(processRequest);
        notifyProcessCallback(processRequest,
                Boolean.TRUE.equals(request.getApproved()) ? "PROCESS_APPROVED" : "PROCESS_REJECTED",
                Boolean.TRUE.equals(request.getApproved()) ? "流程申请已通过" : "流程申请已拒绝");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessRequest(Long id, String deleteReason) {
        ProcessRequest processRequest = getRequiredProcessRequest(id);
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
        notifyProcessCallback(processRequest, "PROCESS_DELETED", "流程申请已删除");
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
        String processBeanName = resolveFormProcessBeanName(processRequest);
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
        Task currentTask = taskService.createTaskQuery()
                .processInstanceId(processRequest.getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            Object approved = getHistoricVariableValue(processRequest.getProcessInstanceId(), APPROVED_VARIABLE_NAME);
            processRequest.setCurrentTaskId(null);
            processRequest.setCurrentTaskName(null);
            if (Boolean.FALSE.equals(approved)) {
                processRequest.setStatus(ProcessStatusConstant.REJECTED);
            } else {
                processRequest.setStatus(ProcessStatusConstant.APPROVED);
            }
        } else if (currentTask != null) {
            processRequest.setCurrentTaskId(currentTask.getId());
            processRequest.setCurrentTaskName(currentTask.getName());
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
                .processDefinitionKey(processRequest.getProcessDefinitionKey())
                .processDefinitionId(resolveProcessDefinitionId(processRequest))
                .processBeanName(resolveProcessBeanName(processRequest))
                .activeNodeIds(resolveActiveNodeIds(processRequest))
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

    private void notifyProcessCallback(ProcessRequest processRequest, String eventType, String message) {
        String processBeanName = resolveProcessBeanName(processRequest);
        if (!StringUtils.hasText(processBeanName)) {
            return;
        }
        ProcessRequestResponse response = buildProcessRequestResponse(processRequest);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", response.getId());
        payload.put("processNo", response.getProcessNo());
        payload.put("starter", response.getStarter());
        payload.put("taskName", response.getTaskName());
        payload.put("businessTitle", response.getBusinessTitle());
        payload.put("status", response.getStatus());
        payload.put("processDefinitionId", response.getProcessDefinitionId());
        payload.put("currentTaskId", response.getCurrentTaskId());
        payload.put("currentTaskName", response.getCurrentTaskName());
        payload.put("activeNodeIds", response.getActiveNodeIds());
        payload.put("formData", response.getFormData());
        payload.put(PROCESS_BEAN_NAME, processBeanName);
        wcdkProcessClientCallbackService.callback(WcdkProcessConnectionEvent.builder()
                .processInstanceId(response.getProcessInstanceId())
                .processDefinitionId(response.getProcessDefinitionId())
                .processDefinitionKey(response.getProcessDefinitionKey())
                .businessKey(response.getProcessNo())
                .processBeanName(processBeanName)
                .eventType(eventType)
                .message(message)
                .eventTime(LocalDateTime.now())
                .payload(payload)
                .build());
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
        return null;
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
}

