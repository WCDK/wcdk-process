package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 * @date 2026/7/23
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTaskInfoResponse {

    private String taskId;

    private String taskDefinitionKey;

    private String taskName;

    private String assignee;

    private String processInstanceId;

    private String processDefinitionId;

    private Long processRequestId;
}
