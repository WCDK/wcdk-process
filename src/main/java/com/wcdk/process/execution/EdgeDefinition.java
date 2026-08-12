package com.wcdk.process.execution;

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
public class EdgeDefinition {

    private String id;
    private String sourceNodeId;
    private String targetNodeId;
    private String conditionExpression;
    private boolean isDefault;

    public EdgeDefinition() {
    }

    public EdgeDefinition(String id, String sourceNodeId, String targetNodeId) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
    }

    public EdgeDefinition(String id, String sourceNodeId, String targetNodeId, String conditionExpression) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.conditionExpression = conditionExpression;
    }
}