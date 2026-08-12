package com.wcdk.process.history;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface HistoryProjector {

    Mono<ProcessInstanceHistory> getProcessInstanceHistory(String processInstanceId);

    Flux<ActivityHistory> getActivityHistory(String processInstanceId);

    Flux<TaskHistory> getTaskHistory(String processInstanceId);

    Flux<VariableHistory> getVariableHistory(String processInstanceId);

    Flux<ProcessInstanceHistory> getProcessInstancesByDefinitionKey(String processDefinitionKey);
}