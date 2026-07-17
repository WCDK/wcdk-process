/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
var processDiagramStyleId = "process-diagram-inline-style";

window.ProcessDiagram = {
    name: "process-diagram",
    template: `
        <div class="process-diagram-shell">
            <a>鼠标悬浮节点两秒，可查看该节点完整链路</a>
             <a>单击节点可查看节点详情</a>
            <div class="process-diagram-panel" v-if="hasDiagramData">
                <div class="process-diagram-toolbar">
                    <div class="process-diagram-summary">
                        <span class="process-diagram-badge process-diagram-badge-primary">节点 {{ nodeCount }}</span>
                        <span class="process-diagram-badge">连线 {{ flowCount }}</span>
                        <span class="process-diagram-badge process-diagram-badge-success" v-if="activeCount">当前节点 {{ activeCount }}</span>
                    </div>
                    <div class="process-diagram-legend">
                        <span class="process-diagram-legend-item">
                            <i class="legend-dot legend-dot-start"></i>
                            开始 / 结束
                        </span>
                        <span class="process-diagram-legend-item">
                            <i class="legend-dot legend-dot-task"></i>
                            任务节点
                        </span>
                        <span class="process-diagram-legend-item">
                            <i class="legend-dot legend-dot-gateway"></i>
                            网关节点
                        </span>
                        <span class="process-diagram-legend-item" v-if="activeCount">
                            <i class="legend-dot legend-dot-active"></i>
                            当前高亮
                        </span>
                    </div>
                </div>
                <div class="process-canvas-wrapper" ref="canvasWrapper">
                    <canvas
                        ref="canvas"
                        @mousemove="handleCanvasMouseMove"
                        @mouseleave="handleCanvasMouseLeave"
                        @click="handleCanvasClick"
                        class="process-diagram-canvas">
                    </canvas>
                    <div
                        v-if="hoverTooltip.visible && hoverTooltip.node"
                        ref="tooltip"
                        class="process-diagram-tooltip"
                        :style="{ left: hoverTooltip.left + 'px', top: hoverTooltip.top + 'px', transform: hoverTooltip.transform }">
                        <div class="process-diagram-tooltip-title">{{ resolveTooltipTitle(hoverTooltip.node) }}</div>
                        <div class="process-diagram-tooltip-row">
                            <span class="process-diagram-tooltip-label">类型</span>
                            <span>{{ resolveNodeTypeLabel(hoverTooltip.node.elementType) || '-' }}</span>
                        </div>
                        <div class="process-diagram-tooltip-row">
                            <span class="process-diagram-tooltip-label">标识</span>
                            <span>{{ hoverTooltip.node.elementId || '-' }}</span>
                        </div>
                    </div>
                </div>
                <el-dialog
                    title="节点详情"
                    :visible.sync="nodeDetailVisible"
                    width="520px">
                    <div v-if="selectedNode" class="process-node-detail">
                        <div class="process-node-detail-row">
                            <span>节点名称</span>
                            <strong>{{ selectedNode.elementName || selectedNode.elementId || "-" }}</strong>
                        </div>
                        <div class="process-node-detail-row">
                            <span>节点标识</span>
                            <strong>{{ selectedNode.elementId || "-" }}</strong>
                        </div>
                        <div class="process-node-detail-row">
                            <span>节点类型</span>
                            <strong>{{ resolveNodeTypeLabel(selectedNode.elementType) || selectedNode.elementType || "-" }}</strong>
                        </div>
                        <div
                            v-for="item in resolveNodePropertySummaries(selectedNode)"
                            :key="item.label"
                            class="process-node-detail-row">
                            <span>{{ item.label }}</span>
                            <strong>{{ item.value }}</strong>
                        </div>
                        <div class="process-node-detail-row">
                            <span>入口数量</span>
                            <strong>{{ selectedNode.incomingCount || 0 }}</strong>
                        </div>
                        <div class="process-node-detail-row">
                            <span>出口数量</span>
                            <strong>{{ selectedNode.outgoingCount || 0 }}</strong>
                        </div>
                        <div class="process-node-detail-desc" v-if="selectedNode.documentation">
                            <span>节点说明</span>
                            <p>{{ selectedNode.documentation }}</p>
                        </div>
                    </div>
                </el-dialog>
            </div>
            <div class="empty-panel process-diagram-empty" v-else>
                未加载到流程图数据，请稍后重试
            </div>
        </div>
    `,
    props: {
        detail: { type: Object },
        activeNodeIds: {
            type: Array,
            default: function () {
                return [];
            }
        }
    },
    data: function () {
        return {
            renderedNodes: [],
            canvasViewport: {
                minX: 0,
                minY: 0,
                padding: 48
            },
            hoverTooltip: {
                visible: false,
                left: 0,
                top: 0,
                transform: "translate(12px,12px)",
                node: null
            },
            nodeDetailVisible: false,
            selectedNode: null,
            hoverPathTimer: null,
            hoverPathNodeId: "",
            hoverPath: {
                nodeIds: [],
                edgeIds: []
            }
        };
    },
    computed: {
        hasDiagramData: function () {
            return !!(this.detail && Array.isArray(this.detail.nodes) && this.detail.nodes.length);
        },
        nodeCount: function () {
            return this.hasDiagramData ? this.detail.nodes.length : 0;
        },
        flowCount: function () {
            return this.detail && Array.isArray(this.detail.sequenceFlows) ? this.detail.sequenceFlows.length : 0;
        },
        activeCount: function () {
            return Array.isArray(this.activeNodeIds) ? this.activeNodeIds.length : 0;
        }
    },
    methods: {
        ensureStyle: function () {
            if (document.getElementById(processDiagramStyleId)) {
                return;
            }
            var style = document.createElement("style");
            style.id = processDiagramStyleId;
            style.textContent = [
                ".process-diagram-shell{width:100%;}",
                ".process-diagram-panel{padding:14px;border:1px solid #dbe6f3;border-radius:20px;background:linear-gradient(180deg,#fbfdff 0%,#f4f8fc 100%);box-shadow:inset 0 1px 0 rgba(255,255,255,0.8);}",
                ".process-diagram-toolbar{display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap;align-items:center;margin-bottom:12px;}",
                ".process-diagram-summary,.process-diagram-legend{display:flex;gap:8px;flex-wrap:wrap;align-items:center;}",
                ".process-diagram-badge{display:inline-flex;align-items:center;padding:6px 12px;border-radius:999px;background:#edf3fb;color:#48617f;font-size:12px;font-weight:600;}",
                ".process-diagram-badge-primary{background:rgba(52,119,246,0.12);color:#2563eb;}",
                ".process-diagram-badge-success{background:rgba(16,163,127,0.14);color:#0f9f75;}",
                ".process-diagram-legend-item{display:inline-flex;align-items:center;gap:6px;color:#6b7f99;font-size:12px;}",
                ".legend-dot{width:10px;height:10px;border-radius:50%;display:inline-block;box-shadow:0 0 0 3px rgba(255,255,255,0.78);}",
                ".legend-dot-start{background:#10b981;}",
                ".legend-dot-task{background:#3b82f6;}",
                ".legend-dot-gateway{background:#f59e0b;}",
                ".legend-dot-active{background:#22c55e;}",
                ".process-canvas-wrapper{position:relative;overflow:auto;padding:10px;border-radius:16px;background:linear-gradient(180deg,rgba(255,255,255,0.96) 0%,rgba(244,248,252,0.98) 100%);border:1px solid rgba(219,230,243,0.9);}",
                ".process-diagram-canvas{display:block;max-width:none;border-radius:14px;}",
                ".process-diagram-tooltip{position:absolute;z-index:3;min-width:180px;max-width:260px;padding:12px 14px;border-radius:14px;background:rgba(15,23,42,0.94);color:#f8fafc;box-shadow:0 18px 40px rgba(15,23,42,0.24);pointer-events:none;}",
                ".process-diagram-tooltip-title{margin-bottom:8px;font-size:13px;font-weight:700;line-height:1.5;word-break:break-word;}",
                ".process-diagram-tooltip-row{display:flex;gap:10px;justify-content:space-between;font-size:12px;line-height:1.6;color:#dbe7f5;}",
                ".process-diagram-tooltip-label{color:#93a8c3;flex-shrink:0;}",
                ".process-node-detail{display:grid;gap:10px;}",
                ".process-node-detail-row{display:flex;justify-content:space-between;gap:16px;padding:10px 12px;border:1px solid #e5edf7;border-radius:10px;background:#f8fbff;}",
                ".process-node-detail-row span,.process-node-detail-desc span{color:#64748b;font-size:13px;flex-shrink:0;}",
                ".process-node-detail-row strong{color:#1f2a44;font-size:13px;text-align:right;word-break:break-word;}",
                ".process-node-detail-desc{padding:10px 12px;border:1px solid #e5edf7;border-radius:10px;background:#f8fbff;}",
                ".process-node-detail-desc p{margin:8px 0 0;color:#1f2a44;line-height:1.7;word-break:break-word;}",
                ".process-diagram-empty{padding:36px 18px;border:1px dashed #d8e3f4;border-radius:18px;background:#f8fbff;}"
            ].join("");
            document.head.appendChild(style);
        },
        handleResize: function () {
            this.renderCanvas();
        },
        renderCanvas: function (preserveTooltip) {
            if (!this.hasDiagramData) {
                return;
            }
            this.renderDiagramCanvas(this.$refs.canvas, this.detail, this.activeNodeIds || [], !!preserveTooltip);
        },
        renderDiagramCanvas: function (canvas, detail, activeNodeIds, preserveTooltip) {
            if (!canvas || !detail || !Array.isArray(detail.nodes) || !detail.nodes.length) {
                return;
            }
            var nodes = detail.nodes.slice();
            var padding = 48;
            var minX = Number.MAX_SAFE_INTEGER;
            var minY = Number.MAX_SAFE_INTEGER;
            var maxX = Number.MIN_SAFE_INTEGER;
            var maxY = Number.MIN_SAFE_INTEGER;
            for (var i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                var nodeX = typeof node.x === "number" ? node.x : 0;
                var nodeY = typeof node.y === "number" ? node.y : 0;
                var nodeWidth = typeof node.width === "number" ? node.width : 132;
                var nodeHeight = typeof node.height === "number" ? node.height : 64;
                minX = Math.min(minX, nodeX);
                minY = Math.min(minY, nodeY);
                maxX = Math.max(maxX, nodeX + nodeWidth);
                maxY = Math.max(maxY, nodeY + nodeHeight);
            }
            var wrapper = canvas.parentNode;
            var logicalContentWidth = Math.max(640, Math.ceil(maxX - minX + padding * 2));
            var logicalHeight = Math.max(360, Math.ceil(maxY - minY + padding * 2));
            var visibleWidth = wrapper ? Math.max(wrapper.clientWidth - 20, 0) : 0;
            var logicalWidth = Math.max(logicalContentWidth, visibleWidth || 0);
            var pixelRatio = window.devicePixelRatio || 1;
            canvas.width = logicalWidth * pixelRatio;
            canvas.height = logicalHeight * pixelRatio;
            canvas.style.width = logicalWidth + "px";
            canvas.style.height = logicalHeight + "px";
            var context = canvas.getContext("2d");
            context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
            context.clearRect(0, 0, logicalWidth, logicalHeight);
            this.renderedNodes = nodes;
            this.canvasViewport = {
                minX: minX,
                minY: minY,
                padding: padding
            };
            if (!preserveTooltip) {
                this.hideTooltip();
            }
            this.drawCanvasBackground(context, logicalWidth, logicalHeight);
            this.drawSubProcessContainers(context, nodes, minX, minY, padding, activeNodeIds || []);
            this.drawProcessEdges(context, detail.sequenceFlows || [], nodes, minX, minY, padding, activeNodeIds || []);
            this.drawProcessNodes(context, nodes, minX, minY, padding, activeNodeIds || []);
        },
        handleCanvasMouseMove: function (event) {
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper || !this.renderedNodes.length) {
                return;
            }
            var wrapperRect = wrapper.getBoundingClientRect();
            var offsetX = event.clientX - wrapperRect.left + wrapper.scrollLeft - 10;
            var offsetY = event.clientY - wrapperRect.top + wrapper.scrollTop - 10;
            var hoveredNode = this.findNodeAtPosition(offsetX, offsetY);
            if (!hoveredNode) {
                this.clearHoverPath(true);
                this.hideTooltip();
                return;
            }
            this.scheduleHoverPath(hoveredNode);
            this.hoverTooltip = {
                visible: true,
                left: offsetX,
                top: offsetY,
                transform: "translate(12px,12px)",
                node: hoveredNode
            };
            this.$nextTick(function () {
                this.repositionTooltip(offsetX, offsetY);
            });
        },
        handleCanvasMouseLeave: function () {
            this.clearHoverPath(true);
            this.hideTooltip();
        },
        handleCanvasClick: function (event) {
            var node = this.resolveNodeFromCanvasEvent(event);
            if (!node) {
                return;
            }
            this.selectedNode = node;
            this.nodeDetailVisible = true;
        },
        resolveNodeFromCanvasEvent: function (event) {
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper || !this.renderedNodes.length) {
                return null;
            }
            var wrapperRect = wrapper.getBoundingClientRect();
            var offsetX = event.clientX - wrapperRect.left + wrapper.scrollLeft - 10;
            var offsetY = event.clientY - wrapperRect.top + wrapper.scrollTop - 10;
            return this.findNodeAtPosition(offsetX, offsetY);
        },
        hideTooltip: function () {
            this.hoverTooltip.visible = false;
            this.hoverTooltip.node = null;
            this.hoverTooltip.transform = "translate(12px,12px)";
        },
        scheduleHoverPath: function (node) {
            var nodeId = node && node.elementId ? node.elementId : "";
            if (!nodeId || nodeId === this.hoverPathNodeId) {
                return;
            }
            this.clearHoverPath(true);
            this.hoverPathNodeId = nodeId;
            this.hoverPathTimer = window.setTimeout(function () {
                this.hoverPath = this.buildHoverPath(nodeId);
                this.renderCanvas(true);
            }.bind(this), 2000);
        },
        clearHoverPath: function (rerender) {
            if (this.hoverPathTimer) {
                window.clearTimeout(this.hoverPathTimer);
                this.hoverPathTimer = null;
            }
            var hadPath = this.hoverPath.nodeIds.length || this.hoverPath.edgeIds.length;
            this.hoverPathNodeId = "";
            this.hoverPath = {
                nodeIds: [],
                edgeIds: []
            };
            if (rerender && hadPath) {
                this.renderCanvas(true);
            }
        },
        repositionTooltip: function (anchorX, anchorY) {
            var wrapper = this.$refs.canvasWrapper;
            var tooltip = this.$refs.tooltip;
            if (!wrapper || !tooltip || !this.hoverTooltip.visible) {
                return;
            }
            var gap = 12;
            var tooltipWidth = tooltip.offsetWidth || 220;
            var tooltipHeight = tooltip.offsetHeight || 96;
            var viewportLeft = wrapper.scrollLeft;
            var viewportTop = wrapper.scrollTop;
            var viewportRight = viewportLeft + wrapper.clientWidth;
            var viewportBottom = viewportTop + wrapper.clientHeight;
            var left = anchorX;
            var top = anchorY;
            var transformX = gap;
            var transformY = gap;

            if (anchorX + gap + tooltipWidth > viewportRight && anchorX - gap - tooltipWidth >= viewportLeft) {
                transformX = -tooltipWidth - gap;
            }
            if (anchorY + gap + tooltipHeight > viewportBottom && anchorY - gap - tooltipHeight >= viewportTop) {
                transformY = -tooltipHeight - gap;
            }

            var finalLeft = left + transformX;
            var finalTop = top + transformY;

            if (finalLeft < viewportLeft + 6) {
                left = viewportLeft + 6;
                transformX = 0;
            } else if (finalLeft + tooltipWidth > viewportRight - 6) {
                left = Math.max(viewportLeft + 6, viewportRight - tooltipWidth - 6);
                transformX = 0;
            }

            if (finalTop < viewportTop + 6) {
                top = viewportTop + 6;
                transformY = 0;
            } else if (finalTop + tooltipHeight > viewportBottom - 6) {
                top = Math.max(viewportTop + 6, viewportBottom - tooltipHeight - 6);
                transformY = 0;
            }

            this.hoverTooltip.left = left;
            this.hoverTooltip.top = top;
            this.hoverTooltip.transform = "translate(" + transformX + "px," + transformY + "px)";
        },
        findNodeAtPosition: function (offsetX, offsetY) {
            for (var i = this.renderedNodes.length - 1; i >= 0; i -= 1) {
                var node = this.renderedNodes[i];
                var bounds = this.resolveNodeBounds(node, this.canvasViewport.minX, this.canvasViewport.minY, this.canvasViewport.padding);
                if (this.isPointInsideNode(offsetX, offsetY, bounds, node.elementType)) {
                    return node;
                }
            }
            return null;
        },
        resolveNodeBounds: function (node, minX, minY, padding) {
            return {
                x: (node.x || 0) - minX + padding,
                y: (node.y || 0) - minY + padding,
                width: node.width || 132,
                height: node.height || 64
            };
        },
        isPointInsideNode: function (pointX, pointY, bounds, elementType) {
            if (elementType === "StartEvent" || elementType === "EndEvent") {
                var radius = Math.min(bounds.width, bounds.height) / 2;
                var centerX = bounds.x + bounds.width / 2;
                var centerY = bounds.y + bounds.height / 2;
                var dx = pointX - centerX;
                var dy = pointY - centerY;
                return dx * dx + dy * dy <= radius * radius;
            }
            if (elementType === "ExclusiveGateway" || elementType === "ParallelGateway") {
                var diamondCenterX = bounds.x + bounds.width / 2;
                var diamondCenterY = bounds.y + bounds.height / 2;
                var normalizedX = Math.abs(pointX - diamondCenterX) / (bounds.width / 2);
                var normalizedY = Math.abs(pointY - diamondCenterY) / (bounds.height / 2);
                return normalizedX + normalizedY <= 1;
            }
            return pointX >= bounds.x && pointX <= bounds.x + bounds.width && pointY >= bounds.y && pointY <= bounds.y + bounds.height;
        },
        buildHoverPath: function (nodeId) {
            var detail = this.detail || {};
            var sequenceFlows = Array.isArray(detail.sequenceFlows) ? detail.sequenceFlows : [];
            var nodeMap = {};
            var incomingMap = {};
            var outgoingMap = {};
            var nodeIds = {};
            var edgeIds = {};
            (detail.nodes || []).forEach(function (node) {
                if (node.elementId) {
                    nodeMap[node.elementId] = true;
                }
            });
            sequenceFlows.forEach(function (edge) {
                if (!edge.sourceRef || !edge.targetRef) {
                    return;
                }
                if (!incomingMap[edge.targetRef]) {
                    incomingMap[edge.targetRef] = [];
                }
                if (!outgoingMap[edge.sourceRef]) {
                    outgoingMap[edge.sourceRef] = [];
                }
                incomingMap[edge.targetRef].push(edge);
                outgoingMap[edge.sourceRef].push(edge);
            });
            if (!nodeMap[nodeId]) {
                return { nodeIds: [], edgeIds: [] };
            }
            nodeIds[nodeId] = true;
            this.collectHoverPath(nodeId, incomingMap, "sourceRef", nodeIds, edgeIds);
            this.collectHoverPath(nodeId, outgoingMap, "targetRef", nodeIds, edgeIds);
            return {
                nodeIds: Object.keys(nodeIds),
                edgeIds: Object.keys(edgeIds)
            };
        },
        collectHoverPath: function (startNodeId, edgeMap, nextNodeField, nodeIds, edgeIds) {
            var queue = [startNodeId];
            var visited = {};
            while (queue.length) {
                var currentNodeId = queue.shift();
                if (visited[currentNodeId]) {
                    continue;
                }
                visited[currentNodeId] = true;
                var edges = edgeMap[currentNodeId] || [];
                for (var i = 0; i < edges.length; i += 1) {
                    var edge = edges[i];
                    var nextNodeId = edge[nextNodeField];
                    if (!nextNodeId) {
                        continue;
                    }
                    edgeIds[this.resolveEdgeId(edge)] = true;
                    nodeIds[nextNodeId] = true;
                    queue.push(nextNodeId);
                }
            }
        },
        resolveEdgeId: function (edge) {
            return edge.elementId || ((edge.sourceRef || "") + "->" + (edge.targetRef || ""));
        },
        buildValueMap: function (values) {
            var result = {};
            (values || []).forEach(function (value) {
                if (value) {
                    result[value] = true;
                }
            });
            return result;
        },
        drawCanvasBackground: function (context, width, height) {
            var background = context.createLinearGradient(0, 0, 0, height);
            background.addColorStop(0, "#fcfdff");
            background.addColorStop(1, "#eef5fb");
            context.save();
            context.fillStyle = background;
            context.fillRect(0, 0, width, height);
            context.strokeStyle = "rgba(148, 163, 184, 0.12)";
            context.lineWidth = 1;
            for (var x = 24; x < width; x += 32) {
                context.beginPath();
                context.moveTo(x, 0);
                context.lineTo(x, height);
                context.stroke();
            }
            for (var y = 24; y < height; y += 32) {
                context.beginPath();
                context.moveTo(0, y);
                context.lineTo(width, y);
                context.stroke();
            }
            context.restore();
        },
        drawProcessEdges: function (context, sequenceFlows, nodes, minX, minY, padding, activeNodeIds) {
            if (!sequenceFlows || !sequenceFlows.length) {
                return;
            }
            var nodeMap = {};
            var activeNodeMap = {};
            var hoverEdgeMap = this.buildValueMap(this.hoverPath.edgeIds);
            var i;
            for (i = 0; i < nodes.length; i += 1) {
                nodeMap[nodes[i].elementId] = nodes[i];
            }
            for (i = 0; i < activeNodeIds.length; i += 1) {
                activeNodeMap[activeNodeIds[i]] = true;
            }
            for (var j = 0; j < sequenceFlows.length; j += 1) {
                var edge = sequenceFlows[j];
                var sourceNode = nodeMap[edge.sourceRef];
                var targetNode = nodeMap[edge.targetRef];
                if (!sourceNode || !targetNode) {
                    continue;
                }
                var isHoverEdge = !!hoverEdgeMap[this.resolveEdgeId(edge)];
                var isActiveEdge = !!(activeNodeMap[edge.sourceRef] || activeNodeMap[edge.targetRef]);
                context.save();
                context.lineWidth = isHoverEdge ? 4 : (isActiveEdge ? 3 : 2);
                context.strokeStyle = isHoverEdge ? "#f97316" : (isActiveEdge ? "#22c55e" : "#8fa3bf");
                context.shadowColor = isHoverEdge ? "rgba(249,115,22,0.32)" : (isActiveEdge ? "rgba(34,197,94,0.24)" : "rgba(15,23,42,0.08)");
                context.shadowBlur = isHoverEdge ? 16 : (isActiveEdge ? 12 : 4);
                if (Array.isArray(edge.waypoints) && edge.waypoints.length >= 2) {
                    var normalizedWaypoints = this.normalizeEdgeWaypoints(edge.waypoints, minX, minY, padding);
                    this.drawEdgePathByWaypoints(context, normalizedWaypoints);
                    var lastPoint = normalizedWaypoints[normalizedWaypoints.length - 1];
                    this.drawArrowHead(context, lastPoint.x, lastPoint.y, isHoverEdge ? "#f97316" : (isActiveEdge ? "#22c55e" : "#8fa3bf"));
                } else {
                    var startPoint = this.resolveExitPoint(sourceNode, minX, minY, padding);
                    var endPoint = this.resolveEntryPoint(targetNode, minX, minY, padding);
                    var turnOffset = Math.max(28, Math.min(54, Math.abs(endPoint.x - startPoint.x) / 2));
                    var midX = startPoint.x + turnOffset;
                    var endTurnX = endPoint.x - turnOffset;
                    context.beginPath();
                    context.moveTo(startPoint.x, startPoint.y);
                    if (endPoint.x <= startPoint.x + 24) {
                        var detourX = startPoint.x + 28;
                        context.lineTo(detourX, startPoint.y);
                        context.quadraticCurveTo(detourX + 10, startPoint.y, detourX + 10, startPoint.y + (endPoint.y > startPoint.y ? 10 : -10));
                        context.lineTo(detourX + 10, endPoint.y - (endPoint.y > startPoint.y ? 10 : -10));
                        context.quadraticCurveTo(detourX + 10, endPoint.y, detourX + 20, endPoint.y);
                        context.lineTo(endPoint.x, endPoint.y);
                    } else {
                        context.lineTo(midX - 10, startPoint.y);
                        context.quadraticCurveTo(midX, startPoint.y, midX, startPoint.y + (endPoint.y > startPoint.y ? 10 : -10));
                        context.lineTo(endTurnX, endPoint.y - (endPoint.y > startPoint.y ? 10 : -10));
                        context.quadraticCurveTo(endTurnX, endPoint.y, endTurnX + 10, endPoint.y);
                        context.lineTo(endPoint.x, endPoint.y);
                    }
                    context.stroke();
                    this.drawArrowHead(context, endPoint.x, endPoint.y, isHoverEdge ? "#f97316" : (isActiveEdge ? "#22c55e" : "#8fa3bf"));
                }
                context.restore();
            }
        },
        drawSubProcessContainers: function (context, nodes, minX, minY, padding, activeNodeIds) {
            if (!nodes || !nodes.length) {
                return;
            }
            var activeNodeMap = {};
            var hoverNodeMap = this.buildValueMap(this.hoverPath.nodeIds);
            for (var i = 0; i < activeNodeIds.length; i += 1) {
                activeNodeMap[activeNodeIds[i]] = true;
            }
            for (var j = 0; j < nodes.length; j += 1) {
                var node = nodes[j];
                if (node.elementType !== "SubProcess") {
                    continue;
                }
                var x = (node.x || 0) - minX + padding;
                var y = (node.y || 0) - minY + padding;
                var width = node.width || 132;
                var height = node.height || 64;
                var isActive = !!activeNodeMap[node.elementId];
                var isHoverPathNode = !!hoverNodeMap[node.elementId];
                var theme = this.resolveNodeTheme(node, isActive, isHoverPathNode);
                context.save();
                context.shadowColor = isHoverPathNode ? "rgba(249,115,22,0.24)" : (isActive ? "rgba(34,197,94,0.16)" : "rgba(15,23,42,0.06)");
                context.shadowBlur = isHoverPathNode ? 18 : (isActive ? 14 : 8);
                context.shadowOffsetY = isHoverPathNode ? 10 : (isActive ? 8 : 4);
                context.fillStyle = this.createNodeGradient(context, x, y, width, height, theme.fillStart, theme.fillEnd);
                context.strokeStyle = theme.stroke;
                context.lineWidth = isHoverPathNode ? 4 : (isActive ? 3 : 2);
                this.drawRoundedRect(context, x, y, width, height, 18);
                context.fill();
                context.stroke();
                context.restore();
                this.drawNodeText(context, node, x, y, width, height, theme, isActive || isHoverPathNode);
            }
        },
        normalizeEdgeWaypoints: function (waypoints, minX, minY, padding) {
            var result = [];
            for (var i = 0; i < waypoints.length; i += 1) {
                var point = waypoints[i] || {};
                result.push({
                    x: (Number(point.x) || 0) - minX + padding,
                    y: (Number(point.y) || 0) - minY + padding
                });
            }
            return result;
        },
        drawEdgePathByWaypoints: function (context, waypoints) {
            if (!Array.isArray(waypoints) || waypoints.length < 2) {
                return;
            }
            context.beginPath();
            context.moveTo(waypoints[0].x, waypoints[0].y);
            for (var i = 1; i < waypoints.length; i += 1) {
                context.lineTo(waypoints[i].x, waypoints[i].y);
            }
            context.stroke();
        },
        resolveExitPoint: function (node, minX, minY, padding) {
            var x = (node.x || 0) - minX + padding;
            var y = (node.y || 0) - minY + padding;
            var width = node.width || 132;
            var height = node.height || 64;
            if (node.elementType === "ExclusiveGateway" || node.elementType === "ParallelGateway") {
                return { x: x + width, y: y + height / 2 };
            }
            return { x: x + width, y: y + height / 2 };
        },
        resolveEntryPoint: function (node, minX, minY, padding) {
            var x = (node.x || 0) - minX + padding;
            var y = (node.y || 0) - minY + padding;
            var width = node.width || 132;
            var height = node.height || 64;
            if (node.elementType === "ExclusiveGateway" || node.elementType === "ParallelGateway") {
                return { x: x, y: y + height / 2 };
            }
            return { x: x, y: y + height / 2 };
        },
        drawArrowHead: function (context, x, y, color) {
            context.save();
            context.fillStyle = color;
            context.beginPath();
            context.moveTo(x + 1, y);
            context.lineTo(x - 11, y - 6);
            context.lineTo(x - 11, y + 6);
            context.closePath();
            context.fill();
            context.restore();
        },
        drawProcessNodes: function (context, nodes, minX, minY, padding, activeNodeIds) {
            var activeNodeMap = {};
            var hoverNodeMap = this.buildValueMap(this.hoverPath.nodeIds);
            for (var i = 0; i < activeNodeIds.length; i += 1) {
                activeNodeMap[activeNodeIds[i]] = true;
            }
            for (var j = 0; j < nodes.length; j += 1) {
                var node = nodes[j];
                if (node.elementType === "SubProcess") {
                    continue;
                }
                var x = (node.x || 0) - minX + padding;
                var y = (node.y || 0) - minY + padding;
                var width = node.width || 132;
                var height = node.height || 64;
                var isActive = !!activeNodeMap[node.elementId];
                var isHoverPathNode = !!hoverNodeMap[node.elementId];
                var theme = this.resolveNodeTheme(node, isActive, isHoverPathNode);
                context.save();
                context.shadowColor = isHoverPathNode ? "rgba(249,115,22,0.28)" : (isActive ? "rgba(34,197,94,0.22)" : "rgba(15,23,42,0.10)");
                context.shadowBlur = isHoverPathNode ? 22 : (isActive ? 18 : 10);
                context.shadowOffsetY = isHoverPathNode ? 12 : (isActive ? 10 : 6);
                context.fillStyle = this.createNodeGradient(context, x, y, width, height, theme.fillStart, theme.fillEnd);
                context.strokeStyle = theme.stroke;
                context.lineWidth = isHoverPathNode ? 4 : (isActive ? 3 : 2);
                if (node.elementType === "StartEvent" || node.elementType === "EndEvent") {
                    this.drawCircleNode(context, x, y, width, height);
                } else if (node.elementType === "ExclusiveGateway" || node.elementType === "ParallelGateway") {
                    this.drawDiamondNode(context, x, y, width, height);
                } else {
                    this.drawRoundedRect(context, x, y, width, height, 18);
                }
                context.fill();
                context.stroke();
                context.restore();
                this.drawNodeText(context, node, x, y, width, height, theme, isActive || isHoverPathNode);
            }
        },
        createNodeGradient: function (context, x, y, width, height, fillStart, fillEnd) {
            var gradient = context.createLinearGradient(x, y, x, y + height);
            gradient.addColorStop(0, fillStart);
            gradient.addColorStop(1, fillEnd);
            return gradient;
        },
        resolveNodeTheme: function (node, isActive, isHoverPathNode) {
            if (isHoverPathNode) {
                return {
                    fillStart: "#fff7ed",
                    fillEnd: "#fed7aa",
                    stroke: "#f97316",
                    title: "#7c2d12",
                    subTitle: "#ea580c"
                };
            }
            if (isActive) {
                return {
                    fillStart: "#f0fdf4",
                    fillEnd: "#dcfce7",
                    stroke: "#22c55e",
                    title: "#14532d",
                    subTitle: "#15803d"
                };
            }
            if (node.elementType === "StartEvent") {
                return {
                    fillStart: "#ecfdf5",
                    fillEnd: "#d1fae5",
                    stroke: "#10b981",
                    title: "#065f46",
                    subTitle: "#059669"
                };
            }
            if (node.elementType === "EndEvent") {
                return {
                    fillStart: "#fff1f2",
                    fillEnd: "#ffe4e6",
                    stroke: "#f43f5e",
                    title: "#881337",
                    subTitle: "#e11d48"
                };
            }
            if (node.elementType === "ExclusiveGateway" || node.elementType === "ParallelGateway") {
                return {
                    fillStart: "#fff7ed",
                    fillEnd: "#ffedd5",
                    stroke: "#f59e0b",
                    title: "#9a3412",
                    subTitle: "#ea580c"
                };
            }
            if (node.elementType === "SubProcess") {
                return {
                    fillStart: "#f8fafc",
                    fillEnd: "#edf4ff",
                    stroke: "#7c93b6",
                    title: "#334155",
                    subTitle: "#64748b"
                };
            }
            return {
                fillStart: "#f8fbff",
                fillEnd: "#e7f0ff",
                stroke: "#3b82f6",
                title: "#1e3a8a",
                subTitle: "#2563eb"
            };
        },
        drawCircleNode: function (context, x, y, width, height) {
            context.beginPath();
            context.arc(x + width / 2, y + height / 2, Math.min(width, height) / 2, 0, Math.PI * 2);
        },
        drawDiamondNode: function (context, x, y, width, height) {
            var centerX = x + width / 2;
            var centerY = y + height / 2;
            context.beginPath();
            context.moveTo(centerX, y);
            context.lineTo(x + width, centerY);
            context.lineTo(centerX, y + height);
            context.lineTo(x, centerY);
            context.closePath();
        },
        drawRoundedRect: function (context, x, y, width, height, radius) {
            var safeRadius = Math.min(radius, width / 2, height / 2);
            context.beginPath();
            context.moveTo(x + safeRadius, y);
            context.lineTo(x + width - safeRadius, y);
            context.quadraticCurveTo(x + width, y, x + width, y + safeRadius);
            context.lineTo(x + width, y + height - safeRadius);
            context.quadraticCurveTo(x + width, y + height, x + width - safeRadius, y + height);
            context.lineTo(x + safeRadius, y + height);
            context.quadraticCurveTo(x, y + height, x, y + height - safeRadius);
            context.lineTo(x, y + safeRadius);
            context.quadraticCurveTo(x, y, x + safeRadius, y);
            context.closePath();
        },
        drawNodeText: function (context, node, x, y, width, height, theme, isActive) {
            var title = this.resolveNodeSummary(node);
            context.save();
            context.textAlign = "center";
            context.textBaseline = "middle";
            context.fillStyle = theme.title;
            context.font = "600 14px Microsoft YaHei";
            context.fillText(title, x + width / 2, y + height / 2);
            context.restore();
        },
        resolveNodeSummary: function (node) {
            var sourceText = node.elementName || node.elementId || "";
            var maxLength = node.elementType === "ExclusiveGateway" || node.elementType === "ParallelGateway" ? 4 : 8;
            if (sourceText.length <= maxLength) {
                return sourceText;
            }
            return sourceText.slice(0, maxLength) + "...";
        },
        resolveTooltipTitle: function (node) {
            return node.elementName || node.elementId || "-";
        },
        resolveNodePropertySummaries: function (node) {
            var properties = this.normalizeNodeProperties(node);
            var rows = [];
            var pushRow = function (label, value) {
                if (value === null || typeof value === "undefined" || value === "") {
                    return;
                }
                if (Array.isArray(value)) {
                    value = value.join("，");
                }
                if (typeof value === "object") {
                    value = JSON.stringify(value);
                }
                rows.push({
                    label: label,
                    value: value === true ? "是" : (value === false ? "否" : String(value))
                });
            };
            pushRow("表单标识", properties.formKey);
            pushRow("发起人变量", properties.initiator);
            pushRow("办理人", properties.assignee);
            pushRow("候选用户", properties.candidateUsers);
            pushRow("候选组", properties.candidateGroups);
            pushRow("到期时间", properties.dueDate);
            pushRow("优先级", properties.priority);
            pushRow("实现类", properties.className);
            pushRow("委托表达式", properties.delegateExpression);
            pushRow("执行表达式", properties.expression);
            pushRow("结果变量", properties.resultVariable);
            pushRow("脚本格式", properties.scriptFormat);
            pushRow("调用类型", properties.type);
            pushRow("收件人", properties.to);
            pushRow("邮件主题", properties.subject);
            pushRow("调用流程", properties.calledElement);
            pushRow("事件定义", properties.eventDefinitionType);
            pushRow("消息引用", properties.messageRef);
            pushRow("定时表达式", properties.timerDefinition);
            pushRow("信号引用", properties.signalRef);
            pushRow("错误引用", properties.errorRef);
            pushRow("集合变量", properties.collection);
            pushRow("元素变量", properties.elementVariable);
            pushRow("完成条件", properties.completionCondition);
            pushRow("跳过表达式", properties.skipExpression);
            pushRow("多实例", properties.multiInstanceEnabled);
            pushRow("异步执行", properties.async);
            pushRow("排他执行", properties.exclusive);
            return rows;
        },
        normalizeNodeProperties: function (node) {
            var result = {};
            var merge = function (source) {
                if (!source) {
                    return;
                }
                if (typeof source === "string") {
                    try {
                        source = JSON.parse(source);
                    } catch (error) {
                        return;
                    }
                }
                if (Array.isArray(source)) {
                    for (var index = 0; index < source.length; index += 1) {
                        var item = source[index] || {};
                        var key = item.name || item.key || item.code || "";
                        if (key) {
                            result[key] = item.value;
                        }
                    }
                    return;
                }
                if (typeof source !== "object") {
                    return;
                }
                for (var key in source) {
                    if (Object.prototype.hasOwnProperty.call(source, key) && typeof source[key] !== "undefined" && source[key] !== null && source[key] !== "") {
                        result[key] = source[key];
                    }
                }
            };
            merge(node && node.properties);
            merge(node && node.attributes);
            merge(node && node.extensionProperties);
            merge(node && node.flowableProperties);
            merge(node && node.propertyMap);
            merge(node && node.config);
            var directKeys = [
                "initiator", "formKey", "assignee", "candidateUsers", "candidateGroups", "dueDate", "priority",
                "className", "delegateExpression", "expression", "resultVariable", "scriptFormat", "script",
                "calledElement", "messageRef", "timerDefinition", "signalRef", "errorRef", "eventDefinitionType",
                "collection", "elementVariable", "completionCondition", "skipExpression", "async", "exclusive",
                "type", "to", "subject", "text"
            ];
            for (var index = 0; index < directKeys.length; index += 1) {
                var directKey = directKeys[index];
                if (node && typeof node[directKey] !== "undefined" && node[directKey] !== null && node[directKey] !== "") {
                    result[directKey] = node[directKey];
                }
            }
            if (!result.className && result["class"]) {
                result.className = result["class"];
            }
            if (typeof result.async === "string") {
                result.async = result.async === "true";
            }
            if (typeof result.exclusive === "string") {
                result.exclusive = result.exclusive !== "false";
            }
            if (typeof result.multiInstanceEnabled === "string") {
                result.multiInstanceEnabled = result.multiInstanceEnabled === "true";
            }
            return result;
        },
        wrapText: function (context, text, maxWidth, font) {
            var safeText = text || "";
            if (!safeText) {
                return [""];
            }
            context.save();
            context.font = font || "14px Microsoft YaHei";
            var chars = safeText.split("");
            var lines = [];
            var line = "";
            for (var i = 0; i < chars.length; i += 1) {
                var nextLine = line + chars[i];
                if (context.measureText(nextLine).width > maxWidth && line) {
                    lines.push(line);
                    line = chars[i];
                    if (lines.length >= 2) {
                        break;
                    }
                    continue;
                }
                line = nextLine;
            }
            if (line && lines.length < 2) {
                lines.push(line);
            }
            if (!lines.length) {
                lines.push(safeText);
            }
            if (lines.length === 2 && chars.length > (lines[0].length + lines[1].length)) {
                lines[1] = lines[1].slice(0, Math.max(0, lines[1].length - 1)) + "...";
            }
            context.restore();
            return lines;
        },
        resolveNodeTypeLabel: function (elementType) {
            var labelMap = {
                StartEvent: "开始",
                EndEvent: "结束",
                UserTask: "用户任务",
                ScriptTask: "脚本任务",
                ManualTask: "人工任务",
                ServiceTask: "服务任务",
                ReceiveTask: "接收任务",
                BusinessRuleTask: "业务规则任务",
                CallActivity: "调用活动",
                BoundaryEvent: "边界事件",
                IntermediateCatchEvent: "中间捕获事件",
                IntermediateThrowEvent: "中间抛出事件",
                ExclusiveGateway: "排他网关",
                ParallelGateway: "并行网关",
                InclusiveGateway: "包容网关",
                EventGateway: "事件网关",
                TextAnnotation: "注释",
                SequenceFlow: "连线"
            };
            return labelMap[elementType] || "";
        }
    },
    watch: {
        detail: function () {
            this.clearHoverPath(false);
            this.$nextTick(this.renderCanvas);
        },
        activeNodeIds: function () {
            this.clearHoverPath(false);
            this.$nextTick(this.renderCanvas);
        }
    },
    mounted: function () {
        this.ensureStyle();
        window.addEventListener("resize", this.handleResize);
        this.renderCanvas();
    },
    beforeDestroy: function () {
        window.removeEventListener("resize", this.handleResize);
        this.clearHoverPath(false);
        this.hideTooltip();
    }
};

