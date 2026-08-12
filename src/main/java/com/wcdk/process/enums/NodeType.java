package com.wcdk.process.enums;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public enum NodeType {
    START_EVENT,
    END_EVENT,
    USER_TASK,
    SERVICE_TASK,
    SCRIPT_TASK,
    EXCLUSIVE_GATEWAY,
    PARALLEL_GATEWAY,
    INCLUSIVE_GATEWAY,
    CALL_ACTIVITY,
    SUB_PROCESS,
    INTERMEDIATE_CATCH_EVENT,
    INTERMEDIATE_THROW_EVENT,
    BOUNDARY_EVENT
}