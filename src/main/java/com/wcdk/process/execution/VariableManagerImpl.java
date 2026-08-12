package com.wcdk.process.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.entity.VariableEntity;
import com.wcdk.process.enums.VariableScopeType;
import com.wcdk.process.enums.VariableValueType;
import com.wcdk.process.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class VariableManagerImpl implements ReactiveVariableManager {

    private final VariableRepository variableRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Object> getVariable(String scopeType, String scopeId, String name) {
        return variableRepository.findByScopeTypeAndScopeIdAndName(scopeType, scopeId, name)
                .map(this::extractValue);
    }

    @Override
    public Mono<Void> setVariable(String scopeType, String scopeId, String name, Object value) {
        return variableRepository.findByScopeTypeAndScopeIdAndName(scopeType, scopeId, name)
                .flatMap(entity -> {
                    updateEntity(entity, value);
                    entity.setUpdatedAt(Instant.now());
                    return variableRepository.updateById(entity).then(Mono.empty());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    VariableEntity entity = new VariableEntity();
                    entity.setScopeType(scopeType);
                    entity.setScopeId(scopeId);
                    entity.setName(name);
                    entity.setRevision(1L);
                    entity.setCreatedAt(Instant.now());
                    entity.setUpdatedAt(Instant.now());
                    updateEntity(entity, value);
                    return variableRepository.insert(entity).then(Mono.empty());
                }))
                .then();
    }

    @Override
    public Mono<Void> removeVariable(String scopeType, String scopeId, String name) {
        return variableRepository.findByScopeTypeAndScopeIdAndName(scopeType, scopeId, name)
                .flatMap(entity -> variableRepository.deleteById(entity.getId()))
                .then();
    }

    @Override
    public Flux<VariableEntry> getAllVariables(String scopeType, String scopeId) {
        return variableRepository.findByScopeTypeAndScopeId(scopeType, scopeId)
                .map(entity -> new VariableEntry(entity.getName(), extractValue(entity), entity.getValueType()));
    }

    @Override
    public Mono<Void> setProcessVariable(String processInstanceId, String name, Object value) {
        return setVariable(VariableScopeType.PROCESS.name(), processInstanceId, name, value);
    }

    @Override
    public Mono<Object> getProcessVariable(String processInstanceId, String name) {
        return getVariable(VariableScopeType.PROCESS.name(), processInstanceId, name);
    }

    @Override
    public Mono<Void> setExecutionVariable(String executionId, String name, Object value) {
        return setVariable(VariableScopeType.EXECUTION.name(), executionId, name, value);
    }

    @Override
    public Mono<Object> getExecutionVariable(String executionId, String name) {
        return getVariable(VariableScopeType.EXECUTION.name(), executionId, name);
    }

    @Override
    public Mono<Void> removeExecutionVariable(String executionId, String name) {
        return removeVariable(VariableScopeType.EXECUTION.name(), executionId, name);
    }

    @Override
    public Flux<VariableEntry> getAllExecutionVariables(String executionId) {
        return getAllVariables(VariableScopeType.EXECUTION.name(), executionId);
    }

    @Override
    public Mono<Object> getVariableHierarchical(String processInstanceId, String executionId, String name) {
        return getExecutionVariable(executionId, name)
                .switchIfEmpty(getProcessVariable(processInstanceId, name));
    }

    private Object extractValue(VariableEntity entity) {
        if (entity.getValueType() == null) {
            return null;
        }
        VariableValueType type = VariableValueType.valueOf(entity.getValueType());
        return switch (type) {
            case STRING -> entity.getValueText();
            case LONG -> entity.getValueLong();
            case DOUBLE -> entity.getValueDouble();
            case BOOLEAN -> entity.getValueBoolean() != null && entity.getValueBoolean() == 1;
            case JSON -> {
                try {
                    yield objectMapper.readValue(entity.getValueText(), Object.class);
                } catch (JsonProcessingException e) {
                    yield entity.getValueText();
                }
            }
            case NULL -> null;
        };
    }

    private void updateEntity(VariableEntity entity, Object value) {
        if (value == null) {
            entity.setValueType(VariableValueType.NULL.name());
            return;
        }
        if (value instanceof String s) {
            entity.setValueType(VariableValueType.STRING.name());
            entity.setValueText(s);
        } else if (value instanceof Long l) {
            entity.setValueType(VariableValueType.LONG.name());
            entity.setValueLong(l);
        } else if (value instanceof Integer i) {
            entity.setValueType(VariableValueType.LONG.name());
            entity.setValueLong(i.longValue());
        } else if (value instanceof Double d) {
            entity.setValueType(VariableValueType.DOUBLE.name());
            entity.setValueDouble(d);
        } else if (value instanceof Boolean b) {
            entity.setValueType(VariableValueType.BOOLEAN.name());
            entity.setValueBoolean(b ? 1 : 0);
        } else {
            entity.setValueType(VariableValueType.JSON.name());
            try {
                entity.setValueText(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                entity.setValueText(value.toString());
            }
        }
    }
}