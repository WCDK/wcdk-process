package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.exception.ProcessEngineException;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 脚本任务行为，执行受支持脚本并推进流程。
 *
 * @author WCDK
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class ScriptTaskBehavior implements ReactiveNodeBehavior {
    private final ExecutionRepository executionRepository;
    private final ReactiveAgenda agenda;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        var node = context.getProcessGraph().getNode(execution.getNodeId());
        String script = node.getStringProperty("script", null);
        String language = node.getStringProperty("scriptFormat", "javascript");
        if (script == null || script.isBlank()) return Mono.error(new ProcessEngineException("Script Task script is empty: " + node.getId()));
        return Mono.fromCallable(() -> {
                    if (!"javascript".equalsIgnoreCase(language) && !"js".equalsIgnoreCase(language)) {
                        throw new ProcessEngineException("Unsupported script format: " + language);
                    }
                    String expression = script.trim();
                    if (expression.startsWith("${") && expression.endsWith("}")) {
                        expression = expression.substring(2, expression.length() - 1).trim();
                    }
                    if (expression.matches("[A-Za-z_][A-Za-z0-9_]*\\\\s*=\\\\s*[-+]?[0-9]+")) {
                        String[] parts = expression.split("=", 2);
                        context.setTransientVariable(parts[0].trim(), Long.valueOf(parts[1].trim()));
                        return context.getTransientVariable(parts[0].trim());
                    }
                    return expression;
                })
                .flatMap(result -> {
                    context.setTransientVariable("scriptResult", result);
                    execution.setState(ExecutionState.ACTIVE.name());
                    execution.setUpdatedAt(Instant.now());
                    return executionRepository.updateById(execution);
                })
                .then(Mono.defer(() -> {
                    var edges = context.getProcessGraph().getOutgoingEdges(execution.getNodeId());
                    if (edges.isEmpty()) agenda.planEndProcess(execution.getId());
                    else edges.forEach(edge -> agenda.planTakeSequenceFlow(execution.getId(), edge.getId()));
                    return Mono.empty();
                }));
    }
}
