package com.wcdk.process.execution;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveVariableManager {

    Mono<Object> getVariable(String scopeType, String scopeId, String name);

    Mono<Void> setVariable(String scopeType, String scopeId, String name, Object value);

    Mono<Void> removeVariable(String scopeType, String scopeId, String name);

    Flux<VariableEntry> getAllVariables(String scopeType, String scopeId);

    Mono<Void> setProcessVariable(String processInstanceId, String name, Object value);

    Mono<Object> getProcessVariable(String processInstanceId, String name);

    Mono<Void> setExecutionVariable(String executionId, String name, Object value);

    Mono<Object> getExecutionVariable(String executionId, String name);

    Mono<Void> removeExecutionVariable(String executionId, String name);

    Flux<VariableEntry> getAllExecutionVariables(String executionId);

    Mono<Object> getVariableHierarchical(String processInstanceId, String executionId, String name);

    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    record VariableEntry(String name, Object value, String valueType) {
    }
}