package com.wcdk.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wcdk.process.dto.WcdkProcessClientDefinition;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.entity.WcdkProcessClient;
import com.wcdk.process.entity.WcdkProcessClientProcess;
import com.wcdk.process.mapper.WcdkProcessClientProcessMapper;
import com.wcdk.process.mapper.WcdkProcessClientMapper;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
public class WcdkProcessClientRegistryServiceImpl implements WcdkProcessClientRegistryService {

    private final WcdkProcessClientMapper wcdkProcessClientMapper;

    private final WcdkProcessClientProcessMapper wcdkProcessClientProcessMapper;

    private final ObjectProvider<WcdkProcessClientCallbackService> wcdkProcessClientCallbackServiceProvider;

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

    private Map<String, String> readHeaders(String authFlg) {
        if (!StringUtils.hasText(authFlg)) {
            return Map.of();
        }
        Map<String, String> header = new HashMap<>();
        header.put("WCDK_AUTH", authFlg);
        return header;
    }
}


