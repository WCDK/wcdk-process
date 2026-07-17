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
@Tag(name = "流程模型管理", description = "提供流程模型创建、更新、查询、部署和删除接口")
public class FlowableModelController {

    private final FlowableModelService flowableModelService;

    @PostMapping
    @Operation(summary = "创建流程模型", description = "根据请求参数创建新的流程模型")
    public ApiResponse<ModelResponse> createModel(@RequestBody ModelCreateRequest request) {
        return ApiResponse.success("流程模型创建成功", flowableModelService.createModel(request));
    }

    @PutMapping("/{modelId}")
    @Operation(summary = "更新流程模型", description = "根据模型编号更新流程模型的基础信息和定义内容")
    public ApiResponse<ModelResponse> updateModel(@PathVariable String modelId, @RequestBody ModelUpdateRequest request) {
        return ApiResponse.success("流程模型更新成功", flowableModelService.updateModel(modelId, request));
    }

    @GetMapping("/list")
    @Operation(summary = "查询流程模型列表", description = "查询当前系统中的流程模型列表")
    public ApiResponse<List<ModelResponse>> listModel(@RequestParam(required = false) String modelName,
                                                      @RequestParam(required = false) String modelKey,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String deployed) {
        return ApiResponse.success(flowableModelService.listModel(modelName, modelKey, category, deployed));
    }

    @GetMapping("/{modelId}/xml")
    @Operation(summary = "查询流程模型源码", description = "根据模型编号查询流程模型源码内容")
    public ApiResponse<String> getModelXml(@PathVariable String modelId) {
        return ApiResponse.success(flowableModelService.getModelXml(modelId));
    }

    @PostMapping("/{modelId}/deploy")
    @Operation(summary = "部署流程模型", description = "根据模型编号将流程模型部署为可执行流程定义，并绑定 processBean")
    public ApiResponse<DeploymentResponse> deployModel(@PathVariable String modelId,
                                                       @RequestParam(required = false) String clientId,
                                                       @RequestParam(required = false) String processBeanName) {
        return ApiResponse.success("流程模型部署成功", flowableModelService.deployModel(modelId, clientId, processBeanName));
    }

    @DeleteMapping("/{modelId}")
    @Operation(summary = "删除流程模型", description = "根据模型编号删除流程模型及其关联数据")
    public ApiResponse<Void> deleteModel(@PathVariable String modelId) {
        flowableModelService.deleteModel(modelId);
        return ApiResponse.success("流程模型删除成功", null);
    }
}
