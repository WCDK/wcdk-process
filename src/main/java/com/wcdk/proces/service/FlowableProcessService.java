package com.wcdk.proces.service;

import com.wcdk.proces.dto.ProcessInstanceResponse;
import com.wcdk.proces.dto.StartProcessRequest;
import com.wcdk.proces.dto.TaskCompleteRequest;
import com.wcdk.proces.dto.TaskResponse;

import java.util.List;

/**
 * 流程实例与任务处理服务。
 *
 * @author WCDK
 * @date 2026/7/13
 */
public interface FlowableProcessService {

    ProcessInstanceResponse startProcess(StartProcessRequest request);

    ProcessInstanceResponse getProcessInstance(String processInstanceId);

    List<TaskResponse> listTask(String assignee);

    void completeTask(TaskCompleteRequest request);

    void deleteProcessInstance(String processInstanceId, String deleteReason);

    void deleteTask(String taskId, String deleteReason);
}
