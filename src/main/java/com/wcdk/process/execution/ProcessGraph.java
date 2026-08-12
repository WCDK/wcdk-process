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
public class ProcessGraph {

    private String processDefinitionId;
    private Map<String, NodeDefinition> nodes = new HashMap<>();
    private Map<String, EdgeDefinition> edges = new HashMap<>();
    private String startNodeId;

    public void addNode(NodeDefinition node) {
        nodes.put(node.getId(), node);
    }

    public void addEdge(EdgeDefinition edge) {
        edges.put(edge.getId(), edge);
        NodeDefinition source = nodes.get(edge.getSourceNodeId());
        if (source != null) {
            source.getOutgoingEdges().add(edge.getId());
        }
        NodeDefinition target = nodes.get(edge.getTargetNodeId());
        if (target != null) {
            target.getIncomingEdges().add(edge.getId());
        }
    }

    public NodeDefinition getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public EdgeDefinition getEdge(String edgeId) {
        return edges.get(edgeId);
    }

    public List<NodeDefinition> getOutgoingNodes(String nodeId) {
        NodeDefinition node = nodes.get(nodeId);
        if (node == null) {
            return List.of();
        }
        return node.getOutgoingEdges().stream()
                .map(edges::get)
                .filter(e -> e != null)
                .map(e -> nodes.get(e.getTargetNodeId()))
                .filter(n -> n != null)
                .toList();
    }

    public List<EdgeDefinition> getOutgoingEdges(String nodeId) {
        NodeDefinition node = nodes.get(nodeId);
        if (node == null) {
            return List.of();
        }
        return node.getOutgoingEdges().stream()
                .map(edges::get)
                .filter(e -> e != null)
                .toList();
    }

    public List<NodeDefinition> getIncomingNodes(String nodeId) {
        NodeDefinition node = nodes.get(nodeId);
        if (node == null) {
            return List.of();
        }
        return node.getIncomingEdges().stream()
                .map(edges::get)
                .filter(e -> e != null)
                .map(e -> nodes.get(e.getSourceNodeId()))
                .filter(n -> n != null)
                .toList();
    }

    public NodeDefinition getStartNode() {
        return nodes.get(startNodeId);
    }
}