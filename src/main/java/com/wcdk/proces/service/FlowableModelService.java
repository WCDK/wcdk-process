package com.wcdk.proces.service;

import com.wcdk.proces.dto.DeploymentResponse;
import com.wcdk.proces.dto.ModelCreateRequest;
import com.wcdk.proces.dto.ModelResponse;
import com.wcdk.proces.dto.ModelUpdateRequest;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
public interface FlowableModelService {

    ModelResponse createModel(ModelCreateRequest request);

    ModelResponse updateModel(String modelId, ModelUpdateRequest request);

    List<ModelResponse> listModel();

    String getModelXml(String modelId);

    DeploymentResponse deployModel(String modelId);

    void deleteModel(String modelId);
}
