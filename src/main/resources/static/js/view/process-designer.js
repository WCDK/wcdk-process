/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
var processDesignerStyleId = "wcdk-process-inline-style";

window.ProcessDesigner = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                       <div class="section-kicker">流程设计工作区</div>
                        <h2>流程设计</h2>
                    </div>
                    <div class="designer-header-actions">
                        <el-button @click="handleExportBpmn">导出 BPMN</el-button>
                        <el-button @click="handleExportBpmnXml">导出 BPMN.XML</el-button>
                        <el-button @click="handleExportPng">导出 PNG</el-button>
                        <el-button @click="handleCenterCanvas">居中显示</el-button>
                        <el-button @click="handleResetCanvas">清空画布</el-button>
                        <el-button type="primary" @click="handleRefresh">刷新画布</el-button>
                    </div>
                </div>

                <div
                    class="designer-layout"
                    :class="{
                        'designer-layout-left-collapsed': leftPanelCollapsed,
                        'designer-layout-right-collapsed': rightPanelCollapsed
                    }">
                    <aside class="designer-sidebar designer-palette-panel" :class="{ 'designer-sidebar-collapsed': leftPanelCollapsed }">
                        <div class="designer-sidebar-head">
                            <div>
                                <div class="designer-panel-title">Flowable 建模节点</div>
                                <div class="designer-panel-subtitle">保持当前页面视觉风格，补齐流程设计工作区的检索、分组和常用操作体验。</div>
                            </div>
                            <div class="designer-sidebar-actions">
                                <span class="mini-tag" v-if="!leftPanelCollapsed">可用节点 {{ totalPaletteNodeCount }}</span>
                                <button type="button" class="designer-collapse-button" @click="toggleLeftPanel">
                                    {{ leftPanelCollapsed ? ">" : "<" }}
                                </button>
                            </div>
                        </div>

                        <template v-if="!leftPanelCollapsed">
                            <el-input
                                v-model.trim="paletteKeyword"
                                clearable
                                size="small"
                                placeholder="搜索节点名称、类型或说明">
                                <i slot="prefix" class="el-input__icon el-icon-search"></i>
                            </el-input>

                            <div class="designer-group-scroll">
                                <div
                                    v-for="group in visibleNodeGroups"
                                    :key="group.category"
                                    class="designer-group">
                                    <button
                                        type="button"
                                        class="designer-group-toggle"
                                        @click="toggleGroup(group.category)">
                                        <span class="designer-group-title">{{ group.category }}</span>
                                        <span class="designer-group-meta">{{ group.nodes.length }} 个节点</span>
                                        <span class="designer-group-arrow">{{ isGroupExpanded(group.category) ? "-" : "+" }}</span>
                                    </button>
                                    <div class="designer-node-list" v-if="isGroupExpanded(group.category)">
                                        <div
                                            v-for="node in group.nodes"
                                            :key="node.type"
                                            class="designer-palette-node"
                                            :class="'designer-palette-node-' + node.kind"
                                            draggable="true"
                                            @dragstart="handlePaletteDragStart(node)"
                                            @dragend="handlePaletteDragEnd">
                                            <div class="designer-palette-node-icon" :class="'designer-shape-' + node.kind">
                                                <span>{{ node.shortLabel }}</span>
                                            </div>
                                            <div class="designer-palette-node-main">
                                                <div class="designer-palette-node-title">{{ node.label }}</div>
                                                <div class="designer-palette-node-text">{{ node.description }}</div>
                                                <div class="designer-palette-node-meta">{{ node.bpmnType }}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div v-if="!visibleNodeGroups.length" class="empty-panel process-schema-empty">
                                    未找到符合条件的节点，请调整搜索关键词。
                                </div>
                            </div>
                        </template>
                    </aside>

                    <section class="designer-stage-panel">
                        <div class="designer-stage-head">
                            <div>
                                <div class="designer-panel-title">流程画布</div>
                                <div class="designer-panel-subtitle">支持拖拽设置、编辑连线、属性面板编排，便于流程设计</div>
                            </div>
                            <div class="designer-stage-stats">
                                <span class="mini-tag">节点数：{{ canvasNodes.length }}</span>
                                <span class="mini-tag">连线数：{{ canvasEdges.length }}</span>
                                <span class="mini-tag" v-if="pendingSourceNode">连线起点：{{ pendingSourceNode.name }}</span>
                                <span class="mini-tag" v-if="selectedNode">当前选中：{{ selectedNode.name }}</span>
                                <span class="mini-tag" v-if="selectedEdge">当前连线：{{ resolveEdgeDisplayName(selectedEdge) }}</span>
                            </div>
                        </div>

                        <div class="designer-stage-toolbar">
                            <div class="designer-stage-toolbar-group">
                                <button type="button" class="designer-tool-button" @click="handleZoomOut">
                                    <i class="el-icon-minus"></i>
                                </button>
                                <button type="button" class="designer-tool-button designer-tool-button-wide" @click="handleResetZoom">
                                    缩放 {{ canvasScalePercent }}
                                </button>
                                <button type="button" class="designer-tool-button" @click="handleZoomIn">
                                    <i class="el-icon-plus"></i>
                                </button>
                                <button type="button" class="designer-tool-button designer-tool-button-wide" @click="handleFitCanvas">
                                    适应视图
                                </button>
                                <button type="button" class="designer-tool-button designer-tool-button-wide" @click="handleCenterCanvas">
                                    居中查看
                                </button>
                            </div>
                            <div class="designer-stage-toolbar-group">
                                <span class="designer-toolbar-tip" v-if="connectDragState.active">拖拽到目标节点后松开鼠标，完成连线</span>
                                <span class="designer-toolbar-tip" v-else>悬浮节点中央出现连线手柄，按住左键拖拽即可连线</span>
                            </div>
                        </div>

                        <div
                            ref="canvasWrapper"
                            class="designer-stage-wrapper"
                            :class="{
                                'designer-stage-wrapper-dragging': !!dragPaletteNode,
                                'designer-stage-wrapper-panning': panCanvasState.active
                            }"
                            @dragover.prevent
                            @contextmenu.prevent
                            @mousedown="handleCanvasWrapperMouseDown"
                            @click="handleCanvasClick"
                            @drop="handleCanvasDrop">
                            <div class="designer-stage-viewport" :style="viewportStyle">
                                <div class="designer-stage-content" :style="contentStyle">
                                    <div class="designer-stage-grid"></div>
                                    <svg class="designer-stage-lines" v-if="canvasEdges.length || connectPreviewPath">
                                        <defs>
                                            <marker id="designer-arrowhead" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
                                                <path d="M0,0 L10,4 L0,8 z" fill="#7a93b8"></path>
                                            </marker>
                                            <marker id="designer-arrowhead-preview" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
                                                <path d="M0,0 L10,4 L0,8 z" fill="#f59e0b"></path>
                                            </marker>
                                        </defs>
                                        <path
                                            v-for="edge in visibleCanvasEdges"
                                            :key="edge.id"
                                            class="designer-stage-line"
                                            :class="{ active: selectedEdgeId === edge.id }"
                                            :d="resolveEdgePath(edge)"
                                            marker-end="url(#designer-arrowhead)"
                                            @click.stop="handleEdgeSelect(edge.id)">
                                        </path>
                                        <path
                                            v-if="connectPreviewPath"
                                            class="designer-stage-line designer-stage-line-preview"
                                            :d="connectPreviewPath"
                                            marker-end="url(#designer-arrowhead-preview)">
                                        </path>
                                    </svg>

                                    <div class="designer-stage-watermark">
                                        <span>Flowable 风格细节参考</span>
                                        <strong>WCDK 流程设计画布</strong>
                                    </div>

                                    <div class="designer-stage-empty" v-if="!visibleCanvasNodes.length">
                                        将左侧流程节点拖拽到此处，开始绘制流程。                                    </div>

                                    <div
                                        v-for="node in visibleContainerNodes"
                                        :key="'container-' + node.id"
                                        class="designer-stage-node"
                                        :class="[
                                            'designer-stage-node-' + node.kind,
                                            {
                                                active: selectedNodeId === node.id,
                                                'designer-stage-node-connect-pending': pendingSourceId === node.id
                                            }
                                        ]"
                                        :style="resolveNodeStyle(node)"
                                        :data-node-id="node.id"
                                        @mouseenter="handleNodeMouseEnter(node.id)"
                                        @mouseleave="handleNodeMouseLeave(node.id)"
                                        @mousedown.stop.prevent="handleNodeMouseDown(node, $event)"
                                        @click.stop="handleNodeSelect(node.id)">
                                        <div class="designer-stage-node-badge">{{ node.bpmnType }}</div>
                                        <button
                                            v-if="node.bpmnType === 'subProcess'"
                                            type="button"
                                            class="designer-subprocess-toggle"
                                            :title="node.expanded ? '收起子流程' : '展开子流程'"
                                            @click.stop="toggleSubProcess(node.id)">
                                            {{ node.expanded ? "-" : "+" }}
                                        </button>
                                        <button
                                            v-if="selectedNodeId === node.id"
                                            type="button"
                                            class="designer-resize-handle"
                                            title="拖拽调整节点大小"
                                            @mousedown.stop.prevent="handleResizeMouseDown(node, $event)">
                                        </button>
                                        <button
                                            v-if="shouldShowConnectHandle(node)"
                                            type="button"
                                            class="designer-connect-handle"
                                            title="按住拖拽开始连线"
                                            @mousedown.stop.prevent="handleConnectHandleMouseDown(node, $event)">
                                            <span></span>
                                        </button>
                                        <div class="designer-stage-node-shape" :class="'designer-shape-' + node.kind">
                                            <span>{{ node.shortLabel }}</span>
                                        </div>
                                        <div class="designer-stage-node-name">{{ node.name }}</div>
                                        <div class="designer-stage-node-type">{{ node.label }}</div>
                                    </div>

                                    <div
                                        v-for="node in visibleLeafNodes"
                                        :key="node.id"
                                        class="designer-stage-node"
                                        :class="[
                                            'designer-stage-node-' + node.kind,
                                            {
                                                active: selectedNodeId === node.id,
                                                'designer-stage-node-connect-pending': pendingSourceId === node.id
                                            }
                                        ]"
                                        :style="resolveNodeStyle(node)"
                                        :data-node-id="node.id"
                                        @mouseenter="handleNodeMouseEnter(node.id)"
                                        @mouseleave="handleNodeMouseLeave(node.id)"
                                        @mousedown.stop.prevent="handleNodeMouseDown(node, $event)"
                                        @click.stop="handleNodeSelect(node.id)">
                                        <div class="designer-stage-node-badge">{{ node.bpmnType }}</div>
                                        <button
                                            v-if="selectedNodeId === node.id"
                                            type="button"
                                            class="designer-resize-handle"
                                            title="拖拽调整节点大小"
                                            @mousedown.stop.prevent="handleResizeMouseDown(node, $event)">
                                        </button>
                                        <button
                                            v-if="shouldShowConnectHandle(node)"
                                            type="button"
                                            class="designer-connect-handle"
                                            title="按住拖拽开始连线"
                                            @mousedown.stop.prevent="handleConnectHandleMouseDown(node, $event)">
                                            <span></span>
                                        </button>
                                        <div class="designer-stage-node-shape" :class="'designer-shape-' + node.kind">
                                            <span>{{ node.shortLabel }}</span>
                                        </div>
                                        <div class="designer-stage-node-name">{{ node.name }}</div>
                                        <div class="designer-stage-node-type">{{ node.label }}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <aside class="designer-sidebar designer-config-panel" :class="{ 'designer-sidebar-collapsed': rightPanelCollapsed }">
                        <div class="designer-sidebar-head">
                            <div>
                                <div class="designer-panel-title">属性面板</div>
                                <div class="designer-panel-subtitle">右侧展示当前设计元素的摘要信息与属性编辑内容。</div>
                            </div>
                            <div class="designer-sidebar-actions">
                                <span class="mini-tag" v-if="!rightPanelCollapsed">{{ selectedElementTypeLabel }}</span>
                                <button type="button" class="designer-collapse-button" @click="toggleRightPanel">
                                    {{ rightPanelCollapsed ? "<" : ">" }}
                                </button>
                            </div>
                        </div>

                        <template v-if="!rightPanelCollapsed">
                        <div class="designer-summary-card">
                            <div class="designer-summary-title">流程摘要</div>
                            <div class="designer-summary-grid">
                                <div class="designer-summary-item">
                                    <span>节点总数</span>
                                    <strong>{{ canvasNodes.length }}</strong>
                                </div>
                                <div class="designer-summary-item">
                                    <span>连线总数</span>
                                    <strong>{{ canvasEdges.length }}</strong>
                                </div>
                                <div class="designer-summary-item">
                                    <span>当前缩放</span>
                                    <strong>{{ canvasScalePercent }}</strong>
                                </div>
                                <div class="designer-summary-item">
                                    <span>画布尺寸</span>
                                    <strong>{{ canvasWidth }} × {{ canvasHeight }}</strong>
                                </div>
                            </div>
                        </div>

                        <div class="designer-summary-card">
                            <div class="designer-summary-title">操作提示</div>
                            <div class="designer-summary-list">
                                <span>拖拽左侧节点到画布。</span>
                                <span>双击节点开始或完成连线。</span>
                                <span>点击右侧面板可直接编辑名称、标识和说明。</span>
                            </div>
                        </div>

                        <div v-if="selectedNode" class="designer-form-block">
                            <div class="designer-panel-title">节点属性</div>
                            <el-form label-position="top" @submit.native.prevent>
                                <el-form-item label="节点名称">
                                    <el-input v-model.trim="selectedNode.name" placeholder="请输入节点名称"></el-input>
                                </el-form-item>
                                <el-form-item label="节点标识">
                                    <el-input v-model.trim="selectedNode.code" placeholder="请输入节点标识"></el-input>
                                </el-form-item>
                                <el-form-item label="Flowable 类型">
                                    <el-input :value="selectedNode.bpmnType" disabled></el-input>
                                </el-form-item>
                                <el-form-item label="节点分类">
                                    <el-input :value="selectedNode.label" disabled></el-input>
                                </el-form-item>
                                <el-form-item label="所属子流程" v-if="selectedNode.bpmnType !== 'subProcess'">
                                    <el-select
                                        v-model="selectedNode.parentId"
                                        clearable
                                        filterable
                                        placeholder="请选择所属子流程">
                                        <el-option
                                            v-for="item in availableSubProcessOptions"
                                            :key="item.id"
                                            :label="item.name"
                                            :value="item.id">
                                        </el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="父级节点" v-else>
                                    <el-input :value="resolveParentNodeName(selectedNode.parentId)" disabled></el-input>
                                </el-form-item>
                                <el-form-item label="节点说明">
                                    <el-input
                                        v-model.trim="selectedNode.documentation"
                                        type="textarea"
                                        :rows="5"
                                        placeholder="请输入节点说明">
                                    </el-input>
                                </el-form-item>
                                <div class="designer-node-meta">
                                    <span class="mini-tag">坐标 X：{{ selectedNode.x }}</span>
                                    <span class="mini-tag">坐标 Y：{{ selectedNode.y }}</span>
                                    <span class="mini-tag">宽度：{{ selectedNode.width }}</span>
                                    <span class="mini-tag">高度：{{ selectedNode.height }}</span>
                                    <span class="mini-tag" v-if="selectedNode.parentId">归属：{{ resolveParentNodeName(selectedNode.parentId) }}</span>
                                </div>
                                <div class="form-actions">
                                    <el-button type="danger" @click="handleDeleteSelectedNode">删除节点</el-button>
                                </div>
                            </el-form>
                        </div>

                        <div v-else-if="selectedEdge" class="designer-form-block">
                            <div class="designer-panel-title">连线属性</div>
                            <el-form label-position="top" @submit.native.prevent>
                                <el-form-item label="Flowable 类型">
                                    <el-input value="sequenceFlow" disabled></el-input>
                                </el-form-item>
                                <el-form-item label="连线名称">
                                    <el-input v-model.trim="selectedEdge.name" placeholder="请输入连线名称"></el-input>
                                </el-form-item>
                                <div class="designer-node-meta">
                                    <span class="mini-tag">起点：{{ resolveNodeName(selectedEdge.sourceId) }}</span>
                                    <span class="mini-tag">终点：{{ resolveNodeName(selectedEdge.targetId) }}</span>
                                </div>
                                <div class="form-actions">
                                    <el-button type="danger" @click="handleDeleteSelectedEdge">删除连线</el-button>
                                </div>
                            </el-form>
                        </div>

                        <div v-else class="empty-panel process-schema-empty">
                            请先在画布中选择一个节点或连线，再查看和编辑对应属性。                        </div>
                        </template>
                    </aside>
                </div>
            </section>
        </section>
    `,
    data: function () {
        return {
            nodeGroups: [
                {
                    category: "事件",
                    nodes: [
                        {
                            type: "startEvent",
                            bpmnType: "startEvent",
                            label: "启动事件",
                            shortLabel: "启动",
                            description: "流程从这里发起。",
                            kind: "event",
                            width: 92,
                            height: 92
                        },
                        {
                            type: "endEvent",
                            bpmnType: "endEvent",
                            label: "结束事件",
                            shortLabel: "结束",
                            description: "流程在这里结束。",
                            kind: "event",
                            width: 92,
                            height: 92
                        },
                        {
                            type: "boundaryEvent",
                            bpmnType: "boundaryEvent",
                            label: "边界事件",
                            shortLabel: "边界",
                            description: "挂载在活动边界上的事件。",
                            kind: "event",
                            width: 92,
                            height: 92
                        },
                        {
                            type: "intermediateCatchEvent",
                            bpmnType: "intermediateCatchEvent",
                            label: "中间捕获事件",
                            shortLabel: "捕获",
                            description: "等待中间事件被捕获。",
                            kind: "event",
                            width: 92,
                            height: 92
                        },
                        {
                            type: "intermediateThrowEvent",
                            bpmnType: "intermediateThrowEvent",
                            label: "中间抛出事件",
                            shortLabel: "抛出",
                            description: "在流程中抛出中间事件。",
                            kind: "event",
                            width: 92,
                            height: 92
                        }
                    ]
                },
                {
                    category: "任务",
                    nodes: [
                        {
                            type: "userTask",
                            bpmnType: "userTask",
                            label: "用户任务",
                            shortLabel: "用户",
                            description: "配置人工办理节点。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "scriptTask",
                            bpmnType: "scriptTask",
                            label: "脚本任务",
                            shortLabel: "脚本",
                            description: "通过脚本执行自动化逻辑。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "serviceTask",
                            bpmnType: "serviceTask",
                            label: "服务任务",
                            shortLabel: "服务",
                            description: "配置系统自动执行节点。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "mailTask",
                            bpmnType: "mailTask",
                            label: "邮件任务",
                            shortLabel: "邮件",
                            description: "发送邮件或通知消息。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "manualTask",
                            bpmnType: "manualTask",
                            label: "手动任务",
                            shortLabel: "手动",
                            description: "流程外部人工处理任务。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "receiveTask",
                            bpmnType: "receiveTask",
                            label: "接收任务",
                            shortLabel: "接收",
                            description: "等待外部消息或回调。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "businessRuleTask",
                            bpmnType: "businessRuleTask",
                            label: "业务规则任务",
                            shortLabel: "规则",
                            description: "调用规则引擎处理业务规则。",
                            kind: "task",
                            width: 148,
                            height: 86
                        },
                        {
                            type: "callActivity",
                            bpmnType: "callActivity",
                            label: "调用活动任务",
                            shortLabel: "调用",
                            description: "复用外部流程或活动定义。",
                            kind: "task",
                            width: 148,
                            height: 86
                        }
                    ]
                },
                {
                    category: "网关",
                    nodes: [
                        {
                            type: "parallelGateway",
                            bpmnType: "parallelGateway",
                            label: "并行网关",
                            shortLabel: "并行",
                            description: "控制并行分支或汇聚。",
                            kind: "gateway",
                            width: 112,
                            height: 112
                        },
                        {
                            type: "exclusiveGateway",
                            bpmnType: "exclusiveGateway",
                            label: "排他网关",
                            shortLabel: "排他",
                            description: "根据条件选择唯一分支。",
                            kind: "gateway",
                            width: 112,
                            height: 112
                        },
                        {
                            type: "inclusiveGateway",
                            bpmnType: "inclusiveGateway",
                            label: "包容网关",
                            shortLabel: "包容",
                            description: "根据条件选择一个或多个分支。",
                            kind: "gateway",
                            width: 112,
                            height: 112
                        },
                        {
                            type: "eventGateway",
                            bpmnType: "eventGateway",
                            label: "事件网关",
                            shortLabel: "事件",
                            description: "通过事件结果驱动后续路径。",
                            kind: "gateway",
                            width: 112,
                            height: 112
                        }
                    ]
                },
                {
                    category: "活动与泳道",
                    nodes: [
                        {
                            type: "subProcess",
                            bpmnType: "subProcess",
                            label: "子流程",
                            shortLabel: "子流",
                            description: "组织一组可折叠的流程步骤。",
                            kind: "container",
                            width: 168,
                            height: 96
                        },
                        {
                            type: "pool",
                            bpmnType: "pool",
                            label: "池",
                            shortLabel: "池",
                            description: "表示一个参与者或业务主体。",
                            kind: "container",
                            width: 168,
                            height: 96
                        },
                        {
                            type: "lane",
                            bpmnType: "lane",
                            label: "泳道",
                            shortLabel: "泳道",
                            description: "表示参与者内部职责划分。",
                            kind: "container",
                            width: 168,
                            height: 96
                        }
                    ]
                },
                {
                    category: "补充说明",
                    nodes: [
                        {
                            type: "annotation",
                            bpmnType: "textAnnotation",
                            label: "注释",
                            shortLabel: "注释",
                            description: "补充流程说明和备注信息。",
                            kind: "artifact",
                            width: 168,
                            height: 86
                        }
                    ]
                }
            ],
            paletteKeyword: "",
            canvasNodes: [],
            canvasEdges: [],
            selectedNodeId: "",
            selectedEdgeId: "",
            pendingSourceId: "",
            hoverNodeId: "",
            dragPaletteNode: null,
            dragNodeState: {
                active: false,
                nodeId: "",
                offsetX: 0,
                offsetY: 0,
                startX: 0,
                startY: 0,
                childOffsets: []
            },
            resizeNodeState: {
                active: false,
                nodeId: "",
                startPointerX: 0,
                startPointerY: 0,
                startWidth: 0,
                startHeight: 0
            },
            panCanvasState: {
                active: false,
                startClientX: 0,
                startClientY: 0,
                startScrollLeft: 0,
                startScrollTop: 0
            },
            connectDragState: {
                active: false,
                sourceId: "",
                currentX: 0,
                currentY: 0,
                targetNodeId: ""
            },
            expandedGroups: {},
            nextNodeIndex: 1,
            canvasScale: 1,
            canvasWidth: 2400,
            canvasHeight: 1400,
            leftPanelCollapsed: false,
            rightPanelCollapsed: false,
            importedProcessDefinitionId: "",
            importedDeploymentId: ""
        };
    },
    computed: {
        selectedNode: function () {
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                if (this.canvasNodes[index].id === this.selectedNodeId) {
                    return this.canvasNodes[index];
                }
            }
            return null;
        },
        selectedEdge: function () {
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                if (this.canvasEdges[index].id === this.selectedEdgeId) {
                    return this.canvasEdges[index];
                }
            }
            return null;
        },
        pendingSourceNode: function () {
            if (!this.pendingSourceId) {
                return null;
            }
            return this.findNodeById(this.pendingSourceId);
        },
        visibleCanvasNodes: function () {
            var nodes = [];
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (this.isNodeVisible(node)) {
                    nodes.push(node);
                }
            }
            return nodes;
        },
        visibleContainerNodes: function () {
            return this.visibleCanvasNodes.filter(function (node) {
                return node.kind === "container";
            });
        },
        visibleLeafNodes: function () {
            return this.visibleCanvasNodes.filter(function (node) {
                return node.kind !== "container";
            });
        },
        visibleCanvasEdges: function () {
            var edges = [];
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                var edge = this.canvasEdges[index];
                if (this.isEdgeVisible(edge)) {
                    edges.push(edge);
                }
            }
            return edges;
        },
        connectPreviewPath: function () {
            if (!this.connectDragState.active || !this.connectDragState.sourceId) {
                return "";
            }
            return this.resolvePreviewEdgePath(this.connectDragState.sourceId, this.connectDragState.currentX, this.connectDragState.currentY);
        },
        totalPaletteNodeCount: function () {
            return this.getAllPaletteNodes().length;
        },
        visibleNodeGroups: function () {
            var keyword = this.normalizeKeyword(this.paletteKeyword);
            var groups = [];
            for (var index = 0; index < this.nodeGroups.length; index += 1) {
                var group = this.nodeGroups[index];
                var visibleNodes = [];
                for (var nodeIndex = 0; nodeIndex < group.nodes.length; nodeIndex += 1) {
                    var node = group.nodes[nodeIndex];
                    if (keyword && !this.matchPaletteNode(node, keyword, group.category)) {
                        continue;
                    }
                    visibleNodes.push(node);
                }
                if (visibleNodes.length) {
                    groups.push({
                        category: group.category,
                        nodes: visibleNodes
                    });
                }
            }
            return groups;
        },
        canvasScalePercent: function () {
            return Math.round(this.canvasScale * 100) + "%";
        },
        viewportStyle: function () {
            return {
                width: Math.round(this.canvasWidth * this.canvasScale) + "px",
                height: Math.round(this.canvasHeight * this.canvasScale) + "px"
            };
        },
        contentStyle: function () {
            return {
                width: this.canvasWidth + "px",
                height: this.canvasHeight + "px",
                transform: "scale(" + this.canvasScale + ")"
            };
        },
        availableSubProcessOptions: function () {
            if (!this.selectedNode) {
                return [];
            }
            var options = [];
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (node.bpmnType !== "subProcess" || node.id === this.selectedNode.id) {
                    continue;
                }
                if (this.isNodeDescendantOf(node.id, this.selectedNode.id)) {
                    continue;
                }
                if (!node.expanded && node.id !== this.selectedNode.parentId) {
                    continue;
                }
                options.push({
                    id: node.id,
                    name: node.name || node.code || node.id
                });
            }
            return options;
        },
        selectedElementTypeLabel: function () {
            if (this.selectedNode) {
                return "节点属性";
            }
            if (this.selectedEdge) {
                return "连线属性";
            }
            return "流程概览";
        }
    },
    methods: {
        ensureStyle: function () {
            if (document.getElementById(processDesignerStyleId)) {
                return;
            }
            var style = document.createElement("style");
            style.id = processDesignerStyleId;
            style.textContent = [
                ".designer-layout{display:grid;grid-template-columns:300px minmax(0,1fr) 320px;gap:18px;align-items:stretch;min-height:calc(100vh - 260px);}",
                ".designer-layout-left-collapsed{grid-template-columns:64px minmax(0,1fr) 320px;}",
                ".designer-layout-right-collapsed{grid-template-columns:300px minmax(0,1fr) 64px;}",
                ".designer-layout-left-collapsed.designer-layout-right-collapsed{grid-template-columns:64px minmax(0,1fr) 64px;}",
                ".designer-sidebar,.designer-stage-panel{min-width:0;border:1px solid #e3ebf7;border-radius:22px;background:linear-gradient(180deg,#fbfdff 0%,#f4f8fd 100%);box-shadow:inset 0 1px 0 rgba(255,255,255,0.85);}",
                ".designer-sidebar{padding:18px;display:flex;flex-direction:column;gap:16px;overflow:hidden;}",
                ".designer-sidebar-collapsed{padding:18px 10px;}",
                ".designer-sidebar-collapsed .designer-sidebar-head{justify-content:center;}",
                ".designer-sidebar-collapsed .designer-sidebar-head>div:first-child{display:none;}",
                ".designer-sidebar-collapsed .designer-sidebar-actions{width:100%;justify-content:center;}",
                ".designer-stage-panel{padding:18px;display:flex;flex-direction:column;gap:16px;}",
                ".designer-header-actions{display:flex;gap:10px;flex-wrap:wrap;}",
                ".designer-sidebar-head{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;}",
                ".designer-sidebar-actions{display:flex;align-items:center;gap:8px;}",
                ".designer-collapse-button{width:28px;height:28px;border:none;border-radius:10px;background:rgba(227,235,247,0.88);color:#30445f;cursor:pointer;font-weight:700;transition:background .18s ease,transform .18s ease;}",
                ".designer-collapse-button:hover{background:#d6e4fb;transform:translateY(-1px);}",
                ".designer-panel-title{font-size:18px;font-weight:700;color:#18263f;}",
                ".designer-panel-subtitle{font-size:12px;line-height:1.7;color:#70829d;}",
                ".designer-group-scroll{display:grid;gap:12px;overflow:auto;padding-right:4px;}",
                ".designer-group{display:grid;gap:10px;}",
                ".designer-group-toggle{width:100%;padding:10px 12px;border:none;border-radius:14px;background:rgba(227,235,247,0.76);display:grid;grid-template-columns:minmax(0,1fr) auto auto;align-items:center;gap:10px;cursor:pointer;text-align:left;transition:background .18s ease,color .18s ease;}",
                ".designer-group-toggle:hover{background:rgba(203,220,242,0.92);}",
                ".designer-group-title{font-size:13px;font-weight:700;color:#3b4e68;}",
                ".designer-group-meta{font-size:12px;color:#6e84a3;}",
                ".designer-group-arrow{font-size:16px;line-height:1;color:#5f7494;}",
                ".designer-node-list{display:grid;gap:10px;}",
                ".designer-palette-node{padding:12px;border:1px solid #d9e5f4;border-radius:18px;background:#ffffff;display:flex;gap:12px;align-items:center;cursor:grab;transition:transform .18s ease,box-shadow .18s ease,border-color .18s ease;}",
                ".designer-palette-node:hover{transform:translateY(-1px);box-shadow:0 14px 24px rgba(52,119,246,0.12);border-color:#b9d0f7;}",
                ".designer-palette-node:active{cursor:grabbing;}",
                ".designer-palette-node-icon{flex-shrink:0;display:flex;align-items:center;justify-content:center;color:#1f3d74;font-size:12px;font-weight:700;}",
                ".designer-palette-node-main{min-width:0;display:grid;gap:4px;}",
                ".designer-palette-node-title{font-size:14px;font-weight:700;color:#1a2942;}",
                ".designer-palette-node-text{font-size:12px;line-height:1.6;color:#70829d;}",
                ".designer-palette-node-meta{font-size:11px;color:#8ca0bc;}",
                ".designer-stage-head{display:flex;justify-content:space-between;gap:14px;align-items:flex-start;}",
                ".designer-stage-stats{display:flex;gap:8px;flex-wrap:wrap;justify-content:flex-end;}",
                ".designer-stage-toolbar{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:10px 12px;border:1px solid #dbe6f6;border-radius:16px;background:rgba(255,255,255,0.88);}",
                ".designer-stage-toolbar-group{display:flex;align-items:center;gap:8px;flex-wrap:wrap;}",
                ".designer-tool-button{height:34px;min-width:34px;padding:0 12px;border:none;border-radius:12px;background:rgba(227,235,247,0.88);color:#30445f;cursor:pointer;transition:background .18s ease,transform .18s ease,box-shadow .18s ease;}",
                ".designer-tool-button:hover{background:#d6e4fb;transform:translateY(-1px);box-shadow:0 10px 18px rgba(52,119,246,0.12);}",
                ".designer-tool-button-wide{min-width:88px;}",
                ".designer-toolbar-tip{font-size:12px;color:#6d809d;}",
                ".designer-stage-wrapper{position:relative;flex:1;min-height:620px;overflow:auto;border:1px dashed #cfdced;border-radius:22px;background:linear-gradient(180deg,#ffffff 0%,#f6faff 100%);cursor:grab;}",
                ".designer-stage-wrapper-dragging{border-color:#7eb0ff;box-shadow:inset 0 0 0 2px rgba(52,119,246,0.12);}",
                ".designer-stage-wrapper-panning{cursor:grabbing;}",
                ".designer-stage-wrapper-panning .designer-stage-node,.designer-stage-wrapper-panning .designer-stage-line{cursor:grabbing;}",
                ".designer-stage-viewport{position:relative;}",
                ".designer-stage-content{position:relative;transform-origin:0 0;}",
                ".designer-stage-grid{position:absolute;inset:0;background-image:linear-gradient(rgba(148,163,184,0.14) 1px,transparent 1px),linear-gradient(90deg,rgba(148,163,184,0.14) 1px,transparent 1px);background-size:32px 32px;pointer-events:none;}",
                ".designer-stage-lines{position:absolute;inset:0;width:100%;height:100%;overflow:visible;pointer-events:none;z-index:2;}",
                ".designer-stage-line{fill:none;stroke:#7a93b8;stroke-width:3;stroke-linecap:round;stroke-linejoin:round;cursor:pointer;pointer-events:auto;}",
                ".designer-stage-line.active{stroke:#3477f6;}",
                ".designer-stage-line-preview{stroke:#f59e0b;stroke-dasharray:8 6;pointer-events:none;}",
                ".designer-stage-watermark{position:absolute;top:26px;right:28px;padding:14px 16px;border-radius:18px;border:1px solid rgba(207,220,237,0.88);background:rgba(255,255,255,0.86);backdrop-filter:blur(6px);display:grid;gap:4px;box-shadow:0 16px 30px rgba(31,45,61,0.08);pointer-events:none;}",
                ".designer-stage-watermark span{font-size:11px;color:#7c8fa9;letter-spacing:1px;}",
                ".designer-stage-watermark strong{font-size:14px;color:#20324c;}",
                ".designer-stage-empty{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);padding:18px 22px;border-radius:18px;background:rgba(255,255,255,0.92);border:1px dashed #d7e4f6;color:#70829d;font-size:13px;line-height:1.7;max-width:360px;text-align:center;}",
                ".designer-stage-node{position:absolute;padding:16px 12px 12px;border-radius:20px;border:2px solid #cfe0f6;background:#ffffff;box-shadow:0 16px 26px rgba(31,45,61,0.10);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;cursor:pointer;user-select:none;transition:border-color .18s ease,box-shadow .18s ease,transform .18s ease;z-index:3;}",
                ".designer-stage-node.active{border-color:#3477f6;box-shadow:0 20px 32px rgba(52,119,246,0.18);transform:translateY(-1px);}",
                ".designer-stage-node-connect-pending{border-color:#f59e0b;box-shadow:0 20px 32px rgba(245,158,11,0.18);}",
                ".designer-stage-node-event{border-radius:999px;background:linear-gradient(180deg,#effcf5 0%,#dcfce7 100%);}",
                ".designer-stage-node-task{background:linear-gradient(180deg,#f8fbff 0%,#e7f0ff 100%);}",
                ".designer-stage-node-gateway{background:linear-gradient(180deg,#fff8ec 0%,#ffedd5 100%);}",
                ".designer-stage-node-container{background:linear-gradient(180deg,#f5f7ff 0%,#e9edff 100%);z-index:1;}",
                ".designer-stage-node-artifact{background:linear-gradient(180deg,#fffdf5 0%,#fff8d9 100%);}",
                ".designer-stage-node-badge{position:absolute;top:-12px;padding:3px 10px;border-radius:999px;background:#e9f1ff;color:#2a61bf;font-size:11px;font-weight:700;box-shadow:0 8px 18px rgba(52,119,246,0.10);}",
                ".designer-subprocess-toggle{position:absolute;right:10px;top:10px;width:26px;height:26px;border:none;border-radius:8px;background:rgba(227,235,247,0.92);color:#30445f;font-size:16px;font-weight:700;line-height:1;cursor:pointer;box-shadow:0 8px 16px rgba(52,119,246,0.10);}",
                ".designer-subprocess-toggle:hover{background:#d6e4fb;}",
                ".designer-resize-handle{position:absolute;right:8px;bottom:8px;width:16px;height:16px;border:none;border-radius:4px;background:linear-gradient(135deg,#d6e4fb 0%,#3477f6 100%);cursor:nwse-resize;box-shadow:0 8px 14px rgba(52,119,246,0.18);}",
                ".designer-connect-handle{position:absolute;left:50%;top:50%;width:34px;height:34px;margin-left:-17px;margin-top:-17px;border:none;border-radius:50%;background:linear-gradient(180deg,#ffffff 0%,#fef3c7 100%);box-shadow:0 10px 18px rgba(245,158,11,0.22);display:flex;align-items:center;justify-content:center;cursor:crosshair;z-index:3;}",
                ".designer-connect-handle span{position:relative;display:block;width:16px;height:16px;}",
                ".designer-connect-handle span:before,.designer-connect-handle span:after{content:'';position:absolute;background:#d97706;border-radius:999px;}",
                ".designer-connect-handle span:before{left:7px;top:0;width:2px;height:16px;}",
                ".designer-connect-handle span:after{left:0;top:7px;width:16px;height:2px;}",
                ".designer-connect-handle:hover{transform:scale(1.06);box-shadow:0 14px 24px rgba(245,158,11,0.28);}",
                ".designer-stage-node-shape{display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:700;color:#23406f;}",
                ".designer-shape-event{width:46px;height:46px;border-radius:50%;border:2px solid rgba(16,163,127,0.42);background:rgba(255,255,255,0.68);}",
                ".designer-shape-task{width:52px;height:32px;border-radius:12px;border:2px solid rgba(52,119,246,0.28);background:rgba(255,255,255,0.78);}",
                ".designer-shape-gateway{width:44px;height:44px;border:2px solid rgba(245,158,11,0.38);background:rgba(255,255,255,0.76);transform:rotate(45deg);border-radius:12px;}",
                ".designer-shape-gateway span{transform:rotate(-45deg);}",
                ".designer-shape-container{width:58px;height:34px;border-radius:10px;border:2px dashed rgba(99,102,241,0.34);background:rgba(255,255,255,0.78);}",
                ".designer-shape-artifact{width:58px;height:34px;border-radius:8px;border:2px dashed rgba(217,119,6,0.34);background:rgba(255,255,255,0.78);}",
                ".designer-stage-node-name{max-width:100%;font-size:13px;font-weight:700;color:#1d2d46;text-align:center;line-height:1.4;word-break:break-word;}",
                ".designer-stage-node-type{font-size:12px;color:#6d809d;text-align:center;}",
                ".designer-summary-card{padding:14px 16px;border-radius:18px;border:1px solid #dfe8f6;background:rgba(255,255,255,0.88);display:grid;gap:12px;}",
                ".designer-summary-title{font-size:14px;font-weight:700;color:#20324c;}",
                ".designer-summary-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;}",
                ".designer-summary-item{padding:10px 12px;border-radius:14px;background:rgba(241,246,253,0.92);display:grid;gap:6px;}",
                ".designer-summary-item span{font-size:12px;color:#7487a1;}",
                ".designer-summary-item strong{font-size:14px;color:#223550;}",
                ".designer-summary-list{display:grid;gap:8px;}",
                ".designer-summary-list span{font-size:12px;line-height:1.7;color:#6d809d;}",
                ".designer-form-block{display:grid;gap:10px;}",
                ".designer-node-meta{display:flex;gap:8px;flex-wrap:wrap;}",
                "@media (max-width: 1460px){.designer-layout{grid-template-columns:280px minmax(0,1fr) 300px;}}",
                "@media (max-width: 1240px){.designer-layout{grid-template-columns:1fr;}.designer-stage-wrapper{min-height:460px;}.designer-summary-grid{grid-template-columns:repeat(2,minmax(0,1fr));}}"
            ].join("");
            document.head.appendChild(style);
        },
        getAllPaletteNodes: function () {
            var nodes = [];
            for (var groupIndex = 0; groupIndex < this.nodeGroups.length; groupIndex += 1) {
                var group = this.nodeGroups[groupIndex];
                for (var nodeIndex = 0; nodeIndex < group.nodes.length; nodeIndex += 1) {
                    nodes.push(group.nodes[nodeIndex]);
                }
            }
            return nodes;
        },
        normalizeKeyword: function (keyword) {
            return (keyword || "").toLowerCase().replace(/\s+/g, "");
        },
        matchPaletteNode: function (node, keyword, category) {
            var content = [
                category,
                node.label,
                node.shortLabel,
                node.description,
                node.type,
                node.bpmnType
            ].join("").toLowerCase().replace(/\s+/g, "");
            return content.indexOf(keyword) >= 0;
        },
        buildExportFileName: function (extension) {
            var now = new Date();
            var pad = function (value) {
                return value < 10 ? "0" + value : String(value);
            };
            var timestamp = [
                now.getFullYear(),
                pad(now.getMonth() + 1),
                pad(now.getDate()),
                "-",
                pad(now.getHours()),
                pad(now.getMinutes()),
                pad(now.getSeconds())
            ].join("");
            return "wcdk-process-" + timestamp + "." + extension;
        },
        escapeXml: function (value) {
            return String(value == null ? "" : value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&apos;");
        },
        sanitizeBpmnId: function (value, fallbackPrefix, usedIds) {
            var source = String(value || "").replace(/[^0-9A-Za-z_:.\\-]/g, "_");
            if (!source) {
                source = fallbackPrefix;
            }
            if (/^[^A-Za-z_]/.test(source)) {
                source = fallbackPrefix + "_" + source;
            }
            var candidate = source;
            var index = 1;
            while (usedIds[candidate]) {
                candidate = source + "_" + index;
                index += 1;
            }
            usedIds[candidate] = true;
            return candidate;
        },
        downloadTextFile: function (fileName, content, mimeType) {
            var blob = new Blob([content], {type: mimeType || "text/plain;charset=utf-8"});
            this.downloadBlobFile(fileName, blob);
        },
        downloadBlobFile: function (fileName, blob) {
            var link = document.createElement("a");
            var url = URL.createObjectURL(blob);
            link.href = url;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.setTimeout(function () {
                URL.revokeObjectURL(url);
            }, 0);
        },
        buildProcessDesignerExportPayload: function (format) {
            var nodes = [];
            var edges = [];
            for (var nodeIndex = 0; nodeIndex < this.canvasNodes.length; nodeIndex += 1) {
                var node = this.canvasNodes[nodeIndex];
                nodes.push({
                    id: node.id,
                    type: node.type,
                    bpmnType: node.bpmnType,
                    kind: node.kind,
                    label: node.label,
                    shortLabel: node.shortLabel,
                    name: node.name,
                    code: node.code,
                    documentation: node.documentation,
                    parentId: node.parentId,
                    expanded: !!node.expanded,
                    width: node.width,
                    height: node.height,
                    x: node.x,
                    y: node.y
                });
            }
            for (var edgeIndex = 0; edgeIndex < this.canvasEdges.length; edgeIndex += 1) {
                var edge = this.canvasEdges[edgeIndex];
                edges.push({
                    id: edge.id,
                    sourceId: edge.sourceId,
                    targetId: edge.targetId,
                    name: edge.name
                });
            }
            return {
                format: format,
                canvasWidth: this.canvasWidth,
                canvasHeight: this.canvasHeight,
                nodes: nodes,
                edges: edges
            };
        },
        decodeBase64ToBlob: function (contentBase64, contentType) {
            var binary = window.atob(contentBase64 || "");
            var bytes = new Uint8Array(binary.length);
            for (var index = 0; index < binary.length; index += 1) {
                bytes[index] = binary.charCodeAt(index);
            }
            return new Blob([bytes], {type: contentType || "application/octet-stream"});
        },
        requestProcessDesignerExport: async function (format) {
            var result = await window.AppService.requestJson("/flowable/designer/export", {
                method: "POST",
                body: JSON.stringify(this.buildProcessDesignerExportPayload(format))
            });
            return result.data || null;
        },
        loadRouteProcessDefinitionIfNeeded: async function () {
            var route = this.$route || {};
            var query = route.query || {};
            var processDefinitionId = query.processDefinitionId || "";
            var deploymentId = query.deploymentId || "";
            if (!processDefinitionId) {
                this.importedProcessDefinitionId = "";
                this.importedDeploymentId = deploymentId;
                this.$nextTick(this.handleCenterCanvas);
                return;
            }
            if (this.importedProcessDefinitionId === processDefinitionId && this.importedDeploymentId === deploymentId) {
                this.$nextTick(this.handleCenterCanvas);
                return;
            }
            try {
                var detail = await this.$root.fetchProcessDefinitionDetail(processDefinitionId);
                this.importProcessDefinitionDetail(detail, deploymentId);
                this.importedProcessDefinitionId = processDefinitionId;
                this.importedDeploymentId = deploymentId;
                this.$message.success("已加载部署流程到设计画布");
            } catch (error) {
                this.$message.error(error && error.message ? error.message : "加载部署流程失败");
            }
        },
        importProcessDefinitionDetail: function (detail, deploymentId) {
            if (!detail) {
                throw new Error("未查询到流程定义详情");
            }
            var detailNodes = Array.isArray(detail.nodes) ? detail.nodes : [];
            var detailEdges = Array.isArray(detail.sequenceFlows) ? detail.sequenceFlows : [];
            var parentMap = this.buildImportedParentMap(detail.bpmnXml);
            var childCountMap = {};
            for (var parentNodeId in parentMap) {
                if (Object.prototype.hasOwnProperty.call(parentMap, parentNodeId) && parentMap[parentNodeId]) {
                    childCountMap[parentMap[parentNodeId]] = (childCountMap[parentMap[parentNodeId]] || 0) + 1;
                }
            }
            var nodes = [];
            var maxRight = 0;
            var maxBottom = 0;
            for (var nodeIndex = 0; nodeIndex < detailNodes.length; nodeIndex += 1) {
                var importedNode = this.buildImportedCanvasNode(
                    detailNodes[nodeIndex],
                    parentMap[detailNodes[nodeIndex].elementId] || "",
                    !!childCountMap[detailNodes[nodeIndex].elementId]
                );
                nodes.push(importedNode);
                maxRight = Math.max(maxRight, importedNode.x + importedNode.width);
                maxBottom = Math.max(maxBottom, importedNode.y + importedNode.height);
            }
            var edges = [];
            for (var edgeIndex = 0; edgeIndex < detailEdges.length; edgeIndex += 1) {
                var sequenceFlow = detailEdges[edgeIndex] || {};
                edges.push({
                    id: sequenceFlow.elementId || ("designer-edge-" + sequenceFlow.sourceRef + "-" + sequenceFlow.targetRef),
                    sourceId: sequenceFlow.sourceRef || "",
                    targetId: sequenceFlow.targetRef || "",
                    name: sequenceFlow.elementName || ""
                });
            }
            this.canvasNodes = nodes;
            this.canvasEdges = edges;
            this.selectedNodeId = "";
            this.selectedEdgeId = "";
            this.pendingSourceId = "";
            this.hoverNodeId = "";
            this.nextNodeIndex = nodes.length + 1;
            this.canvasScale = 1;
            this.canvasWidth = Math.max(2400, Math.ceil(maxRight + 240));
            this.canvasHeight = Math.max(1400, Math.ceil(maxBottom + 240));
            this.importedDeploymentId = deploymentId || detail.deploymentId || "";
            this.$nextTick(this.handleCenterCanvas);
        },
        buildImportedCanvasNode: function (detailNode, parentId, expanded) {
            var bpmnType = this.resolveImportedBpmnType(detailNode && detailNode.elementType);
            var paletteNode = this.findPaletteNodeByBpmnType(bpmnType);
            var width = Math.max(this.toFiniteNumber(detailNode && detailNode.width, paletteNode.width), paletteNode.width);
            var height = Math.max(this.toFiniteNumber(detailNode && detailNode.height, paletteNode.height), paletteNode.height);
            return {
                id: detailNode && detailNode.elementId ? detailNode.elementId : ("designer-node-" + this.nextNodeIndex),
                type: paletteNode.type,
                bpmnType: paletteNode.bpmnType,
                kind: paletteNode.kind,
                allowIncoming: this.isNodeIncomingAllowed(paletteNode.bpmnType),
                allowOutgoing: this.isNodeOutgoingAllowed(paletteNode.bpmnType),
                allowSequenceFlow: this.isSequenceFlowNode(paletteNode.bpmnType),
                label: paletteNode.label,
                shortLabel: paletteNode.shortLabel,
                name: detailNode && detailNode.elementName ? detailNode.elementName : paletteNode.label,
                code: detailNode && detailNode.elementId ? detailNode.elementId : this.buildFlowableCode(paletteNode.bpmnType, this.nextNodeIndex),
                documentation: detailNode && detailNode.documentation ? detailNode.documentation : "",
                parentId: parentId || "",
                expanded: paletteNode.bpmnType === "subProcess" ? !!expanded : false,
                collapsedWidth: paletteNode.bpmnType === "subProcess" ? width : 0,
                collapsedHeight: paletteNode.bpmnType === "subProcess" ? height : 0,
                expandedWidth: paletteNode.bpmnType === "subProcess" ? Math.max(width, 320) : 0,
                expandedHeight: paletteNode.bpmnType === "subProcess" ? Math.max(height, 220) : 0,
                width: width,
                height: height,
                x: Math.max(Math.round(this.toFiniteNumber(detailNode && detailNode.x, 0)), 0),
                y: Math.max(Math.round(this.toFiniteNumber(detailNode && detailNode.y, 0)), 0)
            };
        },
        buildImportedParentMap: function (bpmnXml) {
            if (!bpmnXml) {
                return {};
            }
            var parser = new DOMParser();
            var documentNode = parser.parseFromString(bpmnXml, "text/xml");
            var parseError = documentNode.getElementsByTagName("parsererror");
            if (parseError && parseError.length) {
                return {};
            }
            var processElement = this.findFirstElementByLocalName(documentNode, "process");
            if (!processElement) {
                return {};
            }
            var parentMap = {};
            this.collectImportedParentMap(processElement, "", parentMap);
            return parentMap;
        },
        collectImportedParentMap: function (containerElement, parentId, parentMap) {
            var children = containerElement && containerElement.children ? containerElement.children : [];
            for (var index = 0; index < children.length; index += 1) {
                var child = children[index];
                var localName = child.localName || child.nodeName || "";
                var bpmnType = this.resolveImportedBpmnType(localName);
                if (!bpmnType) {
                    continue;
                }
                var elementId = child.getAttribute("id") || "";
                if (!elementId) {
                    continue;
                }
                parentMap[elementId] = parentId || "";
                if (bpmnType === "subProcess") {
                    this.collectImportedParentMap(child, elementId, parentMap);
                }
            }
        },
        findPaletteNodeByBpmnType: function (bpmnType) {
            var allPaletteNodes = this.getAllPaletteNodes();
            for (var index = 0; index < allPaletteNodes.length; index += 1) {
                if (allPaletteNodes[index].bpmnType === bpmnType) {
                    return allPaletteNodes[index];
                }
            }
            return {
                type: bpmnType || "userTask",
                bpmnType: bpmnType || "userTask",
                label: bpmnType || "任务",
                shortLabel: "节点",
                description: "",
                kind: "task",
                width: 120,
                height: 72
            };
        },
        resolveImportedBpmnType: function (elementType) {
            var typeMap = {
                StartEvent: "startEvent",
                startEvent: "startEvent",
                EndEvent: "endEvent",
                endEvent: "endEvent",
                BoundaryEvent: "boundaryEvent",
                boundaryEvent: "boundaryEvent",
                IntermediateCatchEvent: "intermediateCatchEvent",
                intermediateCatchEvent: "intermediateCatchEvent",
                IntermediateThrowEvent: "intermediateThrowEvent",
                intermediateThrowEvent: "intermediateThrowEvent",
                UserTask: "userTask",
                userTask: "userTask",
                ScriptTask: "scriptTask",
                scriptTask: "scriptTask",
                ServiceTask: "serviceTask",
                serviceTask: "serviceTask",
                MailTask: "mailTask",
                mailTask: "mailTask",
                ManualTask: "manualTask",
                manualTask: "manualTask",
                ReceiveTask: "receiveTask",
                receiveTask: "receiveTask",
                BusinessRuleTask: "businessRuleTask",
                businessRuleTask: "businessRuleTask",
                CallActivity: "callActivity",
                callActivity: "callActivity",
                SubProcess: "subProcess",
                subProcess: "subProcess",
                ParallelGateway: "parallelGateway",
                parallelGateway: "parallelGateway",
                ExclusiveGateway: "exclusiveGateway",
                exclusiveGateway: "exclusiveGateway",
                InclusiveGateway: "inclusiveGateway",
                inclusiveGateway: "inclusiveGateway",
                EventGateway: "eventGateway",
                eventGateway: "eventGateway",
                TextAnnotation: "textAnnotation",
                textAnnotation: "textAnnotation"
            };
            return typeMap[elementType] || "";
        },
        findFirstElementByLocalName: function (root, localName) {
            var elements = root.getElementsByTagNameNS("*", localName);
            return elements && elements.length ? elements[0] : null;
        },
        toFiniteNumber: function (value, fallback) {
            var nextValue = Number(value);
            return Number.isFinite(nextValue) ? nextValue : fallback;
        },
        collectExportableNodes: function () {
            var supportedTypes = {
                startEvent: true,
                endEvent: true,
                boundaryEvent: true,
                intermediateCatchEvent: true,
                intermediateThrowEvent: true,
                userTask: true,
                scriptTask: true,
                serviceTask: true,
                mailTask: true,
                manualTask: true,
                receiveTask: true,
                businessRuleTask: true,
                callActivity: true,
                subProcess: true,
                parallelGateway: true,
                exclusiveGateway: true,
                inclusiveGateway: true,
                eventGateway: true,
                textAnnotation: true
            };
            var exportableNodes = [];
            var skippedNodes = [];
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (supportedTypes[node.bpmnType]) {
                    exportableNodes.push(node);
                } else {
                    skippedNodes.push(node);
                }
            }
            return {
                exportableNodes: exportableNodes,
                skippedNodes: skippedNodes
            };
        },
        resolveParentNodeName: function (parentId) {
            if (!parentId) {
                return "主流程";
            }
            var parentNode = this.findNodeById(parentId);
            return parentNode ? (parentNode.name || parentNode.code || parentNode.id) : "主流程";
        },
        resolveNodeContainerId: function (nodeId) {
            var node = this.findNodeById(nodeId);
            return node && node.parentId ? node.parentId : "";
        },
        keepNodeInsideParent: function (node) {
            if (!node || !node.parentId) {
                return;
            }
            var parentNode = this.findNodeById(node.parentId);
            if (!parentNode) {
                return;
            }
            var padding = 16;
            var minX = parentNode.x + padding;
            var minY = parentNode.y + 44;
            var maxX = parentNode.x + parentNode.width - node.width - padding;
            var maxY = parentNode.y + parentNode.height - node.height - padding;
            node.x = Math.max(minX, Math.min(node.x, Math.max(maxX, minX)));
            node.y = Math.max(minY, Math.min(node.y, Math.max(maxY, minY)));
        },
        getNodeMinimumWidth: function (node) {
            if (!node) {
                return 80;
            }
            if (node.kind === "event") {
                return 72;
            }
            if (node.kind === "gateway") {
                return 88;
            }
            if (node.bpmnType === "subProcess") {
                return 220;
            }
            return 120;
        },
        getNodeMinimumHeight: function (node) {
            if (!node) {
                return 60;
            }
            if (node.kind === "event") {
                return 72;
            }
            if (node.kind === "gateway") {
                return 88;
            }
            if (node.bpmnType === "subProcess") {
                return 160;
            }
            return 72;
        },
        getDescendantNodes: function (parentId) {
            var descendants = [];
            if (!parentId) {
                return descendants;
            }
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (this.isNodeDescendantOf(node.id, parentId)) {
                    descendants.push(node);
                }
            }
            return descendants;
        },
        buildChildOffsetSnapshot: function (parentNode) {
            var result = [];
            var descendants = this.getDescendantNodes(parentNode.id);
            for (var index = 0; index < descendants.length; index += 1) {
                result.push({
                    id: descendants[index].id,
                    offsetX: descendants[index].x - parentNode.x,
                    offsetY: descendants[index].y - parentNode.y
                });
            }
            return result;
        },
        moveChildNodesWithParent: function (parentNode) {
            if (!parentNode || !this.dragNodeState.childOffsets || !this.dragNodeState.childOffsets.length) {
                return;
            }
            for (var index = 0; index < this.dragNodeState.childOffsets.length; index += 1) {
                var item = this.dragNodeState.childOffsets[index];
                var childNode = this.findNodeById(item.id);
                if (!childNode) {
                    continue;
                }
                childNode.x = this.normalizeCanvasX(parentNode.x + item.offsetX, childNode.width);
                childNode.y = this.normalizeCanvasY(parentNode.y + item.offsetY, childNode.height);
                this.keepNodeInsideParent(childNode);
            }
        },
        expandSubProcessSize: function (node) {
            if (!node || node.bpmnType !== "subProcess") {
                return;
            }
            if (!node.collapsedWidth) {
                node.collapsedWidth = node.width;
            }
            if (!node.collapsedHeight) {
                node.collapsedHeight = node.height;
            }
            if (!node.expandedWidth) {
                node.expandedWidth = Math.max(node.width, node.collapsedWidth + 120, 320);
            }
            if (!node.expandedHeight) {
                node.expandedHeight = Math.max(node.height, node.collapsedHeight + 100, 220);
            }
            node.width = Math.max(node.expandedWidth, node.collapsedWidth + 120, 320);
            node.height = Math.max(node.expandedHeight, node.collapsedHeight + 100, 220);
        },
        collapseSubProcessSize: function (node) {
            if (!node || node.bpmnType !== "subProcess") {
                return;
            }
            if (node.collapsedWidth) {
                node.width = node.collapsedWidth;
            }
            if (node.collapsedHeight) {
                node.height = node.collapsedHeight;
            }
        },
        isNodeVisible: function (node) {
            if (!node) {
                return false;
            }
            var currentParentId = node.parentId;
            var guard = 0;
            while (currentParentId && guard < this.canvasNodes.length) {
                var parentNode = this.findNodeById(currentParentId);
                if (!parentNode) {
                    return false;
                }
                if (!parentNode.expanded) {
                    return false;
                }
                currentParentId = parentNode.parentId;
                guard += 1;
            }
            return true;
        },
        isEdgeVisible: function (edge) {
            if (!edge) {
                return false;
            }
            return this.isNodeVisible(this.findNodeById(edge.sourceId))
                && this.isNodeVisible(this.findNodeById(edge.targetId));
        },
        isNodeDescendantOf: function (nodeId, targetAncestorId) {
            if (!nodeId || !targetAncestorId || nodeId === targetAncestorId) {
                return false;
            }
            var currentNode = this.findNodeById(nodeId);
            var guard = 0;
            while (currentNode && currentNode.parentId && guard < this.canvasNodes.length) {
                if (currentNode.parentId === targetAncestorId) {
                    return true;
                }
                currentNode = this.findNodeById(currentNode.parentId);
                guard += 1;
            }
            return false;
        },
        resolveDropParentSubProcessId: function (rawX, rawY) {
            var matchedNode = null;
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (node.bpmnType !== "subProcess") {
                    continue;
                }
                if (!node.expanded) {
                    continue;
                }
                if (rawX < node.x || rawX > node.x + node.width || rawY < node.y || rawY > node.y + node.height) {
                    continue;
                }
                if (!matchedNode || (node.width * node.height) < (matchedNode.width * matchedNode.height)) {
                    matchedNode = node;
                }
            }
            return matchedNode ? matchedNode.id : "";
        },
        buildExportNodeLookup: function (exportableNodes) {
            var lookup = {};
            for (var index = 0; index < exportableNodes.length; index += 1) {
                lookup[exportableNodes[index].id] = exportableNodes[index];
            }
            return lookup;
        },
        sanitizeNodeParentRelations: function (exportableNodes, exportableNodeLookup) {
            for (var index = 0; index < exportableNodes.length; index += 1) {
                var node = exportableNodes[index];
                if (!node.parentId) {
                    continue;
                }
                var parentNode = exportableNodeLookup[node.parentId];
                if (!parentNode || parentNode.bpmnType !== "subProcess" || parentNode.id === node.id || this.isNodeDescendantOf(parentNode.id, node.id)) {
                    node.parentId = "";
                }
            }
        },
        validateNodeParentRelation: function (node) {
            if (!node || !node.parentId) {
                return true;
            }
            var parentNode = this.findNodeById(node.parentId);
            if (!parentNode) {
                node.parentId = "";
                return true;
            }
            if (parentNode.bpmnType !== "subProcess") {
                node.parentId = "";
                this.$message.warning("仅支持归属到子流程节点");
                return false;
            }
            if (!parentNode.expanded) {
                node.parentId = "";
                this.$message.warning("子流程展开后才可添加内部节点");
                return false;
            }
            if (parentNode.id === node.id || this.isNodeDescendantOf(parentNode.id, node.id)) {
                node.parentId = "";
                this.$message.warning("子流程归属不能形成循环层级");
                return false;
            }
            return true;
        },
        toggleSubProcess: function (nodeId) {
            var node = this.findNodeById(nodeId);
            if (!node || node.bpmnType !== "subProcess") {
                return;
            }
            node.expanded = !node.expanded;
            if (node.expanded) {
                this.expandSubProcessSize(node);
            } else {
                this.collapseSubProcessSize(node);
            }
            if (!node.expanded && this.selectedNode && this.isNodeDescendantOf(this.selectedNode.id, nodeId)) {
                this.selectedNodeId = nodeId;
            }
            if (!node.expanded && this.selectedEdge && !this.isEdgeVisible(this.selectedEdge)) {
                this.selectedEdgeId = "";
            }
            if (!node.expanded) {
                this.pendingSourceId = this.resolveNodeContainerId(this.pendingSourceId) === nodeId ? "" : this.pendingSourceId;
            }
        },
        buildFlowContainerMaps: function (exportableNodes, sequenceFlows) {
            var nodeChildrenMap = {};
            var flowChildrenMap = {};
            for (var index = 0; index < exportableNodes.length; index += 1) {
                var node = exportableNodes[index];
                var containerId = node.parentId || "";
                if (!nodeChildrenMap[containerId]) {
                    nodeChildrenMap[containerId] = [];
                }
                nodeChildrenMap[containerId].push(node);
            }
            for (var flowIndex = 0; flowIndex < sequenceFlows.length; flowIndex += 1) {
                var flow = sequenceFlows[flowIndex];
                var flowContainerId = flow.containerId || "";
                if (!flowChildrenMap[flowContainerId]) {
                    flowChildrenMap[flowContainerId] = [];
                }
                flowChildrenMap[flowContainerId].push(flow);
            }
            return {
                nodeChildrenMap: nodeChildrenMap,
                flowChildrenMap: flowChildrenMap
            };
        },
        appendContainerFlowElements: function (lines, containerId, nodeChildrenMap, flowChildrenMap, nodeTagMap, nodeIdMap, incomingMap, outgoingMap) {
            var childNodes = nodeChildrenMap[containerId || ""] || [];
            for (var nodeIndex = 0; nodeIndex < childNodes.length; nodeIndex += 1) {
                var node = childNodes[nodeIndex];
                var nodeTag = nodeTagMap[node.bpmnType] || "task";
                lines.push('    <bpmn:' + nodeTag + ' id="' + nodeIdMap[node.id] + '" name="' + this.escapeXml(node.name || node.label || node.code) + '">');
                if (node.documentation) {
                    lines.push('      <bpmn:documentation>' + this.escapeXml(node.documentation) + '</bpmn:documentation>');
                }
                var incomingFlows = incomingMap[node.id] || [];
                for (var incomingIndex = 0; incomingIndex < incomingFlows.length; incomingIndex += 1) {
                    lines.push('      <bpmn:incoming>' + incomingFlows[incomingIndex] + '</bpmn:incoming>');
                }
                var outgoingFlows = outgoingMap[node.id] || [];
                for (var outgoingIndex = 0; outgoingIndex < outgoingFlows.length; outgoingIndex += 1) {
                    lines.push('      <bpmn:outgoing>' + outgoingFlows[outgoingIndex] + '</bpmn:outgoing>');
                }
                if (node.bpmnType === "subProcess") {
                    this.appendContainerFlowElements(lines, node.id, nodeChildrenMap, flowChildrenMap, nodeTagMap, nodeIdMap, incomingMap, outgoingMap);
                }
                lines.push('    </bpmn:' + nodeTag + '>');
            }
            var containerFlows = flowChildrenMap[containerId || ""] || [];
            for (var flowIndex = 0; flowIndex < containerFlows.length; flowIndex += 1) {
                var flow = containerFlows[flowIndex];
                var namePart = flow.name ? ' name="' + this.escapeXml(flow.name) + '"' : "";
                lines.push('    <bpmn:sequenceFlow id="' + flow.id + '"' + namePart + ' sourceRef="' + nodeIdMap[flow.sourceId] + '" targetRef="' + nodeIdMap[flow.targetId] + '" />');
            }
        },
        buildBpmnXmlContent: function () {
            var collected = this.collectExportableNodes();
            var exportableNodes = collected.exportableNodes;
            var skippedNodes = collected.skippedNodes;
            var usedIds = {};
            var processId = this.sanitizeBpmnId("Wcdk_" + Date.now(), "Process", usedIds);
            var definitionsId = this.sanitizeBpmnId("Definitions_" + Date.now(), "Definitions", usedIds);
            var diagramId = this.sanitizeBpmnId(processId + "_Diagram", "Diagram", usedIds);
            var planeId = this.sanitizeBpmnId(processId + "_Plane", "Plane", usedIds);
            var nodeIdMap = {};
            var nodeTagMap = {
                startEvent: "startEvent",
                endEvent: "endEvent",
                boundaryEvent: "boundaryEvent",
                intermediateCatchEvent: "intermediateCatchEvent",
                intermediateThrowEvent: "intermediateThrowEvent",
                userTask: "userTask",
                scriptTask: "scriptTask",
                serviceTask: "serviceTask",
                mailTask: "task",
                manualTask: "manualTask",
                receiveTask: "receiveTask",
                businessRuleTask: "businessRuleTask",
                callActivity: "callActivity",
                subProcess: "subProcess",
                parallelGateway: "parallelGateway",
                exclusiveGateway: "exclusiveGateway",
                inclusiveGateway: "inclusiveGateway",
                eventGateway: "eventBasedGateway",
                textAnnotation: "textAnnotation"
            };
            var incomingMap = {};
            var outgoingMap = {};
            var exportableNodeLookup = this.buildExportNodeLookup(exportableNodes);
            this.sanitizeNodeParentRelations(exportableNodes, exportableNodeLookup);
            for (var nodeIndex = 0; nodeIndex < exportableNodes.length; nodeIndex += 1) {
                var exportNode = exportableNodes[nodeIndex];
                nodeIdMap[exportNode.id] = this.sanitizeBpmnId(exportNode.code || exportNode.id, "FlowNode", usedIds);
            }
            var sequenceFlows = [];
            for (var edgeIndex = 0; edgeIndex < this.visibleCanvasEdges.length; edgeIndex += 1) {
                var edge = this.visibleCanvasEdges[edgeIndex];
                if (!exportableNodeLookup[edge.sourceId] || !exportableNodeLookup[edge.targetId]) {
                    continue;
                }
                var sourceContainerId = this.resolveNodeContainerId(edge.sourceId);
                var targetContainerId = this.resolveNodeContainerId(edge.targetId);
                if (sourceContainerId !== targetContainerId) {
                    continue;
                }
                var flowId = this.sanitizeBpmnId("Flow_" + edge.sourceId + "_" + edge.targetId, "SequenceFlow", usedIds);
                sequenceFlows.push({
                    id: flowId,
                    sourceId: edge.sourceId,
                    targetId: edge.targetId,
                    name: edge.name || "",
                    containerId: sourceContainerId
                });
                if (!outgoingMap[edge.sourceId]) {
                    outgoingMap[edge.sourceId] = [];
                }
                if (!incomingMap[edge.targetId]) {
                    incomingMap[edge.targetId] = [];
                }
                outgoingMap[edge.sourceId].push(flowId);
                incomingMap[edge.targetId].push(flowId);
            }
            var lines = [
                '<?xml version="1.0" encoding="UTF-8"?>',
                '<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
                '                  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"',
                '                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"',
                '                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"',
                '                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"',
                '                  id="' + definitionsId + '"',
                '                  targetNamespace="http://flowable.org/processdef">',
                '  <bpmn:process id="' + processId + '" name="wcdk-process-' + processId + '" isExecutable="true">'
            ];
            var containerMaps = this.buildFlowContainerMaps(exportableNodes, sequenceFlows);
            this.appendContainerFlowElements(
                lines,
                "",
                containerMaps.nodeChildrenMap,
                containerMaps.flowChildrenMap,
                nodeTagMap,
                nodeIdMap,
                incomingMap,
                outgoingMap
            );
            lines.push('  </bpmn:process>');
            lines.push('  <bpmndi:BPMNDiagram id="' + diagramId + '">');
            lines.push('    <bpmndi:BPMNPlane id="' + planeId + '" bpmnElement="' + processId + '">');
            for (var shapeIndex = 0; shapeIndex < exportableNodes.length; shapeIndex += 1) {
                var shapeNode = exportableNodes[shapeIndex];
                lines.push('      <bpmndi:BPMNShape id="' + nodeIdMap[shapeNode.id] + '_di" bpmnElement="' + nodeIdMap[shapeNode.id] + '">');
                lines.push('        <dc:Bounds x="' + shapeNode.x + '" y="' + shapeNode.y + '" width="' + shapeNode.width + '" height="' + shapeNode.height + '" />');
                lines.push('      </bpmndi:BPMNShape>');
            }
            for (var edgeDiIndex = 0; edgeDiIndex < sequenceFlows.length; edgeDiIndex += 1) {
                var sequenceFlow = sequenceFlows[edgeDiIndex];
                var originalEdge = null;
                for (var searchEdgeIndex = 0; searchEdgeIndex < this.canvasEdges.length; searchEdgeIndex += 1) {
                    var currentEdge = this.canvasEdges[searchEdgeIndex];
                    if (currentEdge.sourceId === sequenceFlow.sourceId && currentEdge.targetId === sequenceFlow.targetId) {
                        originalEdge = currentEdge;
                        break;
                    }
                }
                var sourceNode = this.findNodeById(sequenceFlow.sourceId);
                var targetNode = this.findNodeById(sequenceFlow.targetId);
                if (!sourceNode || !targetNode) {
                    continue;
                }
                var start = this.resolveNodeCenter(sourceNode, true);
                var end = this.resolveNodeCenter(targetNode, false);
                var turnOffset = Math.max(36, Math.min(84, Math.abs(end.x - start.x) / 2));
                var midX = start.x + turnOffset;
                var endX = end.x - turnOffset;
                if (end.x <= start.x + 24) {
                    midX = start.x + 40;
                    endX = end.x - 40;
                }
                lines.push('      <bpmndi:BPMNEdge id="' + sequenceFlow.id + '_di" bpmnElement="' + sequenceFlow.id + '">');
                lines.push('        <di:waypoint x="' + start.x + '" y="' + start.y + '" />');
                lines.push('        <di:waypoint x="' + midX + '" y="' + start.y + '" />');
                lines.push('        <di:waypoint x="' + endX + '" y="' + end.y + '" />');
                lines.push('        <di:waypoint x="' + end.x + '" y="' + end.y + '" />');
                if (originalEdge && originalEdge.name) {
                    lines.push('        <bpmndi:BPMNLabel>');
                    lines.push('          <dc:Bounds x="' + Math.round((start.x + end.x) / 2 - 40) + '" y="' + Math.round((start.y + end.y) / 2 - 24) + '" width="80" height="20" />');
                    lines.push('        </bpmndi:BPMNLabel>');
                }
                lines.push('      </bpmndi:BPMNEdge>');
            }
            lines.push('    </bpmndi:BPMNPlane>');
            lines.push('  </bpmndi:BPMNDiagram>');
            lines.push('</bpmn:definitions>');
            return {
                xml: lines.join("\n"),
                skippedNodes: skippedNodes
            };
        },
        notifySkippedBpmnNodes: function (skippedNodes) {
            if (!skippedNodes.length) {
                return;
            }
            var typeLabels = [];
            var labelMap = {};
            for (var index = 0; index < skippedNodes.length; index += 1) {
                labelMap[skippedNodes[index].label] = true;
            }
            for (var label in labelMap) {
                if (Object.prototype.hasOwnProperty.call(labelMap, label)) {
                    typeLabels.push(label);
                }
            }
            this.$message.warning("以下节点暂未写入 BPMN 语义导出：" + typeLabels.join("、"));
        },
        handleExportBpmn: function () {
            if (!this.canvasNodes.length) {
                this.$message.warning("请先在画布中添加流程节点后再导出");
                return;
            }
            var self = this;
            this.requestProcessDesignerExport("bpmn").then(function (exportData) {
                if (!exportData) {
                    throw new Error("BPMN 导出失败");
                }
                self.downloadBlobFile(
                    exportData.fileName || "wcdk-process.bpmn",
                    self.decodeBase64ToBlob(exportData.contentBase64, exportData.contentType)
                );
                self.notifySkippedBpmnNodes(exportData.skippedNodeLabels || []);
                self.$message.success("BPMN 文件已开始下载");
            }).catch(function (error) {
                self.$message.error(error && error.message ? error.message : "BPMN 导出失败");
            });
        },
        handleExportBpmnXml: function () {
            if (!this.canvasNodes.length) {
                this.$message.warning("请先在画布中添加流程节点后再导出");
                return;
            }
            var self = this;
            this.requestProcessDesignerExport("bpmn20.xml").then(function (exportData) {
                if (!exportData) {
                    throw new Error("BPMN.XML 导出失败");
                }
                self.downloadBlobFile(
                    exportData.fileName || "wcdk-process.bpmn20.xml",
                    self.decodeBase64ToBlob(exportData.contentBase64, exportData.contentType)
                );
                self.notifySkippedBpmnNodes(exportData.skippedNodeLabels || []);
                self.$message.success("BPMN.XML 文件已开始下载");
            }).catch(function (error) {
                self.$message.error(error && error.message ? error.message : "BPMN.XML 导出失败");
            });
        },
        resolveExportBounds: function () {
            var bounds = this.resolveCanvasBounds();
            return {
                left: bounds.left,
                top: bounds.top,
                width: Math.max(bounds.right - bounds.left, 320),
                height: Math.max(bounds.bottom - bounds.top, 220)
            };
        },
        resolveSvgNodeMarkup: function (node) {
            var centerX = node.x + node.width / 2;
            var centerY = node.y + node.height / 2;
            var titleY = centerY + 8;
            var subtitleY = titleY + 20;
            var badgeWidth = Math.min(Math.max(node.bpmnType.length * 8 + 24, 72), Math.max(node.width - 16, 72));
            var badgeX = centerX - badgeWidth / 2;
            var badgeY = node.y - 14;
            var boxMarkup = "";
            var iconMarkup = "";
            if (node.kind === "event") {
                var radius = Math.min(node.width, node.height) / 2;
                boxMarkup = '<circle cx="' + centerX + '" cy="' + centerY + '" r="' + radius + '" fill="url(#eventGradient)" stroke="#cfe0f6" stroke-width="2"></circle>';
                iconMarkup = '<circle cx="' + centerX + '" cy="' + (centerY - 14) + '" r="18" fill="rgba(255,255,255,0.92)" stroke="rgba(16,163,127,0.42)" stroke-width="2"></circle>';
            } else if (node.kind === "gateway") {
                var halfWidth = node.width / 2;
                var halfHeight = node.height / 2;
                boxMarkup = '<polygon points="' + centerX + ',' + node.y + ' ' + (node.x + node.width) + ',' + centerY + ' ' + centerX + ',' + (node.y + node.height) + ' ' + node.x + ',' + centerY + '" fill="url(#gatewayGradient)" stroke="#cfe0f6" stroke-width="2"></polygon>';
                iconMarkup = '<polygon points="' + centerX + ',' + (centerY - 22) + ' ' + (centerX + 18) + ',' + (centerY - 4) + ' ' + centerX + ',' + (centerY + 14) + ' ' + (centerX - 18) + ',' + (centerY - 4) + '" fill="rgba(255,255,255,0.92)" stroke="rgba(245,158,11,0.38)" stroke-width="2"></polygon>';
            } else {
                var fillId = node.kind === "container" ? "containerGradient" : node.kind === "artifact" ? "artifactGradient" : "taskGradient";
                var strokeDasharray = node.kind === "container" || node.kind === "artifact" ? ' stroke-dasharray="8 4"' : "";
                boxMarkup = '<rect x="' + node.x + '" y="' + node.y + '" width="' + node.width + '" height="' + node.height + '" rx="20" ry="20" fill="url(#' + fillId + ')" stroke="#cfe0f6" stroke-width="2"' + strokeDasharray + '></rect>';
                var iconStroke = node.kind === "container" ? "rgba(99,102,241,0.34)" : node.kind === "artifact" ? "rgba(217,119,6,0.34)" : "rgba(52,119,246,0.28)";
                var iconDasharray = node.kind === "container" || node.kind === "artifact" ? ' stroke-dasharray="6 4"' : "";
                iconMarkup = '<rect x="' + (centerX - 26) + '" y="' + (centerY - 30) + '" width="52" height="32" rx="' + (node.kind === "task" ? 12 : 10) + '" ry="' + (node.kind === "task" ? 12 : 10) + '" fill="rgba(255,255,255,0.92)" stroke="' + iconStroke + '" stroke-width="2"' + iconDasharray + '></rect>';
            }
            return [
                '<g>',
                boxMarkup,
                '<rect x="' + badgeX + '" y="' + badgeY + '" width="' + badgeWidth + '" height="24" rx="12" ry="12" fill="#e9f1ff"></rect>',
                '<text x="' + centerX + '" y="' + (badgeY + 16) + '" font-size="11" font-weight="700" text-anchor="middle" fill="#2a61bf">' + this.escapeXml(node.bpmnType) + '</text>',
                iconMarkup,
                '<text x="' + centerX + '" y="' + titleY + '" font-size="13" font-weight="700" text-anchor="middle" fill="#1d2d46">' + this.escapeXml(node.name || node.label) + '</text>',
                '<text x="' + centerX + '" y="' + subtitleY + '" font-size="12" text-anchor="middle" fill="#6d809d">' + this.escapeXml(node.label) + '</text>',
                '</g>'
            ].join("");
        },
        buildSvgMarkup: function () {
            var bounds = this.resolveExportBounds();
            var svgParts = [
                '<svg xmlns="http://www.w3.org/2000/svg" width="' + bounds.width + '" height="' + bounds.height + '" viewBox="' + bounds.left + " " + bounds.top + " " + bounds.width + " " + bounds.height + '">',
                '<defs>',
                '<linearGradient id="stageBackground" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#ffffff"></stop><stop offset="100%" stop-color="#f6faff"></stop></linearGradient>',
                '<linearGradient id="eventGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#effcf5"></stop><stop offset="100%" stop-color="#dcfce7"></stop></linearGradient>',
                '<linearGradient id="taskGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#f8fbff"></stop><stop offset="100%" stop-color="#e7f0ff"></stop></linearGradient>',
                '<linearGradient id="gatewayGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#fff8ec"></stop><stop offset="100%" stop-color="#ffedd5"></stop></linearGradient>',
                '<linearGradient id="containerGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#f5f7ff"></stop><stop offset="100%" stop-color="#e9edff"></stop></linearGradient>',
                '<linearGradient id="artifactGradient" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#fffdf5"></stop><stop offset="100%" stop-color="#fff8d9"></stop></linearGradient>',
                '<marker id="designer-arrowhead-export" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto"><path d="M0,0 L10,4 L0,8 z" fill="#7a93b8"></path></marker>',
                '</defs>',
                '<rect x="' + bounds.left + '" y="' + bounds.top + '" width="' + bounds.width + '" height="' + bounds.height + '" fill="url(#stageBackground)"></rect>'
            ];
            for (var edgeIndex = 0; edgeIndex < this.canvasEdges.length; edgeIndex += 1) {
                var edge = this.canvasEdges[edgeIndex];
                var path = this.resolveEdgePath(edge);
                if (!path) {
                    continue;
                }
                svgParts.push('<path d="' + path + '" fill="none" stroke="#7a93b8" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" marker-end="url(#designer-arrowhead-export)"></path>');
                if (edge.name) {
                    var sourceNode = this.findNodeById(edge.sourceId);
                    var targetNode = this.findNodeById(edge.targetId);
                    if (sourceNode && targetNode) {
                        var sourceCenter = this.resolveNodeCenter(sourceNode, true);
                        var targetCenter = this.resolveNodeCenter(targetNode, false);
                        svgParts.push('<text x="' + Math.round((sourceCenter.x + targetCenter.x) / 2) + '" y="' + Math.round((sourceCenter.y + targetCenter.y) / 2 - 10) + '" font-size="12" text-anchor="middle" fill="#556b8b">' + this.escapeXml(edge.name) + '</text>');
                    }
                }
            }
            for (var nodeIndex = 0; nodeIndex < this.visibleCanvasNodes.length; nodeIndex += 1) {
                svgParts.push(this.resolveSvgNodeMarkup(this.visibleCanvasNodes[nodeIndex]));
            }
            svgParts.push("</svg>");
            return svgParts.join("");
        },
        renderPngBlob: function () {
            var bounds = this.resolveExportBounds();
            var svgMarkup = this.buildSvgMarkup();
            return new Promise(function (resolve, reject) {
                var canvas = document.createElement("canvas");
                canvas.width = Math.max(Math.ceil(bounds.width), 1);
                canvas.height = Math.max(Math.ceil(bounds.height), 1);
                var context = canvas.getContext("2d");
                var image = new Image();
                image.onload = function () {
                    context.fillStyle = "#ffffff";
                    context.fillRect(0, 0, canvas.width, canvas.height);
                    context.drawImage(image, 0, 0);
                    if (canvas.toBlob) {
                        canvas.toBlob(function (blob) {
                            if (!blob) {
                                reject(new Error("PNG 数据生成失败"));
                                return;
                            }
                            resolve(blob);
                        }, "image/png");
                        return;
                    }
                    var dataUrl = canvas.toDataURL("image/png");
                    var base64 = dataUrl.split(",")[1] || "";
                    var binary = atob(base64);
                    var bytes = new Uint8Array(binary.length);
                    for (var index = 0; index < binary.length; index += 1) {
                        bytes[index] = binary.charCodeAt(index);
                    }
                    resolve(new Blob([bytes], {type: "image/png"}));
                };
                image.onerror = function () {
                    reject(new Error("SVG 渲染失败"));
                };
                image.src = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svgMarkup);
            });
        },
        handleExportPng: function () {
            var self = this;
            if (!this.canvasNodes.length) {
                this.$message.warning("请先在画布中添加流程节点后再导出");
                return;
            }
            this.requestProcessDesignerExport("png").then(function (exportData) {
                if (!exportData) {
                    throw new Error("PNG 导出失败");
                }
                self.downloadBlobFile(
                    exportData.fileName || "wcdk-process.png",
                    self.decodeBase64ToBlob(exportData.contentBase64, exportData.contentType)
                );
                self.$message.success("PNG 文件已开始下载");
            }).catch(function (error) {
                self.$message.error(error && error.message ? error.message : "PNG 导出失败");
            });
        },
        handleRefresh: function () {
            this.ensureExpandedGroups();
            this.$nextTick(this.handleCenterCanvas);
            this.$message.success("流程设计画布已刷新");
        },
        toggleLeftPanel: function () {
            this.leftPanelCollapsed = !this.leftPanelCollapsed;
        },
        toggleRightPanel: function () {
            this.rightPanelCollapsed = !this.rightPanelCollapsed;
        },
        toggleGroup: function (category) {
            this.$set(this.expandedGroups, category, !this.isGroupExpanded(category));
        },
        isGroupExpanded: function (category) {
            return !!this.expandedGroups[category];
        },
        ensureExpandedGroups: function () {
            for (var index = 0; index < this.nodeGroups.length; index += 1) {
                var category = this.nodeGroups[index].category;
                if (typeof this.expandedGroups[category] === "undefined") {
                    this.$set(this.expandedGroups, category, false);
                }
            }
        },
        handleResetCanvas: function () {
            this.canvasNodes = [];
            this.canvasEdges = [];
            this.selectedNodeId = "";
            this.selectedEdgeId = "";
            this.pendingSourceId = "";
            this.nextNodeIndex = 1;
            this.canvasScale = 1;
            this.$nextTick(this.handleCenterCanvas);
            this.$message.success("流程设计画布已清空");
        },
        handlePaletteDragStart: function (node) {
            this.dragPaletteNode = node;
        },
        handlePaletteDragEnd: function () {
            this.dragPaletteNode = null;
        },
        handleCanvasDrop: function (event) {
            if (!this.dragPaletteNode) {
                return;
            }
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper) {
                return;
            }
            var rect = wrapper.getBoundingClientRect();
            var rawX = (event.clientX - rect.left + wrapper.scrollLeft) / this.canvasScale;
            var rawY = (event.clientY - rect.top + wrapper.scrollTop) / this.canvasScale;
            var node = this.createCanvasNode(this.dragPaletteNode, rawX, rawY);
            node.parentId = this.resolveDropParentSubProcessId(rawX, rawY);
            this.keepNodeInsideParent(node);
            this.canvasNodes.push(node);
            this.selectedNodeId = node.id;
            this.selectedEdgeId = "";
            this.dragPaletteNode = null;
        },
        createCanvasNode: function (paletteNode, rawX, rawY) {
            var width = paletteNode.width;
            var height = paletteNode.height;
            var nodeIndex = this.nextNodeIndex;
            this.nextNodeIndex += 1;
            return {
                id: "designer-node-" + nodeIndex,
                type: paletteNode.type,
                bpmnType: paletteNode.bpmnType,
                kind: paletteNode.kind,
                allowIncoming: this.isNodeIncomingAllowed(paletteNode.bpmnType),
                allowOutgoing: this.isNodeOutgoingAllowed(paletteNode.bpmnType),
                allowSequenceFlow: this.isSequenceFlowNode(paletteNode.bpmnType),
                label: paletteNode.label,
                shortLabel: paletteNode.shortLabel,
                name: paletteNode.label + nodeIndex,
                code: this.buildFlowableCode(paletteNode.bpmnType, nodeIndex),
                documentation: "",
                parentId: "",
                expanded: false,
                collapsedWidth: paletteNode.bpmnType === "subProcess" ? width : 0,
                collapsedHeight: paletteNode.bpmnType === "subProcess" ? height : 0,
                expandedWidth: paletteNode.bpmnType === "subProcess" ? Math.max(width + 120, 320) : 0,
                expandedHeight: paletteNode.bpmnType === "subProcess" ? Math.max(height + 100, 220) : 0,
                width: width,
                height: height,
                x: this.normalizeCanvasX(rawX - width / 2, width),
                y: this.normalizeCanvasY(rawY - height / 2, height)
            };
        },
        buildFlowableCode: function (bpmnType, nodeIndex) {
            var typeMap = {
                startEvent: "StartEvent",
                endEvent: "EndEvent",
                boundaryEvent: "BoundaryEvent",
                intermediateCatchEvent: "IntermediateCatchEvent",
                intermediateThrowEvent: "IntermediateThrowEvent",
                userTask: "UserTask",
                scriptTask: "ScriptTask",
                serviceTask: "ServiceTask",
                mailTask: "MailTask",
                manualTask: "ManualTask",
                receiveTask: "ReceiveTask",
                businessRuleTask: "BusinessRuleTask",
                callActivity: "CallActivity",
                subProcess: "SubProcess",
                parallelGateway: "ParallelGateway",
                exclusiveGateway: "ExclusiveGateway",
                inclusiveGateway: "InclusiveGateway",
                eventGateway: "EventGateway",
                pool: "Pool",
                lane: "Lane",
                textAnnotation: "TextAnnotation"
            };
            return (typeMap[bpmnType] || "FlowNode") + "_" + nodeIndex;
        },
        isSequenceFlowNode: function (bpmnType) {
            return [
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
                "eventGateway"
            ].indexOf(bpmnType) >= 0;
        },
        isNodeIncomingAllowed: function (bpmnType) {
            return bpmnType !== "startEvent" && this.isSequenceFlowNode(bpmnType);
        },
        isNodeOutgoingAllowed: function (bpmnType) {
            return bpmnType !== "endEvent" && this.isSequenceFlowNode(bpmnType);
        },
        resolveNodeStyle: function (node) {
            return {
                left: node.x + "px",
                top: node.y + "px",
                width: node.width + "px",
                height: node.height + "px"
            };
        },
        shouldShowConnectHandle: function (node) {
            return !!node
                && node.allowOutgoing
                && this.isNodeVisible(node)
                && (this.hoverNodeId === node.id || this.connectDragState.sourceId === node.id);
        },
        handleNodeSelect: function (nodeId) {
            this.selectedNodeId = nodeId;
            this.selectedEdgeId = "";
        },
        handleNodeMouseEnter: function (nodeId) {
            this.hoverNodeId = nodeId;
            if (this.connectDragState.active) {
                this.connectDragState.targetNodeId = nodeId;
            }
        },
        handleNodeMouseLeave: function (nodeId) {
            if (this.hoverNodeId === nodeId) {
                this.hoverNodeId = "";
            }
            if (this.connectDragState.active && this.connectDragState.targetNodeId === nodeId) {
                this.connectDragState.targetNodeId = "";
            }
        },
        handleConnectHandleMouseDown: function (node, event) {
            if (!event || event.button !== 0) {
                return;
            }
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper || !node || !node.allowOutgoing) {
                return;
            }
            var pointer = this.resolveCanvasPointer(event);
            this.connectDragState.active = true;
            this.connectDragState.sourceId = node.id;
            this.connectDragState.currentX = pointer.x;
            this.connectDragState.currentY = pointer.y;
            this.connectDragState.targetNodeId = "";
            this.pendingSourceId = node.id;
            this.selectedNodeId = node.id;
            this.selectedEdgeId = "";
        },
        createConnection: function (sourceId, targetId) {
            var sourceNode = this.findNodeById(sourceId);
            var targetNode = this.findNodeById(targetId);
            if (!targetNode || !targetNode.allowSequenceFlow) {
                this.pendingSourceId = "";
                this.$message.warning("当前节点类型不支持顺序流连线");
                return false;
            }
            if (!sourceNode) {
                this.pendingSourceId = "";
                return false;
            }
            if (sourceId === targetId) {
                this.$message.warning("起点和终点不能是同一个节点");
                return false;
            }
            if (!sourceNode.allowOutgoing) {
                this.pendingSourceId = "";
                this.$message.warning("当前起点节点不能发出顺序流");
                return false;
            }
            if (!targetNode.allowIncoming) {
                this.pendingSourceId = "";
                this.$message.warning("当前终点节点不能接收顺序流");
                return false;
            }
            if (this.resolveNodeContainerId(sourceId) !== this.resolveNodeContainerId(targetId)) {
                this.pendingSourceId = "";
                this.$message.warning("顺序流不能直接跨子流程边界连接");
                return false;
            }
            if (this.hasEdge(sourceId, targetId)) {
                this.$message.warning("该连线已存在");
                this.pendingSourceId = "";
                return false;
            }
            var edge = this.createCanvasEdge(sourceId, targetId);
            this.canvasEdges.push(edge);
            this.selectedEdgeId = edge.id;
            this.selectedNodeId = "";
            this.pendingSourceId = "";
            this.$message.success("连线已创建");
            return true;
        },
        handleDeleteSelectedNode: function () {
            if (!this.selectedNodeId) {
                return;
            }
            var selectedNode = this.findNodeById(this.selectedNodeId);
            var fallbackParentId = selectedNode && selectedNode.parentId ? selectedNode.parentId : "";
            for (var updateIndex = 0; updateIndex < this.canvasNodes.length; updateIndex += 1) {
                if (this.canvasNodes[updateIndex].parentId === this.selectedNodeId) {
                    this.canvasNodes[updateIndex].parentId = fallbackParentId;
                }
            }
            var nextNodes = [];
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                if (this.canvasNodes[index].id !== this.selectedNodeId) {
                    nextNodes.push(this.canvasNodes[index]);
                }
            }
            this.canvasNodes = nextNodes;
            this.removeEdgesByNodeId(this.selectedNodeId);
            this.selectedNodeId = "";
            this.$message.success("节点已删除");
        },
        handleEdgeSelect: function (edgeId) {
            this.selectedEdgeId = edgeId;
            this.selectedNodeId = "";
            this.pendingSourceId = "";
        },
        handleDeleteSelectedEdge: function () {
            if (!this.selectedEdgeId) {
                return;
            }
            var nextEdges = [];
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                if (this.canvasEdges[index].id !== this.selectedEdgeId) {
                    nextEdges.push(this.canvasEdges[index]);
                }
            }
            this.canvasEdges = nextEdges;
            this.selectedEdgeId = "";
            this.$message.success("连线已删除");
        },
        handleCanvasClick: function () {
            if (this.connectDragState.active || this.panCanvasState.active) {
                return;
            }
            this.pendingSourceId = "";
            this.selectedNodeId = "";
            this.selectedEdgeId = "";
        },
        handleCanvasWrapperMouseDown: function (event) {
            if (!event || event.button !== 2) {
                return;
            }
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper) {
                return;
            }
            event.preventDefault();
            this.panCanvasState.active = true;
            this.panCanvasState.startClientX = event.clientX;
            this.panCanvasState.startClientY = event.clientY;
            this.panCanvasState.startScrollLeft = wrapper.scrollLeft;
            this.panCanvasState.startScrollTop = wrapper.scrollTop;
        },
        handleDocumentKeyDown: function (event) {
            if (!event || event.key !== "Delete") {
                return;
            }
            var target = event.target;
            var tagName = target && target.tagName ? String(target.tagName).toUpperCase() : "";
            var isEditable = !!(target && (target.isContentEditable || tagName === "INPUT" || tagName === "TEXTAREA"));
            if (isEditable) {
                return;
            }
            if (this.selectedNodeId) {
                event.preventDefault();
                this.handleDeleteSelectedNode();
                return;
            }
            if (this.selectedEdgeId) {
                event.preventDefault();
                this.handleDeleteSelectedEdge();
            }
        },
        resolveCanvasPointer: function (event) {
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper) {
                return {x: 0, y: 0};
            }
            var rect = wrapper.getBoundingClientRect();
            return {
                x: (event.clientX - rect.left + wrapper.scrollLeft) / this.canvasScale,
                y: (event.clientY - rect.top + wrapper.scrollTop) / this.canvasScale
            };
        },
        handleNodeMouseDown: function (node, event) {
            if (event && event.button === 2) {
                this.handleCanvasWrapperMouseDown(event);
                return;
            }
            if (!event || event.button !== 0) {
                return;
            }
            if (this.connectDragState.active || this.resizeNodeState.active) {
                return;
            }
            if (!this.isNodeVisible(node)) {
                return;
            }
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper) {
                return;
            }
            this.selectedNodeId = node.id;
            this.dragNodeState.active = true;
            this.dragNodeState.nodeId = node.id;
            this.dragNodeState.offsetX = (event.clientX - wrapper.getBoundingClientRect().left + wrapper.scrollLeft) / this.canvasScale - node.x;
            this.dragNodeState.offsetY = (event.clientY - wrapper.getBoundingClientRect().top + wrapper.scrollTop) / this.canvasScale - node.y;
            this.dragNodeState.startX = node.x;
            this.dragNodeState.startY = node.y;
            this.dragNodeState.childOffsets = node.bpmnType === "subProcess" ? this.buildChildOffsetSnapshot(node) : [];
        },
        handleResizeMouseDown: function (node, event) {
            if (!event || event.button !== 0) {
                return;
            }
            if (!node || !this.isNodeVisible(node)) {
                return;
            }
            var pointer = this.resolveCanvasPointer(event);
            this.selectedNodeId = node.id;
            this.resizeNodeState.active = true;
            this.resizeNodeState.nodeId = node.id;
            this.resizeNodeState.startPointerX = pointer.x;
            this.resizeNodeState.startPointerY = pointer.y;
            this.resizeNodeState.startWidth = node.width;
            this.resizeNodeState.startHeight = node.height;
        },
        handleDocumentMouseMove: function (event) {
            if (this.panCanvasState.active) {
                var panWrapper = this.$refs.canvasWrapper;
                if (!panWrapper) {
                    return;
                }
                panWrapper.scrollLeft = Math.max(this.panCanvasState.startScrollLeft - (event.clientX - this.panCanvasState.startClientX), 0);
                panWrapper.scrollTop = Math.max(this.panCanvasState.startScrollTop - (event.clientY - this.panCanvasState.startClientY), 0);
                return;
            }
            if (this.connectDragState.active) {
                var pointer = this.resolveCanvasPointer(event);
                this.connectDragState.currentX = pointer.x;
                this.connectDragState.currentY = pointer.y;
                var hoveredNodeId = this.resolveNodeIdFromEventTarget(event);
                this.connectDragState.targetNodeId = hoveredNodeId;
                if (hoveredNodeId) {
                    this.hoverNodeId = hoveredNodeId;
                }
                return;
            }
            if (this.resizeNodeState.active) {
                var resizeNode = this.findNodeById(this.resizeNodeState.nodeId);
                if (!resizeNode) {
                    return;
                }
                var resizePointer = this.resolveCanvasPointer(event);
                var nextWidth = this.resizeNodeState.startWidth + (resizePointer.x - this.resizeNodeState.startPointerX);
                var nextHeight = this.resizeNodeState.startHeight + (resizePointer.y - this.resizeNodeState.startPointerY);
                resizeNode.width = Math.max(this.getNodeMinimumWidth(resizeNode), Math.round(nextWidth));
                resizeNode.height = Math.max(this.getNodeMinimumHeight(resizeNode), Math.round(nextHeight));
                if (resizeNode.bpmnType === "subProcess") {
                    if (resizeNode.expanded) {
                        resizeNode.expandedWidth = resizeNode.width;
                        resizeNode.expandedHeight = resizeNode.height;
                    } else {
                        resizeNode.collapsedWidth = resizeNode.width;
                        resizeNode.collapsedHeight = resizeNode.height;
                    }
                }
                this.keepNodeInsideParent(resizeNode);
                return;
            }
            if (!this.dragNodeState.active) {
                return;
            }
            var wrapper = this.$refs.canvasWrapper;
            var node = this.selectedNode;
            if (!wrapper || !node) {
                return;
            }
            var rect = wrapper.getBoundingClientRect();
            var nextX = (event.clientX - rect.left + wrapper.scrollLeft) / this.canvasScale - this.dragNodeState.offsetX;
            var nextY = (event.clientY - rect.top + wrapper.scrollTop) / this.canvasScale - this.dragNodeState.offsetY;
            node.x = this.normalizeCanvasX(nextX, node.width);
            node.y = this.normalizeCanvasY(nextY, node.height);
            this.keepNodeInsideParent(node);
            if (node.bpmnType === "subProcess") {
                this.moveChildNodesWithParent(node);
            }
        },
        resolveNodeIdFromEventTarget: function (event) {
            if (!event || typeof document.elementFromPoint !== "function") {
                return "";
            }
            var element = document.elementFromPoint(event.clientX, event.clientY);
            if (!element || !element.closest) {
                return "";
            }
            var nodeElement = element.closest("[data-node-id]");
            return nodeElement ? nodeElement.getAttribute("data-node-id") || "" : "";
        },
        handleDocumentMouseUp: function () {
            if (this.panCanvasState.active) {
                this.panCanvasState.active = false;
                return;
            }
            if (this.connectDragState.active) {
                var sourceId = this.connectDragState.sourceId;
                var targetId = this.connectDragState.targetNodeId;
                this.connectDragState.active = false;
                this.connectDragState.sourceId = "";
                this.connectDragState.targetNodeId = "";
                if (sourceId && targetId) {
                    this.createConnection(sourceId, targetId);
                } else {
                    this.pendingSourceId = "";
                }
                return;
            }
            if (this.resizeNodeState.active) {
                this.resizeNodeState.active = false;
                this.resizeNodeState.nodeId = "";
                return;
            }
            this.dragNodeState.active = false;
            this.dragNodeState.nodeId = "";
            this.dragNodeState.childOffsets = [];
        },
        createCanvasEdge: function (sourceId, targetId) {
            return {
                id: "designer-edge-" + sourceId + "-" + targetId,
                sourceId: sourceId,
                targetId: targetId,
                name: ""
            };
        },
        resolveEdgeDisplayName: function (edge) {
            if (!edge) {
                return "-";
            }
            if (edge.name) {
                return edge.name;
            }
            return this.resolveNodeName(edge.sourceId) + " 到 " + this.resolveNodeName(edge.targetId);
        },
        hasEdge: function (sourceId, targetId) {
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                if (this.canvasEdges[index].sourceId === sourceId && this.canvasEdges[index].targetId === targetId) {
                    return true;
                }
            }
            return false;
        },
        removeEdgesByNodeId: function (nodeId) {
            var nextEdges = [];
            var shouldClearSelectedEdge = false;
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                var edge = this.canvasEdges[index];
                if (this.selectedEdgeId === edge.id && (edge.sourceId === nodeId || edge.targetId === nodeId)) {
                    shouldClearSelectedEdge = true;
                }
                if (edge.sourceId !== nodeId && edge.targetId !== nodeId) {
                    nextEdges.push(edge);
                }
            }
            this.canvasEdges = nextEdges;
            if (shouldClearSelectedEdge) {
                this.selectedEdgeId = "";
            }
        },
        removeIncompatibleEdgesForNode: function (nodeId) {
            if (!nodeId) {
                return;
            }
            var currentContainerId = this.resolveNodeContainerId(nodeId);
            var nextEdges = [];
            var removedCount = 0;
            var shouldClearSelectedEdge = false;
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                var edge = this.canvasEdges[index];
                if (edge.sourceId !== nodeId && edge.targetId !== nodeId) {
                    nextEdges.push(edge);
                    continue;
                }
                var otherNodeId = edge.sourceId === nodeId ? edge.targetId : edge.sourceId;
                if (this.resolveNodeContainerId(otherNodeId) === currentContainerId) {
                    nextEdges.push(edge);
                    continue;
                }
                removedCount += 1;
                if (this.selectedEdgeId === edge.id) {
                    shouldClearSelectedEdge = true;
                }
            }
            if (removedCount > 0) {
                this.canvasEdges = nextEdges;
                if (shouldClearSelectedEdge) {
                    this.selectedEdgeId = "";
                }
                this.$message.warning("节点归属变更后，已自动移除跨子流程边界的连线");
            }
        },
        findNodeById: function (nodeId) {
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                if (this.canvasNodes[index].id === nodeId) {
                    return this.canvasNodes[index];
                }
            }
            return null;
        },
        resolveNodeName: function (nodeId) {
            var node = this.findNodeById(nodeId);
            return node ? node.name : "-";
        },
        resolveNodeCenter: function (node, isSource) {
            if (!node) {
                return {x: 0, y: 0};
            }
            return {
                x: isSource ? node.x + node.width : node.x,
                y: node.y + node.height / 2
            };
        },
        buildEdgePathByPoints: function (start, end) {
            var turnOffset = Math.max(36, Math.min(84, Math.abs(end.x - start.x) / 2));
            var midX = start.x + turnOffset;
            var endX = end.x - turnOffset;
            if (end.x <= start.x + 24) {
                midX = start.x + 40;
                endX = end.x - 40;
            }
            return [
                "M", start.x, start.y,
                "L", midX, start.y,
                "L", endX, end.y,
                "L", end.x, end.y
            ].join(" ");
        },
        resolvePreviewEdgePath: function (sourceId, currentX, currentY) {
            var sourceNode = this.findNodeById(sourceId);
            if (!sourceNode) {
                return "";
            }
            var start = this.resolveNodeCenter(sourceNode, true);
            var targetNode = this.connectDragState.targetNodeId ? this.findNodeById(this.connectDragState.targetNodeId) : null;
            var end = targetNode ? this.resolveNodeCenter(targetNode, false) : {x: currentX, y: currentY};
            return this.buildEdgePathByPoints(start, end);
        },
        resolveEdgePath: function (edge) {
            var sourceNode = this.findNodeById(edge.sourceId);
            var targetNode = this.findNodeById(edge.targetId);
            if (!sourceNode || !targetNode) {
                return "";
            }
            var start = this.resolveNodeCenter(sourceNode, true);
            var end = this.resolveNodeCenter(targetNode, false);
            return this.buildEdgePathByPoints(start, end);
        },
        normalizeCanvasX: function (value, width) {
            return Math.max(0, Math.min(Math.round(value), Math.max(this.canvasWidth - width, 0)));
        },
        normalizeCanvasY: function (value, height) {
            return Math.max(0, Math.min(Math.round(value), Math.max(this.canvasHeight - height, 0)));
        },
        setCanvasScale: function (nextScale) {
            var wrapper = this.$refs.canvasWrapper;
            var safeScale = Math.max(0.5, Math.min(1.6, Number(nextScale || 1)));
            if (!wrapper) {
                this.canvasScale = safeScale;
                return;
            }
            var centerX = (wrapper.scrollLeft + wrapper.clientWidth / 2) / this.canvasScale;
            var centerY = (wrapper.scrollTop + wrapper.clientHeight / 2) / this.canvasScale;
            this.canvasScale = safeScale;
            this.$nextTick(function () {
                wrapper.scrollLeft = Math.max(centerX * safeScale - wrapper.clientWidth / 2, 0);
                wrapper.scrollTop = Math.max(centerY * safeScale - wrapper.clientHeight / 2, 0);
            });
        },
        handleZoomIn: function () {
            this.setCanvasScale(this.canvasScale + 0.1);
        },
        handleZoomOut: function () {
            this.setCanvasScale(this.canvasScale - 0.1);
        },
        handleResetZoom: function () {
            this.setCanvasScale(1);
        },
        resolveCanvasBounds: function () {
            if (!this.visibleCanvasNodes.length) {
                return {
                    left: 0,
                    top: 0,
                    right: this.canvasWidth,
                    bottom: this.canvasHeight
                };
            }
            var left = this.canvasWidth;
            var top = this.canvasHeight;
            var right = 0;
            var bottom = 0;
            for (var index = 0; index < this.visibleCanvasNodes.length; index += 1) {
                var node = this.visibleCanvasNodes[index];
                left = Math.min(left, node.x);
                top = Math.min(top, node.y);
                right = Math.max(right, node.x + node.width);
                bottom = Math.max(bottom, node.y + node.height);
            }
            return {
                left: Math.max(left - 80, 0),
                top: Math.max(top - 80, 0),
                right: Math.min(right + 80, this.canvasWidth),
                bottom: Math.min(bottom + 80, this.canvasHeight)
            };
        },
        handleFitCanvas: function () {
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper) {
                return;
            }
            var bounds = this.resolveCanvasBounds();
            var contentWidth = Math.max(bounds.right - bounds.left, 320);
            var contentHeight = Math.max(bounds.bottom - bounds.top, 220);
            var scaleX = wrapper.clientWidth / contentWidth;
            var scaleY = wrapper.clientHeight / contentHeight;
            this.setCanvasScale(Math.min(scaleX, scaleY, 1.2));
            this.$nextTick(this.handleCenterCanvas);
        },
        handleCenterCanvas: function () {
            var wrapper = this.$refs.canvasWrapper;
            if (!wrapper) {
                return;
            }
            var bounds = this.resolveCanvasBounds();
            var centerX = (bounds.left + bounds.right) / 2;
            var centerY = (bounds.top + bounds.bottom) / 2;
            wrapper.scrollLeft = Math.max(centerX * this.canvasScale - wrapper.clientWidth / 2, 0);
            wrapper.scrollTop = Math.max(centerY * this.canvasScale - wrapper.clientHeight / 2, 0);
        }
    },
    mounted: async function () {
        this.ensureStyle();
        this.ensureExpandedGroups();
        for (var index = 0; index < this.nodeGroups.length; index += 1) {
            this.$set(this.expandedGroups, this.nodeGroups[index].category, index < 1);
        }
        document.addEventListener("mousemove", this.handleDocumentMouseMove);
        document.addEventListener("mouseup", this.handleDocumentMouseUp);
        document.addEventListener("keydown", this.handleDocumentKeyDown);
        await this.loadRouteProcessDefinitionIfNeeded();
        this.$nextTick(this.handleCenterCanvas);
    },
    beforeDestroy: function () {
        document.removeEventListener("mousemove", this.handleDocumentMouseMove);
        document.removeEventListener("mouseup", this.handleDocumentMouseUp);
        document.removeEventListener("keydown", this.handleDocumentKeyDown);
    },
    watch: {
        $route: function () {
            if (this.$route && this.$route.path === "/designer") {
                this.loadRouteProcessDefinitionIfNeeded();
            }
        },
        "selectedNode.parentId": function () {
            if (!this.selectedNode) {
                return;
            }
            if (!this.validateNodeParentRelation(this.selectedNode)) {
                return;
            }
            this.keepNodeInsideParent(this.selectedNode);
            this.removeIncompatibleEdgesForNode(this.selectedNode.id);
        }
    }
};
