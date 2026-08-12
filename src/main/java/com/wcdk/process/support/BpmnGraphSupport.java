package com.wcdk.process.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.execution.EdgeDefinition;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.enums.NodeType;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BPMN XML 与运行时流程图转换工具。
 *
 * @auther WCDK
 * @date 2026/8/11
 * @version 1.0
 */
@Component
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class BpmnGraphSupport {

    private static final Map<String, NodeType> NODE_TYPES = Map.ofEntries(
            Map.entry("startEvent", NodeType.START_EVENT),
            Map.entry("endEvent", NodeType.END_EVENT),
            Map.entry("userTask", NodeType.USER_TASK),
            Map.entry("serviceTask", NodeType.SERVICE_TASK),
            Map.entry("scriptTask", NodeType.SCRIPT_TASK),
            Map.entry("exclusiveGateway", NodeType.EXCLUSIVE_GATEWAY),
            Map.entry("parallelGateway", NodeType.PARALLEL_GATEWAY),
            Map.entry("inclusiveGateway", NodeType.INCLUSIVE_GATEWAY),
            Map.entry("callActivity", NodeType.CALL_ACTIVITY),
            Map.entry("subProcess", NodeType.SUB_PROCESS),
            Map.entry("intermediateCatchEvent", NodeType.INTERMEDIATE_CATCH_EVENT),
            Map.entry("intermediateThrowEvent", NodeType.INTERMEDIATE_THROW_EVENT),
            Map.entry("boundaryEvent", NodeType.BOUNDARY_EVENT)
    );

    private final ObjectMapper objectMapper;

    public BpmnGraphSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedBpmn parse(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("BPMN 文件内容不能为空");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            Element process = firstElement(document, "process");
            if (process == null) {
                throw new IllegalArgumentException("BPMN 文件中缺少 process 节点");
            }

            ProcessGraph graph = new ProcessGraph();
            String processKey = process.getAttribute("id");
            graph.setProcessDefinitionId(processKey);
            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Object>> sequenceFlows = new ArrayList<>();

            NodeList children = process.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (!(child instanceof Element element)) {
                    continue;
                }
                String localName = localName(element);
                NodeType nodeType = NODE_TYPES.get(localName);
                if (nodeType != null) {
                    String id = element.getAttribute("id");
                    NodeDefinition definition = new NodeDefinition(id, nodeType, element.getAttribute("name"));
                    for (int attributeIndex = 0; attributeIndex < element.getAttributes().getLength(); attributeIndex++) {
                        Node attribute = element.getAttributes().item(attributeIndex);
                        String attributeName = attribute.getLocalName() != null
                                ? attribute.getLocalName() : attribute.getNodeName().replaceFirst("^.*:", "");
                        if (!"id".equals(attributeName) && !"name".equals(attributeName)) {
                            definition.setProperty(attributeName, attribute.getNodeValue());
                        }
                    }
                    String documentation = textOf(element, "documentation");
                    if (documentation != null) {
                        definition.setProperty("documentation", documentation);
                    }
                    graph.addNode(definition);
                    if (nodeType == NodeType.START_EVENT && graph.getStartNodeId() == null) {
                        graph.setStartNodeId(id);
                    }
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("elementId", id);
                    item.put("elementType", localName);
                    item.put("elementName", element.getAttribute("name"));
                    item.put("documentation", documentation);
                    nodes.add(item);
                }
            }

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (!(child instanceof Element element) || !"sequenceFlow".equals(localName(element))) {
                    continue;
                }
                String id = element.getAttribute("id");
                String source = element.getAttribute("sourceRef");
                String target = element.getAttribute("targetRef");
                String condition = textOf(element, "conditionExpression");
                graph.addEdge(new EdgeDefinition(id, source, target, condition));
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("elementId", id);
                item.put("sourceRef", source);
                item.put("targetRef", target);
                item.put("conditionExpression", condition);
                sequenceFlows.add(item);
            }
            applyDiagramBounds(document, graph, nodes);
            return new ParsedBpmn(processKey, process.getAttribute("name"), graph, nodes, sequenceFlows);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("BPMN 文件解析失败: " + exception.getMessage(), exception);
        }
    }

    public String toGraphJson(ProcessGraph graph) {
        try {
            return objectMapper.writeValueAsString(graph);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("流程图序列化失败", exception);
        }
    }

    private void applyDiagramBounds(
            Document document, ProcessGraph graph, List<Map<String, Object>> nodes) {
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        nodes.forEach(node -> byId.put(String.valueOf(node.get("elementId")), node));
        NodeList shapes = document.getElementsByTagNameNS("*", "BPMNShape");
        for (int i = 0; i < shapes.getLength(); i++) {
            Element shape = (Element) shapes.item(i);
            Map<String, Object> node = byId.get(shape.getAttribute("bpmnElement"));
            Element bounds = firstElement(shape, "Bounds");
            if (node == null || bounds == null) {
                continue;
            }
            Double x = number(bounds.getAttribute("x"));
            Double y = number(bounds.getAttribute("y"));
            Double width = number(bounds.getAttribute("width"));
            Double height = number(bounds.getAttribute("height"));
            node.put("x", x);
            node.put("y", y);
            node.put("width", width);
            node.put("height", height);
            NodeDefinition definition = graph.getNode(shape.getAttribute("bpmnElement"));
            if (definition != null) {
                definition.setProperty("x", x);
                definition.setProperty("y", y);
                definition.setProperty("width", width);
                definition.setProperty("height", height);
            }
        }
    }

    private Double number(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ignored) {
            return 0D;
        }
    }

    private Element firstElement(Node parent, String localName) {
        if (parent instanceof Document document) {
            NodeList elements = document.getElementsByTagNameNS("*", localName);
            return elements.getLength() == 0 ? null : (Element) elements.item(0);
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element && localName.equals(localName(element))) {
                return element;
            }
        }
        return null;
    }

    private String textOf(Element parent, String childName) {
        Element child = firstElement(parent, childName);
        return child == null ? null : child.getTextContent().trim();
    }

    private String localName(Element element) {
        return element.getLocalName() != null ? element.getLocalName() : element.getTagName().replaceFirst("^.*:", "");
    }

    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    public record ParsedBpmn(
            String processKey,
            String processName,
            ProcessGraph graph,
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> sequenceFlows) {
    }
}