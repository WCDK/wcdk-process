package com.wcdk.process.service.impl;

import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.service.WcdkProcessCallbackService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Service
@RequiredArgsConstructor
public class WcdkProcessCallbackServiceImpl implements WcdkProcessCallbackService {

    public static final String PROCESS_BEAN_NAME = "processBeanName";

    private final RuntimeService runtimeService;

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    @Override
    public void callback(WcdkProcessConnectionEvent request) {
        if (request == null) {
            throw new IllegalArgumentException("流程回调请求不能为空");
        }
        String processBeanName = resolveProcessBeanName(request);
        WcdkProcessConnectionEvent event = WcdkProcessConnectionEvent.builder()
                .connectionId(request.getConnectionId())
                .clientId(request.getClientId())
                .clientName(request.getClientName())
                .processInstanceId(resolveProcessInstanceId(request))
                .processDefinitionId(resolveProcessDefinitionId(request))
                .processDefinitionKey(resolveProcessDefinitionKey(request))
                .processDefinitionName(request.getProcessDefinitionName())
                .businessKey(resolveBusinessKey(request))
                .approvalId(request.getApprovalId())
                .approvalName(request.getApprovalName())
                .currentTaskId(request.getCurrentTaskId())
                .currentTaskName(request.getCurrentTaskName())
                .currentTasks(request.getCurrentTasks())
                .taskApproved(request.getTaskApproved())
                .taskApprovalResult(request.getTaskApprovalResult())
                .currentApprovalResult(request.getCurrentApprovalResult())
                .relatedFormData(request.getRelatedFormData())
                .relatedFormId(request.getRelatedFormId())
                .relatedFormName(request.getRelatedFormName())
                .relatedForms(request.getRelatedForms())
                .nextTaskId(request.getNextTaskId())
                .nextTaskName(request.getNextTaskName())
                .nextTasks(request.getNextTasks())
                .processBeanName(processBeanName)
                .eventType(request.getEventType())
                .message(request.getMessage())
                .eventTime(request.getEventTime())
                .errorMessage(request.getErrorMessage())
                .build();
        validateClientProcessBinding(event);
    }

    private void validateClientProcessBinding(WcdkProcessConnectionEvent event) {
        if (!StringUtils.hasText(event.getClientId())) {
            return;
        }
        if (wcdkProcessClientRegistryService.hasProcessBinding(event.getClientId(), event.getProcessDefinitionId())) {
            return;
        }
        throw new IllegalArgumentException("当前客户端未绑定该流程定义");
    }

    private String resolveProcessBeanName(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessBeanName())) {
            return request.getProcessBeanName();
        }
        String processInstanceId = resolveProcessInstanceId(request);
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("流程回调时必须提供processBeanName或processInstanceId");
        }
        Object variableValue = runtimeService.getVariable(processInstanceId, PROCESS_BEAN_NAME);
        if (variableValue == null || !StringUtils.hasText(String.valueOf(variableValue))) {
            throw new IllegalArgumentException("流程实例未设置流程处理器");
        }
        return String.valueOf(variableValue);
    }

    private String resolveProcessInstanceId(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessInstanceId())) {
            return request.getProcessInstanceId();
        }
        return null;
    }

    private String resolveProcessDefinitionId(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessDefinitionId())) {
            return request.getProcessDefinitionId();
        }
        ProcessInstance processInstance = getProcessInstance(resolveProcessInstanceId(request));
        return processInstance == null ? null : processInstance.getProcessDefinitionId();
    }

    private String resolveBusinessKey(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getBusinessKey())) {
            return request.getBusinessKey();
        }
        ProcessInstance processInstance = getProcessInstance(resolveProcessInstanceId(request));
        return processInstance == null ? null : processInstance.getBusinessKey();
    }

    private String resolveProcessDefinitionKey(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessDefinitionKey())) {
            return request.getProcessDefinitionKey();
        }
        ProcessInstance processInstance = getProcessInstance(resolveProcessInstanceId(request));
        return processInstance == null ? null : processInstance.getProcessDefinitionKey();
    }

    private ProcessInstance getProcessInstance(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            return null;
        }
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

}
