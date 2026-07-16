package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import com.wcdk.process.dto.WcdkProcessClientResponse;
import com.wcdk.process.service.FlowableDeployService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@RestController
@RequestMapping("/flowable/deploy")
@RequiredArgsConstructor
@Tag(name = "流程部署管理", description = "提供流程定义部署、部署列表查询、流程定义查询和部署删除接口")
public class FlowableDeployController {

    private final FlowableDeployService flowableDeployService;

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    @PostMapping("/process")
    @Operation(summary = "部署流程定义文件", description = "上传流程定义文件并创建流程部署，可选绑定客户端与流程处理器")
    public ApiResponse<DeploymentResponse> deployProcess(@RequestParam String deploymentName,
                                                         @RequestParam(required = false) String category,
                                                         @RequestParam(required = false) String clientId,
                                                         @RequestParam(required = false) String processBeanName,
                                                         @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("流程部署成功", flowableDeployService.deployProcess(deploymentName, category, clientId, processBeanName, file));
    }

    @GetMapping("/list")
    @Operation(summary = "查询部署列表", description = "查询当前系统中的流程部署记录列表")
    public ApiResponse<List<DeploymentResponse>> listDeployment(@RequestParam(required = false) String deploymentName,
                                                               @RequestParam(required = false) String category) {
        return ApiResponse.success(flowableDeployService.listDeployment(deploymentName, category));
    }

    @GetMapping("/client/list")
    @Operation(summary = "查询部署客户端候选项", description = "查询流程部署时可选择的已注册客户端及其流程处理器")
    public ApiResponse<PageResponse<WcdkProcessClientResponse>> listClient(@RequestParam(defaultValue = "1") Long pageNum,
                                                                           @RequestParam(defaultValue = "500") Long pageSize,
                                                                           @RequestParam(required = false) String clientId,
                                                                           @RequestParam(required = false) String clientName) {
        return ApiResponse.success(wcdkProcessClientRegistryService.listClient(
                pageNum,
                pageSize,
                clientId,
                clientName,
                null,
                null,
                "clientId",
                "ascending"
        ));
    }

    @GetMapping("/definition/list")
    @Operation(summary = "查询流程定义列表", description = "查询已部署成功的流程定义列表")
    public ApiResponse<List<ProcessDefinitionResponse>> listProcessDefinition() {
        return ApiResponse.success(flowableDeployService.listProcessDefinition());
    }

    @GetMapping("/definition/{processDefinitionId}")
    @Operation(summary = "查询流程定义详情", description = "根据流程定义编号查询流程定义详情与流程图结构")
    public ApiResponse<ProcessDefinitionDetailResponse> getProcessDefinitionDetail(@PathVariable String processDefinitionId) {
        return ApiResponse.success(flowableDeployService.getProcessDefinitionDetail(processDefinitionId));
    }

    @DeleteMapping
    @Operation(summary = "删除流程部署", description = "根据部署编号删除流程部署，可选是否级联删除关联流程实例")
    public ApiResponse<Void> deleteDeployment(@RequestParam String deploymentId,
                                              @RequestParam(defaultValue = "true") Boolean cascade) {
        flowableDeployService.deleteDeployment(deploymentId, cascade);
        return ApiResponse.success("删除部署成功", null);
    }
}
