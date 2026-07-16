package com.wcdk.process.common;

import com.wcdk.process.dto.ProcessDesignerExportEdgeRequest;
import com.wcdk.process.dto.ProcessDesignerExportNodeRequest;
import com.wcdk.process.dto.ProcessDesignerExportRequest;
import com.wcdk.process.dto.ProcessDesignerExportResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Component
public class ProcessDesignerExportSupport {

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    @Value("${wcdk.process.water-mark.text:WCDK}")
    private String waterMarkText;
    @Value("${wcdk.process.water-mark.color:#000000}")
    private String waterMarkColor;
    @Value("${wcdk.process.water-mark.opacity:0.5}")
    private Double waterMarkOpacity;
    @Value("${wcdk.process.water-mark.font-size:24}")
    private Integer waterMarkFontSize;
    @Value("${wcdk.process.water-mark.position:bottom-right}")
    private String waterMarkPosition;

    public ProcessDesignerExportResponse export(ProcessDesignerExportRequest request) {
        validateRequest(request);
        String format = normalizeFormat(request.getFormat());
        ExportContext context = buildExportContext(request);
        if ("png".equals(format)) {
            byte[] pngBytes = buildPngBytes(context);
            return ProcessDesignerExportResponse.builder()
                    .fileName(buildExportFileName("png"))
                    .contentType("image/png")
                    .contentBase64(Base64.getEncoder().encodeToString(pngBytes))
                    .skippedNodeLabels(List.of())
                    .build();
        }
        String xml = buildBpmnXml(context);
        return ProcessDesignerExportResponse.builder()
                .fileName(buildExportFileName(format))
                .contentType("application/xml;charset=UTF-8")
                .contentBase64(Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8)))
                .skippedNodeLabels(context.getSkippedNodeLabels())
                .build();
    }

    private void validateRequest(ProcessDesignerExportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("����������Ϊ��");
        }
        String format = normalizeFormat(request.getFormat());
        if (!"bpmn".equals(format) && !"bpmn20.xml".equals(format) && !"png".equals(format)) {
            throw new IllegalArgumentException("������ʽ��֧�� BPMN��BPMN.XML �� PNG");
        }
        if (CollectionUtils.isEmpty(request.getNodes())) {
            throw new IllegalArgumentException("�����ڻ�����������̽ڵ���ٵ���");
        }
    }

    private String normalizeFormat(String format) {
        String normalized = StringUtils.hasText(format) ? format.trim().toLowerCase(Locale.ROOT) : "";
        if ("bpmn.xml".equals(normalized) || "xml".equals(normalized)) {
            return "bpmn20.xml";
        }
        return normalized;
    }

    private String buildExportFileName(String extension) {
        return "wcdk-process-" + LocalDateTime.now().format(FILE_TIME_FORMATTER) + "." + extension;
    }

    private ExportContext buildExportContext(ProcessDesignerExportRequest request) {
        List<ProcessDesignerExportNodeRequest> sourceNodes = safeList(request.getNodes());
        List<ProcessDesignerExportEdgeRequest> sourceEdges = safeList(request.getEdges());
        List<ProcessDesignerExportNodeRequest> exportableNodes = new ArrayList<>();
        LinkedHashSet<String> skippedNodeLabels = new LinkedHashSet<>();
        for (ProcessDesignerExportNodeRequest node : sourceNodes) {
            if (node == null) {
                continue;
            }
            if (isSupportedBpmnType(node.getBpmnType())) {
                exportableNodes.add(copyNode(node));
            } else if (StringUtils.hasText(node.getLabel())) {
                skippedNodeLabels.add(node.getLabel());
            }
        }
        Map<String, ProcessDesignerExportNodeRequest> nodeLookup = buildNodeLookup(exportableNodes);
        sanitizeNodeParentRelations(exportableNodes, nodeLookup);
        return ExportContext.builder()
                .canvasWidth(request.getCanvasWidth() == null ? 2400 : request.getCanvasWidth())
                .canvasHeight(request.getCanvasHeight() == null ? 1400 : request.getCanvasHeight())
                .nodes(exportableNodes)
                .edges(copyEdges(sourceEdges))
                .nodeLookup(nodeLookup)
                .skippedNodeLabels(new ArrayList<>(skippedNodeLabels))
                .build();
    }

    private List<ProcessDesignerExportEdgeRequest> copyEdges(List<ProcessDesignerExportEdgeRequest> edges) {
        List<ProcessDesignerExportEdgeRequest> result = new ArrayList<>();
        for (ProcessDesignerExportEdgeRequest edge : safeList(edges)) {
            if (edge == null) {
                continue;
            }
            result.add(ProcessDesignerExportEdgeRequest.builder()
                    .id(edge.getId())
                    .sourceId(edge.getSourceId())
                    .targetId(edge.getTargetId())
                    .name(edge.getName())
                    .build());
        }
        return result;
    }

    private ProcessDesignerExportNodeRequest copyNode(ProcessDesignerExportNodeRequest node) {
        return ProcessDesignerExportNodeRequest.builder()
                .id(node.getId())
                .type(node.getType())
                .bpmnType(node.getBpmnType())
                .kind(node.getKind())
                .label(node.getLabel())
                .shortLabel(node.getShortLabel())
                .name(node.getName())
                .code(node.getCode())
                .documentation(node.getDocumentation())
                .parentId(node.getParentId())
                .expanded(Boolean.TRUE.equals(node.getExpanded()))
                .width(safeInteger(node.getWidth(), 120))
                .height(safeInteger(node.getHeight(), 72))
                .x(safeInteger(node.getX(), 0))
                .y(safeInteger(node.getY(), 0))
                .build();
    }

    private int safeInteger(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private boolean isSupportedBpmnType(String bpmnType) {
        return Set.of(
                "startEvent",
                "endEvent",
                "boundaryEvent",
                "intermediateCatchEvent",
                "intermediateThrowEvent",
                "userTask",
                "scriptTask",
                "serviceTask",
                "mailTask",
                "manualTask",
                "receiveTask",
                "businessRuleTask",
                "callActivity",
                "subProcess",
                "parallelGateway",
                "exclusiveGateway",
                "inclusiveGateway",
                "eventGateway",
                "textAnnotation"
        ).contains(bpmnType);
    }

    private Map<String, ProcessDesignerExportNodeRequest> buildNodeLookup(Collection<ProcessDesignerExportNodeRequest> nodes) {
        Map<String, ProcessDesignerExportNodeRequest> lookup = new LinkedHashMap<>();
        for (ProcessDesignerExportNodeRequest node : nodes) {
            if (node != null && StringUtils.hasText(node.getId())) {
                lookup.put(node.getId(), node);
            }
        }
        return lookup;
    }

    private void sanitizeNodeParentRelations(List<ProcessDesignerExportNodeRequest> nodes,
                                             Map<String, ProcessDesignerExportNodeRequest> nodeLookup) {
        for (ProcessDesignerExportNodeRequest node : nodes) {
            if (!StringUtils.hasText(node.getParentId())) {
                continue;
            }
            ProcessDesignerExportNodeRequest parentNode = nodeLookup.get(node.getParentId());
            if (parentNode == null
                    || !"subProcess".equals(parentNode.getBpmnType())
                    || parentNode.getId().equals(node.getId())
                    || isNodeDescendantOf(parentNode.getId(), node.getId(), nodeLookup)) {
                node.setParentId("");
            }
        }
    }

    private boolean isNodeDescendantOf(String nodeId,
                                       String targetAncestorId,
                                       Map<String, ProcessDesignerExportNodeRequest> nodeLookup) {
        if (!StringUtils.hasText(nodeId) || !StringUtils.hasText(targetAncestorId) || nodeId.equals(targetAncestorId)) {
            return false;
        }
        ProcessDesignerExportNodeRequest currentNode = nodeLookup.get(nodeId);
        int guard = 0;
        while (currentNode != null && StringUtils.hasText(currentNode.getParentId()) && guard < nodeLookup.size()) {
            if (targetAncestorId.equals(currentNode.getParentId())) {
                return true;
            }
            currentNode = nodeLookup.get(currentNode.getParentId());
            guard += 1;
        }
        return false;
    }

    private String buildBpmnXml(ExportContext context) {
        Map<String, Boolean> usedIds = new LinkedHashMap<>();
        String processId = sanitizeBpmnId("Wcdk_" + System.currentTimeMillis(), "Process", usedIds);
        String definitionsId = sanitizeBpmnId("Definitions_" + System.currentTimeMillis(), "Definitions", usedIds);
        String diagramId = sanitizeBpmnId(processId + "_Diagram", "Diagram", usedIds);
        String planeId = sanitizeBpmnId(processId + "_Plane", "Plane", usedIds);
        Map<String, String> nodeIdMap = new LinkedHashMap<>();
        Map<String, String> nodeTagMap = buildNodeTagMap();
        Map<String, List<String>> incomingMap = new LinkedHashMap<>();
        Map<String, List<String>> outgoingMap = new LinkedHashMap<>();
        for (ProcessDesignerExportNodeRequest node : context.getNodes()) {
            nodeIdMap.put(node.getId(), sanitizeBpmnId(firstText(node.getCode(), node.getId()), "FlowNode", usedIds));
        }
        List<SequenceFlowExport> sequenceFlows = buildSequenceFlows(context, nodeIdMap, usedIds, incomingMap, outgoingMap);
        ContainerMaps containerMaps = buildFlowContainerMaps(context.getNodes(), sequenceFlows);
        List<String> lines = new ArrayList<>();
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        lines.add("<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        lines.add("                  xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"");
        lines.add("                  xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"");
        lines.add("                  xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"");
        lines.add("                  xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"");
        lines.add("                  id=\"" + definitionsId + "\"");
        lines.add("                  targetNamespace=\"http://flowable.org/processdef\">");
        lines.add("  <bpmn:process id=\"" + processId + "\" name=\"wcdk-process-" + processId + "\" isExecutable=\"true\">");
        appendContainerFlowElements(lines, "", containerMaps.getNodeChildrenMap(), containerMaps.getFlowChildrenMap(),
                nodeTagMap, nodeIdMap, incomingMap, outgoingMap);
        lines.add("  </bpmn:process>");
        lines.add("  <bpmndi:BPMNDiagram id=\"" + diagramId + "\">");
        lines.add("    <bpmndi:BPMNPlane id=\"" + planeId + "\" bpmnElement=\"" + processId + "\">");
        for (ProcessDesignerExportNodeRequest node : context.getNodes()) {
            lines.add("      <bpmndi:BPMNShape id=\"" + nodeIdMap.get(node.getId()) + "_di\" bpmnElement=\"" + nodeIdMap.get(node.getId()) + "\">");
            lines.add("        <dc:Bounds x=\"" + node.getX() + "\" y=\"" + node.getY() + "\" width=\"" + node.getWidth() + "\" height=\"" + node.getHeight() + "\" />");
            lines.add("      </bpmndi:BPMNShape>");
        }
        for (SequenceFlowExport sequenceFlow : sequenceFlows) {
            ProcessDesignerExportNodeRequest sourceNode = context.getNodeLookup().get(sequenceFlow.getSourceId());
            ProcessDesignerExportNodeRequest targetNode = context.getNodeLookup().get(sequenceFlow.getTargetId());
            if (sourceNode == null || targetNode == null) {
                continue;
            }
            Point start = resolveNodeCenter(sourceNode, true);
            Point end = resolveNodeCenter(targetNode, false);
            EdgePoints edgePoints = buildEdgePoints(start, end);
            lines.add("      <bpmndi:BPMNEdge id=\"" + sequenceFlow.getId() + "_di\" bpmnElement=\"" + sequenceFlow.getId() + "\">");
            lines.add("        <di:waypoint x=\"" + edgePoints.getStartX() + "\" y=\"" + edgePoints.getStartY() + "\" />");
            lines.add("        <di:waypoint x=\"" + edgePoints.getMidX() + "\" y=\"" + edgePoints.getStartY() + "\" />");
            lines.add("        <di:waypoint x=\"" + edgePoints.getEndX() + "\" y=\"" + edgePoints.getEndY() + "\" />");
            lines.add("        <di:waypoint x=\"" + edgePoints.getTargetX() + "\" y=\"" + edgePoints.getTargetY() + "\" />");
            if (StringUtils.hasText(sequenceFlow.getName())) {
                int labelX = Math.round((edgePoints.getStartX() + edgePoints.getTargetX()) / 2F - 40);
                int labelY = Math.round((edgePoints.getStartY() + edgePoints.getTargetY()) / 2F - 24);
                lines.add("        <bpmndi:BPMNLabel>");
                lines.add("          <dc:Bounds x=\"" + labelX + "\" y=\"" + labelY + "\" width=\"80\" height=\"20\" />");
                lines.add("        </bpmndi:BPMNLabel>");
            }
            lines.add("      </bpmndi:BPMNEdge>");
        }
        lines.add("    </bpmndi:BPMNPlane>");
        lines.add("  </bpmndi:BPMNDiagram>");
        lines.add("</bpmn:definitions>");
        return String.join("\n", lines);
    }

    private Map<String, String> buildNodeTagMap() {
        Map<String, String> nodeTagMap = new LinkedHashMap<>();
        nodeTagMap.put("startEvent", "startEvent");
        nodeTagMap.put("endEvent", "endEvent");
        nodeTagMap.put("boundaryEvent", "boundaryEvent");
        nodeTagMap.put("intermediateCatchEvent", "intermediateCatchEvent");
        nodeTagMap.put("intermediateThrowEvent", "intermediateThrowEvent");
        nodeTagMap.put("userTask", "userTask");
        nodeTagMap.put("scriptTask", "scriptTask");
        nodeTagMap.put("serviceTask", "serviceTask");
        nodeTagMap.put("mailTask", "task");
        nodeTagMap.put("manualTask", "manualTask");
        nodeTagMap.put("receiveTask", "receiveTask");
        nodeTagMap.put("businessRuleTask", "businessRuleTask");
        nodeTagMap.put("callActivity", "callActivity");
        nodeTagMap.put("subProcess", "subProcess");
        nodeTagMap.put("parallelGateway", "parallelGateway");
        nodeTagMap.put("exclusiveGateway", "exclusiveGateway");
        nodeTagMap.put("inclusiveGateway", "inclusiveGateway");
        nodeTagMap.put("eventGateway", "eventBasedGateway");
        nodeTagMap.put("textAnnotation", "textAnnotation");
        return nodeTagMap;
    }

    private List<SequenceFlowExport> buildSequenceFlows(ExportContext context,
                                                        Map<String, String> nodeIdMap,
                                                        Map<String, Boolean> usedIds,
                                                        Map<String, List<String>> incomingMap,
                                                        Map<String, List<String>> outgoingMap) {
        List<SequenceFlowExport> sequenceFlows = new ArrayList<>();
        for (ProcessDesignerExportEdgeRequest edge : context.getEdges()) {
            if (edge == null) {
                continue;
            }
            ProcessDesignerExportNodeRequest sourceNode = context.getNodeLookup().get(edge.getSourceId());
            ProcessDesignerExportNodeRequest targetNode = context.getNodeLookup().get(edge.getTargetId());
            if (sourceNode == null || targetNode == null) {
                continue;
            }
            String sourceContainerId = resolveNodeContainerId(sourceNode);
            String targetContainerId = resolveNodeContainerId(targetNode);
            if (!sourceContainerId.equals(targetContainerId)) {
                continue;
            }
            String flowId = sanitizeBpmnId("Flow_" + edge.getSourceId() + "_" + edge.getTargetId(), "SequenceFlow", usedIds);
            sequenceFlows.add(SequenceFlowExport.builder()
                    .id(flowId)
                    .sourceId(edge.getSourceId())
                    .targetId(edge.getTargetId())
                    .name(edge.getName())
                    .containerId(sourceContainerId)
                    .build());
            outgoingMap.computeIfAbsent(edge.getSourceId(), key -> new ArrayList<>()).add(flowId);
            incomingMap.computeIfAbsent(edge.getTargetId(), key -> new ArrayList<>()).add(flowId);
        }
        return sequenceFlows;
    }

    private String resolveNodeContainerId(ProcessDesignerExportNodeRequest node) {
        return node != null && StringUtils.hasText(node.getParentId()) ? node.getParentId() : "";
    }

    private ContainerMaps buildFlowContainerMaps(List<ProcessDesignerExportNodeRequest> nodes, List<SequenceFlowExport> flows) {
        Map<String, List<ProcessDesignerExportNodeRequest>> nodeChildrenMap = new LinkedHashMap<>();
        Map<String, List<SequenceFlowExport>> flowChildrenMap = new LinkedHashMap<>();
        for (ProcessDesignerExportNodeRequest node : nodes) {
            String containerId = StringUtils.hasText(node.getParentId()) ? node.getParentId() : "";
            nodeChildrenMap.computeIfAbsent(containerId, key -> new ArrayList<>()).add(node);
        }
        for (SequenceFlowExport flow : flows) {
            String containerId = StringUtils.hasText(flow.getContainerId()) ? flow.getContainerId() : "";
            flowChildrenMap.computeIfAbsent(containerId, key -> new ArrayList<>()).add(flow);
        }
        return new ContainerMaps(nodeChildrenMap, flowChildrenMap);
    }

    private void appendContainerFlowElements(List<String> lines,
                                             String containerId,
                                             Map<String, List<ProcessDesignerExportNodeRequest>> nodeChildrenMap,
                                             Map<String, List<SequenceFlowExport>> flowChildrenMap,
                                             Map<String, String> nodeTagMap,
                                             Map<String, String> nodeIdMap,
                                             Map<String, List<String>> incomingMap,
                                             Map<String, List<String>> outgoingMap) {
        List<ProcessDesignerExportNodeRequest> childNodes = nodeChildrenMap.getOrDefault(containerId, List.of());
        for (ProcessDesignerExportNodeRequest node : childNodes) {
            String nodeTag = nodeTagMap.getOrDefault(node.getBpmnType(), "task");
            lines.add("    <bpmn:" + nodeTag + " id=\"" + nodeIdMap.get(node.getId()) + "\" name=\"" + escapeXml(firstText(node.getName(), node.getLabel(), node.getCode())) + "\">");
            if (StringUtils.hasText(node.getDocumentation())) {
                lines.add("      <bpmn:documentation>" + escapeXml(node.getDocumentation()) + "</bpmn:documentation>");
            }
            for (String incomingFlowId : incomingMap.getOrDefault(node.getId(), List.of())) {
                lines.add("      <bpmn:incoming>" + incomingFlowId + "</bpmn:incoming>");
            }
            for (String outgoingFlowId : outgoingMap.getOrDefault(node.getId(), List.of())) {
                lines.add("      <bpmn:outgoing>" + outgoingFlowId + "</bpmn:outgoing>");
            }
            if ("subProcess".equals(node.getBpmnType())) {
                appendContainerFlowElements(lines, node.getId(), nodeChildrenMap, flowChildrenMap, nodeTagMap, nodeIdMap, incomingMap, outgoingMap);
            }
            lines.add("    </bpmn:" + nodeTag + ">");
        }
        for (SequenceFlowExport flow : flowChildrenMap.getOrDefault(containerId, List.of())) {
            String namePart = StringUtils.hasText(flow.getName()) ? " name=\"" + escapeXml(flow.getName()) + "\"" : "";
            lines.add("    <bpmn:sequenceFlow id=\"" + flow.getId() + "\"" + namePart + " sourceRef=\"" + nodeIdMap.get(flow.getSourceId()) + "\" targetRef=\"" + nodeIdMap.get(flow.getTargetId()) + "\" />");
        }
    }

    private String sanitizeBpmnId(String value, String fallbackPrefix, Map<String, Boolean> usedIds) {
        String source = String.valueOf(value == null ? "" : value).replaceAll("[^0-9A-Za-z_:.\\-]", "_");
        if (!StringUtils.hasText(source)) {
            source = fallbackPrefix;
        }
        if (!Character.isLetter(source.charAt(0)) && source.charAt(0) != '_') {
            source = fallbackPrefix + "_" + source;
        }
        String candidate = source;
        int index = 1;
        while (usedIds.containsKey(candidate)) {
            candidate = source + "_" + index;
            index += 1;
        }
        usedIds.put(candidate, true);
        return candidate;
    }

    private String escapeXml(String value) {
        return String.valueOf(value == null ? "" : value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private byte[] buildPngBytes(ExportContext context) {
        Bounds bounds = resolveVisibleBounds(context);
        BufferedImage image = new BufferedImage(Math.max(bounds.getWidth(), 1), Math.max(bounds.getHeight(), 1), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawBackground(graphics, bounds);
            List<ProcessDesignerExportNodeRequest> visibleNodes = resolveVisibleNodes(context);
            Map<String, ProcessDesignerExportNodeRequest> visibleNodeLookup = buildNodeLookup(visibleNodes);
            drawVisibleEdges(graphics, context.getEdges(), visibleNodeLookup, bounds);
            drawVisibleNodes(graphics, visibleNodes, bounds);
            drawWatermark(graphics, bounds);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("PNG ����ʧ��", exception);
        } finally {
            graphics.dispose();
        }
    }

    private void drawBackground(Graphics2D graphics, Bounds bounds) {
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setColor(new Color(148, 163, 184, 28));
        for (int x = 0; x <= bounds.getWidth(); x += 32) {
            graphics.drawLine(x, 0, x, bounds.getHeight());
        }
        for (int y = 0; y <= bounds.getHeight(); y += 32) {
            graphics.drawLine(0, y, bounds.getWidth(), y);
        }
    }

    private void drawWatermark(Graphics2D graphics, Bounds bounds) {
        AlphaComposite originalComposite = (AlphaComposite) graphics.getComposite();
        Font originalFont = graphics.getFont();
        Color originalColor = graphics.getColor();
        try {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, waterMarkOpacity.floatValue()));
            graphics.setColor(Color.decode(waterMarkColor));
            graphics.setFont(new Font("Microsoft YaHei", Font.BOLD, waterMarkFontSize));
            int centerX = bounds.getWidth() / 2;
            int centerY = bounds.getHeight() / 2;
            drawCenteredWatermark(graphics, waterMarkText, centerX, centerY);
        } finally {
            graphics.setComposite(originalComposite);
            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
        }
    }

    private void drawCenteredWatermark(Graphics2D graphics, String text, int centerX, int centerY) {
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int textHeight = graphics.getFontMetrics().getHeight();
        graphics.drawString(
                text,
                centerX - textWidth / 2,
                centerY + graphics.getFontMetrics().getAscent() - textHeight / 2
        );
    }

    private List<ProcessDesignerExportNodeRequest> resolveVisibleNodes(ExportContext context) {
        List<ProcessDesignerExportNodeRequest> visibleNodes = new ArrayList<>();
        for (ProcessDesignerExportNodeRequest node : context.getNodes()) {
            if (isNodeVisible(node, context.getNodeLookup())) {
                visibleNodes.add(node);
            }
        }
        return visibleNodes;
    }

    private boolean isNodeVisible(ProcessDesignerExportNodeRequest node,
                                  Map<String, ProcessDesignerExportNodeRequest> nodeLookup) {
        if (node == null) {
            return false;
        }
        String currentParentId = node.getParentId();
        int guard = 0;
        while (StringUtils.hasText(currentParentId) && guard < nodeLookup.size()) {
            ProcessDesignerExportNodeRequest parentNode = nodeLookup.get(currentParentId);
            if (parentNode == null) {
                return false;
            }
            if (!Boolean.TRUE.equals(parentNode.getExpanded())) {
                return false;
            }
            currentParentId = parentNode.getParentId();
            guard += 1;
        }
        return true;
    }

    private Bounds resolveVisibleBounds(ExportContext context) {
        List<ProcessDesignerExportNodeRequest> visibleNodes = resolveVisibleNodes(context);
        if (visibleNodes.isEmpty()) {
            return new Bounds(0, 0, Math.max(context.getCanvasWidth(), 320), Math.max(context.getCanvasHeight(), 220));
        }
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = 0;
        int bottom = 0;
        for (ProcessDesignerExportNodeRequest node : visibleNodes) {
            left = Math.min(left, node.getX());
            top = Math.min(top, node.getY());
            right = Math.max(right, node.getX() + node.getWidth());
            bottom = Math.max(bottom, node.getY() + node.getHeight());
        }
        int padding = 40;
        return new Bounds(
                Math.max(left - padding, 0),
                Math.max(top - padding, 0),
                Math.max(right - left + padding * 2, 320),
                Math.max(bottom - top + padding * 2, 220)
        );
    }

    private void drawVisibleEdges(Graphics2D graphics,
                                  List<ProcessDesignerExportEdgeRequest> edges,
                                  Map<String, ProcessDesignerExportNodeRequest> visibleNodeLookup,
                                  Bounds bounds) {
        graphics.setStroke(new BasicStroke(3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.setColor(new Color(122, 147, 184));
        for (ProcessDesignerExportEdgeRequest edge : edges) {
            ProcessDesignerExportNodeRequest sourceNode = visibleNodeLookup.get(edge.getSourceId());
            ProcessDesignerExportNodeRequest targetNode = visibleNodeLookup.get(edge.getTargetId());
            if (sourceNode == null || targetNode == null) {
                continue;
            }
            Point start = resolveNodeCenter(sourceNode, true);
            Point end = resolveNodeCenter(targetNode, false);
            EdgePoints points = buildEdgePoints(start, end);
            Path2D path = new Path2D.Float();
            path.moveTo(points.getStartX() - bounds.getLeft(), points.getStartY() - bounds.getTop());
            path.lineTo(points.getMidX() - bounds.getLeft(), points.getStartY() - bounds.getTop());
            path.lineTo(points.getEndX() - bounds.getLeft(), points.getEndY() - bounds.getTop());
            path.lineTo(points.getTargetX() - bounds.getLeft(), points.getTargetY() - bounds.getTop());
            graphics.draw(path);
            drawArrow(graphics,
                    points.getEndX() - bounds.getLeft(),
                    points.getEndY() - bounds.getTop(),
                    points.getTargetX() - bounds.getLeft(),
                    points.getTargetY() - bounds.getTop());
            if (StringUtils.hasText(edge.getName())) {
                graphics.setColor(new Color(85, 107, 139));
                graphics.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
                int labelX = Math.round((points.getStartX() + points.getTargetX()) / 2F - bounds.getLeft());
                int labelY = Math.round((points.getStartY() + points.getTargetY()) / 2F - bounds.getTop() - 10);
                graphics.drawString(edge.getName(), labelX, labelY);
                graphics.setColor(new Color(122, 147, 184));
            }
        }
    }

    private void drawVisibleNodes(Graphics2D graphics,
                                  List<ProcessDesignerExportNodeRequest> nodes,
                                  Bounds bounds) {
        for (ProcessDesignerExportNodeRequest node : nodes) {
            int x = node.getX() - bounds.getLeft();
            int y = node.getY() - bounds.getTop();
            if ("event".equals(node.getKind())) {
                drawEventNode(graphics, node, x, y);
            } else if ("gateway".equals(node.getKind())) {
                drawGatewayNode(graphics, node, x, y);
            } else {
                drawRectNode(graphics, node, x, y);
            }
            drawNodeBadge(graphics, node, x, y);
            drawNodeTexts(graphics, node, x, y);
        }
    }

    private void drawEventNode(Graphics2D graphics, ProcessDesignerExportNodeRequest node, int x, int y) {
        graphics.setPaint(new GradientPaint(x, y, new Color(239, 252, 245), x, y + node.getHeight(), new Color(220, 252, 231)));
        graphics.fillOval(x, y, node.getWidth(), node.getHeight());
        graphics.setColor(new Color(207, 224, 246));
        graphics.setStroke(new BasicStroke(2F));
        graphics.drawOval(x, y, node.getWidth(), node.getHeight());
        int iconDiameter = Math.min(36, Math.max(24, node.getWidth() / 2));
        int iconX = x + (node.getWidth() - iconDiameter) / 2;
        int iconY = y + 14;
        graphics.setColor(new Color(255, 255, 255, 235));
        graphics.fillOval(iconX, iconY, iconDiameter, iconDiameter);
        graphics.setColor(new Color(16, 163, 127, 107));
        graphics.drawOval(iconX, iconY, iconDiameter, iconDiameter);
    }

    private void drawGatewayNode(Graphics2D graphics, ProcessDesignerExportNodeRequest node, int x, int y) {
        int centerX = x + node.getWidth() / 2;
        int centerY = y + node.getHeight() / 2;
        Polygon polygon = new Polygon(
                new int[]{centerX, x + node.getWidth(), centerX, x},
                new int[]{y, centerY, y + node.getHeight(), centerY},
                4
        );
        graphics.setPaint(new GradientPaint(x, y, new Color(255, 248, 236), x, y + node.getHeight(), new Color(255, 237, 213)));
        graphics.fillPolygon(polygon);
        graphics.setColor(new Color(207, 224, 246));
        graphics.setStroke(new BasicStroke(2F));
        graphics.drawPolygon(polygon);
        Polygon icon = new Polygon(
                new int[]{centerX, centerX + 18, centerX, centerX - 18},
                new int[]{centerY - 22, centerY - 4, centerY + 14, centerY - 4},
                4
        );
        graphics.setColor(new Color(255, 255, 255, 235));
        graphics.fillPolygon(icon);
        graphics.setColor(new Color(245, 158, 11, 97));
        graphics.drawPolygon(icon);
    }

    private void drawRectNode(Graphics2D graphics, ProcessDesignerExportNodeRequest node, int x, int y) {
        Color startColor = new Color(248, 251, 255);
        Color endColor = new Color(231, 240, 255);
        if ("container".equals(node.getKind())) {
            startColor = new Color(245, 247, 255);
            endColor = new Color(233, 237, 255);
        } else if ("artifact".equals(node.getKind())) {
            startColor = new Color(255, 253, 245);
            endColor = new Color(255, 248, 217);
        }
        RoundRectangle2D rectangle = new RoundRectangle2D.Float(x, y, node.getWidth(), node.getHeight(), 20, 20);
        graphics.setPaint(new GradientPaint(x, y, startColor, x, y + node.getHeight(), endColor));
        graphics.fill(rectangle);
        if ("container".equals(node.getKind()) || "artifact".equals(node.getKind())) {
            graphics.setStroke(new BasicStroke(2F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10F, new float[]{8F, 4F}, 0F));
        } else {
            graphics.setStroke(new BasicStroke(2F));
        }
        graphics.setColor(new Color(207, 224, 246));
        graphics.draw(rectangle);
        int iconWidth = 52;
        int iconHeight = 32;
        int iconX = x + (node.getWidth() - iconWidth) / 2;
        int iconY = y + 12;
        RoundRectangle2D iconRectangle = new RoundRectangle2D.Float(iconX, iconY, iconWidth, iconHeight, "task".equals(node.getKind()) ? 12 : 10, "task".equals(node.getKind()) ? 12 : 10);
        graphics.setColor(new Color(255, 255, 255, 235));
        graphics.fill(iconRectangle);
        if ("container".equals(node.getKind()) || "artifact".equals(node.getKind())) {
            graphics.setStroke(new BasicStroke(2F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10F, new float[]{6F, 4F}, 0F));
        } else {
            graphics.setStroke(new BasicStroke(2F));
        }
        graphics.setColor(resolveIconStrokeColor(node.getKind()));
        graphics.draw(iconRectangle);
    }

    private Color resolveIconStrokeColor(String kind) {
        if ("container".equals(kind)) {
            return new Color(99, 102, 241, 87);
        }
        if ("artifact".equals(kind)) {
            return new Color(217, 119, 6, 87);
        }
        return new Color(52, 119, 246, 71);
    }

    private void drawNodeBadge(Graphics2D graphics, ProcessDesignerExportNodeRequest node, int x, int y) {
        int badgeWidth = Math.min(Math.max(node.getBpmnType().length() * 8 + 24, 72), Math.max(node.getWidth() - 16, 72));
        int badgeX = x + node.getWidth() / 2 - badgeWidth / 2;
        int badgeY = y - 14;
        graphics.setColor(new Color(233, 241, 255));
        graphics.fillRoundRect(badgeX, badgeY, badgeWidth, 24, 12, 12);
        graphics.setColor(new Color(42, 97, 191));
        graphics.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        drawCenteredText(graphics, node.getBpmnType(), badgeX, badgeY + 5, badgeWidth, 16);
    }

    private void drawNodeTexts(Graphics2D graphics, ProcessDesignerExportNodeRequest node, int x, int y) {
        int centerX = x + node.getWidth() / 2;
        int titleY = y + node.getHeight() / 2 + 8;
        int subtitleY = titleY + 20;
        graphics.setColor(new Color(29, 45, 70));
        graphics.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        drawCenteredText(graphics, firstText(node.getName(), node.getLabel()), centerX - node.getWidth() / 2 + 8, titleY - 12, Math.max(node.getWidth() - 16, 60), 16);
        graphics.setColor(new Color(109, 128, 157));
        graphics.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        drawCenteredText(graphics, firstText(node.getLabel(), ""), centerX - node.getWidth() / 2 + 8, subtitleY - 12, Math.max(node.getWidth() - 16, 60), 16);
    }

    private void drawCenteredText(Graphics2D graphics, String text, int x, int y, int width, int height) {
        String displayText = ellipsisText(graphics, firstText(text, ""), width);
        int textWidth = graphics.getFontMetrics().stringWidth(displayText);
        int textX = x + Math.max((width - textWidth) / 2, 0);
        int textY = y + ((height - graphics.getFontMetrics().getHeight()) / 2) + graphics.getFontMetrics().getAscent();
        graphics.drawString(displayText, textX, textY);
    }

    private String ellipsisText(Graphics2D graphics, String text, int maxWidth) {
        if (graphics.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        String value = text;
        while (value.length() > 1 && graphics.getFontMetrics().stringWidth(value + suffix) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value + suffix;
    }

    private void drawArrow(Graphics2D graphics, int fromX, int fromY, int toX, int toY) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        int arrowLength = 10;
        int arrowWidth = 5;
        int x1 = (int) (toX - arrowLength * Math.cos(angle - Math.PI / 6));
        int y1 = (int) (toY - arrowLength * Math.sin(angle - Math.PI / 6));
        int x2 = (int) (toX - arrowLength * Math.cos(angle + Math.PI / 6));
        int y2 = (int) (toY - arrowLength * Math.sin(angle + Math.PI / 6));
        Polygon arrow = new Polygon(new int[]{toX, x1, x2}, new int[]{toY, y1, y2}, 3);
        graphics.fillPolygon(arrow);
    }

    private Point resolveNodeCenter(ProcessDesignerExportNodeRequest node, boolean source) {
        return new Point(
                source ? node.getX() + node.getWidth() : node.getX(),
                node.getY() + node.getHeight() / 2
        );
    }

    private EdgePoints buildEdgePoints(Point start, Point end) {
        int turnOffset = Math.max(36, Math.min(84, Math.abs(end.getX() - start.getX()) / 2));
        int midX = start.getX() + turnOffset;
        int endX = end.getX() - turnOffset;
        if (end.getX() <= start.getX() + 24) {
            midX = start.getX() + 40;
            endX = end.getX() - 40;
        }
        return new EdgePoints(start.getX(), start.getY(), midX, endX, end.getX(), end.getY());
    }

    private <T> List<T> safeList(List<T> source) {
        return source == null ? List.of() : source;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExportContext {

        private Integer canvasWidth;

        private Integer canvasHeight;

        private List<ProcessDesignerExportNodeRequest> nodes;

        private List<ProcessDesignerExportEdgeRequest> edges;

        private Map<String, ProcessDesignerExportNodeRequest> nodeLookup;

        private List<String> skippedNodeLabels;
    }

    @Data
    @AllArgsConstructor
    private static class ContainerMaps {

        private Map<String, List<ProcessDesignerExportNodeRequest>> nodeChildrenMap;

        private Map<String, List<SequenceFlowExport>> flowChildrenMap;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SequenceFlowExport {

        private String id;

        private String sourceId;

        private String targetId;

        private String name;

        private String containerId;
    }

    @Data
    @AllArgsConstructor
    private static class Point {

        private int x;

        private int y;
    }

    @Data
    @AllArgsConstructor
    private static class EdgePoints {

        private int startX;

        private int startY;

        private int midX;

        private int endX;

        private int targetX;

        private int targetY;

        public int getEndY() {
            return targetY;
        }
    }

    @Data
    @AllArgsConstructor
    private static class Bounds {

        private int left;

        private int top;

        private int width;

        private int height;
    }
}
