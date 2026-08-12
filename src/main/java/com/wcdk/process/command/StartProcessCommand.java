package com.wcdk.process.command;

import com.wcdk.process.engine.ReactiveCommand;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.engine.ReactiveRuntimeService;
import com.wcdk.process.execution.ExecutionContext;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 启动流程实例命令。
 * <p>封装启动新流程实例所需的参数，实现 {@link ReactiveCommand} 接口。</p>
 * <p>包含流程定义键、业务标识、发起人、流程变量和租户标识等信息，
 * 由流程引擎执行并返回创建的 {@link ProcessInstanceEntity}。</p>
 * @author wcdk
 */
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class StartProcessCommand implements ReactiveCommand<ProcessInstanceEntity> {

    private final String processDefinitionKey;
    private final String businessKey;
    private final String starter;
    private final Map<String, Object> variables;
    private final String tenantId;

    public StartProcessCommand(String processDefinitionKey, String businessKey, String starter, Map<String, Object> variables) {
        this(processDefinitionKey, businessKey, starter, variables, "default");
    }

    @Override
    public Mono<ProcessInstanceEntity> execute(ExecutionContext context) {
        return Mono.empty();
    }
}