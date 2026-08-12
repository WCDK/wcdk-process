package com.wcdk.process.behavior;

import com.wcdk.process.execution.ExecutionContext;
import reactor.core.publisher.Mono;

/**
 * 服务任务委托接口。
 * <p>函数式接口，用于定义服务任务（Service Task）的执行逻辑。</p>
 * <p>实现类需在 {@link #execute(ExecutionContext)} 方法中编写具体的业务处理代码，
 * 流程引擎会在服务任务节点被激活时回调此方法。</p>
 * @author wcdk
 */
@FunctionalInterface
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ServiceTaskDelegate {

    Mono<Void> execute(ExecutionContext context);
}