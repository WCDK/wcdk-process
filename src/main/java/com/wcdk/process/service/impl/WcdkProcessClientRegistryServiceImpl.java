package com.wcdk.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.WcdkProcessClientDefinition;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.dto.WcdkProcessClientResponse;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.entity.WcdkProcessClient;
import com.wcdk.process.entity.WcdkProcessClientProcess;
import com.wcdk.process.mapper.WcdkProcessClientProcessMapper;
import com.wcdk.process.mapper.WcdkProcessClientMapper;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class WcdkProcessClientRegistryServiceImpl implements WcdkProcessClientRegistryService {

    private static final String REGISTER_CALLBACK_PATH = "/wcdk_process/register_bak";

    private static final Duration CLIENT_DETECT_TIMEOUT = Duration.ofSeconds(5);

    private final WcdkProcessClientMapper wcdkProcessClientMapper;

    private final WcdkProcessClientProcessMapper wcdkProcessClientProcessMapper;

    private final ObjectProvider<WcdkProcessClientCallbackService> wcdkProcessClientCallbackServiceProvider;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(buildRequestFactory())
            .build();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(WcdkProcessClientRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("客户端注册请求不能为空");
        }
        if (!StringUtils.hasText(request.getClientId())) {
            throw new IllegalArgumentException("客户端标识不能为空");
        }
        if (!StringUtils.hasText(request.getClientName())) {
            throw new IllegalArgumentException("客户端名称不能为空");
        }
        if (!StringUtils.hasText(request.getCallbackUrl())) {
            throw new IllegalArgumentException("客户端回调地址不能为空");
        }
        String clientId = request.getClientId().trim();
        Set<String> processBeanNames = normalizeProcessBeanNames(request.getProcessBeanNames());
        LocalDateTime now = LocalDateTime.now();
        WcdkProcessClient client = wcdkProcessClientMapper.selectOne(new LambdaQueryWrapper<WcdkProcessClient>()
                .eq(WcdkProcessClient::getClientId, clientId)
                .last("LIMIT 1"));
        if (client == null) {
            client = WcdkProcessClient.builder()
                    .clientId(clientId)
                    .createTime(now)
                    .build();
        }
        client.setClientName(request.getClientName().trim());
        client.setCallbackUrl(request.getCallbackUrl().trim());
        client.setAuthFlg(request.getAuthFlg());
        client.setUpdateTime(now);
        if (client.getId() == null) {
            wcdkProcessClientMapper.insert(client);
        } else {
            wcdkProcessClientMapper.updateById(client);
        }

        wcdkProcessClientProcessMapper.delete(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getClientId, clientId)
                .isNull(WcdkProcessClientProcess::getProcessDefinitionId));
        for (String processBeanName : processBeanNames) {
            wcdkProcessClientProcessMapper.insert(WcdkProcessClientProcess.builder()
                    .clientId(clientId)
                    .processBeanName(processBeanName)
                    .createTime(now)
                    .build());
        }

        WcdkProcessClientCallbackService callbackService = wcdkProcessClientCallbackServiceProvider.getIfAvailable();
        if (callbackService != null) {
            callbackService.notifyRegisterSuccess(request);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindProcessDefinition(String processDefinitionId, String processBeanName, String processName) {
        if (!StringUtils.hasText(processDefinitionId) || !StringUtils.hasText(processBeanName)) {
            return;
        }
        String trimmedProcessDefinitionId = processDefinitionId.trim();
        String trimmedProcessBeanName = processBeanName.trim();
        String trimmedProcessName = StringUtils.hasText(processName) ? processName.trim() : null;
        List<WcdkProcessClientProcess> capabilityRows = wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessBeanName, trimmedProcessBeanName)
                .isNull(WcdkProcessClientProcess::getProcessDefinitionId));
        if (capabilityRows.isEmpty()) {
            return;
        }
        Set<String> clientIds = capabilityRows.stream()
                .map(WcdkProcessClientProcess::getClientId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (clientIds.isEmpty()) {
            return;
        }
        wcdkProcessClientProcessMapper.delete(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, trimmedProcessDefinitionId));
        LocalDateTime now = LocalDateTime.now();
        for (String clientId : clientIds) {
            wcdkProcessClientProcessMapper.insert(WcdkProcessClientProcess.builder()
                    .clientId(clientId)
                    .processBeanName(trimmedProcessBeanName)
                    .processDefinitionId(trimmedProcessDefinitionId)
                    .processName(trimmedProcessName)
                    .createTime(now)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindProcessDefinition(String clientId, String processDefinitionId, String processBeanName, String processName) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(processDefinitionId) || !StringUtils.hasText(processBeanName)) {
            return;
        }
        String trimmedClientId = clientId.trim();
        String trimmedProcessDefinitionId = processDefinitionId.trim();
        String trimmedProcessBeanName = processBeanName.trim();
        String trimmedProcessName = StringUtils.hasText(processName) ? processName.trim() : null;
        wcdkProcessClientProcessMapper.delete(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, trimmedProcessDefinitionId));
        wcdkProcessClientProcessMapper.insert(WcdkProcessClientProcess.builder()
                .clientId(trimmedClientId)
                .processBeanName(trimmedProcessBeanName)
                .processDefinitionId(trimmedProcessDefinitionId)
                .processName(trimmedProcessName)
                .createTime(LocalDateTime.now())
                .build());
    }

    @Override
    public boolean hasProcessBinding(String clientId, String processDefinitionId) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(processDefinitionId)) {
            return false;
        }
        Long count = wcdkProcessClientProcessMapper.selectCount(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getClientId, clientId.trim())
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, processDefinitionId.trim()));
        return count != null && count > 0;
    }

    @Override
    public List<WcdkProcessClientDefinition> listByProcessDefinitionId(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return List.of();
        }
        List<WcdkProcessClientProcess> clientProcesses = wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getProcessDefinitionId, processDefinitionId.trim()));
        if (clientProcesses.isEmpty()) {
            return List.of();
        }
        Set<String> clientIds = clientProcesses.stream()
                .map(WcdkProcessClientProcess::getClientId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (clientIds.isEmpty()) {
            return List.of();
        }
        List<WcdkProcessClient> clients = wcdkProcessClientMapper.selectList(new LambdaQueryWrapper<WcdkProcessClient>()
                .in(WcdkProcessClient::getClientId, clientIds));
        return clients.stream()
                .map(this::buildClientDefinition)
                .toList();
    }

    @Override
    public List<String> listProcessBeanNameByClientId(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            return List.of();
        }
        return wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                        .eq(WcdkProcessClientProcess::getClientId, clientId.trim())
                        .isNull(WcdkProcessClientProcess::getProcessDefinitionId))
                .stream()
                .map(WcdkProcessClientProcess::getProcessBeanName)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public PageResponse<WcdkProcessClientResponse> listClientOption(long pageNum, long pageSize, String clientId, String clientName) {
        LambdaQueryWrapper<WcdkProcessClient> queryWrapper = new LambdaQueryWrapper<WcdkProcessClient>()
                .like(StringUtils.hasText(clientId), WcdkProcessClient::getClientId, clientId == null ? null : clientId.trim())
                .like(StringUtils.hasText(clientName), WcdkProcessClient::getClientName, clientName == null ? null : clientName.trim())
                .orderByAsc(WcdkProcessClient::getClientId);
        Page<WcdkProcessClient> page = wcdkProcessClientMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        return new PageResponse<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords().stream()
                        .map(client -> buildClientResponse(client, List.of()))
                        .toList()
        );
    }

    @Override
    public PageResponse<WcdkProcessClientResponse> listClient(long pageNum,
                                                             long pageSize,
                                                             String clientId,
                                                             String clientName,
                                                             String callbackUrl,
                                                             String processBeanName,
                                                             String sortProp,
                                                             String sortOrder) {
        Set<String> processBeanClientIds = resolveClientIdsByProcessBeanName(processBeanName);
        if (StringUtils.hasText(processBeanName) && processBeanClientIds.isEmpty()) {
            return new PageResponse<>(0L, pageNum, pageSize, List.of());
        }
        LambdaQueryWrapper<WcdkProcessClient> queryWrapper = new LambdaQueryWrapper<WcdkProcessClient>()
                .like(StringUtils.hasText(clientId), WcdkProcessClient::getClientId, clientId == null ? null : clientId.trim())
                .like(StringUtils.hasText(clientName), WcdkProcessClient::getClientName, clientName == null ? null : clientName.trim())
                .like(StringUtils.hasText(callbackUrl), WcdkProcessClient::getCallbackUrl, callbackUrl == null ? null : callbackUrl.trim())
                .in(!processBeanClientIds.isEmpty(), WcdkProcessClient::getClientId, processBeanClientIds);
        applyClientSort(queryWrapper, sortProp, sortOrder);
        Page<WcdkProcessClient> page = wcdkProcessClientMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        List<String> clientIds = page.getRecords().stream()
                .map(WcdkProcessClient::getClientId)
                .filter(StringUtils::hasText)
                .toList();
        Map<String, List<WcdkProcessClientProcess>> processMap = listClientProcessMap(clientIds);
        return new PageResponse<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords().stream()
                        .map(client -> buildClientResponse(client, processMap.getOrDefault(client.getClientId(), List.of())))
                        .toList()
        );
    }

    @Override
    public boolean detectClient(String clientId) {
        WcdkProcessClient client = getRequiredClient(clientId);
        String callbackUrl = buildRegisterCallbackUrl(client.getCallbackUrl());
        WcdkProcessConnectionEvent event = WcdkProcessConnectionEvent.builder()
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .processBeanName("register_bak")
                .eventType("CLIENT_HEALTH_CHECK")
                .message("客户端存活检测")
                .eventTime(LocalDateTime.now())
                .build();
        try {
            restClient.post()
                    .uri(callbackUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (StringUtils.hasText(client.getAuthFlg())) {
                            headers.set("WCDK_AUTH", client.getAuthFlg().trim());
                        }
                    })
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException exception) {
            log.warn("客户端存活检测失败，clientId={}, callbackUrl={}", client.getClientId(), callbackUrl, exception);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeClient(String clientId) {
        WcdkProcessClient client = getRequiredClient(clientId);
        wcdkProcessClientProcessMapper.delete(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                .eq(WcdkProcessClientProcess::getClientId, client.getClientId()));
        wcdkProcessClientMapper.deleteById(client.getId());
    }

    private Set<String> normalizeProcessBeanNames(Set<String> processBeanNames) {
        if (processBeanNames == null || processBeanNames.isEmpty()) {
            return Set.of();
        }
        return processBeanNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private WcdkProcessClientDefinition buildClientDefinition(WcdkProcessClient client) {
        return WcdkProcessClientDefinition.builder()
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .callbackUrl(client.getCallbackUrl())
                .callbackHeaders(readHeaders(client.getAuthFlg()))
                .build();
    }

    private static SimpleClientHttpRequestFactory buildRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CLIENT_DETECT_TIMEOUT);
        requestFactory.setReadTimeout(CLIENT_DETECT_TIMEOUT);
        return requestFactory;
    }

    private WcdkProcessClient getRequiredClient(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("客户端标识不能为空");
        }
        WcdkProcessClient client = wcdkProcessClientMapper.selectOne(new LambdaQueryWrapper<WcdkProcessClient>()
                .eq(WcdkProcessClient::getClientId, clientId.trim())
                .last("LIMIT 1"));
        if (client == null) {
            throw new IllegalArgumentException("未查询到客户端注册信息，客户端标识：" + clientId);
        }
        return client;
    }

    private String buildRegisterCallbackUrl(String callbackUrl) {
        if (!StringUtils.hasText(callbackUrl)) {
            throw new IllegalArgumentException("客户端回调地址不能为空");
        }
        String normalizedUrl = callbackUrl.trim();
        while (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }
        if (normalizedUrl.endsWith(REGISTER_CALLBACK_PATH)) {
            return normalizedUrl;
        }
        return normalizedUrl + REGISTER_CALLBACK_PATH;
    }

    private Set<String> resolveClientIdsByProcessBeanName(String processBeanName) {
        if (!StringUtils.hasText(processBeanName)) {
            return Set.of();
        }
        return wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                        .like(WcdkProcessClientProcess::getProcessBeanName, processBeanName.trim()))
                .stream()
                .map(WcdkProcessClientProcess::getClientId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private void applyClientSort(LambdaQueryWrapper<WcdkProcessClient> queryWrapper, String sortProp, String sortOrder) {
        boolean ascending = "ascending".equals(sortOrder);
        if ("clientId".equals(sortProp)) {
            queryWrapper.orderBy(true, ascending, WcdkProcessClient::getClientId);
            return;
        }
        if ("clientName".equals(sortProp)) {
            queryWrapper.orderBy(true, ascending, WcdkProcessClient::getClientName);
            return;
        }
        if ("createTime".equals(sortProp)) {
            queryWrapper.orderBy(true, ascending, WcdkProcessClient::getCreateTime);
            return;
        }
        queryWrapper.orderBy(true, ascending, WcdkProcessClient::getUpdateTime);
    }

    private Map<String, List<WcdkProcessClientProcess>> listClientProcessMap(List<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return Map.of();
        }
        return wcdkProcessClientProcessMapper.selectList(new LambdaQueryWrapper<WcdkProcessClientProcess>()
                        .in(WcdkProcessClientProcess::getClientId, clientIds))
                .stream()
                .collect(Collectors.groupingBy(WcdkProcessClientProcess::getClientId));
    }

    private WcdkProcessClientResponse buildClientResponse(WcdkProcessClient client, List<WcdkProcessClientProcess> clientProcesses) {
        List<WcdkProcessClientProcess> processRows = clientProcesses == null ? List.of() : clientProcesses;
        List<String> processBeanNames = processRows.stream()
                .map(WcdkProcessClientProcess::getProcessBeanName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        List<String> processNames = processRows.stream()
                .map(WcdkProcessClientProcess::getProcessName)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        long processBindingCount = processRows.stream()
                .filter(row -> StringUtils.hasText(row.getProcessDefinitionId()))
                .count();
        return WcdkProcessClientResponse.builder()
                .id(client.getId())
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .callbackUrl(client.getCallbackUrl())
                .authFlg(client.getAuthFlg())
                .clientStatus("未检测")
                .processBeanNames(processBeanNames)
                .processNames(processNames)
                .processBeanCount((long) processBeanNames.size())
                .processBindingCount(processBindingCount)
                .createTime(client.getCreateTime())
                .updateTime(client.getUpdateTime())
                .build();
    }

    private Map<String, String> readHeaders(String authFlg) {
        if (!StringUtils.hasText(authFlg)) {
            return Map.of();
        }
        Map<String, String> header = new HashMap<>();
        header.put("WCDK_AUTH", authFlg);
        return header;
    }
}
