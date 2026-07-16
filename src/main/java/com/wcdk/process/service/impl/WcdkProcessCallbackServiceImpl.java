package com.wcdk.process.service.impl;

import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.service.WcdkProcessCallbackService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

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
                .businessKey(resolveBusinessKey(request))
                .processBeanName(processBeanName)
                .eventType(request.getEventType())
                .message(request.getMessage())
                .eventTime(request.getEventTime())
                .payload(request.getPayload())
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
        throw new IllegalArgumentException("��ǰ�ͻ���δ�󶨸����̶���");
    }

    private String resolveProcessBeanName(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessBeanName())) {
            return request.getProcessBeanName();
        }
        String payloadProcessBeanName = getPayloadString(request.getPayload(), PROCESS_BEAN_NAME);
        if (StringUtils.hasText(payloadProcessBeanName)) {
            return payloadProcessBeanName;
        }
        String processInstanceId = resolveProcessInstanceId(request);
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("���̻ص�ʱ�����ṩprocessBeanName��processInstanceId");
        }
        Object variableValue = runtimeService.getVariable(processInstanceId, PROCESS_BEAN_NAME);
        if (variableValue == null || !StringUtils.hasText(String.valueOf(variableValue))) {
            throw new IllegalArgumentException("����ʵ��δ�������̴�����");
        }
        return String.valueOf(variableValue);
    }

    private String resolveProcessInstanceId(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessInstanceId())) {
            return request.getProcessInstanceId();
        }
        return getPayloadString(request.getPayload(), "processInstanceId");
    }

    private String resolveProcessDefinitionId(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessDefinitionId())) {
            return request.getProcessDefinitionId();
        }
        String payloadProcessDefinitionId = getPayloadString(request.getPayload(), "processDefinitionId");
        if (StringUtils.hasText(payloadProcessDefinitionId)) {
            return payloadProcessDefinitionId;
        }
        ProcessInstance processInstance = getProcessInstance(resolveProcessInstanceId(request));
        return processInstance == null ? null : processInstance.getProcessDefinitionId();
    }

    private String resolveBusinessKey(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getBusinessKey())) {
            return request.getBusinessKey();
        }
        String payloadBusinessKey = getPayloadString(request.getPayload(), "businessKey");
        if (StringUtils.hasText(payloadBusinessKey)) {
            return payloadBusinessKey;
        }
        ProcessInstance processInstance = getProcessInstance(resolveProcessInstanceId(request));
        return processInstance == null ? null : processInstance.getBusinessKey();
    }

    private String resolveProcessDefinitionKey(WcdkProcessConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessDefinitionKey())) {
            return request.getProcessDefinitionKey();
        }
        String payloadProcessDefinitionKey = getPayloadString(request.getPayload(), "processDefinitionKey");
        if (StringUtils.hasText(payloadProcessDefinitionKey)) {
            return payloadProcessDefinitionKey;
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

    private String getPayloadString(Map<String, Object> payload, String key) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text : null;
    }
}


