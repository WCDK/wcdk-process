package com.wcdk.process.engine;

import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveCommandExecutor {

    <T> Mono<T> execute(ReactiveCommand<T> command);
}