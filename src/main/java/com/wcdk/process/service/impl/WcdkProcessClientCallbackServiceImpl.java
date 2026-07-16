package com.wcdk.process.service.impl;

import com.wcdk.process.dto.WcdkProcessClientDefinition;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class WcdkProcessClientCallbackServiceImpl implements WcdkProcessClientCallbackService {

    private static final String REGISTER_CALLBACK_PATH = "/wcdk_process/register_bak";

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    @Override
    public void callback(WcdkProcessConnectionEvent event) {
        if (event == null || !StringUtils.hasText(event.getProcessDefinitionId())) {
            return;
        }
        List<WcdkProcessClientDefinition> clientDefinitions = wcdkProcessClientRegistryService.listByProcessDefinitionId(event.getProcessDefinitionId());
        if (clientDefinitions.isEmpty()) {
            log.info("δ�ҵ��ɽ������̻ص��Ŀͻ��ˣ�processBeanName={}", event.getProcessBeanName());
            return;
        }
        for (WcdkProcessClientDefinition clientDefinition : clientDefinitions) {
            try {
                RestClient.create().post()
                        .uri(clientDefinition.getCallbackUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(headers -> applyHeaders(headers, clientDefinition))
                        .body(fillClientInfo(event, clientDefinition))
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception exception) {
                log.warn("���̻ص��ͻ���ʧ�ܣ�clientId={}, callbackUrl={}", clientDefinition.getClientId(), clientDefinition.getCallbackUrl(), exception);
            }
        }
    }

    @Override
    public void notifyRegisterSuccess(WcdkProcessClientRegisterRequest request) {
        if (request == null || !StringUtils.hasText(request.getCallbackUrl())) {
            return;
        }
        String callbackUrl = buildRegisterCallbackUrl(request.getCallbackUrl());
        WcdkProcessConnectionEvent event = WcdkProcessConnectionEvent.builder()
                .clientId(request.getClientId())
                .clientName(request.getClientName())
                .processBeanName("register_bak")
                .eventType("REGISTER_SUCCESS")
                .message("�ͻ���ע��ɹ�")
                .eventTime(LocalDateTime.now())
                .build();
        try {
            RestClient.create().post()
                    .uri(callbackUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> applyRegisterHeaders(headers, request))
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            log.warn("�ͻ���ע��ɹ��ص�ʧ�ܣ�clientId={}, callbackUrl={}", request.getClientId(), callbackUrl, exception);
        }
    }

    private void applyHeaders(HttpHeaders headers, WcdkProcessClientDefinition clientDefinition) {
        if (clientDefinition.getCallbackHeaders() == null || clientDefinition.getCallbackHeaders().isEmpty()) {
            return;
        }
        clientDefinition.getCallbackHeaders().forEach(headers::set);
    }

    private void applyRegisterHeaders(HttpHeaders headers, WcdkProcessClientRegisterRequest request) {
        buildRegisterHeaders(request).forEach(headers::set);
    }

    private Map<String, String> buildRegisterHeaders(WcdkProcessClientRegisterRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (request.getRequestHeaderParams() != null && !request.getRequestHeaderParams().isEmpty()) {
            headers.putAll(request.getRequestHeaderParams());
        }
        if (StringUtils.hasText(request.getAuthFlg())) {
            headers.put("WCDK_AUTH", request.getAuthFlg().trim());
        }
        return headers;
    }

    private String buildRegisterCallbackUrl(String callbackUrl) {
        String normalizedUrl = callbackUrl.trim();
        while (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        if (normalizedUrl.endsWith(REGISTER_CALLBACK_PATH)) {
            return normalizedUrl;
        }
        return normalizedUrl + REGISTER_CALLBACK_PATH;
    }

    private WcdkProcessConnectionEvent fillClientInfo(WcdkProcessConnectionEvent source, WcdkProcessClientDefinition clientDefinition) {
        return WcdkProcessConnectionEvent.builder()
                .connectionId(source.getConnectionId())
                .clientId(clientDefinition.getClientId())
                .clientName(clientDefinition.getClientName())
                .processInstanceId(source.getProcessInstanceId())
                .processDefinitionId(source.getProcessDefinitionId())
                .processDefinitionKey(source.getProcessDefinitionKey())
                .businessKey(source.getBusinessKey())
                .processBeanName(source.getProcessBeanName())
                .eventType(source.getEventType())
                .message(source.getMessage())
                .eventTime(source.getEventTime() == null ? LocalDateTime.now() : source.getEventTime())
                .payload(source.getPayload())
                .errorMessage(source.getErrorMessage())
                .build();
    }
}


