package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.dto.ProcessFormBindingSaveRequest;
import com.wcdk.process.dto.ProcessFormResponse;
import com.wcdk.process.dto.ProcessFormSaveRequest;
import com.wcdk.process.service.ProcessFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@RestController
@RequestMapping("/process/form")
@Tag(name = "流程表单管理", description = "提供流程表单方案分页查询、保存和删除接口")
public class ProcessFormController {

    private final ProcessFormService processFormService;

    public ProcessFormController(ProcessFormService processFormService) {
        this.processFormService = processFormService;
    }

    @GetMapping("/list")
    @Operation(summary = "查询表单方案列表", description = "按表单名称和表单标识分页查询表单设计方案")
    public ApiResponse<PageResponse<ProcessFormResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                               @RequestParam(defaultValue = "10") Long pageSize,
                                                               @RequestParam(required = false) String formName,
                                                               @RequestParam(required = false) String formKey,
                                                               @RequestParam(required = false) Boolean boundProcess,
                                                               @RequestParam(required = false) String processNode) {
        AuthContextHolder.requirePermission("menu:form");
        return ApiResponse.success(processFormService.listForm(pageNum, pageSize, formName, formKey, boundProcess, processNode));
    }

    @PostMapping
    @Operation(summary = "保存表单方案", description = "保存表单设计方案，同一表单标识会覆盖最新方案")
    public ApiResponse<ProcessFormResponse> save(@RequestBody ProcessFormSaveRequest request) {
        AuthContextHolder.requirePermission("form:save");
        return ApiResponse.success("表单方案保存成功", processFormService.saveForm(request));
    }

    @GetMapping("/key/{formKey}")
    @Operation(summary = "查询表单方案详情", description = "根据表单标识查询可用于流程图预览的表单方案详情")
    public ApiResponse<ProcessFormResponse> getByKey(@PathVariable String formKey) {
        AuthContextHolder.requireUser();
        return ApiResponse.success(processFormService.getFormByKey(formKey));
    }

    @GetMapping("/binding/{processDefinitionId}")
    @Operation(summary = "查询流程表单绑定", description = "根据流程定义编号查询用户任务节点已绑定的表单方案")
    public ApiResponse<List<ProcessFormResponse>> listBinding(@PathVariable String processDefinitionId) {
        AuthContextHolder.requireUser();
        return ApiResponse.success(processFormService.listFormBinding(processDefinitionId));
    }

    @PostMapping("/binding")
    @Operation(summary = "保存流程表单绑定", description = "保存流程定义下用户任务节点和表单方案的关联关系")
    public ApiResponse<Void> saveBinding(@RequestBody ProcessFormBindingSaveRequest request) {
        AuthContextHolder.requirePermission("designer:save");
        processFormService.saveFormBinding(request);
        return ApiResponse.success("流程表单绑定保存成功", null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除表单方案", description = "根据表单方案ID删除表单设计方案")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("form:save");
        processFormService.deleteForm(id);
        return ApiResponse.success("表单方案删除成功", null);
    }
}
