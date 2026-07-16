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
@Tag(name = "����ʵ������", description = "�ṩ�������������ʵ����ѯ�������ѯ���������ӿ�")
public class FlowableProcessController {

    private final FlowableProcessService flowableProcessService;

    @PostMapping("/start")
    @Operation(summary = "�������ʵ��", description = "�������̶����������µ�����ʵ��")
    public ApiResponse<ProcessInstanceResponse> startProcess(@RequestBody StartProcessRequest request) {
        return ApiResponse.success("��������ɹ�", flowableProcessService.startProcess(request));
    }

    @GetMapping("/instance/{processInstanceId}")
    @Operation(summary = "��ѯ����ʵ��", description = "��������ʵ����Ų�ѯ����ʵ������")
    public ApiResponse<ProcessInstanceResponse> getProcessInstance(@PathVariable String processInstanceId) {
        return ApiResponse.success(flowableProcessService.getProcessInstance(processInstanceId));
    }

    @GetMapping("/task/list")
    @Operation(summary = "��ѯ�����б�", description = "��������������ѯ����������б�")
    public ApiResponse<List<TaskResponse>> listTask(@RequestParam(required = false) String assignee) {
        return ApiResponse.success(flowableProcessService.listTask(assignee));
    }

    @PostMapping("/task/complete")
    @Operation(summary = "��������", description = "�����������������ɵ�ǰ��������")
    public ApiResponse<Void> completeTask(@RequestBody TaskCompleteRequest request) {
        flowableProcessService.completeTask(request);
        return ApiResponse.success("���̴���ɹ�", null);
    }

    @DeleteMapping("/instance/{processInstanceId}")
    @Operation(summary = "ɾ������ʵ��", description = "��������ʵ�����ɾ�������е�����ʵ����������ʱ����")
    public ApiResponse<Void> deleteProcessInstance(@PathVariable String processInstanceId,
                                                   @RequestParam(required = false) String deleteReason) {
        flowableProcessService.deleteProcessInstance(processInstanceId, deleteReason);
        return ApiResponse.success("����ʵ��ɾ���ɹ�", null);
    }

    @DeleteMapping("/task/{taskId}")
    @Operation(summary = "ɾ����������", description = "����������ɾ����ǰ������������")
    public ApiResponse<Void> deleteTask(@PathVariable String taskId,
                                        @RequestParam(required = false) String deleteReason) {
        flowableProcessService.deleteTask(taskId, deleteReason);
        return ApiResponse.success("��������ɾ���ɹ�", null);
    }
}
