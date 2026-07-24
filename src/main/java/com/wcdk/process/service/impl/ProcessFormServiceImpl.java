package com.wcdk.process.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.context.AuthenticatedUser;
import com.wcdk.process.dto.ProcessFormBindingItemRequest;
import com.wcdk.process.dto.ProcessFormBindingSaveRequest;
import com.wcdk.process.dto.ProcessFormResponse;
import com.wcdk.process.dto.ProcessFormSaveRequest;
import com.wcdk.process.entity.WcdkProcessForm;
import com.wcdk.process.entity.WcdkProcessFormBinding;
import com.wcdk.process.mapper.WcdkProcessFormBindingMapper;
import com.wcdk.process.mapper.WcdkProcessFormMapper;
import com.wcdk.process.service.ProcessFormService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.RepositoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@Service
public class ProcessFormServiceImpl extends ServiceImpl<WcdkProcessFormMapper, WcdkProcessForm> implements ProcessFormService {

    private static final String DEFAULT_TENANT_ID = "";

    private static final String PROCESS_SCOPE = "PROCESS";

    private final WcdkProcessFormBindingMapper formBindingMapper;

    private final RepositoryService repositoryService;

    public ProcessFormServiceImpl(WcdkProcessFormBindingMapper formBindingMapper, RepositoryService repositoryService) {
        this.formBindingMapper = formBindingMapper;
        this.repositoryService = repositoryService;
    }

