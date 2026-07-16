package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.ProcessInstanceResponse;
import com.wcdk.process.dto.StartProcessRequest;
import com.wcdk.process.dto.TaskCompleteRequest;
import com.wcdk.process.dto.TaskResponse;
import com.wcdk.process.service.FlowableProcessService;
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

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@RestController
@RequestMapping("/flowable/process")
@RequiredArgsConstructor
@Tag(name = "流程实例管理", description = "提供流程启动、流程实例查询、任务查询和任务办理接口")
public class FlowableProcessController {

    private final FlowableProcessService flowableProcessService;

    @PostMapping("/start")
    @Operation(summary = "启动流程实例", description = "根据流程定义参数启动新的流程实例")
    public ApiResponse<ProcessInstanceResponse> startProcess(@RequestBody StartProcessRequest request) {
        return ApiResponse.success("流程启动成功", flowableProcessService.startProcess(request));
    }

    @GetMapping("/instance/{processInstanceId}")
    @Operation(summary = "查询流程实例", description = "根据流程实例编号查询流程实例详情")
    public ApiResponse<ProcessInstanceResponse> getProcessInstance(@PathVariable String processInstanceId) {
        return ApiResponse.success(flowableProcessService.getProcessInstance(processInstanceId));
    }

    @GetMapping("/task/list")
    @Operation(summary = "查询任务列表", description = "按办理人条件查询待办或任务列表")
    public ApiResponse<List<TaskResponse>> listTask(@RequestParam(required = false) String assignee) {
        return ApiResponse.success(flowableProcessService.listTask(assignee));
    }

    @PostMapping("/task/complete")
    @Operation(summary = "办理任务", description = "根据任务办理请求完成当前流程任务")
    public ApiResponse<Void> completeTask(@RequestBody TaskCompleteRequest request) {
        flowableProcessService.completeTask(request);
        return ApiResponse.success("流程处理成功", null);
    }

    @DeleteMapping("/instance/{processInstanceId}")
    @Operation(summary = "删除流程实例", description = "根据流程实例编号删除运行中的流程实例及其运行时数据")
    public ApiResponse<Void> deleteProcessInstance(@PathVariable String processInstanceId,
                                                   @RequestParam(required = false) String deleteReason) {
        flowableProcessService.deleteProcessInstance(processInstanceId, deleteReason);
        return ApiResponse.success("流程实例删除成功", null);
    }

    @DeleteMapping("/task/{taskId}")
    @Operation(summary = "删除流程任务", description = "根据任务编号删除当前流程任务数据")
    public ApiResponse<Void> deleteTask(@PathVariable String taskId,
                                        @RequestParam(required = false) String deleteReason) {
        flowableProcessService.deleteTask(taskId, deleteReason);
        return ApiResponse.success("流程任务删除成功", null);
    }
}
