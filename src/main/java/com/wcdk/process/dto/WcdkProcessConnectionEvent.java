package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程客户端回调事件。
 *
 * @auther WCDK
 * @date 2026/7/13
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessConnectionEvent {

    /**
     * 客户端连接记录ID。
     */
    private String connectionId;

    /**
     * 客户端标识。
     */
    private String clientId;

    /**
     * 客户端名称。
     */
    private String clientName;

    /**
     * 流程实例ID。
     */
    private String processInstanceId;

    /**
     * 流程定义ID。
     */
    private String processDefinitionId;

    /**
     * 流程定义标识。
     */
    private String processDefinitionKey;

    /**
     * 流程定义名称。
     */
    private String processDefinitionName;

    /**
     * 业务主键。
     */
    private String businessKey;

    /**
     * 审批人ID。
     */
    private String approvalId;

    /**
     * 审批人名称。
     */
    private String approvalName;

    /**
     * 当前审批任务ID。
     */
    private String currentTaskId;

    /**
     * 当前审批任务名称。
     */
    private String currentTaskName;

    private List<ProcessTaskInfoResponse> currentTasks;

    /**
     * 任务是否审批通过。
     */
    private Boolean taskApproved;

    /**
     * 任务审批结果文本。
     */
    private String taskApprovalResult;

    /**
     * 当前审批结果，0表示通过，1表示未通过，2表示驳回。
     */
    private Integer currentApprovalResult;

    /**
     * 本次审批提交的表单数据。
     */
    private Map<String, Object> approvalFormData;

    /**
     * 关联表单数据。
     */
    private Map<String, Object> relatedFormData;

    /**
     * 关联表单ID。
     */
    private Long relatedFormId;

    /**
     * 关联表单名称。
     */
    private String relatedFormName;

    /**
     * 当前任务关联表单列表。
     */
    private List<Map<String, Object>> relatedForms;

    /**
     * 下一任务ID。
     */
    private String nextTaskId;

    /**
     * 下一任务名称。
     */
    private String nextTaskName;

    private List<ProcessTaskInfoResponse> nextTasks;

    /**
     * 客户端流程处理器名称。
     */
    private String processBeanName;

    /**
     * 回调事件类型。
     */
    private String eventType;

    /**
     * 回调消息。
     */
    private String message;

    /**
     * 事件发生时间。
     */
    private LocalDateTime eventTime;

    /**
     * 扩展载荷。
     */
    /**
     * 错误消息。
     */
    private String errorMessage;
}
