package com.wcdk.proces.service.impl;

import com.alibaba.fastjson2.JSON;
import com.wcdk.proces.dto.DeploymentResponse;
import com.wcdk.proces.dto.ModelCreateRequest;
import com.wcdk.proces.dto.ModelResponse;
import com.wcdk.proces.dto.ModelUpdateRequest;
import com.wcdk.proces.service.FlowableModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableModelServiceImpl implements FlowableModelService {

    private final RepositoryService repositoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelResponse createModel(ModelCreateRequest request) {
        validateModelRequest(request.getModelName(), request.getModelKey(), request.getBpmnXml());
        Model model = repositoryService.newModel();
        model.setName(request.getModelName());
        model.setKey(request.getModelKey());
        model.setCategory(request.getCategory());
        model.setMetaInfo(buildMetaInfo(request.getModelName(), request.getModelKey(), request.getCategory()));
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), request.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        log.info("流程模型创建成功，模型ID：{}", model.getId());
        return buildModelResponse(getRequiredModel(model.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelResponse updateModel(String modelId, ModelUpdateRequest request) {
        Model model = getRequiredModel(modelId);
        if (StringUtils.hasText(request.getModelName())) {
            model.setName(request.getModelName());
        }
        model.setCategory(request.getCategory());
        model.setMetaInfo(buildMetaInfo(model.getName(), model.getKey(), model.getCategory()));
        repositoryService.saveModel(model);
        if (StringUtils.hasText(request.getBpmnXml())) {
            repositoryService.addModelEditorSource(modelId, request.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        }
        log.info("流程模型更新成功，模型ID：{}", modelId);
        return buildModelResponse(getRequiredModel(modelId));
    }

    @Override
    public List<ModelResponse> listModel() {
        return repositoryService.createModelQuery()
                .orderByCreateTime()
                .desc()
                .list()
                .stream()
                .map(this::buildModelResponse)
                .toList();
    }

    @Override
    public String getModelXml(String modelId) {
        Model model = getRequiredModel(modelId);
        byte[] source = repositoryService.getModelEditorSource(model.getId());
        if (source == null || source.length == 0) {
            throw new IllegalArgumentException("模型尚未保存 BPMN XML 内容");
        }
        return new String(source, StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeploymentResponse deployModel(String modelId) {
        Model model = getRequiredModel(modelId);
        byte[] source = repositoryService.getModelEditorSource(modelId);
        if (source == null || source.length == 0) {
            throw new IllegalArgumentException("模型尚未保存 BPMN XML 内容，无法部署");
        }
        String resourceName = model.getKey() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .category(model.getCategory())
                .addString(resourceName, new String(source, StandardCharsets.UTF_8))
                .deploy();
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);
        log.info("流程模型部署成功，模型ID：{}，部署ID：{}", modelId, deployment.getId());
        return DeploymentResponse.builder()
                .deploymentId(deployment.getId())
                .deploymentName(deployment.getName())
                .fileName(resourceName)
                .category(deployment.getCategory())
                .deployTime(deployment.getDeploymentTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(String modelId) {
        getRequiredModel(modelId);
        repositoryService.deleteModel(modelId);
        log.info("流程模型删除成功，模型ID：{}", modelId);
    }

    private void validateModelRequest(String modelName, String modelKey, String bpmnXml) {
        if (!StringUtils.hasText(modelName)) {
            throw new IllegalArgumentException("模型名称不能为空");
        }
        if (!StringUtils.hasText(modelKey)) {
            throw new IllegalArgumentException("模型标识不能为空");
        }
        if (!StringUtils.hasText(bpmnXml)) {
            throw new IllegalArgumentException("BPMN XML 内容不能为空");
        }
    }

    private Model getRequiredModel(String modelId) {
        Model model = repositoryService.getModel(modelId);
        if (model == null) {
            throw new IllegalArgumentException("未查询到对应模型，模型ID：" + modelId);
        }
        return model;
    }

    private String buildMetaInfo(String modelName, String modelKey, String category) {
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("name", modelName);
        metaInfo.put("key", modelKey);
        metaInfo.put("category", category);
        return JSON.toJSONString(metaInfo);
    }

    private ModelResponse buildModelResponse(Model model) {
        return ModelResponse.builder()
                .modelId(model.getId())
                .modelName(model.getName())
                .modelKey(model.getKey())
                .category(model.getCategory())
                .version(model.getVersion())
                .deploymentId(model.getDeploymentId())
                .createTime(model.getCreateTime())
                .lastUpdateTime(model.getLastUpdateTime())
                .build();
    }
}
