package com.wcdk.process.engine;

import com.wcdk.process.engine.impl.ReactiveAgendaImpl;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveAgenda {

    void planContinueProcessInNode(String executionId, String nodeId);

    void planTakeSequenceFlow(String executionId, String edgeId);

    void planEndProcess(String executionId);

    void planCompleteTask(String executionId, String taskId);

    void planCreateTask(String executionId, String taskId);

    ReactiveAgendaImpl.Operation nextOperation();

    boolean hasOperations();

    void clearOperations();
}