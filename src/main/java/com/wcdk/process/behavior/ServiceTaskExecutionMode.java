package com.wcdk.process.behavior;

/**
 * 服务任务执行模式枚举。
 * <p>定义服务任务（Service Task）的两种执行方式：</p>
 * <ul>
 *   <li>{@link #INLINE} - 内联执行，在当前流程线程中同步执行任务逻辑</li>
 *   <li>{@link #ASYNC} - 异步执行，将任务提交到异步线程池中执行</li>
 * </ul>
 * @author wcdk
 */
public enum ServiceTaskExecutionMode {
    INLINE,
    ASYNC
}