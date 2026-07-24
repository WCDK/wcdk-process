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
public class ProcessDefinitionUpdateRequest {

    /**
     * 当前部署ID
     */
    private String deploymentId;

    /**
     * 当前流程定义ID
     */
    private String processDefinitionId;

    /**
     * BPMN XML 内容
     */
    private String bpmnXml;
}
