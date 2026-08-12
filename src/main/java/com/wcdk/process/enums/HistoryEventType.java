package com.wcdk.process.enums;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public enum HistoryEventType {
    PROCESS_STARTED,
    PROCESS_COMPLETED,
    PROCESS_TERMINATED,
    ACTIVITY_STARTED,
    ACTIVITY_COMPLETED,
    TASK_CREATED,
    TASK_CLAIMED,
    TASK_COMPLETED,
    TASK_CANCELLED,
    VARIABLE_CREATED,
    VARIABLE_UPDATED,
    VARIABLE_DELETED
}