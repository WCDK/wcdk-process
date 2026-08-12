package com.wcdk.process.engine;

import com.wcdk.process.execution.ExecutionContext;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveCommand<T> {

    Mono<T> execute(ExecutionContext context);
}