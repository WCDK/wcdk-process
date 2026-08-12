package com.wcdk.process.behavior;

import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.ReactiveVariableManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 序列流条件表达式求值器。
 * <p>负责解析和评估流程图中序列流（Sequence Flow）上的条件表达式，
 * 决定流程执行时应该选择哪条路径。</p>
 * <p>支持的表达式语法包括：</p>
 * <ul>
 *   <li>{@code ${variableName == 'value'}} - 等值比较</li>
 *   <li>{@code ${variableName != 'value'}} - 不等比较</li>
 *   <li>{@code ${variableName > value}} - 大于比较</li>
 *   <li>{@code ${variableName < value}} - 小于比较</li>
 *   <li>{@code ${booleanVariable}} - 布尔变量直接求值</li>
 * </ul>
 * @author wcdk
 */
@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SequenceFlowCondition {

    private final ReactiveVariableManager variableManager;

    public Mono<Boolean> evaluate(ExecutionContext context, String conditionExpression) {
        if (conditionExpression == null || conditionExpression.isBlank()) {
            return Mono.just(true);
        }
        String trimmed = conditionExpression.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            return evaluateInnerExpression(context, trimmed.substring(2, trimmed.length() - 1));
        }
        return Mono.just(Boolean.parseBoolean(trimmed));
    }

    private Mono<Boolean> evaluateInnerExpression(ExecutionContext context, String expression) {
        if (expression.contains("==")) {
            String[] parts = expression.split("==", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim().replace("'", "").replace("\"", "");
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> expected.equals(String.valueOf(actual)))
                    .defaultIfEmpty("null".equals(expected));
        }
        if (expression.contains("!=")) {
            String[] parts = expression.split("!=", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim().replace("'", "").replace("\"", "");
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> !expected.equals(String.valueOf(actual)))
                    .defaultIfEmpty(!"null".equals(expected));
        }
        if (expression.contains(">")) {
            String[] parts = expression.split(">", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim();
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> compareNumbers(actual, expected, true))
                    .defaultIfEmpty(false);
        }
        if (expression.contains("<")) {
            String[] parts = expression.split("<", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim();
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> compareNumbers(actual, expected, false))
                    .defaultIfEmpty(false);
        }

        return variableManager.getProcessVariable(context.getProcessInstanceId(), expression.trim())
                .map(value -> value instanceof Boolean bool ? bool : value != null)
                .defaultIfEmpty(false);
    }

    private boolean compareNumbers(Object actual, String expected, boolean greaterThan) {
        try {
            double actualNumber = Double.parseDouble(String.valueOf(actual));
            double expectedNumber = Double.parseDouble(expected);
            return greaterThan ? actualNumber > expectedNumber : actualNumber < expectedNumber;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}