    @Override
    public PageResponse<ProcessFormResponse> listForm(long pageNum, long pageSize, String formName, String formKey,
                                                      Boolean boundProcess, String processNode) {
        List<ProcessFormResponse> filteredRecords = lambdaQuery()
                .like(StringUtils.hasText(formName), WcdkProcessForm::getFormName, trimValue(formName))
                .like(StringUtils.hasText(formKey), WcdkProcessForm::getFormKey, trimValue(formKey))
                .eq(WcdkProcessForm::getStatus, 1)
                .orderByDesc(WcdkProcessForm::getUpdateTime)
                .list()
                .stream()
                .map(this::toResponse)
                .filter(record -> matchesBoundProcess(record, boundProcess))
                .filter(record -> matchesProcessNode(record, processNode))
                .toList();
        long currentPage = Math.max(pageNum, 1L);
        long currentSize = Math.max(pageSize, 1L);
        int fromIndex = (int) Math.min((currentPage - 1L) * currentSize, filteredRecords.size());
        int toIndex = (int) Math.min(fromIndex + currentSize, filteredRecords.size());
        return new PageResponse<>((long) filteredRecords.size(), currentPage, currentSize, filteredRecords.subList(fromIndex, toIndex));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessFormResponse saveForm(ProcessFormSaveRequest request) {
        validateSaveRequest(request);
        String formKey = request.getFormKey().trim();
        String formName = request.getFormName().trim();
        String schemaJson = JSON.toJSONString(request.getSchema());
        LocalDateTime now = LocalDateTime.now();
        WcdkProcessForm entity = lambdaQuery()
                .eq(WcdkProcessForm::getFormKey, formKey)
                .eq(WcdkProcessForm::getFormVersion, 1)
                .eq(WcdkProcessForm::getTenantId, DEFAULT_TENANT_ID)
                .one();
        if (entity == null) {
            AuthenticatedUser currentUser = AuthContextHolder.get();
            entity = WcdkProcessForm.builder()
                    .formKey(formKey)
                    .formName(formName)
                    .formVersion(1)
                    .formSchemaJson(schemaJson)
                    .resourceName(formKey + ".json")
                    .tenantId(DEFAULT_TENANT_ID)
                    .status(1)
                    .createUser(currentUser == null ? null : currentUser.getUsername())
                    .createTime(now)
                    .updateTime(now)
                    .build();
            save(entity);
            return toResponse(entity);
        }
        entity.setFormName(formName);
        entity.setFormSchemaJson(schemaJson);
        entity.setResourceName(formKey + ".json");
        entity.setStatus(1);
        entity.setUpdateTime(now);
        updateById(entity);
        return toResponse(entity);
    }

    @Override
    public ProcessFormResponse getFormByKey(String formKey) {
        if (!StringUtils.hasText(formKey)) {
            throw new IllegalArgumentException("表单标识不能为空");
        }
        WcdkProcessForm entity = lambdaQuery()
                .eq(WcdkProcessForm::getFormKey, formKey.trim())
                .eq(WcdkProcessForm::getStatus, 1)
                .orderByDesc(WcdkProcessForm::getUpdateTime)
                .list()
                .stream()
                .findFirst()
                .orElse(null);
        if (entity == null) {
            throw new IllegalArgumentException("未查询到对应表单方案：" + formKey);
        }
        return toResponse(entity);
    }

    @Override
    public List<ProcessFormResponse> listFormBinding(String processDefinitionId) {
        if (!StringUtils.hasText(processDefinitionId)) {
            return List.of();
        }
        List<WcdkProcessFormBinding> bindings = formBindingMapper.selectList(new LambdaQueryWrapper<WcdkProcessFormBinding>()
                .eq(WcdkProcessFormBinding::getProcessDefinitionId, processDefinitionId.trim())
                .eq(WcdkProcessFormBinding::getTenantId, DEFAULT_TENANT_ID)
                .eq(WcdkProcessFormBinding::getStatus, 1)
                .orderByAsc(WcdkProcessFormBinding::getTaskDefinitionKey)
                .orderByDesc(WcdkProcessFormBinding::getUpdateTime));
        if (bindings == null || bindings.isEmpty()) {
            return List.of();
        }
        return bindings.stream()
                .map(this::toBindingResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFormBinding(ProcessFormBindingSaveRequest request) {
        validateBindingRequest(request);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(request.getProcessDefinitionId().trim())
                .singleResult();
        if (processDefinition == null) {
            throw new IllegalArgumentException("未查询到对应流程定义");
        }
        LocalDateTime now = LocalDateTime.now();
        formBindingMapper.delete(new LambdaQueryWrapper<WcdkProcessFormBinding>()
                .eq(WcdkProcessFormBinding::getProcessDefinitionId, processDefinition.getId())
                .eq(WcdkProcessFormBinding::getTenantId, DEFAULT_TENANT_ID));
        for (ProcessFormBindingItemRequest binding : request.getBindings()) {
            String taskDefinitionKey = binding.getTaskDefinitionKey().trim();
            Set<Long> formIds = new LinkedHashSet<>(binding.getFormIds());
            for (Long formId : formIds) {
                WcdkProcessForm form = getById(formId);
                if (form == null || !Objects.equals(form.getStatus(), 1)) {
                    throw new IllegalArgumentException("未查询到可用表单方案，表单ID：" + formId);
                }
                formBindingMapper.insert(WcdkProcessFormBinding.builder()
                        .formId(formId)
                        .processDefinitionId(processDefinition.getId())
                        .processDefinitionKey(processDefinition.getKey())
                        .processDefinitionVersion(processDefinition.getVersion())
                        .deploymentId(processDefinition.getDeploymentId())
                        .tenantId(DEFAULT_TENANT_ID)
                        .bindScope("TASK")
                        .taskDefinitionKey(taskDefinitionKey)
                        .status(1)
                        .remark(form.getFormName())
                        .createTime(now)
                        .updateTime(now)
                        .build());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForm(Long id) {
        WcdkProcessForm entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("鏈煡璇㈠埌琛ㄥ崟鏂规");
        }
        removeById(id);
    }

    private void validateSaveRequest(ProcessFormSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("琛ㄥ崟鍙傛暟涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(request.getFormKey())) {
            throw new IllegalArgumentException("琛ㄥ崟鏍囪瘑涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(request.getFormName())) {
            throw new IllegalArgumentException("琛ㄥ崟鍚嶇О涓嶈兘涓虹┖");
        }
        if (!(request.getSchema() instanceof Collection<?>)) {
            throw new IllegalArgumentException("表单方案格式不正确");
        }
    }

    private void validateBindingRequest(ProcessFormBindingSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("流程表单绑定参数不能为空");
        }
        if (!StringUtils.hasText(request.getProcessDefinitionId())) {
            throw new IllegalArgumentException("流程定义ID不能为空");
        }
        if (request.getBindings() == null) {
            throw new IllegalArgumentException("流程表单绑定列表不能为空");
        }
        for (ProcessFormBindingItemRequest binding : request.getBindings()) {
            if (binding == null || !StringUtils.hasText(binding.getTaskDefinitionKey())) {
                throw new IllegalArgumentException("用户任务节点不能为空");
            }
            if (binding.getFormIds() == null || binding.getFormIds().isEmpty()) {
                throw new IllegalArgumentException("绑定表单不能为空");
            }
        }
    }

    private ProcessFormResponse toResponse(WcdkProcessForm entity) {
        Object schema = parseSchema(entity.getFormSchemaJson());
        FormBindingSummary bindingSummary = buildBindingSummary(entity.getId());
        return ProcessFormResponse.builder()
                .id(entity.getId())
                .formKey(entity.getFormKey())
                .formName(entity.getFormName())
                .formVersion(entity.getFormVersion())
                .fieldCount(schema instanceof Collection<?> ? ((Collection<?>) schema).size() : 0)
                .schema(schema)
                .boundProcess(bindingSummary.boundProcess())
                .processDefinitionId(bindingSummary.processDefinitionId())
                .processNodeId(bindingSummary.processNodeId())
                .processNodeName(bindingSummary.processNodeName())
                .processNode(bindingSummary.processNode())
                .resourceName(entity.getResourceName())
                .tenantId(entity.getTenantId())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createUser(entity.getCreateUser())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private ProcessFormResponse toBindingResponse(WcdkProcessFormBinding binding) {
        if (binding == null || binding.getFormId() == null) {
            return null;
        }
        WcdkProcessForm form = getById(binding.getFormId());
        if (form == null || !Objects.equals(form.getStatus(), 1)) {
            return null;
        }
        Object schema = parseSchema(form.getFormSchemaJson());
        return ProcessFormResponse.builder()
                .id(form.getId())
                .formKey(form.getFormKey())
                .formName(form.getFormName())
                .formVersion(form.getFormVersion())
                .fieldCount(schema instanceof Collection<?> ? ((Collection<?>) schema).size() : 0)
                .schema(schema)
                .boundProcess(true)
                .processDefinitionId(binding.getProcessDefinitionId())
                .processNodeId(binding.getTaskDefinitionKey())
                .processNodeName(resolveNodeName(binding.getProcessDefinitionId(), binding.getTaskDefinitionKey(), new ConcurrentHashMap<>()))
                .processNode(binding.getTaskDefinitionKey())
                .resourceName(form.getResourceName())
                .tenantId(form.getTenantId())
                .status(form.getStatus())
                .remark(form.getRemark())
                .createUser(form.getCreateUser())
                .createTime(form.getCreateTime())
                .updateTime(form.getUpdateTime())
                .build();
    }

    private Object parseSchema(String schemaJson) {
        if (!StringUtils.hasText(schemaJson)) {
            return java.util.List.of();
        }
        try {
            Object schema = JSON.parse(schemaJson);
            return schema == null ? java.util.List.of() : schema;
        } catch (Exception ex) {
            return java.util.List.of();
        }
    }

    private String trimValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private FormBindingSummary buildBindingSummary(Long formId) {
        if (formId == null) {
            return FormBindingSummary.empty();
        }
        List<WcdkProcessFormBinding> bindings = formBindingMapper.selectList(new LambdaQueryWrapper<WcdkProcessFormBinding>()
                .eq(WcdkProcessFormBinding::getFormId, formId)
                .eq(WcdkProcessFormBinding::getStatus, 1)
                .orderByDesc(WcdkProcessFormBinding::getUpdateTime));
        if (bindings == null || bindings.isEmpty()) {
            return FormBindingSummary.empty();
        }
        Set<String> processDefinitionIds = new LinkedHashSet<>();
        Set<String> processNodeIds = new LinkedHashSet<>();
        Set<String> processNodeNames = new LinkedHashSet<>();
        Map<String, String> nodeNameCache = new ConcurrentHashMap<>();
        for (WcdkProcessFormBinding binding : bindings) {
            addIfPresent(processDefinitionIds, binding.getProcessDefinitionId());
            String taskDefinitionKey = trimValue(binding.getTaskDefinitionKey());
            if (StringUtils.hasText(taskDefinitionKey)) {
                addIfPresent(processNodeIds, taskDefinitionKey);
                addIfPresent(processNodeNames, resolveNodeName(binding.getProcessDefinitionId(), taskDefinitionKey, nodeNameCache));
            } else if (PROCESS_SCOPE.equalsIgnoreCase(binding.getBindScope())) {
                addIfPresent(processNodeNames, "流程级");
            }
        }
        return new FormBindingSummary(
                true,
                joinValues(processDefinitionIds),
                joinValues(processNodeIds),
                joinValues(processNodeNames),
                joinValues(processNodeNames)
        );
    }

    private String resolveNodeName(String processDefinitionId, String taskDefinitionKey, Map<String, String> nodeNameCache) {
        if (!StringUtils.hasText(processDefinitionId) || !StringUtils.hasText(taskDefinitionKey)) {
            return null;
        }
        String cacheKey = processDefinitionId + "#" + taskDefinitionKey;
        return nodeNameCache.computeIfAbsent(cacheKey, key -> {
            try {
                BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
                if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
                    return taskDefinitionKey;
                }
                FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(taskDefinitionKey, true);
                if (flowElement instanceof FlowNode && StringUtils.hasText(flowElement.getName())) {
                    return flowElement.getName().trim();
                }
            } catch (Exception ignored) {
                return taskDefinitionKey;
            }
            return taskDefinitionKey;
        });
    }

    private boolean matchesBoundProcess(ProcessFormResponse record, Boolean boundProcess) {
        return boundProcess == null || Objects.equals(Boolean.TRUE.equals(record.getBoundProcess()), boundProcess);
    }

    private boolean matchesProcessNode(ProcessFormResponse record, String processNode) {
        if (!StringUtils.hasText(processNode)) {
            return true;
        }
        String keyword = processNode.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(record.getProcessNodeId(), keyword)
                || containsIgnoreCase(record.getProcessNodeName(), keyword)
                || containsIgnoreCase(record.getProcessNode(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return StringUtils.hasText(keyword)
                && StringUtils.hasText(value)
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void addIfPresent(Set<String> values, String value) {
        if (StringUtils.hasText(value)) {
            values.add(value.trim());
        }
    }

    private String joinValues(Set<String> values) {
        return values == null || values.isEmpty() ? null : String.join(", ", values);
    }

    private static class FormBindingSummary {

        private final Boolean boundProcess;

        private final String processDefinitionId;

        private final String processNodeId;

        private final String processNodeName;

        private final String processNode;

        private FormBindingSummary(Boolean boundProcess,
                                   String processDefinitionId,
                                   String processNodeId,
                                   String processNodeName,
                                   String processNode) {
            this.boundProcess = boundProcess;
            this.processDefinitionId = processDefinitionId;
            this.processNodeId = processNodeId;
            this.processNodeName = processNodeName;
            this.processNode = processNode;
        }

        private Boolean boundProcess() {
            return boundProcess;
        }

        private String processDefinitionId() {
            return processDefinitionId;
        }

        private String processNodeId() {
            return processNodeId;
        }

        private String processNodeName() {
            return processNodeName;
        }

        private String processNode() {
            return processNode;
        }

        private static FormBindingSummary empty() {
            return new FormBindingSummary(false, null, null, null, null);
        }
    }
}
