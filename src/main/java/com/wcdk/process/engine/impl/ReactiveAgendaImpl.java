package com.wcdk.process.engine.impl;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ReactiveAgendaImpl implements com.wcdk.process.engine.ReactiveAgenda {

    private final Queue<Operation> operations = new LinkedList<>();

    @Data
    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    public static class Operation {
        private final String type;
        private final String executionId;
        private final String targetId;
    }

    @Override
    public void planContinueProcessInNode(String executionId, String nodeId) {
        operations.offer(new Operation("CONTINUE_PROCESS", executionId, nodeId));
    }

    @Override
    public void planTakeSequenceFlow(String executionId, String edgeId) {
        operations.offer(new Operation("TAKE_SEQUENCE_FLOW", executionId, edgeId));
    }

    @Override
    public void planEndProcess(String executionId) {
        operations.offer(new Operation("END_PROCESS", executionId, null));
    }

    @Override
    public void planCompleteTask(String executionId, String taskId) {
        operations.offer(new Operation("COMPLETE_TASK", executionId, taskId));
    }

    @Override
    public void planCreateTask(String executionId, String taskId) {
        operations.offer(new Operation("CREATE_TASK", executionId, taskId));
    }

    @Override
    public Operation nextOperation() {
        return operations.poll();
    }

    @Override
    public boolean hasOperations() {
        return !operations.isEmpty();
    }

    @Override
    public void clearOperations() {
        operations.clear();
    }
}