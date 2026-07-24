package com.wcdk.process.service.impl;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.config.WcdkProcessNacosProperties;
import com.wcdk.process.dto.WcdkProcessClientDefinition;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.dto.WcdkProcessRpcCallbackResponse;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.time.Duration;
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

    private static final Duration CALLBACK_TIMEOUT = Duration.ofSeconds(5);

    private static final ParameterizedTypeReference<ApiResponse<Object>> RPC_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    private final ObjectProvider<LoadBalancerClient> loadBalancerClientProvider;

    private final WcdkProcessNacosProperties wcdkProcessNacosProperties;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(buildRequestFactory())
            .build();

    @Override
    public void callback(WcdkProcessConnectionEvent event) {
        if (event == null || !StringUtils.hasText(event.getProcessDefinitionId())) {
            return;
        }
        List<WcdkProcessClientDefinition> clientDefinitions = wcdkProcessClientRegistryService.listByProcessDefinitionId(event.getProcessDefinitionId());
        if (clientDefinitions.isEmpty()) {
            log.info("未找到可接收流程回调的客户端，processBeanName={}", event.getProcessBeanName());
            return;
        }
        for (WcdkProcessClientDefinition clientDefinition : clientDefinitions) {
            try {
                invokeClient(clientDefinition, event);
            } catch (RestClientResponseException exception) {
                log.warn("流程回调客户端失败，clientId={}, callbackUrl={}, statusCode={}, responseBody={}",
                        clientDefinition.getClientId(), clientDefinition.getCallbackUrl(),
                        exception.getStatusCode(), exception.getResponseBodyAsString(), exception);
            } catch (Exception exception) {
                log.warn("流程回调客户端失败，clientId={}, callbackUrl={}", clientDefinition.getClientId(), clientDefinition.getCallbackUrl(), exception);
            }
        }
    }

    @Override
    public List<WcdkProcessRpcCallbackResponse> rpcCallback(WcdkProcessConnectionEvent event) {
        if (event == null || !StringUtils.hasText(event.getProcessDefinitionId())) {
            throw new IllegalArgumentException("RPC回调必须提供流程定义ID");
        }
        List<WcdkProcessClientDefinition> clientDefinitions = wcdkProcessClientRegistryService.listByProcessDefinitionId(event.getProcessDefinitionId());
        if (clientDefinitions.isEmpty()) {
            return List.of();
        }
        List<WcdkProcessRpcCallbackResponse> responses = new ArrayList<>();
        for (WcdkProcessClientDefinition clientDefinition : clientDefinitions) {
            String processBeanName = resolveProcessBeanName(event, clientDefinition);
            try {
                ApiResponse<Object> response = invokeClient(clientDefinition, event);
                responses.add(WcdkProcessRpcCallbackResponse.builder()
                        .clientId(clientDefinition.getClientId())
                        .clientName(clientDefinition.getClientName())
                        .processBeanName(processBeanName)
                        .success(response != null && response.getCode() != null && response.getCode() == 200)
                        .data(response == null ? null : response.getData())
                        .message(response == null ? "客户端未返回内容" : response.getMessage())
                        .build());
            } catch (RestClientResponseException exception) {
                log.warn("RPC回调客户端失败，clientId={}, callbackUrl={}, statusCode={}, responseBody={}",
                        clientDefinition.getClientId(), clientDefinition.getCallbackUrl(),
                        exception.getStatusCode(), exception.getResponseBodyAsString(), exception);
                responses.add(buildFailedRpcResponse(clientDefinition, processBeanName, exception.getResponseBodyAsString()));
            } catch (Exception exception) {
                log.warn("RPC回调客户端失败，clientId={}, callbackUrl={}", clientDefinition.getClientId(), clientDefinition.getCallbackUrl(), exception);
                responses.add(buildFailedRpcResponse(clientDefinition, processBeanName, exception.getMessage()));
            }
        }
        return responses;
    }

    @Override
    public void notifyRegisterSuccess(WcdkProcessClientRegisterRequest request) {
        if (request == null || (!StringUtils.hasText(request.getCallbackUrl()) && !StringUtils.hasText(request.getServiceName()))) {
            return;
        }
        String callbackUrl = buildRegisterCallbackUrl(request);
        WcdkProcessConnectionEvent event = WcdkProcessConnectionEvent.builder()
                .clientId(request.getClientId())
                .clientName(request.getClientName())
                .processBeanName("register_bak")
                .eventType("REGISTER_SUCCESS")
                .message("客户端注册成功")
                .eventTime(LocalDateTime.now())
                .build();
        try {
            restClient.post()
                    .uri(callbackUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> applyRegisterHeaders(headers, request))
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            log.warn("客户端注册成功回调失败，clientId={}, callbackUrl={}, statusCode={}, responseBody={}",
                    request.getClientId(), callbackUrl, exception.getStatusCode(), exception.getResponseBodyAsString(), exception);
        } catch (Exception exception) {
            log.warn("客户端注册成功回调失败，clientId={}, callbackUrl={}", request.getClientId(), callbackUrl, exception);
        }
    }

    private static SimpleClientHttpRequestFactory buildRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CALLBACK_TIMEOUT);
        requestFactory.setReadTimeout(CALLBACK_TIMEOUT);
        return requestFactory;
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

    private String buildRegisterCallbackUrl(WcdkProcessClientRegisterRequest request) {
        String normalizedUrl = resolveCallbackBaseUrl(request.getServiceName(), request.getCallbackUrl());
        if (normalizedUrl.endsWith(REGISTER_CALLBACK_PATH)) {
            return normalizedUrl;
        }
        return normalizedUrl + REGISTER_CALLBACK_PATH;
    }

    private WcdkProcessConnectionEvent fillClientInfo(WcdkProcessConnectionEvent source, WcdkProcessClientDefinition clientDefinition) {
        String processBeanName = resolveProcessBeanName(source, clientDefinition);
        return WcdkProcessConnectionEvent.builder()
                .connectionId(source.getConnectionId())
                .clientId(clientDefinition.getClientId())
                .clientName(clientDefinition.getClientName())
                .processInstanceId(source.getProcessInstanceId())
                .processDefinitionId(source.getProcessDefinitionId())
                .processDefinitionKey(source.getProcessDefinitionKey())
                .processDefinitionName(source.getProcessDefinitionName())
                .businessKey(source.getBusinessKey())
                .approvalId(source.getApprovalId())
                .approvalName(source.getApprovalName())
                .currentTaskId(source.getCurrentTaskId())
                .currentTaskName(source.getCurrentTaskName())
                .currentTasks(source.getCurrentTasks())
                .taskApproved(source.getTaskApproved())
                .taskApprovalResult(source.getTaskApprovalResult())
                .currentApprovalResult(source.getCurrentApprovalResult())
                .relatedFormData(source.getRelatedFormData())
                .relatedFormId(source.getRelatedFormId())
                .relatedFormName(source.getRelatedFormName())
                .relatedForms(source.getRelatedForms())
                .nextTaskId(source.getNextTaskId())
                .nextTaskName(source.getNextTaskName())
                .nextTasks(source.getNextTasks())
                .processBeanName(processBeanName)
                .eventType(source.getEventType())
                .message(source.getMessage())
                .eventTime(source.getEventTime() == null ? LocalDateTime.now() : source.getEventTime())
                .errorMessage(source.getErrorMessage())
                .build();
    }

    private ApiResponse<Object> invokeClient(WcdkProcessClientDefinition clientDefinition, WcdkProcessConnectionEvent event) {
        String processBeanName = resolveProcessBeanName(event, clientDefinition);
        if (!StringUtils.hasText(processBeanName)) {
            throw new IllegalArgumentException("流程处理器不能为空");
        }
        return restClient.post()
                .uri(buildProcessBeanCallbackUrl(clientDefinition.getServiceName(), clientDefinition.getCallbackUrl(), processBeanName))
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> applyHeaders(headers, clientDefinition))
                .body(fillClientInfo(event, clientDefinition))
                .retrieve()
                .body(RPC_RESPONSE_TYPE);
    }

    private String buildProcessBeanCallbackUrl(String callbackUrl, String processBeanName) {
        return buildProcessBeanCallbackUrl(null, callbackUrl, processBeanName);
    }

    private String buildProcessBeanCallbackUrl(String serviceName, String callbackUrl, String processBeanName) {
        String normalizedUrl = resolveCallbackBaseUrl(serviceName, callbackUrl);
        return normalizedUrl + "/wcdk_process/" + processBeanName.trim();
    }

    private String resolveCallbackBaseUrl(String serviceName, String callbackUrl) {
        if (Boolean.TRUE.equals(wcdkProcessNacosProperties.getEnabled()) && StringUtils.hasText(serviceName)) {
            LoadBalancerClient loadBalancerClient = loadBalancerClientProvider.getIfAvailable();
            if (loadBalancerClient == null) {
                throw new IllegalStateException("已开启Nacos服务名回调，但未找到LoadBalancerClient");
            }
            ServiceInstance serviceInstance = loadBalancerClient.choose(serviceName.trim());
            if (serviceInstance == null) {
                throw new IllegalStateException("未从注册中心找到服务实例：" + serviceName.trim());
            }
            return trimTrailingSlash(serviceInstance.getUri().toString());
        }
        if (!StringUtils.hasText(callbackUrl)) {
            throw new IllegalArgumentException("客户端回调地址不能为空");
        }
        return trimTrailingSlash(callbackUrl);
    }

    private String trimTrailingSlash(String value) {
        String normalizedUrl = value.trim();
        while (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        return normalizedUrl;
    }

    private WcdkProcessRpcCallbackResponse buildFailedRpcResponse(WcdkProcessClientDefinition clientDefinition,
                                                                  String processBeanName,
                                                                  String message) {
        return WcdkProcessRpcCallbackResponse.builder()
                .clientId(clientDefinition.getClientId())
                .clientName(clientDefinition.getClientName())
                .processBeanName(processBeanName)
                .success(false)
                .message(StringUtils.hasText(message) ? message : "RPC回调客户端失败")
                .build();
    }

    private String resolveProcessBeanName(WcdkProcessConnectionEvent source, WcdkProcessClientDefinition clientDefinition) {
        if (source != null && StringUtils.hasText(source.getProcessBeanName())) {
            return source.getProcessBeanName().trim();
        }
        if (clientDefinition.getProcessBeanNames() == null || clientDefinition.getProcessBeanNames().isEmpty()) {
            return null;
        }
        return clientDefinition.getProcessBeanNames()
                .stream()
                .filter(StringUtils::hasText)
                .findFirst()
                .map(String::trim)
                .orElse(null);
    }
}
