package com.wcdk.process.service;

import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ModelUpdateRequest;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
public interface FlowableModelService {

    ModelResponse createModel(ModelCreateRequest request);

    ModelResponse updateModel(String modelId, ModelUpdateRequest request);

    List<ModelResponse> listModel(String modelName, String modelKey, String category, String deployed);

    String getModelXml(String modelId);

    DeploymentResponse deployModel(String modelId, String clientId, String processBeanName);

    void deleteModel(String modelId);
}
