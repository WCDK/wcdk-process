package com.wcdk.proces.service.impl;

import com.wcdk.proces.dto.WcdkProcesConnectionEvent;
import com.wcdk.proces.service.WcdkProcesCallbackService;
import com.wcdk.proces.support.ProcesBeanRegistry;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * SDK HTTP 回调处理实现。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Service
@RequiredArgsConstructor
public class WcdkProcesCallbackServiceImpl implements WcdkProcesCallbackService {

    public static final String PROCESS_BEAN_NAME = "processBeanName";

    private final RuntimeService runtimeService;

    private final ProcesBeanRegistry procesBeanRegistry;

    @Override
    public void callback(WcdkProcesConnectionEvent request) {
        if (request == null) {
            throw new IllegalArgumentException("流程回调请求不能为空");
        }
        String processBeanName = resolveProcessBeanName(request);
        WcdkProcesConnectionEvent event = WcdkProcesConnectionEvent.builder()
                .connectionId(request.getConnectionId())
                .clientId(request.getClientId())
                .clientName(request.getClientName())
                .processInstanceId(resolveProcessInstanceId(request))
                .processDefinitionKey(resolveProcessDefinitionKey(request))
                .businessKey(resolveBusinessKey(request))
                .processBeanName(processBeanName)
                .eventType(request.getEventType())
                .message(request.getMessage())
                .eventTime(request.getEventTime())
                .payload(request.getPayload())
                .errorMessage(request.getErrorMessage())
                .build();
        procesBeanRegistry.invoke(processBeanName, event);
    }

    private String resolveProcessBeanName(WcdkProcesConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessBeanName())) {
            return request.getProcessBeanName();
        }
        String payloadProcessBeanName = getPayloadString(request.getPayload(), PROCESS_BEAN_NAME);
        if (StringUtils.hasText(payloadProcessBeanName)) {
            return payloadProcessBeanName;
        }
        String processInstanceId = resolveProcessInstanceId(request);
        if (!StringUtils.hasText(processInstanceId)) {
            throw new IllegalArgumentException("流程回调时必须提供 processBeanName 或 processInstanceId");
        }
        Object variableValue = runtimeService.getVariable(processInstanceId, PROCESS_BEAN_NAME);
        if (variableValue == null || !StringUtils.hasText(String.valueOf(variableValue))) {
            throw new IllegalArgumentException("流程实例未绑定流程回调 bean 名称");
        }
        return String.valueOf(variableValue);
    }

    private String resolveProcessInstanceId(WcdkProcesConnectionEvent request) {
        if (StringUtils.hasText(request.getProcessInstanceId())) {
            return request.getProcessInstanceId();
        }
        return getPayloadString(request.getPayload(), "processInstanceId");
    }

    private String resolveBusinessKey(WcdkProcesConnectionEvent request) {
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

    private String resolveProcessDefinitionKey(WcdkProcesConnectionEvent request) {
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
