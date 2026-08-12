package com.wcdk.process.command;

import com.wcdk.process.engine.ReactiveCommand;
import com.wcdk.process.engine.ReactiveTaskService;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.execution.ExecutionContext;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 完成任务命令。
 * <p>封装完成用户任务所需的参数，实现 {@link ReactiveCommand} 接口。</p>
 * <p>包含任务标识、操作用户标识和任务输出变量，
 * 执行后完成指定任务并推动流程继续向下流转。</p>
 * @author wcdk
 */
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class CompleteTaskCommand implements ReactiveCommand<TaskEntity> {

    private final String taskId;
    private final String userId;
    private final Map<String, Object> variables;

    @Override
    public Mono<TaskEntity> execute(ExecutionContext context) {
        return Mono.empty();
    }
}