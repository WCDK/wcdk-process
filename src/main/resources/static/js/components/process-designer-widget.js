/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
var processDesignerStyleId = "wcdk-process-inline-style";

window.WcdkProcessDesigner = {
    name: "WcdkProcessDesigner",
    props: {
        processDefinitionId: {
            type: String,
            default: ""
        },
        deploymentId: {
            type: String,
            default: ""
        },
        modelId: {
            type: String,
            default: ""
        },
        processDefinitionDetail: {
            type: Object,
            default: null
        },
        formBindings: {
            type: Array,
            default: function () {
                return [];
            }
        },
        formRecords: {
            type: Array,
            default: function () {
                return [];
            }
        },
        formTotal: {
            type: Number,
            default: 0
        },
        formLoading: {
            type: Boolean,
            default: false
        },
        buttonPermissions: {
            type: Array,
            default: null
        },
        saveHandler: {
            type: Function,
            default: null
        },
        exportHandler: {
            type: Function,
            default: null
        },
        updateMode: {
            type: Boolean,
            default: false
        },
        autoLoad: {
            type: Boolean,
            default: false
        }
    },
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                       <div class="section-kicker">流程设计工作区</div>
                        <h2>流程设计</h2>
                    </div>
                    <div class="designer-header-actions">
                        <el-button v-if="hasButton('designer:canvas:center')" @click="handleCenterCanvas">居中显示</el-button>
                        <el-button v-if="hasButton('designer:canvas:reset')" @click="handleResetCanvas">清空画布</el-button>
                        <el-button v-if="hasButton('designer:canvas:refresh')" type="primary" @click="handleRefresh">刷新画布</el-button>
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
                                <button  type="button" class="designer-tool-button designer-tool-button-wide" @click="handleCenterCanvas">
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
                                        <g
                                            v-for="edge in visibleNamedCanvasEdges"
                                            :key="'edge-label-' + edge.id"
                                            class="designer-stage-line-label"
                                            :class="{ active: selectedEdgeId === edge.id }"
                                            :transform="'translate(' + resolveEdgeLabelPoint(edge).x + ',' + resolveEdgeLabelPoint(edge).y + ')'"
                                            @click.stop="handleEdgeSelect(edge.id)">
                                            <rect
                                                :x="-resolveEdgeLabelSize(edge).width / 2"
                                                y="-12"
                                                :width="resolveEdgeLabelSize(edge).width"
                                                height="24"
                                                rx="12">
                                            </rect>
                                            <text text-anchor="middle" dominant-baseline="central">{{ resolveEdgeLabelText(edge) }}</text>
                                        </g>
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
                                        <div
                                            v-if="hasBoundForms(node)"
                                            class="designer-stage-node-bind"
                                            :title="resolveNodeBoundFormTitle(node)">
                                            已绑 {{ resolveNodeBoundFormCount(node) }} 个表单
                                        </div>
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
                                        <div
                                            v-if="hasBoundForms(node)"
                                            class="designer-stage-node-bind"
                                            :title="resolveNodeBoundFormTitle(node)">
                                            已绑 {{ resolveNodeBoundFormCount(node) }} 个表单
                                        </div>
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
                                <span>拖拽节点右侧连接点开始连线。</span>
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
                                <template v-if="selectedNode.properties">
                                    <div class="designer-property-divider">节点特性</div>
                                    <el-form-item label="发起人变量" v-if="isStartEventNode(selectedNode)">
                                        <el-input v-model.trim="selectedNode.properties.initiator" placeholder="请输入发起人变量名，例如 startUserId"></el-input>
                                    </el-form-item>
                                    <el-form-item label="表单标识" v-if="supportsFormKey(selectedNode)">
                                        <el-input v-model.trim="selectedNode.properties.formKey" placeholder="请输入表单标识或前端表单地址"></el-input>
                                    </el-form-item>
                                    <template v-if="isUserTaskNode(selectedNode)">
                                        <el-form-item label="绑定表单">
                                            <div class="designer-form-bind-row">
                                                <el-button type="primary" size="mini" @click="openUserTaskFormDialog">绑定表单</el-button>
                                                <span class="helper-text" v-if="!selectedNode.properties.boundFormKeys.length">未绑定表单</span>
                                            </div>
                                            <div class="designer-bound-form-tags" v-if="selectedNode.properties.boundFormKeys.length">
                                                <el-tag
                                                    v-for="formKey in selectedNode.properties.boundFormKeys"
                                                    :key="formKey"
                                                    size="mini"
                                                    closable
                                                    @close="removeBoundFormKey(formKey)">
                                                    {{ resolveBoundFormName(formKey) }}
                                                </el-tag>
                                            </div>
                                        </el-form-item>
                                        <el-form-item label="办理人">
                                            <el-input v-model.trim="selectedNode.properties.assignee" placeholder="请输入办理人表达式，例如 \${assignee}"></el-input>
                                        </el-form-item>
                                        <el-form-item label="候选用户">
                                            <el-input v-model.trim="selectedNode.properties.candidateUsers" placeholder="多个用户用英文逗号分隔"></el-input>
                                        </el-form-item>
                                        <el-form-item label="候选组">
                                            <el-input v-model.trim="selectedNode.properties.candidateGroups" placeholder="多个组用英文逗号分隔"></el-input>
                                        </el-form-item>
                                        <el-form-item label="到期时间">
                                            <el-input v-model.trim="selectedNode.properties.dueDate" placeholder="例如 P2D 或 \${dueDate}"></el-input>
                                        </el-form-item>
                                        <el-form-item label="优先级">
                                            <el-input v-model.trim="selectedNode.properties.priority" placeholder="请输入数字或表达式"></el-input>
                                        </el-form-item>
                                        <el-form-item label="任务监听器">
                                            <el-input v-model.trim="selectedNode.properties.taskListeners" type="textarea" :rows="3" placeholder="每行一个监听器配置，格式：事件:类名或表达式"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isServiceTaskNode(selectedNode)">
                                        <el-form-item label="实现方式">
                                            <el-select v-model="selectedNode.properties.implementationType" placeholder="请选择实现方式">
                                                <el-option label="Java 类" value="class"></el-option>
                                                <el-option label="委托表达式" value="delegateExpression"></el-option>
                                                <el-option label="表达式" value="expression"></el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="Java 类名" v-if="selectedNode.properties.implementationType === 'class'">
                                            <el-input v-model.trim="selectedNode.properties.className" placeholder="请输入完整类名"></el-input>
                                        </el-form-item>
                                        <el-form-item label="委托表达式" v-if="selectedNode.properties.implementationType === 'delegateExpression'">
                                            <el-input v-model.trim="selectedNode.properties.delegateExpression" placeholder="例如 \${serviceTaskDelegate}"></el-input>
                                        </el-form-item>
                                        <el-form-item label="执行表达式" v-if="selectedNode.properties.implementationType === 'expression'">
                                            <el-input v-model.trim="selectedNode.properties.expression" placeholder="例如 \${bean.execute(execution)}"></el-input>
                                        </el-form-item>
                                        <el-form-item label="结果变量">
                                            <el-input v-model.trim="selectedNode.properties.resultVariable" placeholder="请输入执行结果变量名"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isScriptTaskNode(selectedNode)">
                                        <el-form-item label="脚本格式">
                                            <el-input v-model.trim="selectedNode.properties.scriptFormat" placeholder="例如 groovy、javascript"></el-input>
                                        </el-form-item>
                                        <el-form-item label="脚本内容">
                                            <el-input v-model.trim="selectedNode.properties.script" type="textarea" :rows="6" placeholder="请输入脚本内容"></el-input>
                                        </el-form-item>
                                        <el-form-item label="结果变量">
                                            <el-input v-model.trim="selectedNode.properties.resultVariable" placeholder="请输入脚本结果变量名"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isMailTaskNode(selectedNode)">
                                        <el-form-item label="收件人">
                                            <el-input v-model.trim="selectedNode.properties.to" placeholder="请输入收件人，多个地址用英文逗号分隔"></el-input>
                                        </el-form-item>
                                        <el-form-item label="邮件主题">
                                            <el-input v-model.trim="selectedNode.properties.subject" placeholder="请输入邮件主题"></el-input>
                                        </el-form-item>
                                        <el-form-item label="邮件正文">
                                            <el-input v-model.trim="selectedNode.properties.text" type="textarea" :rows="4" placeholder="请输入邮件正文"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isReceiveTaskNode(selectedNode)">
                                        <el-form-item label="消息引用">
                                            <el-input v-model.trim="selectedNode.properties.messageRef" placeholder="请输入消息引用标识"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isBusinessRuleTaskNode(selectedNode)">
                                        <el-form-item label="规则输入变量">
                                            <el-input v-model.trim="selectedNode.properties.ruleVariablesInput" placeholder="请输入规则输入变量"></el-input>
                                        </el-form-item>
                                        <el-form-item label="规则结果变量">
                                            <el-input v-model.trim="selectedNode.properties.resultVariable" placeholder="请输入规则结果变量"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isCallActivityNode(selectedNode)">
                                        <el-form-item label="调用流程标识">
                                            <el-input v-model.trim="selectedNode.properties.calledElement" placeholder="请输入被调用流程定义 Key"></el-input>
                                        </el-form-item>
                                        <el-form-item label="继承变量">
                                            <el-switch v-model="selectedNode.properties.inheritVariables" active-text="是" inactive-text="否"></el-switch>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isEventNode(selectedNode)">
                                        <el-form-item label="事件定义">
                                            <el-select v-model="selectedNode.properties.eventDefinitionType" clearable placeholder="请选择事件定义">
                                                <el-option label="无" value=""></el-option>
                                                <el-option label="消息事件" value="message"></el-option>
                                                <el-option label="定时事件" value="timer"></el-option>
                                                <el-option label="信号事件" value="signal"></el-option>
                                                <el-option label="错误事件" value="error"></el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="消息引用" v-if="selectedNode.properties.eventDefinitionType === 'message'">
                                            <el-input v-model.trim="selectedNode.properties.messageRef" placeholder="请输入消息引用"></el-input>
                                        </el-form-item>
                                        <el-form-item label="定时表达式" v-if="selectedNode.properties.eventDefinitionType === 'timer'">
                                            <el-input v-model.trim="selectedNode.properties.timerDefinition" placeholder="例如 PT2H 或 \${timerValue}"></el-input>
                                        </el-form-item>
                                        <el-form-item label="信号引用" v-if="selectedNode.properties.eventDefinitionType === 'signal'">
                                            <el-input v-model.trim="selectedNode.properties.signalRef" placeholder="请输入信号引用"></el-input>
                                        </el-form-item>
                                        <el-form-item label="错误引用" v-if="selectedNode.properties.eventDefinitionType === 'error'">
                                            <el-input v-model.trim="selectedNode.properties.errorRef" placeholder="请输入错误引用"></el-input>
                                        </el-form-item>
                                        <el-form-item label="挂载活动" v-if="selectedNode.bpmnType === 'boundaryEvent'">
                                            <el-select v-model="selectedNode.properties.attachedToRef" filterable clearable placeholder="请选择挂载活动">
                                                <el-option v-for="item in attachableActivityOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="中断活动" v-if="selectedNode.bpmnType === 'boundaryEvent'">
                                            <el-switch v-model="selectedNode.properties.cancelActivity" active-text="是" inactive-text="否"></el-switch>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isSubProcessNode(selectedNode)">
                                        <el-form-item label="事件子流程">
                                            <el-switch v-model="selectedNode.properties.triggeredByEvent" active-text="是" inactive-text="否"></el-switch>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isLaneOrPoolNode(selectedNode)">
                                        <el-form-item label="参与者名称">
                                            <el-input v-model.trim="selectedNode.properties.participantName" placeholder="请输入参与者或泳道名称"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="isTextAnnotationNode(selectedNode)">
                                        <el-form-item label="注释内容">
                                            <el-input v-model.trim="selectedNode.properties.text" type="textarea" :rows="5" placeholder="请输入注释内容"></el-input>
                                        </el-form-item>
                                    </template>
                                    <template v-if="supportsMultiInstance(selectedNode)">
                                        <div class="designer-property-divider">多实例</div>
                                        <el-form-item label="启用多实例">
                                            <el-switch v-model="selectedNode.properties.multiInstanceEnabled" active-text="是" inactive-text="否"></el-switch>
                                        </el-form-item>
                                        <template v-if="selectedNode.properties.multiInstanceEnabled">
                                            <el-form-item label="串行执行">
                                                <el-switch v-model="selectedNode.properties.multiInstanceSequential" active-text="串行" inactive-text="并行"></el-switch>
                                            </el-form-item>
                                            <el-form-item label="集合变量">
                                                <el-input v-model.trim="selectedNode.properties.collection" placeholder="例如 \${userList}"></el-input>
                                            </el-form-item>
                                            <el-form-item label="元素变量">
                                                <el-input v-model.trim="selectedNode.properties.elementVariable" placeholder="例如 user"></el-input>
                                            </el-form-item>
                                            <el-form-item label="完成条件">
                                                <el-input v-model.trim="selectedNode.properties.completionCondition" placeholder="例如 \${nrOfCompletedInstances/nrOfInstances >= 0.6}"></el-input>
                                            </el-form-item>
                                        </template>
                                    </template>
                                    <template v-if="supportsExecutionConfig(selectedNode)">
                                        <div class="designer-property-divider">执行特性</div>
                                        <el-form-item label="异步执行">
                                            <el-switch v-model="selectedNode.properties.async" active-text="是" inactive-text="否"></el-switch>
                                        </el-form-item>
                                        <el-form-item label="排他执行">
                                            <el-switch v-model="selectedNode.properties.exclusive" active-text="是" inactive-text="否"></el-switch>
                                        </el-form-item>
                                        <el-form-item label="跳过表达式" v-if="isTaskNode(selectedNode)">
                                            <el-input v-model.trim="selectedNode.properties.skipExpression" placeholder="请输入跳过表达式"></el-input>
                                        </el-form-item>
                                        <el-form-item label="执行监听器">
                                            <el-input v-model.trim="selectedNode.properties.executionListeners" type="textarea" :rows="3" placeholder="每行一个监听器配置，格式：事件:类名或表达式"></el-input>
                                        </el-form-item>
                                    </template>
                                </template>
                                <el-form-item label="默认分支" v-if="isExclusiveGatewayNode(selectedNode)">
                                    <el-select
                                        v-model="selectedNode.defaultFlowId"
                                        clearable
                                        filterable
                                        placeholder="请选择默认分支">
                                        <el-option
                                            v-for="item in resolveOutgoingEdgeOptions(selectedNode.id)"
                                            :key="item.id"
                                            :label="item.label"
                                            :value="item.id">
                                        </el-option>
                                    </el-select>
                                </el-form-item>
                                <div v-if="isExclusiveGatewayNode(selectedNode)" class="designer-branch-config">
                                    <div class="designer-panel-subtitle">分支条件</div>
                                    <div
                                        v-for="edge in resolveOutgoingEdges(selectedNode.id)"
                                        :key="edge.id"
                                        class="designer-branch-item">
                                        <div class="designer-branch-head">
                                            <span>{{ resolveEdgeDisplayName(edge) }}</span>
                                            <span class="mini-tag" v-if="selectedNode.defaultFlowId === edge.id">默认分支</span>
                                        </div>
                                        <el-input
                                            v-model.trim="edge.conditionExpression"
                                            type="textarea"
                                            :rows="3"
                                            :disabled="selectedNode.defaultFlowId === edge.id"
                                            :placeholder="resolveBranchConditionPlaceholder(edge)">
                                        </el-input>
                                    </div>
                                    <div v-if="!resolveOutgoingEdges(selectedNode.id).length" class="helper-text">
                                        请先从当前排他网关拖拽连线到目标节点。
                                    </div>
                                </div>
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
                                <el-form-item label="分支条件表达式" v-if="isExclusiveGatewayEdge(selectedEdge)">
                                    <el-input
                                        v-model.trim="selectedEdge.conditionExpression"
                                        type="textarea"
                                        :rows="4"
                                        :disabled="isExclusiveGatewayEdgeDefault(selectedEdge)"
                                        :placeholder="resolveBranchConditionPlaceholder(selectedEdge)">
                                    </el-input>
                                </el-form-item>
                                <div class="designer-node-meta">
                                    <span class="mini-tag">起点：{{ resolveNodeName(selectedEdge.sourceId) }}</span>
                                    <span class="mini-tag">终点：{{ resolveNodeName(selectedEdge.targetId) }}</span>
                                    <span class="mini-tag" v-if="isExclusiveGatewayEdgeDefault(selectedEdge)">默认分支</span>
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

                <el-dialog
                    title="绑定表单"
                    :visible.sync="formBindDialogVisible"
                    width="760px"
                    append-to-body
                    @open="handleFormBindDialogOpen">
                    <div class="designer-form-bind-dialog">
                        <div class="designer-form-bind-filter">
                            <el-input
                                v-model.trim="formBindQuery.formName"
                                size="small"
                                clearable
                                placeholder="表单名称"
                                @keyup.enter.native="handleFormBindQuery">
                            </el-input>
                            <el-input
                                v-model.trim="formBindQuery.formKey"
                                size="small"
                                clearable
                                placeholder="表单标识"
                                @keyup.enter.native="handleFormBindQuery">
                            </el-input>
                            <el-button size="small" type="primary" @click="handleFormBindQuery">查询</el-button>
                            <el-button size="small" @click="resetFormBindQuery">重置</el-button>
                        </div>
                        <el-table
                            ref="formBindTable"
                            v-loading="formBindLoading"
                            :data="formBindRecords"
                            row-key="formKey"
                            height="360"
                            @selection-change="handleFormBindSelectionChange">
                            <el-table-column type="selection" width="48" :reserve-selection="true"></el-table-column>
                            <el-table-column prop="formName" label="表单名称" min-width="160"></el-table-column>
                            <el-table-column prop="formKey" label="表单标识" min-width="180"></el-table-column>
                            <el-table-column prop="formVersion" label="版本" width="80"></el-table-column>
                            <el-table-column prop="updateTime" label="更新时间" min-width="160">
                                <template slot-scope="scope">{{ formatDateTime(scope.row.updateTime) }}</template>
                            </el-table-column>
                            <el-table-column label="操作" width="90" fixed="right">
                                <template slot-scope="scope">
                                    <el-button type="text" @click.stop="previewFormBindRecord(scope.row)">预览</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="designer-form-bind-footer">
                            <el-pagination
                                background
                                layout="total, prev, pager, next"
                                :current-page="formBindPageNum"
                                :page-size="formBindPageSize"
                                :total="formBindTotal"
                                @current-change="handleFormBindPageChange">
                            </el-pagination>
                            <span class="helper-text">已选择 {{ formBindSelectedKeys.length }} 个表单</span>
                        </div>
                    </div>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="formBindDialogVisible = false">取消</el-button>
                        <el-button type="primary" @click="confirmUserTaskFormBinding">确定</el-button>
                    </span>
                </el-dialog>

                <el-dialog
                    title="表单预览"
                    :visible.sync="formPreviewDialogVisible"
                    width="760px"
                    append-to-body>
                    <div v-if="previewFormRecord">
                        <div class="process-schema-panel">
                            <div class="schema-chip-list">
                                <span class="schema-chip">{{ previewFormRecord.formName || previewFormRecord.formKey }}</span>
                                <span class="schema-chip">{{ previewFormRecord.formKey }}</span>
                                <span class="schema-chip">字段数量：{{ previewFormFieldCount }}</span>
                            </div>
                        </div>
                        <el-form label-position="top" class="panel-form" @submit.native.prevent>
                            <div v-if="previewFormFields.length" class="form-grid two-columns dynamic-process-form-grid">
                                <dynamic-process-form-field
                                    v-for="field in previewFormFields"
                                    :key="field.fieldKey"
                                    :field="field"
                                    :form-values="previewFormValues">
                                </dynamic-process-form-field>
                            </div>
                            <div class="empty-panel process-schema-empty" v-else>
                                当前表单没有可预览字段。
                            </div>
                        </el-form>
                    </div>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="formPreviewDialogVisible = false">关闭</el-button>
                    </span>
                </el-dialog>
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
                            label: "聚合网关",
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
            designerSaveLoading: false,
            draftDesignerModelKey: "",
            importedModelId: "",
            importedProcessDefinitionKey: "",
            importedProcessDefinitionName: "",
            importedProcessDefinitionCategory: "",
            importedProcessDefinitionId: "",
            importedDeploymentId: "",
            formBindDialogVisible: false,
            formBindLoading: false,
            formBindRecords: [],
            formBindSelectedKeys: [],
            formBindSelectedMap: {},
            formBindSyncingSelection: false,
            formBindQuery: {
                formName: "",
                formKey: ""
            },
            formBindPageNum: 1,
            formBindPageSize: 10,
            formBindTotal: 0,
            formPreviewDialogVisible: false,
            previewFormRecord: null,
            previewFormFields: [],
            previewFormValues: {},
            previewFormFieldCount: 0
        };
    },
    computed: {
        selectedNode: function () {
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                if (this.canvasNodes[index].id === this.selectedNodeId) {
                    this.ensureNodeProperties(this.canvasNodes[index]);
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
        canShowDesignerSave: function () {
            return true;
        },
        isUpdateProcessMode: function () {
            if (this.updateMode) {
                return true;
            }
            var query = this.$route && this.$route.query ? this.$route.query : {};
            return query.flg === "update";
        },
        designerSaveButtonText: function () {
            return this.importedModelId || this.importedProcessDefinitionId ? "保存修改" : "新增流程";
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
        visibleNamedCanvasEdges: function () {
            return this.visibleCanvasEdges.filter(function (edge) {
                return !!String(edge && edge.name || "").trim();
            });
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
        attachableActivityOptions: function () {
            var options = [];
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (!node || node.id === this.selectedNodeId || !this.isActivityNode(node)) {
                    continue;
                }
                options.push({
                    id: node.code || node.id,
                    name: (node.name || node.code || node.id) + " / " + node.bpmnType
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
        formatDateTime: function (value) {
            if (!value) {
                return "-";
            }
            var date = new Date(value);
            if (Number.isNaN(date.getTime())) {
                return String(value);
            }
            return date.toLocaleString("zh-CN", { hour12: false });
        },
        hasButton: function (permissionCode) {
            if (!permissionCode || !Array.isArray(this.buttonPermissions)) {
                return true;
            }
            return this.buttonPermissions.indexOf(permissionCode) >= 0
                || this.buttonPermissions.indexOf("*") >= 0
                || this.buttonPermissions.indexOf("*:*:*") >= 0;
        },
        cloneDesignerData: function (data) {
            return JSON.parse(JSON.stringify(data || []));
        },
        resolveDesignerModelKey: function () {
            return this.importedProcessDefinitionKey
                || (this.importedProcessDefinitionId ? String(this.importedProcessDefinitionId).split(":")[0] : "")
                || this.ensureDraftDesignerModelKey();
        },
        ensureDraftDesignerModelKey: function () {
            if (!this.draftDesignerModelKey) {
                this.draftDesignerModelKey = "Wcdk_" + Date.now();
            }
            return this.draftDesignerModelKey;
        },
        resolveDesignerModelName: function () {
            return this.importedProcessDefinitionName
                || this.resolveDesignerModelKey();
        },
        handleSaveDesignerModel: async function () {
            this.designerSaveLoading = true;
            try {
                var savePayload = await this.buildDesignerSavePayload();
                var modelResult = null;
                if (typeof this.saveHandler === "function") {
                    modelResult = await this.saveHandler(savePayload);
                }
                this.$emit("save", savePayload);
                this.$message.success((modelResult && modelResult.message) || "流程数据已提交给外部处理");
            } catch (error) {
                this.$message.error(error && error.message ? error.message : "流程保存失败");
            } finally {
                this.designerSaveLoading = false;
            }
        },
        saveDesignerFlowModel: async function () {
            return this.buildDesignerModelSavePayload();
        },
        saveDesignerProcessDefinition: async function () {
            if (!this.importedProcessDefinitionId) {
                throw new Error("未查询到要修改的流程定义");
            }
            return this.buildDesignerSavePayload();
        },
        buildDesignerSavePayload: async function () {
            return {
                mode: this.isUpdateProcessMode && this.importedProcessDefinitionId ? "updateProcessDefinition" : "saveModel",
                model: await this.buildDesignerModelSavePayload(),
                formBinding: this.buildDesignerFormBindingPayload(),
                exportPayload: this.buildProcessDesignerExportPayload("bpmn20.xml")
            };
        },
        buildDesignerModelSavePayload: async function () {
            var modelKey = this.resolveDesignerModelKey();
            return {
                modelName: this.resolveDesignerModelName(),
                modelKey: modelKey,
                category: this.importedProcessDefinitionCategory || "",
                bpmnXml: this.buildBpmnXmlContent().xml
            };
        },
        saveDesignerFormBinding: async function () {
            var payload = this.buildDesignerFormBindingPayload();
            if (!payload.processDefinitionId) {
                return null;
            }
            return payload;
        },
        loadProcessFormBindings: async function (processDefinitionId) {
            if (!processDefinitionId) {
                return;
            }
            this.applyProcessFormBindings(this.formBindings || []);
        },
        applyProcessFormBindings: function (records) {
            var bindingMap = {};
            for (var index = 0; index < (records || []).length; index += 1) {
                var record = records[index] || {};
                var nodeId = String(record.processNodeId || record.processNode || "").trim();
                if (!nodeId || !record.formKey) {
                    continue;
                }
                if (!bindingMap[nodeId]) {
                    bindingMap[nodeId] = [];
                }
                bindingMap[nodeId].push(record);
            }
            for (var nodeIndex = 0; nodeIndex < this.canvasNodes.length; nodeIndex += 1) {
                var node = this.canvasNodes[nodeIndex];
                if (!this.isUserTaskNode(node)) {
                    continue;
                }
                var forms = this.normalizeBoundForms(bindingMap[node.code] || bindingMap[node.id] || []);
                var properties = this.ensureNodeProperties(node);
                this.$set(properties, "boundForms", forms);
                this.$set(properties, "boundFormKeys", forms.map(function (form) {
                    return form.formKey;
                }));
                this.syncUserTaskFormKey(node);
            }
        },
        syncRouteModelId: function (modelId) {
            if (!this.$router || !this.$route || !modelId) {
                return;
            }
            var query = Object.assign({}, this.$route.query || {}, {modelId: modelId});
            this.$router.replace({path: this.$route.path, query: query}).catch(function () {});
        },
        syncRouteProcessDefinition: function (detail) {
            if (!this.$router || !this.$route || !detail || !detail.processDefinitionId) {
                return;
            }
            var query = Object.assign({}, this.$route.query || {}, {
                flg: "update",
                deploymentId: detail.deploymentId || this.importedDeploymentId || "",
                processDefinitionId: detail.processDefinitionId
            });
            delete query.modelId;
            this.$router.replace({path: this.$route.path, query: query}).catch(function () {});
        },
        buildDesignerFormBindingPayload: function () {
            var bindings = [];
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (!this.isUserTaskNode(node)) {
                    continue;
                }
                var properties = this.ensureNodeProperties(node);
                var boundForms = this.normalizeBoundForms(properties.boundForms || []);
                var formIds = [];
                for (var formIndex = 0; formIndex < boundForms.length; formIndex += 1) {
                    var formId = String(boundForms[formIndex].id || "").trim();
                    if (formId && formIds.indexOf(formId) < 0) {
                        formIds.push(formId);
                    }
                }
                if (!formIds.length) {
                    continue;
                }
                bindings.push({
                    taskDefinitionKey: node.code || node.id,
                    formIds: formIds
                });
            }
            return {
                processDefinitionId: this.importedProcessDefinitionId,
                processDefinitionKey: this.importedProcessDefinitionKey,
                processDefinitionVersion: "",
                deploymentId: this.importedDeploymentId,
                bindings: bindings
            };
        },
        buildDefaultNodeProperties: function (bpmnType) {
            return {
                initiator: "",
                formKey: "",
                boundFormKeys: [],
                boundForms: [],
                assignee: "",
                candidateUsers: "",
                candidateGroups: "",
                dueDate: "",
                priority: "",
                taskListeners: "",
                implementationType: "class",
                className: "",
                delegateExpression: "",
                expression: "",
                resultVariable: "",
                scriptFormat: "groovy",
                script: "",
                to: "",
                subject: "",
                text: "",
                messageRef: "",
                ruleVariablesInput: "",
                calledElement: "",
                inheritVariables: true,
                eventDefinitionType: "",
                timerDefinition: "",
                signalRef: "",
                errorRef: "",
                attachedToRef: "",
                cancelActivity: true,
                triggeredByEvent: false,
                participantName: "",
                multiInstanceEnabled: false,
                multiInstanceSequential: false,
                collection: "",
                elementVariable: "",
                completionCondition: "",
                async: false,
                exclusive: true,
                skipExpression: "",
                executionListeners: ""
            };
        },
        ensureNodeProperties: function (node) {
            if (!node) {
                return {};
            }
            var defaults = this.buildDefaultNodeProperties(node.bpmnType);
            if (!node.properties) {
                this.$set(node, "properties", defaults);
                return node.properties;
            }
            for (var key in defaults) {
                if (Object.prototype.hasOwnProperty.call(defaults, key) && typeof node.properties[key] === "undefined") {
                    this.$set(node.properties, key, defaults[key]);
                }
            }
            if (!Array.isArray(node.properties.boundFormKeys)) {
                this.$set(node.properties, "boundFormKeys", this.parseFormKeyList(node.properties.boundFormKeys || node.properties.formKey));
            }
            if (!Array.isArray(node.properties.boundForms)) {
                this.$set(node.properties, "boundForms", []);
            }
            return node.properties;
        },
        mergeNodeProperties: function (bpmnType, sourceProperties) {
            var result = this.buildDefaultNodeProperties(bpmnType);
            var source = sourceProperties || {};
            for (var key in source) {
                if (Object.prototype.hasOwnProperty.call(source, key)) {
                    result[key] = source[key];
                }
            }
            return result;
        },
        cloneNodeProperties: function (node) {
            var properties = this.ensureNodeProperties(node);
            var result = {};
            for (var key in properties) {
                if (Object.prototype.hasOwnProperty.call(properties, key)) {
                    result[key] = properties[key];
                }
            }
            return result;
        },
        isStartEventNode: function (node) {
            return !!node && node.bpmnType === "startEvent";
        },
        isUserTaskNode: function (node) {
            return !!node && node.bpmnType === "userTask";
        },
        isServiceTaskNode: function (node) {
            return !!node && node.bpmnType === "serviceTask";
        },
        isScriptTaskNode: function (node) {
            return !!node && node.bpmnType === "scriptTask";
        },
        isMailTaskNode: function (node) {
            return !!node && node.bpmnType === "mailTask";
        },
        isReceiveTaskNode: function (node) {
            return !!node && node.bpmnType === "receiveTask";
        },
        isBusinessRuleTaskNode: function (node) {
            return !!node && node.bpmnType === "businessRuleTask";
        },
        isCallActivityNode: function (node) {
            return !!node && node.bpmnType === "callActivity";
        },
        isSubProcessNode: function (node) {
            return !!node && node.bpmnType === "subProcess";
        },
        isTextAnnotationNode: function (node) {
            return !!node && node.bpmnType === "textAnnotation";
        },
        isLaneOrPoolNode: function (node) {
            return !!node && (node.bpmnType === "lane" || node.bpmnType === "pool");
        },
        isEventNode: function (node) {
            return !!node && ["startEvent", "endEvent", "boundaryEvent", "intermediateCatchEvent", "intermediateThrowEvent"].indexOf(node.bpmnType) >= 0;
        },
        isTaskNode: function (node) {
            return !!node && ["userTask", "scriptTask", "serviceTask", "mailTask", "manualTask", "receiveTask", "businessRuleTask", "callActivity"].indexOf(node.bpmnType) >= 0;
        },
        isActivityNode: function (node) {
            return this.isTaskNode(node) || this.isSubProcessNode(node);
        },
        supportsFormKey: function (node) {
            return this.isStartEventNode(node) || this.isUserTaskNode(node);
        },
        parseFormKeyList: function (value) {
            if (Array.isArray(value)) {
                return value.filter(function (item) {
                    return !!item;
                }).map(function (item) {
                    return String(item).trim();
                }).filter(function (item, index, array) {
                    return !!item && array.indexOf(item) === index;
                });
            }
            return String(value || "").split(",").map(function (item) {
                return item.trim();
            }).filter(function (item, index, array) {
                return !!item && array.indexOf(item) === index;
            });
        },
        normalizeBoundForms: function (records) {
            var results = [];
            var usedKeys = {};
            for (var index = 0; index < (records || []).length; index += 1) {
                var record = records[index] || {};
                var formKey = String(record.formKey || "").trim();
                if (!formKey || usedKeys[formKey]) {
                    continue;
                }
                usedKeys[formKey] = true;
                results.push({
                    id: record.id || "",
                    formKey: formKey,
                    formName: record.formName || formKey,
                    formVersion: record.formVersion || ""
                });
            }
            return results;
        },
        syncUserTaskFormKey: function (node) {
            var targetNode = node || this.selectedNode;
            if (!targetNode || !targetNode.properties) {
                return;
            }
            var keys = this.parseFormKeyList(targetNode.properties.boundFormKeys);
            this.$set(targetNode.properties, "boundFormKeys", keys);
            this.$set(targetNode.properties, "formKey", keys.join(","));
        },
        openUserTaskFormDialog: function () {
            if (!this.selectedNode || !this.isUserTaskNode(this.selectedNode)) {
                return;
            }
            var properties = this.ensureNodeProperties(this.selectedNode);
            this.formBindSelectedKeys = this.parseFormKeyList(properties.boundFormKeys || properties.formKey);
            this.formBindSelectedMap = {};
            for (var index = 0; index < (properties.boundForms || []).length; index += 1) {
                var item = properties.boundForms[index] || {};
                if (item.formKey) {
                    this.$set(this.formBindSelectedMap, item.formKey, item);
                }
            }
            for (var keyIndex = 0; keyIndex < this.formBindSelectedKeys.length; keyIndex += 1) {
                var formKey = this.formBindSelectedKeys[keyIndex];
                if (!this.formBindSelectedMap[formKey]) {
                    this.$set(this.formBindSelectedMap, formKey, { formKey: formKey, formName: formKey });
                }
            }
            this.formBindDialogVisible = true;
        },
        handleFormBindDialogOpen: function () {
            this.loadFormBindRecords();
        },
        buildFormBindQuery: function () {
            var query = "?pageNum=" + encodeURIComponent(this.formBindPageNum)
                + "&pageSize=" + encodeURIComponent(this.formBindPageSize);
            if (this.formBindQuery.formName) {
                query += "&formName=" + encodeURIComponent(this.formBindQuery.formName);
            }
            if (this.formBindQuery.formKey) {
                query += "&formKey=" + encodeURIComponent(this.formBindQuery.formKey);
            }
            return query;
        },
        loadFormBindRecords: async function () {
            this.formBindLoading = !!this.formLoading;
            this.formBindRecords = this.cloneDesignerData(this.formRecords || []);
            this.formBindTotal = Number(this.formTotal || this.formBindRecords.length || 0);
            this.$emit("form-query", {
                pageNum: this.formBindPageNum,
                pageSize: this.formBindPageSize,
                formName: this.formBindQuery.formName,
                formKey: this.formBindQuery.formKey
            });
            this.$nextTick(this.syncFormBindTableSelection);
        },
        syncFormBindTableSelection: function () {
            var table = this.$refs.formBindTable;
            if (!table || typeof table.clearSelection !== "function") {
                return;
            }
            this.formBindSyncingSelection = true;
            table.clearSelection();
            for (var index = 0; index < this.formBindRecords.length; index += 1) {
                var record = this.formBindRecords[index];
                if (record && this.formBindSelectedKeys.indexOf(record.formKey) >= 0) {
                    table.toggleRowSelection(record, true);
                }
            }
            this.$nextTick(function () {
                this.formBindSyncingSelection = false;
            });
        },
        handleFormBindSelectionChange: function (selection) {
            if (this.formBindSyncingSelection) {
                return;
            }
            var currentPageKeys = this.formBindRecords.map(function (item) {
                return item.formKey;
            });
            var selectedMap = {};
            for (var selectedIndex = 0; selectedIndex < selection.length; selectedIndex += 1) {
                var selected = selection[selectedIndex] || {};
                if (selected.formKey) {
                    selectedMap[selected.formKey] = selected;
                    this.$set(this.formBindSelectedMap, selected.formKey, selected);
                }
            }
            for (var keyIndex = this.formBindSelectedKeys.length - 1; keyIndex >= 0; keyIndex -= 1) {
                var existingKey = this.formBindSelectedKeys[keyIndex];
                if (currentPageKeys.indexOf(existingKey) >= 0 && !selectedMap[existingKey]) {
                    this.formBindSelectedKeys.splice(keyIndex, 1);
                    this.$delete(this.formBindSelectedMap, existingKey);
                }
            }
            for (var addIndex = 0; addIndex < selection.length; addIndex += 1) {
                var formKey = selection[addIndex] && selection[addIndex].formKey;
                if (formKey && this.formBindSelectedKeys.indexOf(formKey) < 0) {
                    this.formBindSelectedKeys.push(formKey);
                }
            }
        },
        handleFormBindQuery: function () {
            this.formBindPageNum = 1;
            this.loadFormBindRecords();
        },
        resetFormBindQuery: function () {
            this.formBindQuery.formName = "";
            this.formBindQuery.formKey = "";
            this.formBindPageNum = 1;
            this.loadFormBindRecords();
        },
        handleFormBindPageChange: function (pageNum) {
            this.formBindPageNum = pageNum;
            this.loadFormBindRecords();
        },
        previewFormBindRecord: function (record) {
            if (!record) {
                return;
            }
            var schema = Array.isArray(record.schema) ? this.cloneDesignerData(record.schema) : [];
            this.previewFormRecord = record;
            this.previewFormFields = this.makePreviewReadonlyFields(schema);
            this.previewFormValues = this.buildPreviewFormValues(this.previewFormFields);
            this.previewFormFieldCount = this.flattenPreviewFields(this.previewFormFields).length;
            this.formPreviewDialogVisible = true;
        },
        makePreviewReadonlyFields: function (fields) {
            var self = this;
            return (fields || []).map(function (field) {
                var nextField = Object.assign({}, field || {});
                nextField.readOnly = true;
                if (Array.isArray(nextField.children)) {
                    if (nextField.componentType === "table" || nextField.type === "table") {
                        nextField.children = nextField.children.map(function (cell) {
                            var nextCell = Object.assign({}, cell || {});
                            nextCell.fields = self.makePreviewReadonlyFields(nextCell.fields || []);
                            return nextCell;
                        });
                    } else {
                        nextField.children = self.makePreviewReadonlyFields(nextField.children || []);
                    }
                }
                return nextField;
            });
        },
        flattenPreviewFields: function (fields) {
            var results = [];
            for (var index = 0; index < (fields || []).length; index += 1) {
                var field = fields[index];
                if (!field) {
                    continue;
                }
                if (field.componentType === "table" || field.type === "table") {
                    var cells = field.children || [];
                    for (var cellIndex = 0; cellIndex < cells.length; cellIndex += 1) {
                        results = results.concat(this.flattenPreviewFields(cells[cellIndex].fields || []));
                    }
                    continue;
                }
                if (field.componentType === "group" || field.type === "group") {
                    results = results.concat(this.flattenPreviewFields(field.children || []));
                    continue;
                }
                if (field.componentType === "button" || field.type === "button" || !field.fieldKey) {
                    continue;
                }
                results.push(field);
            }
            return results;
        },
        buildPreviewFormValues: function (fields) {
            var values = {};
            var flatFields = this.flattenPreviewFields(fields);
            for (var index = 0; index < flatFields.length; index += 1) {
                var field = flatFields[index];
                if (field.componentType === "checkbox" || field.componentType === "switch") {
                    values[field.fieldKey] = field.defaultValue === true || field.defaultValue === "true";
                    continue;
                }
                if (field.componentType === "number") {
                    values[field.fieldKey] = field.defaultValue !== null && field.defaultValue !== undefined && field.defaultValue !== ""
                        ? Number(field.defaultValue)
                        : null;
                    continue;
                }
                values[field.fieldKey] = field.defaultValue !== null && field.defaultValue !== undefined
                    ? field.defaultValue
                    : "";
            }
            return values;
        },
        confirmUserTaskFormBinding: function () {
            if (!this.selectedNode || !this.isUserTaskNode(this.selectedNode)) {
                this.formBindDialogVisible = false;
                return;
            }
            var boundForms = [];
            for (var index = 0; index < this.formBindSelectedKeys.length; index += 1) {
                var formKey = this.formBindSelectedKeys[index];
                boundForms.push(this.formBindSelectedMap[formKey] || { formKey: formKey, formName: formKey });
            }
            var properties = this.ensureNodeProperties(this.selectedNode);
            this.$set(properties, "boundFormKeys", this.parseFormKeyList(this.formBindSelectedKeys));
            this.$set(properties, "boundForms", this.normalizeBoundForms(boundForms));
            this.syncUserTaskFormKey(this.selectedNode);
            this.formBindDialogVisible = false;
        },
        removeBoundFormKey: function (formKey) {
            if (!this.selectedNode || !this.selectedNode.properties) {
                return;
            }
            var keys = this.parseFormKeyList(this.selectedNode.properties.boundFormKeys);
            var nextKeys = keys.filter(function (item) {
                return item !== formKey;
            });
            var nextForms = this.normalizeBoundForms(this.selectedNode.properties.boundForms).filter(function (item) {
                return item.formKey !== formKey;
            });
            this.$set(this.selectedNode.properties, "boundFormKeys", nextKeys);
            this.$set(this.selectedNode.properties, "boundForms", nextForms);
            this.syncUserTaskFormKey(this.selectedNode);
        },
        resolveBoundFormName: function (formKey) {
            if (!this.selectedNode || !this.selectedNode.properties) {
                return formKey;
            }
            var boundForms = this.selectedNode.properties.boundForms || [];
            for (var index = 0; index < boundForms.length; index += 1) {
                if (boundForms[index] && boundForms[index].formKey === formKey) {
                    return (boundForms[index].formName || formKey) + "（" + formKey + "）";
                }
            }
            return formKey;
        },
        hasBoundForms: function (node) {
            return this.resolveNodeBoundFormCount(node) > 0;
        },
        resolveNodeBoundFormCount: function (node) {
            if (!node || !this.isUserTaskNode(node)) {
                return 0;
            }
            var properties = this.ensureNodeProperties(node);
            return this.parseFormKeyList(properties.boundFormKeys || properties.formKey).length;
        },
        resolveNodeBoundFormTitle: function (node) {
            if (!node || !this.isUserTaskNode(node)) {
                return "";
            }
            var properties = this.ensureNodeProperties(node);
            var boundForms = this.normalizeBoundForms(properties.boundForms || []);
            var formKeys = this.parseFormKeyList(properties.boundFormKeys || properties.formKey);
            var labels = [];
            for (var index = 0; index < formKeys.length; index += 1) {
                var formKey = formKeys[index];
                var label = formKey;
                for (var formIndex = 0; formIndex < boundForms.length; formIndex += 1) {
                    if (boundForms[formIndex].formKey === formKey) {
                        label = (boundForms[formIndex].formName || formKey) + "（" + formKey + "）";
                        break;
                    }
                }
                labels.push(label);
            }
            return labels.length ? "已绑定表单：" + labels.join("、") : "";
        },
        supportsMultiInstance: function (node) {
            return this.isTaskNode(node) || this.isSubProcessNode(node);
        },
        supportsExecutionConfig: function (node) {
            return this.isTaskNode(node) || this.isSubProcessNode(node) || (!!node && node.kind === "gateway");
        },
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
                ".designer-header-actions{display:flex;gap:10px;flex-wrap:wrap;justify-content:center;align-items:center;margin-bottom:12px;}" ,
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
                ".designer-stage-line-label{pointer-events:auto;cursor:pointer;}",
                ".designer-stage-line-label rect{fill:rgba(255,255,255,0.94);stroke:#d8e3f4;filter:drop-shadow(0 4px 10px rgba(15,23,42,0.10));}",
                ".designer-stage-line-label text{font-size:12px;font-weight:600;fill:#556b8b;user-select:none;}",
                ".designer-stage-line-label.active rect{stroke:#3477f6;}",
                ".designer-stage-line-label.active text{fill:#245fc9;}",
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
                ".designer-stage-node-bind{position:absolute;left:50%;bottom:-15px;transform:translateX(-50%);max-width:calc(100% - 16px);height:22px;padding:0 9px;border-radius:999px;background:#ecfdf5;border:1px solid rgba(16,185,129,0.32);color:#047857;font-size:11px;font-weight:700;line-height:20px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;box-shadow:0 8px 16px rgba(16,185,129,0.14);}",
                ".designer-summary-card{padding:14px 16px;border-radius:18px;border:1px solid #dfe8f6;background:rgba(255,255,255,0.88);display:grid;gap:12px;}",
                ".designer-summary-title{font-size:14px;font-weight:700;color:#20324c;}",
                ".designer-summary-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;}",
                ".designer-summary-item{padding:10px 12px;border-radius:14px;background:rgba(241,246,253,0.92);display:grid;gap:6px;}",
                ".designer-summary-item span{font-size:12px;color:#7487a1;}",
                ".designer-summary-item strong{font-size:14px;color:#223550;}",
                ".designer-summary-list{display:grid;gap:8px;}",
                ".designer-summary-list span{font-size:12px;line-height:1.7;color:#6d809d;}",
                ".designer-form-block{display:grid;gap:10px;}",
                ".designer-property-divider{margin:4px 0 8px;padding-top:10px;border-top:1px solid #e5edf7;color:#30445f;font-size:13px;font-weight:700;}",
                ".designer-node-meta{display:flex;gap:8px;flex-wrap:wrap;}",
                ".designer-form-bind-row{display:flex;align-items:center;gap:10px;flex-wrap:wrap;}",
                ".designer-bound-form-tags{display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;}",
                ".designer-form-bind-dialog{display:grid;gap:12px;}",
                ".designer-form-bind-filter{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr) auto auto;gap:10px;align-items:center;}",
                ".designer-form-bind-footer{display:flex;align-items:center;justify-content:space-between;gap:12px;}",
                ".designer-branch-config{display:grid;gap:8px;padding:10px;border:1px solid rgba(207,224,246,0.9);border-radius:8px;background:#f8fbff;}",
                ".designer-branch-item{display:grid;gap:6px;}",
                ".designer-branch-head{display:flex;align-items:center;justify-content:space-between;gap:8px;font-size:12px;color:#31415f;}",
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
                    defaultFlowId: node.defaultFlowId,
                    properties: this.cloneNodeProperties(node),
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
                    name: edge.name,
                    conditionExpression: edge.conditionExpression
                });
            }
            return {
                format: format,
                processId: this.resolveDesignerModelKey(),
                processName: this.resolveDesignerModelName(),
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
        decodeBase64ToText: function (contentBase64) {
            var binary = window.atob(contentBase64 || "");
            var bytes = new Uint8Array(binary.length);
            for (var index = 0; index < binary.length; index += 1) {
                bytes[index] = binary.charCodeAt(index);
            }
            if (typeof TextDecoder === "function") {
                return new TextDecoder("utf-8").decode(bytes);
            }
            var text = "";
            for (var textIndex = 0; textIndex < bytes.length; textIndex += 1) {
                text += String.fromCharCode(bytes[textIndex]);
            }
            return decodeURIComponent(escape(text));
        },
        requestProcessDesignerExport: async function (format) {
            var payload = this.buildProcessDesignerExportPayload(format);
            if (typeof this.exportHandler === "function") {
                return await this.exportHandler(payload);
            }
            this.$emit("export", payload);
            return null;
        },
        fetchProcessDefinitionDetailForDesigner: async function (processDefinitionId) {
            if (this.processDefinitionDetail) {
                return this.processDefinitionDetail;
            }
            return null;
        },
        applyDesignerInputs: async function () {
            var processDefinitionId = this.processDefinitionId
                || (this.processDefinitionDetail && this.processDefinitionDetail.processDefinitionId)
                || "";
            this.importedDeploymentId = this.deploymentId
                || (this.processDefinitionDetail && this.processDefinitionDetail.deploymentId)
                || "";
            this.importedModelId = this.modelId || "";
            if (!this.processDefinitionDetail) {
                this.importedProcessDefinitionId = processDefinitionId;
                this.syncFormBindRecordsFromProps();
                this.$nextTick(this.handleCenterCanvas);
                return;
            }
            this.importProcessDefinitionDetail(this.processDefinitionDetail, this.importedDeploymentId);
            this.applyProcessFormBindings(this.formBindings || []);
            this.importedProcessDefinitionId = processDefinitionId;
            this.syncFormBindRecordsFromProps();
            this.$nextTick(this.handleCenterCanvas);
        },
        syncFormBindRecordsFromProps: function () {
            this.formBindLoading = !!this.formLoading;
            this.formBindRecords = this.cloneDesignerData(this.formRecords || []);
            this.formBindTotal = Number(this.formTotal || this.formBindRecords.length || 0);
            this.$nextTick(this.syncFormBindTableSelection);
        },
        loadRouteProcessDefinitionIfNeeded: async function () {
            var processDefinitionId = this.processDefinitionId || "";
            var deploymentId = this.deploymentId || "";
            var modelId = this.modelId || "";
            if (!processDefinitionId) {
                this.importedProcessDefinitionId = "";
                this.importedDeploymentId = deploymentId;
                this.importedModelId = modelId;
                this.$nextTick(this.handleCenterCanvas);
                return;
            }
            if (this.importedProcessDefinitionId === processDefinitionId
                && this.importedDeploymentId === deploymentId
                && this.importedModelId === modelId) {
                this.$nextTick(this.handleCenterCanvas);
                return;
            }
            try {
                var detail = await this.fetchProcessDefinitionDetailForDesigner(processDefinitionId);
                if (!detail) {
                    this.importedProcessDefinitionId = processDefinitionId;
                    this.importedDeploymentId = deploymentId;
                    this.importedModelId = modelId;
                    this.$nextTick(this.handleCenterCanvas);
                    return;
                }
                this.importProcessDefinitionDetail(detail, deploymentId);
                await this.loadProcessFormBindings(processDefinitionId);
                this.importedProcessDefinitionId = processDefinitionId;
                this.importedDeploymentId = deploymentId;
                this.importedModelId = modelId;
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
            var propertyMap = this.buildImportedPropertiesMap(detail.bpmnXml);
            this.importedProcessDefinitionKey = detail.processDefinitionKey || "";
            this.importedProcessDefinitionName = detail.processDefinitionName || detail.processDefinitionKey || "";
            this.importedProcessDefinitionCategory = detail.category || "";
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
                detailNodes[nodeIndex].properties = propertyMap[detailNodes[nodeIndex].elementId] || detailNodes[nodeIndex].properties || {};
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
                    name: sequenceFlow.elementName || "",
                    conditionExpression: sequenceFlow.conditionExpression || ""
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
                defaultFlowId: detailNode && detailNode.defaultFlowId ? detailNode.defaultFlowId : "",
                properties: this.mergeNodeProperties(paletteNode.bpmnType, detailNode && detailNode.properties ? detailNode.properties : {}),
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
        buildImportedPropertiesMap: function (bpmnXml) {
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
            var result = {};
            this.collectImportedProperties(processElement, result);
            return result;
        },
        collectImportedProperties: function (containerElement, result) {
            var children = containerElement && containerElement.children ? containerElement.children : [];
            for (var index = 0; index < children.length; index += 1) {
                var child = children[index];
                var localName = child.localName || child.nodeName || "";
                var bpmnType = this.resolveImportedBpmnType(localName);
                if (!bpmnType) {
                    continue;
                }
                var elementId = child.getAttribute("id") || "";
                if (elementId) {
                    result[elementId] = this.extractImportedNodeProperties(child, bpmnType);
                }
                if (bpmnType === "subProcess") {
                    this.collectImportedProperties(child, result);
                }
            }
        },
        extractImportedNodeProperties: function (element, bpmnType) {
            var properties = this.buildDefaultNodeProperties(bpmnType);
            properties.initiator = this.getAnyAttribute(element, "initiator") || "";
            properties.formKey = this.getAnyAttribute(element, "formKey") || "";
            properties.boundFormKeys = this.parseFormKeyList(properties.formKey);
            properties.assignee = this.getAnyAttribute(element, "assignee") || "";
            properties.candidateUsers = this.getAnyAttribute(element, "candidateUsers") || "";
            properties.candidateGroups = this.getAnyAttribute(element, "candidateGroups") || "";
            properties.dueDate = this.getAnyAttribute(element, "dueDate") || "";
            properties.priority = this.getAnyAttribute(element, "priority") || "";
            properties.className = this.getAnyAttribute(element, "class") || "";
            properties.delegateExpression = this.getAnyAttribute(element, "delegateExpression") || "";
            properties.expression = this.getAnyAttribute(element, "expression") || "";
            properties.resultVariable = this.getAnyAttribute(element, "resultVariable") || "";
            properties.scriptFormat = element.getAttribute("scriptFormat") || properties.scriptFormat;
            properties.calledElement = element.getAttribute("calledElement") || "";
            properties.inheritVariables = this.getAnyAttribute(element, "inheritVariables") !== "false";
            properties.attachedToRef = element.getAttribute("attachedToRef") || "";
            properties.cancelActivity = element.getAttribute("cancelActivity") !== "false";
            properties.triggeredByEvent = element.getAttribute("triggeredByEvent") === "true";
            properties.async = this.getAnyAttribute(element, "async") === "true";
            properties.exclusive = this.getAnyAttribute(element, "exclusive") !== "false";
            properties.ruleVariablesInput = this.getAnyAttribute(element, "ruleVariablesInput") || "";
            properties.to = this.getAnyAttribute(element, "to") || "";
            properties.subject = this.getAnyAttribute(element, "subject") || "";
            properties.text = this.getAnyAttribute(element, "text") || "";
            var scriptElement = this.findFirstElementByLocalName(element, "script");
            if (scriptElement && scriptElement.textContent) {
                properties.script = scriptElement.textContent.trim();
            }
            var textElement = this.findFirstElementByLocalName(element, "text");
            if (textElement && textElement.textContent) {
                properties.text = textElement.textContent.trim();
            }
            this.fillImportedEventProperties(element, properties);
            this.fillImportedMultiInstanceProperties(element, properties);
            this.fillImportedFormBindingProperties(element, properties);
            return properties;
        },
        fillImportedFormBindingProperties: function (element, properties) {
            var propertyElements = this.findElementsByLocalName(element, "property");
            for (var index = 0; index < propertyElements.length; index += 1) {
                var item = propertyElements[index];
                var name = item.getAttribute("name") || "";
                var value = item.getAttribute("value") || "";
                if (name === "wcdk:boundFormKeys") {
                    properties.boundFormKeys = this.parseFormKeyList(value);
                    properties.formKey = properties.boundFormKeys.join(",");
                }
                if (name === "wcdk:boundForms" && value) {
                    try {
                        properties.boundForms = this.normalizeBoundForms(JSON.parse(value));
                    } catch (error) {
                        properties.boundForms = [];
                    }
                }
            }
            if (!properties.boundFormKeys.length) {
                properties.boundFormKeys = this.parseFormKeyList(properties.formKey);
            }
        },
        fillImportedEventProperties: function (element, properties) {
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
        fillImportedMultiInstanceProperties: function (element, properties) {
            var loop = this.findFirstElementByLocalName(element, "multiInstanceLoopCharacteristics");
            if (!loop) {
                return;
            }
            properties.multiInstanceEnabled = true;
            properties.multiInstanceSequential = loop.getAttribute("isSequential") === "true";
            properties.collection = this.getAnyAttribute(loop, "collection") || "";
            properties.elementVariable = this.getAnyAttribute(loop, "elementVariable") || "";
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
                EventBasedGateway: "eventGateway",
                eventBasedGateway: "eventGateway",
                TextAnnotation: "textAnnotation",
                textAnnotation: "textAnnotation"
            };
            return typeMap[elementType] || "";
        },
        findFirstElementByLocalName: function (root, localName) {
            var elements = this.findElementsByLocalName(root, localName);
            return elements && elements.length ? elements[0] : null;
        },
        findElementsByLocalName: function (root, localName) {
            if (!root) {
                return [];
            }
            var elements = root.getElementsByTagNameNS ? root.getElementsByTagNameNS("*", localName) : [];
            if (elements && elements.length) {
                return Array.prototype.slice.call(elements);
            }
            var allElements = root.getElementsByTagName ? root.getElementsByTagName("*") : [];
            var results = [];
            for (var index = 0; index < allElements.length; index += 1) {
                if (allElements[index].localName === localName || allElements[index].nodeName === localName) {
                    results.push(allElements[index]);
                }
            }
            return results;
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
        appendXmlAttribute: function (attributes, name, value) {
            if (value === null || typeof value === "undefined" || value === "") {
                return;
            }
            attributes.push(name + '="' + this.escapeXml(value) + '"');
        },
        buildNodeAttributePart: function (node, nodeIdMap) {
            var properties = this.ensureNodeProperties(node);
            var attributes = [];
            this.appendXmlAttribute(attributes, "name", node.name || node.label || node.code);
            if (properties.async) {
                this.appendXmlAttribute(attributes, "flowable:async", "true");
            }
            if (properties.async && properties.exclusive === false) {
                this.appendXmlAttribute(attributes, "flowable:exclusive", "false");
            }
            if (this.supportsFormKey(node)) {
                this.appendXmlAttribute(attributes, "flowable:formKey", properties.formKey);
            }
            if (node.bpmnType === "startEvent") {
                this.appendXmlAttribute(attributes, "flowable:initiator", properties.initiator);
            }
            if (node.bpmnType === "userTask") {
                this.appendXmlAttribute(attributes, "flowable:assignee", properties.assignee);
                this.appendXmlAttribute(attributes, "flowable:candidateUsers", properties.candidateUsers);
                this.appendXmlAttribute(attributes, "flowable:candidateGroups", properties.candidateGroups);
                this.appendXmlAttribute(attributes, "flowable:dueDate", properties.dueDate);
                this.appendXmlAttribute(attributes, "flowable:priority", properties.priority);
            }
            if (this.isTaskNode(node)) {
                this.appendXmlAttribute(attributes, "flowable:skipExpression", properties.skipExpression);
            }
            if (node.bpmnType === "serviceTask") {
                if (properties.implementationType === "delegateExpression") {
                    this.appendXmlAttribute(attributes, "flowable:delegateExpression", properties.delegateExpression);
                } else if (properties.implementationType === "expression") {
                    this.appendXmlAttribute(attributes, "flowable:expression", properties.expression);
                } else {
                    this.appendXmlAttribute(attributes, "flowable:class", properties.className);
                }
                this.appendXmlAttribute(attributes, "flowable:resultVariable", properties.resultVariable);
            }
            if (node.bpmnType === "scriptTask") {
                this.appendXmlAttribute(attributes, "scriptFormat", properties.scriptFormat);
                this.appendXmlAttribute(attributes, "flowable:resultVariable", properties.resultVariable);
            }
            if (node.bpmnType === "mailTask") {
                this.appendXmlAttribute(attributes, "flowable:type", "mail");
                this.appendXmlAttribute(attributes, "flowable:to", properties.to);
                this.appendXmlAttribute(attributes, "flowable:subject", properties.subject);
                this.appendXmlAttribute(attributes, "flowable:text", properties.text);
            }
            if (node.bpmnType === "businessRuleTask") {
                this.appendXmlAttribute(attributes, "flowable:ruleVariablesInput", properties.ruleVariablesInput);
                this.appendXmlAttribute(attributes, "flowable:resultVariable", properties.resultVariable);
            }
            if (node.bpmnType === "receiveTask") {
                this.appendXmlAttribute(attributes, "messageRef", properties.messageRef);
            }
            if (node.bpmnType === "callActivity") {
                this.appendXmlAttribute(attributes, "calledElement", properties.calledElement);
                if (properties.inheritVariables) {
                    this.appendXmlAttribute(attributes, "flowable:inheritVariables", "true");
                }
            }
            if (node.bpmnType === "boundaryEvent") {
                this.appendXmlAttribute(attributes, "attachedToRef", this.resolveReferencedBpmnId(properties.attachedToRef, nodeIdMap));
                this.appendXmlAttribute(attributes, "cancelActivity", properties.cancelActivity === false ? "false" : "true");
            }
            if (node.bpmnType === "subProcess" && properties.triggeredByEvent) {
                this.appendXmlAttribute(attributes, "triggeredByEvent", "true");
            }
            return attributes.length ? " " + attributes.join(" ") : "";
        },
        resolveReferencedBpmnId: function (value, nodeIdMap) {
            if (!value) {
                return "";
            }
            return nodeIdMap[value] || value;
        },
        appendNodeExtensionElements: function (lines, node) {
            var properties = this.ensureNodeProperties(node);
            var listenerLines = [];
            this.appendListenerLines(listenerLines, "flowable:executionListener", properties.executionListeners);
            if (node.bpmnType === "userTask") {
                this.appendListenerLines(listenerLines, "flowable:taskListener", properties.taskListeners);
                this.appendUserTaskFormBindingLines(listenerLines, properties);
            }
            if (!listenerLines.length) {
                return;
            }
            lines.push("      <bpmn:extensionElements>");
            for (var index = 0; index < listenerLines.length; index += 1) {
                lines.push(listenerLines[index]);
            }
            lines.push("      </bpmn:extensionElements>");
        },
        appendUserTaskFormBindingLines: function (lines, properties) {
            var boundFormKeys = this.parseFormKeyList(properties.boundFormKeys || properties.formKey);
            if (!boundFormKeys.length) {
                return;
            }
            var boundForms = this.normalizeBoundForms(properties.boundForms);
            lines.push("        <flowable:properties>");
            lines.push('          <flowable:property name="wcdk:boundFormKeys" value="' + this.escapeXml(boundFormKeys.join(",")) + '" />');
            if (boundForms.length) {
                lines.push('          <flowable:property name="wcdk:boundForms" value="' + this.escapeXml(JSON.stringify(boundForms)) + '" />');
            }
            lines.push("        </flowable:properties>");
        },
        appendListenerLines: function (lines, tagName, configText) {
            var rows = String(configText || "").split(/\r?\n/);
            for (var index = 0; index < rows.length; index += 1) {
                var row = rows[index].trim();
                if (!row) {
                    continue;
                }
                var parts = row.split(":");
                var eventName = parts.length > 1 ? parts.shift().trim() : "";
                var implementation = parts.join(":").trim();
                var attributes = [];
                this.appendXmlAttribute(attributes, "event", eventName);
                if (/^\$\{.*\}$/.test(implementation)) {
                    this.appendXmlAttribute(attributes, "expression", implementation);
                } else {
                    this.appendXmlAttribute(attributes, "class", implementation || row);
                }
                lines.push("        <" + tagName + " " + attributes.join(" ") + " />");
            }
        },
        appendEventDefinitionElements: function (lines, node) {
            var properties = this.ensureNodeProperties(node);
            if (properties.eventDefinitionType === "message") {
                lines.push('      <bpmn:messageEventDefinition messageRef="' + this.escapeXml(properties.messageRef || "") + '" />');
            } else if (properties.eventDefinitionType === "timer") {
                lines.push("      <bpmn:timerEventDefinition>");
                lines.push("        <bpmn:timeDuration>" + this.escapeXml(properties.timerDefinition || "") + "</bpmn:timeDuration>");
                lines.push("      </bpmn:timerEventDefinition>");
            } else if (properties.eventDefinitionType === "signal") {
                lines.push('      <bpmn:signalEventDefinition signalRef="' + this.escapeXml(properties.signalRef || "") + '" />');
            } else if (properties.eventDefinitionType === "error") {
                lines.push('      <bpmn:errorEventDefinition errorRef="' + this.escapeXml(properties.errorRef || "") + '" />');
            }
        },
        appendSpecialNodeContent: function (lines, node) {
            var properties = this.ensureNodeProperties(node);
            if (node.bpmnType === "scriptTask" && properties.script) {
                lines.push("      <bpmn:script>" + this.escapeXml(properties.script) + "</bpmn:script>");
            }
            if (node.bpmnType === "textAnnotation" && properties.text) {
                lines.push("      <bpmn:text>" + this.escapeXml(properties.text) + "</bpmn:text>");
            }
            this.appendEventDefinitionElements(lines, node);
            this.appendMultiInstanceLoopCharacteristics(lines, node);
        },
        appendMultiInstanceLoopCharacteristics: function (lines, node) {
            var properties = this.ensureNodeProperties(node);
            if (!this.supportsMultiInstance(node) || !properties.multiInstanceEnabled) {
                return;
            }
            var sequentialPart = properties.multiInstanceSequential ? ' isSequential="true"' : ' isSequential="false"';
            var collectionPart = properties.collection ? ' flowable:collection="' + this.escapeXml(properties.collection) + '"' : "";
            var elementPart = properties.elementVariable ? ' flowable:elementVariable="' + this.escapeXml(properties.elementVariable) + '"' : "";
            lines.push("      <bpmn:multiInstanceLoopCharacteristics" + sequentialPart + collectionPart + elementPart + ">");
            if (properties.completionCondition) {
                lines.push("        <bpmn:completionCondition>" + this.escapeXml(properties.completionCondition) + "</bpmn:completionCondition>");
            }
            lines.push("      </bpmn:multiInstanceLoopCharacteristics>");
        },
        appendContainerFlowElements: function (lines, containerId, nodeChildrenMap, flowChildrenMap, nodeTagMap, nodeIdMap, incomingMap, outgoingMap) {
            var childNodes = nodeChildrenMap[containerId || ""] || [];
            var containerFlows = flowChildrenMap[containerId || ""] || [];
            for (var nodeIndex = 0; nodeIndex < childNodes.length; nodeIndex += 1) {
                var node = childNodes[nodeIndex];
                var nodeTag = nodeTagMap[node.bpmnType] || "task";
                var defaultPart = this.resolveDefaultFlowAttribute(node, containerFlows);
                var attributePart = this.buildNodeAttributePart(node, nodeIdMap);
                lines.push('    <bpmn:' + nodeTag + ' id="' + nodeIdMap[node.id] + '"' + attributePart + defaultPart + '>');
                if (node.documentation) {
                    lines.push('      <bpmn:documentation>' + this.escapeXml(node.documentation) + '</bpmn:documentation>');
                }
                this.appendNodeExtensionElements(lines, node);
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
                this.appendSpecialNodeContent(lines, node);
                lines.push('    </bpmn:' + nodeTag + '>');
            }
            for (var flowIndex = 0; flowIndex < containerFlows.length; flowIndex += 1) {
                var flow = containerFlows[flowIndex];
                var namePart = flow.name ? ' name="' + this.escapeXml(flow.name) + '"' : "";
                if (flow.conditionExpression) {
                    lines.push('    <bpmn:sequenceFlow id="' + flow.id + '"' + namePart + ' sourceRef="' + nodeIdMap[flow.sourceId] + '" targetRef="' + nodeIdMap[flow.targetId] + '">');
                    lines.push('      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">' + this.escapeXml(flow.conditionExpression) + '</bpmn:conditionExpression>');
                    lines.push('    </bpmn:sequenceFlow>');
                } else {
                    lines.push('    <bpmn:sequenceFlow id="' + flow.id + '"' + namePart + ' sourceRef="' + nodeIdMap[flow.sourceId] + '" targetRef="' + nodeIdMap[flow.targetId] + '" />');
                }
            }
        },
        resolveDefaultFlowAttribute: function (node, containerFlows) {
            if (!this.isExclusiveGatewayNode(node) || !node.defaultFlowId) {
                return "";
            }
            for (var index = 0; index < containerFlows.length; index += 1) {
                var flow = containerFlows[index];
                if (flow.sourceId === node.id && flow.originalId === node.defaultFlowId) {
                    return ' default="' + flow.id + '"';
                }
            }
            return "";
        },
        buildBpmnXmlContent: function () {
            var collected = this.collectExportableNodes();
            var exportableNodes = collected.exportableNodes;
            var skippedNodes = collected.skippedNodes;
            var usedIds = {};
            var processId = this.sanitizeBpmnId(this.resolveDesignerModelKey(), "Process", usedIds);
            var processName = this.escapeXml(this.resolveDesignerModelName());
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
                var sourceNode = this.findNodeById(edge.sourceId);
                var isDefaultFlow = this.isExclusiveGatewayNode(sourceNode) && sourceNode.defaultFlowId === edge.id;
                sequenceFlows.push({
                    id: flowId,
                    sourceId: edge.sourceId,
                    targetId: edge.targetId,
                    name: edge.name || "",
                    conditionExpression: isDefaultFlow ? "" : edge.conditionExpression || "",
                    originalId: edge.id,
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
                '                  xmlns:flowable="http://flowable.org/bpmn"',
                '                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"',
                '                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"',
                '                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"',
                '                  id="' + definitionsId + '"',
                '                  targetNamespace="http://flowable.org/processdef">',
                '  <bpmn:process id="' + processId + '" name="' + processName + '" isExecutable="true">'
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
                    var bpmnResult = self.buildBpmnXmlContent();
                    self.downloadTextFile(self.buildExportFileName("bpmn"), bpmnResult.xml, "application/xml;charset=utf-8");
                    self.notifySkippedBpmnNodes(bpmnResult.skippedNodes || []);
                    self.$message.success("BPMN 文件已开始下载");
                    return;
                }
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
                    var bpmnXmlResult = self.buildBpmnXmlContent();
                    self.downloadTextFile(self.buildExportFileName("bpmn20.xml"), bpmnXmlResult.xml, "application/xml;charset=utf-8");
                    self.notifySkippedBpmnNodes(bpmnXmlResult.skippedNodes || []);
                    self.$message.success("BPMN.XML 文件已开始下载");
                    return;
                }
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
                    return self.renderPngBlob().then(function (blob) {
                        self.downloadBlobFile(self.buildExportFileName("png"), blob);
                        self.$message.success("PNG 文件已开始下载");
                    });
                }
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
                defaultFlowId: "",
                properties: this.buildDefaultNodeProperties(paletteNode.bpmnType),
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
            this.purgeInvalidDefaultFlowReferences();
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
                name: "",
                conditionExpression: ""
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
            this.purgeInvalidDefaultFlowReferences();
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
                this.purgeInvalidDefaultFlowReferences();
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
        hasEdgeById: function (edgeId) {
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                if (this.canvasEdges[index].id === edgeId) {
                    return true;
                }
            }
            return false;
        },
        purgeInvalidDefaultFlowReferences: function () {
            for (var index = 0; index < this.canvasNodes.length; index += 1) {
                var node = this.canvasNodes[index];
                if (this.isExclusiveGatewayNode(node) && node.defaultFlowId && !this.hasEdgeById(node.defaultFlowId)) {
                    node.defaultFlowId = "";
                }
            }
        },
        resolveNodeName: function (nodeId) {
            var node = this.findNodeById(nodeId);
            return node ? node.name : "-";
        },
        isExclusiveGatewayNode: function (node) {
            return !!node && node.bpmnType === "exclusiveGateway";
        },
        isExclusiveGatewayEdge: function (edge) {
            return !!edge && this.isExclusiveGatewayNode(this.findNodeById(edge.sourceId));
        },
        isExclusiveGatewayEdgeDefault: function (edge) {
            var sourceNode = edge ? this.findNodeById(edge.sourceId) : null;
            return this.isExclusiveGatewayNode(sourceNode) && sourceNode.defaultFlowId === edge.id;
        },
        resolveOutgoingEdgeOptions: function (nodeId) {
            var options = [];
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                var edge = this.canvasEdges[index];
                if (edge.sourceId !== nodeId) {
                    continue;
                }
                options.push({
                    id: edge.id,
                    label: this.resolveEdgeDisplayName(edge)
                });
            }
            return options;
        },
        resolveOutgoingEdges: function (nodeId) {
            var edges = [];
            for (var index = 0; index < this.canvasEdges.length; index += 1) {
                var edge = this.canvasEdges[index];
                if (edge.sourceId === nodeId) {
                    edges.push(edge);
                }
            }
            return edges;
        },
        resolveBranchConditionPlaceholder: function (edge) {
            if (this.isExclusiveGatewayEdgeDefault(edge)) {
                return "默认分支不需要填写条件表达式";
            }
            return "请输入 Flowable 条件表达式，例如 ${approved == 'return'}";
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
        resolveEdgeLabelPoint: function (edge) {
            var sourceNode = this.findNodeById(edge.sourceId);
            var targetNode = this.findNodeById(edge.targetId);
            if (!sourceNode || !targetNode) {
                return { x: 0, y: 0 };
            }
            var start = this.resolveNodeCenter(sourceNode, true);
            var end = this.resolveNodeCenter(targetNode, false);
            return {
                x: Math.round((start.x + end.x) / 2),
                y: Math.round((start.y + end.y) / 2 - 12)
            };
        },
        resolveEdgeLabelText: function (edge) {
            var text = String(edge && edge.name || "").trim();
            return text.length > 18 ? text.slice(0, 18) + "..." : text;
        },
        resolveEdgeLabelSize: function (edge) {
            var text = this.resolveEdgeLabelText(edge);
            return {
                width: Math.min(Math.max(text.length * 13 + 18, 42), 180)
            };
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
        await this.applyDesignerInputs();
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
                this.applyDesignerInputs();
            }
        },
        processDefinitionId: function () {
            this.applyDesignerInputs();
        },
        deploymentId: function () {
            this.applyDesignerInputs();
        },
        modelId: function () {
            this.applyDesignerInputs();
        },
        processDefinitionDetail: function () {
            this.applyDesignerInputs();
        },
        formBindings: function () {
            this.applyProcessFormBindings(this.formBindings || []);
        },
        formRecords: function () {
            this.syncFormBindRecordsFromProps();
        },
        formTotal: function () {
            this.syncFormBindRecordsFromProps();
        },
        formLoading: function () {
            this.syncFormBindRecordsFromProps();
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

if (window.Vue) {
    window.Vue.component("wcdk-process-designer", window.WcdkProcessDesigner);
}
