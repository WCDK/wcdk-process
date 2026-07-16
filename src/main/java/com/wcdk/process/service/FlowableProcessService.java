package com.wcdk.process.service;

import com.wcdk.process.dto.ProcessInstanceResponse;
import com.wcdk.process.dto.StartProcessRequest;
import com.wcdk.process.dto.TaskCompleteRequest;
import com.wcdk.process.dto.TaskResponse;

import java.util.List;

/**
 * ����ʵ�������������
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
