package com.wcdk.proces.annotation;

import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 流程回调处理器标记。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcesBean {

    /**
     * 流程回调 bean 名称
     *
     * @return bean 名称
     */
    @NotNull String value();
}
