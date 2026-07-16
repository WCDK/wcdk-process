package com.wcdk.process.service;

import com.wcdk.process.dto.WcdkProcessClientDefinition;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public interface WcdkProcessClientRegistryService {

    void register(WcdkProcessClientRegisterRequest request);

    void bindProcessDefinition(String processDefinitionId, String processBeanName, String processName);

    boolean hasProcessBinding(String clientId, String processDefinitionId);

    List<WcdkProcessClientDefinition> listByProcessDefinitionId(String processDefinitionId);
}

