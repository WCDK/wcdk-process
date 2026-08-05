package com.wcdk.process.service.impl;

import com.wcdk.process.common.json.JSON;
import com.wcdk.process.common.json.JsonObject;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ModelUpdateRequest;
import com.wcdk.process.service.FlowableModelService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    private static final String PROCESS_BEAN_NAME = "processBeanName";

    private final RepositoryService repositoryService;

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelResponse createModel(ModelCreateRequest request) {
        validateModelRequest(request.getModelName(), request.getModelKey(), request.getBpmnXml());
        validateProcessDefinitionId(request.getModelKey(), request.getBpmnXml());
        Model model = repositoryService.newModel();
        model.setName(request.getModelName());
        model.setKey(request.getModelKey());
        model.setCategory(request.getCategory());
        model.setMetaInfo(buildMetaInfo(request.getModelName(), request.getModelKey(), request.getCategory(), null));
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
        model.setMetaInfo(buildMetaInfo(model.getName(),
                model.getKey(),
                model.getCategory(),
                readProcessBeanName(model.getMetaInfo())));
        repositoryService.saveModel(model);
        if (StringUtils.hasText(request.getBpmnXml())) {
            repositoryService.addModelEditorSource(modelId, request.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        }
        log.info("流程模型更新成功，模型ID：{}", modelId);
        return buildModelResponse(getRequiredModel(modelId));
    }

    @Override
    public List<ModelResponse> listModel(String modelName, String modelKey, String category, String deployed) {
        return repositoryService.createModelQuery()
                .orderByCreateTime()
                .desc()
                .list()
                .stream()
                .map(this::buildModelResponse)
                .filter(model -> matchesModelName(model, modelName))
                .filter(model -> matchesModelKey(model, modelKey))
                .filter(model -> matchesCategory(model, category))
                .filter(model -> matchesDeployed(model, deployed))
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
    public DeploymentResponse deployModel(String modelId, String clientId, String processBeanName) {
        Model model = getRequiredModel(modelId);
        String validClientId = normalizeOptionalValue(clientId);
        String validProcessBeanName = normalizeOptionalValue(processBeanName);
        validateDeployBinding(validClientId, validProcessBeanName);
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
        if (StringUtils.hasText(validClientId) && StringUtils.hasText(validProcessBeanName)) {
            repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .list()
                    .forEach(processDefinition -> wcdkProcessClientRegistryService.bindProcessDefinition(
                            validClientId,
                            processDefinition.getId(),
                            validProcessBeanName,
                            processDefinition.getName()));
        }
        model.setDeploymentId(deployment.getId());
        model.setMetaInfo(buildMetaInfo(model.getName(), model.getKey(), model.getCategory(), validProcessBeanName));
        repositoryService.saveModel(model);
        log.info("流程模型部署成功，模型ID：{}，部署ID：{}，processBean：{}", modelId, deployment.getId(), validProcessBeanName);
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

    private void validateProcessDefinitionId(String modelKey, String bpmnXml) {
        BpmnModel bpmnModel = parseBpmnModel(bpmnXml);
        Process mainProcess = bpmnModel == null ? null : bpmnModel.getMainProcess();
        if (mainProcess == null || !StringUtils.hasText(mainProcess.getId())) {
            throw new IllegalArgumentException("模型源码缺少流程定义ID，请检查 BPMN 中的 process id");
        }
        String processDefinitionId = mainProcess.getId().trim();
        if (!processDefinitionId.equals(modelKey.trim())) {
            throw new IllegalArgumentException("流程定义ID必须与模型标识一致，当前流程定义ID：" + processDefinitionId);
        }
    }

    private BpmnModel parseBpmnModel(String bpmnXml) {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8))) {
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream, StandardCharsets.UTF_8.name());
            return new BpmnXMLConverter().convertToBpmnModel(streamReader);
        } catch (XMLStreamException exception) {
            throw new IllegalArgumentException("模型源码格式不正确，请检查 BPMN XML 内容", exception);
        } catch (IOException exception) {
            throw new IllegalArgumentException("读取 BPMN XML 内容失败", exception);
        }
    }

    private String validateProcessBeanName(String processBeanName) {
        if (!StringUtils.hasText(processBeanName)) {
            throw new IllegalArgumentException("部署流程模型时必须指定processBean");
        }
        return processBeanName.trim();
    }

    private String normalizeOptionalValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void validateDeployBinding(String clientId, String processBeanName) {
        if (!StringUtils.hasText(clientId) && !StringUtils.hasText(processBeanName)) {
            return;
        }
        if (!StringUtils.hasText(clientId)) {
            throw new IllegalArgumentException("选择processName时必须选择客户端");
        }
        if (!StringUtils.hasText(processBeanName)) {
            throw new IllegalArgumentException("选择客户端时必须选择processName");
        }
        List<String> processBeanNames = wcdkProcessClientRegistryService.listProcessBeanNameByClientId(clientId);
        if (!processBeanNames.contains(processBeanName)) {
            throw new IllegalArgumentException("当前客户端未注册该processName");
        }
    }

    private boolean matchesModelName(ModelResponse model, String modelName) {
        return !StringUtils.hasText(modelName) || containsIgnoreCase(model.getModelName(), modelName);
    }

    private boolean matchesModelKey(ModelResponse model, String modelKey) {
        return !StringUtils.hasText(modelKey) || containsIgnoreCase(model.getModelKey(), modelKey);
    }

    private boolean matchesCategory(ModelResponse model, String category) {
        return !StringUtils.hasText(category) || containsIgnoreCase(model.getCategory(), category);
    }

    private boolean matchesDeployed(ModelResponse model, String deployed) {
        if (!StringUtils.hasText(deployed)) {
            return true;
        }
        String trimmedDeployed = deployed.trim();
        if ("deployed".equals(trimmedDeployed)) {
            return StringUtils.hasText(model.getDeploymentId());
        }
        if ("undeployed".equals(trimmedDeployed)) {
            return !StringUtils.hasText(model.getDeploymentId());
        }
        return true;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return StringUtils.hasText(source) && source.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private Model getRequiredModel(String modelId) {
        Model model = repositoryService.getModel(modelId);
        if (model == null) {
            throw new IllegalArgumentException("未查询到对应模型，模型ID：" + modelId);
        }
        return model;
    }

    private String buildMetaInfo(String modelName, String modelKey, String category, String processBeanName) {
        Map<String, Object> metaInfo = new HashMap<>();
        metaInfo.put("name", modelName);
        metaInfo.put("key", modelKey);
        metaInfo.put("category", category);
        metaInfo.put(PROCESS_BEAN_NAME, processBeanName);
        return JSON.toJSONString(metaInfo);
    }

    private String readProcessBeanName(String metaInfoText) {
        if (!StringUtils.hasText(metaInfoText)) {
            return null;
        }
        try {
            JsonObject jsonObject = JSON.parseObject(metaInfoText);
            return jsonObject == null ? null : jsonObject.getString(PROCESS_BEAN_NAME);
        } catch (Exception exception) {
            log.warn("解析流程模型元数据中processBean 失败", exception);
            return null;
        }
    }

    private ModelResponse buildModelResponse(Model model) {
        return ModelResponse.builder()
                .modelId(model.getId())
                .modelName(model.getName())
                .modelKey(model.getKey())
                .category(model.getCategory())
                .processBeanName(readProcessBeanName(model.getMetaInfo()))
                .version(model.getVersion())
                .deploymentId(model.getDeploymentId())
                .createTime(model.getCreateTime())
                .lastUpdateTime(model.getLastUpdateTime())
                .build();
    }
}
