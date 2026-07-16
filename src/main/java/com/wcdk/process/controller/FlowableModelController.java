package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ModelCreateRequest;
import com.wcdk.process.dto.ModelResponse;
import com.wcdk.process.dto.ModelUpdateRequest;
import com.wcdk.process.service.FlowableModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@RestController
@RequestMapping("/flowable/model")
@RequiredArgsConstructor
@Tag(name = "����ģ�͹���", description = "�ṩ����ģ�ʹ��������¡���ѯ�������ɾ���ӿ�")
public class FlowableModelController {

    private final FlowableModelService flowableModelService;

    @PostMapping
    @Operation(summary = "��������ģ��", description = "����������������µ�����ģ��")
    public ApiResponse<ModelResponse> createModel(@RequestBody ModelCreateRequest request) {
        return ApiResponse.success("����ģ�ʹ����ɹ�", flowableModelService.createModel(request));
    }

    @PutMapping("/{modelId}")
    @Operation(summary = "��������ģ��", description = "����ģ�ͱ�Ÿ�������ģ�͵Ļ�����Ϣ�Ͷ�������")
    public ApiResponse<ModelResponse> updateModel(@PathVariable String modelId, @RequestBody ModelUpdateRequest request) {
        return ApiResponse.success("����ģ�͸��³ɹ�", flowableModelService.updateModel(modelId, request));
    }

    @GetMapping("/list")
    @Operation(summary = "��ѯ����ģ���б�", description = "��ѯ��ǰϵͳ�е�����ģ���б�")
    public ApiResponse<List<ModelResponse>> listModel() {
        return ApiResponse.success(flowableModelService.listModel());
    }

    @GetMapping("/{modelId}/xml")
    @Operation(summary = "��ѯ����ģ��Դ��", description = "����ģ�ͱ�Ų�ѯ����ģ��Դ������")
    public ApiResponse<String> getModelXml(@PathVariable String modelId) {
        return ApiResponse.success(flowableModelService.getModelXml(modelId));
    }

    @PostMapping("/{modelId}/deploy")
    @Operation(summary = "��������ģ��", description = "����ģ�ͱ�Ž�����ģ�Ͳ���Ϊ��ִ�����̶��壬���� processBean")
    public ApiResponse<DeploymentResponse> deployModel(@PathVariable String modelId,
                                                       @RequestParam String processBeanName) {
        return ApiResponse.success("����ģ�Ͳ���ɹ�", flowableModelService.deployModel(modelId, processBeanName));
    }

    @DeleteMapping("/{modelId}")
    @Operation(summary = "ɾ������ģ��", description = "����ģ�ͱ��ɾ������ģ�ͼ����������")
    public ApiResponse<Void> deleteModel(@PathVariable String modelId) {
        flowableModelService.deleteModel(modelId);
        return ApiResponse.success("����ģ��ɾ���ɹ�", null);
    }
}
