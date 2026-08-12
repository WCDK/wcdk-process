package com.wcdk.process.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessInstanceHistory {

    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private Integer processDefinitionVersion;
    private String businessKey;
    private String starter;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private Duration duration;
    private List<ActivityHistory> activities;
    private List<TaskHistory> tasks;
    private List<VariableHistory> variables;
}