window.ProcessDiagramUtils = {
    buildPreviewDetailFromXml: function (model, xmlText) {
        var parser = new DOMParser();
        var documentNode = parser.parseFromString(xmlText || "", "text/xml");
        var parseError = documentNode.getElementsByTagName("parsererror");
        if (parseError && parseError.length) {
            throw new Error("流程源码格式不正确");
        }
        var processElement = this.findFirstElementByLocalName(documentNode, "process");
        if (!processElement) {
            throw new Error("流程源码中未找到流程定义");
        }
        var shapeMap = this.buildShapeMap(documentNode);
        var edgeWaypointMap = this.buildEdgeWaypointMap(documentNode);
        var nodes = [];
        var sequenceFlows = [];
        var supportedNodeTypes = {
            startEvent: "StartEvent",
            endEvent: "EndEvent",
            userTask: "UserTask",
            scriptTask: "ScriptTask",
            manualTask: "ManualTask",
            serviceTask: "ServiceTask",
            receiveTask: "ReceiveTask",
            businessRuleTask: "BusinessRuleTask",
            callActivity: "CallActivity",
            boundaryEvent: "BoundaryEvent",
            intermediateCatchEvent: "IntermediateCatchEvent",
            intermediateThrowEvent: "IntermediateThrowEvent",
            subProcess: "SubProcess",
            exclusiveGateway: "ExclusiveGateway",
            parallelGateway: "ParallelGateway",
            inclusiveGateway: "InclusiveGateway",
            eventBasedGateway: "EventGateway",
            textAnnotation: "TextAnnotation"
        };
        this.collectPreviewFlowElements(processElement, shapeMap, edgeWaypointMap, supportedNodeTypes, nodes, sequenceFlows);
        var nodeMap = {};
        for (var nodeIndex = 0; nodeIndex < nodes.length; nodeIndex += 1) {
            nodeMap[nodes[nodeIndex].elementId] = nodes[nodeIndex];
        }
        for (var flowIndex = 0; flowIndex < sequenceFlows.length; flowIndex += 1) {
            var sequenceFlow = sequenceFlows[flowIndex];
            if (nodeMap[sequenceFlow.sourceRef]) {
                nodeMap[sequenceFlow.sourceRef].outgoingCount += 1;
            }
            if (nodeMap[sequenceFlow.targetRef]) {
                nodeMap[sequenceFlow.targetRef].incomingCount += 1;
            }
        }
        return {
            modelName: model && model.modelName ? model.modelName : "",
            modelKey: model && model.modelKey ? model.modelKey : "",
            category: model && model.category ? model.category : "",
            deploymentId: model && model.deploymentId ? model.deploymentId : "",
            lastUpdateTime: model && model.lastUpdateTime ? model.lastUpdateTime : "",
            processKey: processElement.getAttribute("id") || (model && model.modelKey) || "",
            processName: processElement.getAttribute("name") || (model && model.modelName) || "",
            nodeCount: nodes.length,
            userTaskCount: nodes.filter(function (item) { return item.elementType === "UserTask"; }).length,
            sequenceFlowCount: sequenceFlows.length,
            nodes: nodes,
            sequenceFlows: sequenceFlows
        };
    },
    buildShapeMap: function (documentNode) {
        var result = {};
        var shapes = documentNode.getElementsByTagNameNS("*", "BPMNShape");
        for (var index = 0; index < shapes.length; index += 1) {
            var shape = shapes[index];
            var elementId = shape.getAttribute("bpmnElement") || "";
            if (!elementId) {
                continue;
            }
            var bounds = this.findFirstElementByLocalName(shape, "Bounds");
            result[elementId] = {
                x: bounds ? bounds.getAttribute("x") : "",
                y: bounds ? bounds.getAttribute("y") : "",
                width: bounds ? bounds.getAttribute("width") : "",
                height: bounds ? bounds.getAttribute("height") : ""
            };
        }
        return result;
    },
    buildEdgeWaypointMap: function (documentNode) {
        var result = {};
        var edges = documentNode.getElementsByTagNameNS("*", "BPMNEdge");
        for (var index = 0; index < edges.length; index += 1) {
            var edge = edges[index];
            var elementId = edge.getAttribute("bpmnElement") || "";
            if (!elementId) {
                continue;
            }
            var waypoints = edge.getElementsByTagNameNS("*", "waypoint");
            result[elementId] = this.buildPreviewWaypoints(waypoints);
        }
        return result;
    },
    collectPreviewFlowElements: function (containerElement, shapeMap, edgeWaypointMap, supportedNodeTypes, nodes, sequenceFlows) {
        var children = containerElement && containerElement.children ? containerElement.children : [];
        for (var index = 0; index < children.length; index += 1) {
            var child = children[index];
            var localName = child.localName || child.nodeName;
            if (supportedNodeTypes[localName]) {
                var elementId = child.getAttribute("id") || "";
                var bounds = shapeMap[elementId] || {};
                var isEventNode = localName === "startEvent" || localName === "endEvent";
                nodes.push({
                    elementId: elementId,
                    elementName: child.getAttribute("name") || "",
                    elementType: supportedNodeTypes[localName],
                    documentation: this.extractDocumentation(child),
                    properties: this.extractNodeProperties(child, localName),
                    defaultFlowId: child.getAttribute("default") || "",
                    x: this.toNumber(bounds.x, nodes.length * 180),
                    y: this.toNumber(bounds.y, 0),
                    width: this.toNumber(bounds.width, isEventNode ? 56 : 120),
                    height: this.toNumber(bounds.height, isEventNode ? 56 : 60),
                    incomingCount: 0,
                    outgoingCount: 0
                });
                if (localName === "subProcess") {
                    this.collectPreviewFlowElements(child, shapeMap, edgeWaypointMap, supportedNodeTypes, nodes, sequenceFlows);
                }
                continue;
            }
            if (localName === "sequenceFlow") {
                var flowId = child.getAttribute("id") || "";
                sequenceFlows.push({
                    elementId: flowId,
                    elementName: child.getAttribute("name") || "",
                    sourceRef: child.getAttribute("sourceRef") || "",
                    targetRef: child.getAttribute("targetRef") || "",
                    conditionExpression: this.extractConditionExpression(child),
                    waypoints: edgeWaypointMap[flowId] || []
                });
            }
        }
    },
    buildPreviewWaypoints: function (waypointElements) {
        var result = [];
        var waypoints = waypointElements || [];
        for (var index = 0; index < waypoints.length; index += 1) {
            result.push({
                x: this.toNumber(waypoints[index].getAttribute("x"), 0),
                y: this.toNumber(waypoints[index].getAttribute("y"), 0)
            });
        }
        return result;
    },
    findFirstElementByLocalName: function (root, localName) {
        var elements = root.getElementsByTagNameNS("*", localName);
        return elements && elements.length ? elements[0] : null;
    },
    extractDocumentation: function (element) {
        var documentation = this.findFirstElementByLocalName(element, "documentation");
        return documentation && documentation.textContent ? documentation.textContent.trim() : "";
    },
    extractConditionExpression: function (element) {
        var conditionExpression = this.findFirstElementByLocalName(element, "conditionExpression");
        return conditionExpression && conditionExpression.textContent ? conditionExpression.textContent.trim() : "";
    },
    extractNodeProperties: function (element, localName) {
        var properties = {
            initiator: this.getAnyAttribute(element, "initiator"),
            formKey: this.getAnyAttribute(element, "formKey"),
            assignee: this.getAnyAttribute(element, "assignee"),
            candidateUsers: this.getAnyAttribute(element, "candidateUsers"),
            candidateGroups: this.getAnyAttribute(element, "candidateGroups"),
            dueDate: this.getAnyAttribute(element, "dueDate"),
            priority: this.getAnyAttribute(element, "priority"),
            implementationType: "class",
            className: this.getAnyAttribute(element, "class"),
            delegateExpression: this.getAnyAttribute(element, "delegateExpression"),
            expression: this.getAnyAttribute(element, "expression"),
            resultVariable: this.getAnyAttribute(element, "resultVariable"),
            scriptFormat: element.getAttribute("scriptFormat") || "",
            script: "",
            calledElement: element.getAttribute("calledElement") || "",
            inheritVariables: this.getAnyAttribute(element, "inheritVariables") !== "false",
            messageRef: "",
            timerDefinition: "",
            signalRef: "",
            errorRef: "",
            eventDefinitionType: "",
            attachedToRef: element.getAttribute("attachedToRef") || "",
            cancelActivity: element.getAttribute("cancelActivity") !== "false",
            triggeredByEvent: element.getAttribute("triggeredByEvent") === "true",
            async: this.getAnyAttribute(element, "async") === "true",
            exclusive: this.getAnyAttribute(element, "exclusive") !== "false",
            multiInstanceEnabled: false,
            multiInstanceSequential: false,
            collection: "",
            elementVariable: "",
            completionCondition: "",
            text: this.getAnyAttribute(element, "text"),
            to: this.getAnyAttribute(element, "to"),
            subject: this.getAnyAttribute(element, "subject"),
            ruleVariablesInput: this.getAnyAttribute(element, "ruleVariablesInput")
        };
        this.mergeObject(properties, this.extractExtensionFieldProperties(element));
        if (properties.delegateExpression) {
            properties.implementationType = "delegateExpression";
        } else if (properties.expression) {
            properties.implementationType = "expression";
        }
        var scriptElement = this.findFirstElementByLocalName(element, "script");
        if (scriptElement && scriptElement.textContent) {
            properties.script = scriptElement.textContent.trim();
        }
        var textElement = this.findFirstElementByLocalName(element, "text");
        if (textElement && textElement.textContent) {
            properties.text = textElement.textContent.trim();
        }
        this.fillEventProperties(element, properties);
        this.fillMultiInstanceProperties(element, properties);
        return properties;
    },
    mergeObject: function (target, source) {
        if (!target || !source) {
            return target;
        }
        for (var key in source) {
            if (Object.prototype.hasOwnProperty.call(source, key) && source[key] !== "") {
                target[key] = source[key];
            }
        }
        return target;
    },
    extractExtensionFieldProperties: function (element) {
        var result = {};
        var children = element && element.children ? element.children : [];
        for (var childIndex = 0; childIndex < children.length; childIndex += 1) {
            var child = children[childIndex];
            var localName = child.localName || child.nodeName || "";
            if (localName !== "extensionElements") {
                continue;
            }
            var extensionChildren = child.children || [];
            for (var fieldIndex = 0; fieldIndex < extensionChildren.length; fieldIndex += 1) {
                var field = extensionChildren[fieldIndex];
                var fieldLocalName = field.localName || field.nodeName || "";
                if (fieldLocalName !== "field") {
                    continue;
                }
                var fieldName = field.getAttribute("name") || "";
                if (!fieldName) {
                    continue;
                }
                result[fieldName] = this.extractExtensionFieldValue(field);
            }
        }
        return result;
    },
    extractExtensionFieldValue: function (fieldElement) {
        var directValue = this.getAnyAttribute(fieldElement, "stringValue")
            || this.getAnyAttribute(fieldElement, "expression")
            || this.getAnyAttribute(fieldElement, "delegateExpression");
        if (directValue) {
            return directValue;
        }
        var stringElement = this.findFirstElementByLocalName(fieldElement, "string");
        if (stringElement && stringElement.textContent) {
            return stringElement.textContent.trim();
        }
        var expressionElement = this.findFirstElementByLocalName(fieldElement, "expression");
        if (expressionElement && expressionElement.textContent) {
            return expressionElement.textContent.trim();
        }
        return fieldElement && fieldElement.textContent ? fieldElement.textContent.trim() : "";
    },
    fillEventProperties: function (element, properties) {
        var messageEventDefinition = this.findFirstElementByLocalName(element, "messageEventDefinition");
        var timerEventDefinition = this.findFirstElementByLocalName(element, "timerEventDefinition");
        var signalEventDefinition = this.findFirstElementByLocalName(element, "signalEventDefinition");
        var errorEventDefinition = this.findFirstElementByLocalName(element, "errorEventDefinition");
        if (messageEventDefinition) {
            properties.eventDefinitionType = "message";
            properties.messageRef = messageEventDefinition.getAttribute("messageRef") || "";
        } else if (timerEventDefinition) {
            properties.eventDefinitionType = "timer";
            var timeDuration = this.findFirstElementByLocalName(timerEventDefinition, "timeDuration");
            properties.timerDefinition = timeDuration && timeDuration.textContent ? timeDuration.textContent.trim() : "";
        } else if (signalEventDefinition) {
            properties.eventDefinitionType = "signal";
            properties.signalRef = signalEventDefinition.getAttribute("signalRef") || "";
        } else if (errorEventDefinition) {
            properties.eventDefinitionType = "error";
            properties.errorRef = errorEventDefinition.getAttribute("errorRef") || "";
        }
    },
    fillMultiInstanceProperties: function (element, properties) {
        var loop = this.findFirstElementByLocalName(element, "multiInstanceLoopCharacteristics");
        if (!loop) {
            return;
        }
        properties.multiInstanceEnabled = true;
        properties.multiInstanceSequential = loop.getAttribute("isSequential") === "true";
        properties.collection = this.getAnyAttribute(loop, "collection");
        properties.elementVariable = this.getAnyAttribute(loop, "elementVariable");
        var completionCondition = this.findFirstElementByLocalName(loop, "completionCondition");
        properties.completionCondition = completionCondition && completionCondition.textContent ? completionCondition.textContent.trim() : "";
    },
    getAnyAttribute: function (element, localName) {
        if (!element || !element.attributes) {
            return "";
        }
        if (element.hasAttribute(localName)) {
            return element.getAttribute(localName) || "";
        }
        for (var index = 0; index < element.attributes.length; index += 1) {
            var attribute = element.attributes[index];
            if (attribute.localName === localName || attribute.name === localName || attribute.name.slice(-localName.length - 1) === ":" + localName) {
                return attribute.value || "";
            }
        }
        return "";
    },
    toNumber: function (value, fallback) {
        var nextValue = Number(value);
        return Number.isFinite(nextValue) ? nextValue : fallback;
    },
    resolveOrderedNodes: function (detail) {
        if (!detail || !detail.nodes) {
            return [];
        }
        return detail.nodes.slice().sort(function (left, right) {
            var leftX = typeof left.x === "number" ? left.x : Number.MAX_SAFE_INTEGER;
            var rightX = typeof right.x === "number" ? right.x : Number.MAX_SAFE_INTEGER;
            if (leftX !== rightX) {
                return leftX - rightX;
            }
            var leftY = typeof left.y === "number" ? left.y : Number.MAX_SAFE_INTEGER;
            var rightY = typeof right.y === "number" ? right.y : Number.MAX_SAFE_INTEGER;
            return leftY - rightY;
        });
    },
    resolveSequenceFlowSummaries: function (detail, orderedNodes) {
        if (!detail || !detail.sequenceFlows || !detail.sequenceFlows.length) {
            return [];
        }
        var nodeNameMap = {};
        for (var index = 0; index < orderedNodes.length; index += 1) {
            nodeNameMap[orderedNodes[index].elementId] = orderedNodes[index].elementName || orderedNodes[index].elementId;
        }
        return detail.sequenceFlows.map(function (sequenceFlow) {
            return (nodeNameMap[sequenceFlow.sourceRef] || sequenceFlow.sourceRef) + " -> " + (nodeNameMap[sequenceFlow.targetRef] || sequenceFlow.targetRef);
        });
    },
    resolveNodeTypeLabel: function (elementType) {
        var mapping = {
            StartEvent: "开始节点",
            EndEvent: "结束节点",
            UserTask: "用户任务",
            ScriptTask: "脚本任务",
            ManualTask: "人工任务",
            ServiceTask: "服务任务",
            ReceiveTask: "接收任务",
            BusinessRuleTask: "业务规则任务",
            CallActivity: "调用活动",
            BoundaryEvent: "边界事件",
            IntermediateCatchEvent: "中间捕获事件",
            IntermediateThrowEvent: "中间抛出事件",
            ExclusiveGateway: "排他网关",
            ParallelGateway: "并行网关",
            InclusiveGateway: "包容网关",
            EventGateway: "事件网关",
            TextAnnotation: "注释"
        };
        return mapping[elementType] || elementType || "";
    }
};

if (window && window.Vue && typeof window.Vue.component === "function") {
    window.Vue.component("process-diagram", window.ProcessDiagram);
}
