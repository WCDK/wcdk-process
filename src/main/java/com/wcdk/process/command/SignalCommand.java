package com.wcdk.process.command;

import com.wcdk.process.engine.ReactiveCommand;
import com.wcdk.process.execution.ExecutionContext;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * 信号触发命令。
 * <p>封装向指定流程实例发送信号（Signal）所需的参数，实现 {@link ReactiveCommand} 接口。</p>
 * <p>包含信号名称、目标流程实例标识和信号载荷数据，
 * 用于触发流程中等待的信号事件或中断边界事件。</p>
 * @author wcdk
 */
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SignalCommand implements ReactiveCommand<Void> {

    private final String signalName;
    private final String processInstanceId;
    private final Object payload;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        return Mono.empty();
    }
}