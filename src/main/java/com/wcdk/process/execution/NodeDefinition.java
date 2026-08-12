package com.wcdk.process.execution;

import com.wcdk.process.enums.NodeType;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class NodeDefinition {

    private String id;
    private NodeType nodeType;
    private String name;
    private String behaviorClass;
    private Map<String, Object> properties = new HashMap<>();
    private List<String> incomingEdges = new ArrayList<>();
    private List<String> outgoingEdges = new ArrayList<>();

    public NodeDefinition() {
    }

    public NodeDefinition(String id, NodeType nodeType) {
        this.id = id;
        this.nodeType = nodeType;
    }

    public NodeDefinition(String id, NodeType nodeType, String name) {
        this.id = id;
        this.nodeType = nodeType;
        this.name = name;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public String getStringProperty(String key, String defaultValue) {
        Object value = properties.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}