package com.wcdk.process.execution;

import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ExecutionContext {

    private ProcessInstanceEntity processInstance;
    private ProcessDefinitionEntity processDefinition;
    private ExecutionEntity execution;
    private ProcessGraph processGraph;
    private ReactiveVariableManager variableManager;
    private Map<String, Object> transientVariables = new HashMap<>();

    public String getProcessInstanceId() {
        return processInstance != null ? processInstance.getId() : null;
    }

    public String getExecutionId() {
        return execution != null ? execution.getId() : null;
    }

    public String getProcessDefinitionId() {
        return processDefinition != null ? processDefinition.getId() : null;
    }

    public String getCurrentNodeId() {
        return execution != null ? execution.getNodeId() : null;
    }

    public void setTransientVariable(String name, Object value) {
        transientVariables.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTransientVariable(String name) {
        return (T) transientVariables.get(name);
    }
}