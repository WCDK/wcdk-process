package com.wcdk.process.engine;

import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.NodeDefinition;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveNodeBehavior {

    Mono<Void> execute(ExecutionContext context);

    default Mono<Void> trigger(ExecutionContext context) {
        return execute(context);
    }
}