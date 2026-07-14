package com.wcdk.proces.service.impl;

import com.wcdk.proces.dto.WcdkProcesClientRegisterRequest;
import com.wcdk.proces.service.WcdkProcesClientRegistryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WcdkProcesClientRegistryServiceImpl implements WcdkProcesClientRegistryService {

    private final Map<String, Set<String>> clientProcessBeans = new ConcurrentHashMap<>();

    @Override
    public void register(WcdkProcesClientRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("客户端注册请求不能为空");
        }
        if (!StringUtils.hasText(request.getClientId())) {
            throw new IllegalArgumentException("客户端 clientId 不能为空");
        }
        if (request.getProcessBeanNames() == null || request.getProcessBeanNames().isEmpty()) {
            throw new IllegalArgumentException("客户端至少需要注册一个 ProcesBean");
        }
        clientProcessBeans.put(request.getClientId(), new LinkedHashSet<>(request.getProcessBeanNames()));
    }
}
