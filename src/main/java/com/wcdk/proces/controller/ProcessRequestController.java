package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.ProcessDefinitionDetailResponse;
import com.wcdk.proces.dto.ProcessRequestApproveRequest;
import com.wcdk.proces.dto.ProcessRequestCreateRequest;
import com.wcdk.proces.dto.ProcessRequestResponse;
import com.wcdk.proces.service.ProcessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/process/request")
@RequiredArgsConstructor
@Tag(
        name = "流程申请",
        description = "创建、提交、查询、审批和删除流程申请"
)
public class ProcessRequestController {

    private final ProcessRequestService processRequestService;

    @PostMapping
    @Operation(
            summary = "创建流程申请",
            description = "根据动态表单数据创建流程申请"
    )
    public ApiResponse<ProcessRequestResponse> createProcessRequest(@RequestBody ProcessRequestCreateRequest request) {
        return ApiResponse.success("流程申请创建成功", processRequestService.createProcessRequest(request));
    }

    @PostMapping("/{id}/submit")
    @Operation(
            summary = "提交流程申请",
            description = "根据 ID 提交流程草稿"
    )
    public ApiResponse<ProcessRequestResponse> submitProcessRequest(@PathVariable Long id) {
        return ApiResponse.success("流程申请提交成功", processRequestService.submitProcessRequest(id));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "查询流程申请详情",
            description = "根据 ID 查询流程申请详情"
    )
    public ApiResponse<ProcessRequestResponse> getProcessRequest(@PathVariable Long id) {
        return ApiResponse.success(processRequestService.getProcessRequest(id));
    }

    @GetMapping("/{id}/diagram")
    @Operation(
            summary = "查询流程申请流程图详情",
            description = "根据流程申请 ID 查询对应流程图结构及当前步骤节点"
    )
    public ApiResponse<ProcessDefinitionDetailResponse> getProcessRequestDiagramDetail(@PathVariable Long id) {
        return ApiResponse.success(processRequestService.getProcessRequestDiagramDetail(id));
    }

    @GetMapping("/list")
    @Operation(
            summary = "查询流程申请列表",
            description = "按创建时间倒序分页查询流程申请列表"
    )
    public ApiResponse<PageResponse<ProcessRequestResponse>> listProcessRequest(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String processNo,
            @RequestParam(required = false) String starter,
            @RequestParam(required = false) String businessTitle,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String processDefinitionKey,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(processRequestService.listProcessRequest(
                pageNum,
                pageSize,
                processNo,
                starter,
                businessTitle,
                category,
                processDefinitionKey,
                status
        ));
    }

    @PostMapping("/approve")
    @Operation(
            summary = "审批流程申请",
            description = "处理流程申请对应的审批任务"
    )
    public ApiResponse<Void> approveProcessRequest(@RequestBody ProcessRequestApproveRequest request) {
        processRequestService.approveProcessRequest(request);
        return ApiResponse.success("流程申请审批处理成功", null);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除流程申请",
            description = "删除流程申请及其相关的运行时数据"
    )
    public ApiResponse<Void> deleteProcessRequest(@PathVariable Long id,
                                                  @RequestParam(required = false) String deleteReason) {
        processRequestService.deleteProcessRequest(id, deleteReason);
        return ApiResponse.success("流程申请删除成功", null);
    }
}
