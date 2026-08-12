package com.wcdk.process.exception;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessEngineException extends RuntimeException {

    public ProcessEngineException(String message) {
        super(message);
    }

    public ProcessEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}