/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.DynamicProcessFormField = {
    name: "dynamic-process-form-field",
    props: {
        field: {
            type: Object,
            required: true
        },
        formValues: {
            type: Object,
            required: true
        },
        nested: {
            type: Boolean,
            default: false
        }
    },
    template: `
        <div class="dynamic-process-field" :class="{ 'dynamic-process-field-nested': nested }">
            <div v-if="isTableField(field)" class="dynamic-process-table">
                <div class="dynamic-process-table-title">{{ field.label }}</div>
                <div class="dynamic-process-table-grid" :style="resolveTableGridStyle(field)">
                    <div
                        v-for="cell in resolveTableCells(field)"
                        :key="field.fieldKey + '-' + cell.row + '-' + cell.column"
                        class="dynamic-process-table-cell">
                        <dynamic-process-form-field
                            v-for="child in cell.fields || []"
                            :key="child.fieldKey"
                            :field="child"
                            :form-values="formValues"
                            :nested="true">
                        </dynamic-process-form-field>
                    </div>
                </div>
                <div class="helper-text" v-if="field.sourceNodeName">
                    来源节点：{{ field.sourceNodeName }}
                </div>
            </div>
            <el-form-item v-else :label="field.label">
                <el-input
                    v-if="isInputField(field)"
                    v-model.trim="formValues[field.fieldKey]"
                    :placeholder="field.placeholder"
                    :readonly="field.readOnly">
                </el-input>
                <el-input
                    v-else-if="isNumberField(field)"
                    v-model.number="formValues[field.fieldKey]"
                    type="number"
                    :placeholder="field.placeholder"
                    :readonly="field.readOnly">
                </el-input>
                <el-input
                    v-else-if="isTextareaField(field)"
                    v-model="formValues[field.fieldKey]"
                    type="textarea"
                    :rows="field.rows || 4"
                    :placeholder="field.placeholder"
                    :readonly="field.readOnly">
                </el-input>
                <el-select
                    v-else-if="isSelectField(field)"
                    v-model="formValues[field.fieldKey]"
                    :placeholder="field.placeholder"
                    :disabled="field.readOnly"
                    style="width: 100%;">
                    <el-option
                        v-for="option in field.options || []"
                        :key="field.fieldKey + '-' + option.value"
                        :label="option.label"
                        :value="option.value">
                    </el-option>
                </el-select>
                <el-checkbox
                    v-else-if="isCheckboxField(field)"
                    v-model="formValues[field.fieldKey]"
                    :disabled="field.readOnly">
                    {{ field.placeholder || field.label }}
                </el-checkbox>
                <el-switch
                    v-else-if="isSwitchField(field)"
                    v-model="formValues[field.fieldKey]"
                    active-text="启用"
                    inactive-text="关闭"
                    :active-value="true"
                    :inactive-value="false"
                    :disabled="field.readOnly">
                </el-switch>
                <el-date-picker
                    v-else-if="isDateField(field)"
                    v-model="formValues[field.fieldKey]"
                    type="date"
                    value-format="yyyy-MM-dd"
                    :placeholder="field.placeholder"
                    :disabled="field.readOnly"
                    style="width: 100%;">
                </el-date-picker>
                <div v-else-if="isTextField(field)" class="helper-text">
                    {{ field.defaultValue || field.placeholder || field.label }}
                </div>
                <el-input
                    v-else
                    v-model="formValues[field.fieldKey]"
                    :placeholder="field.placeholder"
                    :readonly="field.readOnly">
                </el-input>
                <div class="helper-text" v-if="field.sourceNodeName">
                    来源节点：{{ field.sourceNodeName }}
                    <span v-if="field.required"> | 必填</span>
                </div>
            </el-form-item>
        </div>
    `,
    methods: {
        isTableField: function (field) {
            return field.componentType === "table" || field.type === "table";
        },
        isInputField: function (field) {
            return !field.componentType || field.componentType === "input";
        },
        isTextareaField: function (field) {
            return field.componentType === "textarea";
        },
        isNumberField: function (field) {
            return field.componentType === "number";
        },
        isSelectField: function (field) {
            return field.componentType === "select" || field.componentType === "radio";
        },
        isCheckboxField: function (field) {
            return field.componentType === "checkbox";
        },
        isSwitchField: function (field) {
            return field.componentType === "switch";
        },
        isDateField: function (field) {
            return field.componentType === "date";
        },
        isTextField: function (field) {
            return field.componentType === "text";
        },
        resolveTableCells: function (field) {
            var rows = Math.max(1, field.tableRows || 1);
            var columns = Math.max(1, field.tableColumns || 1);
            var sourceCells = field.children || [];
            var cells = [];
            for (var rowIndex = 0; rowIndex < rows; rowIndex += 1) {
                for (var columnIndex = 0; columnIndex < columns; columnIndex += 1) {
                    cells.push(this.findTableCell(sourceCells, rowIndex, columnIndex) || {
                        row: rowIndex,
                        column: columnIndex,
                        fields: []
                    });
                }
            }
            return cells;
        },
        findTableCell: function (cells, row, column) {
            for (var index = 0; index < cells.length; index += 1) {
                if (cells[index] && cells[index].row === row && cells[index].column === column) {
                    return cells[index];
                }
            }
            return null;
        },
        resolveTableGridStyle: function (field) {
            var columns = Math.max(1, field.tableColumns || 1);
            return {
                gridTemplateColumns: "repeat(" + columns + ", minmax(160px, 1fr))"
            };
        }
    }
};

if (window && window.Vue && typeof window.Vue.component === "function") {
    window.Vue.component("dynamic-process-form-field", window.DynamicProcessFormField);
}

window.ProcessCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">流程定义驱动页面</div>
                        <h2>流程中心</h2>
                    </div>
                    <el-button v-if="$root.hasButton('process:refresh')" @click="handleRefresh">刷新</el-button>
                </div>

                <el-tabs v-model="activeTab" class="process-center-tabs">
                    <el-tab-pane v-if="$root.hasTab('process:tab:create')" label="创建流程" name="create">
                        <div class="process-builder-grid">
                            <div class="process-builder-form">
                                <el-form label-position="top" @submit.native.prevent="submitByAction(primaryActionButton)">
                                    <div class="form-grid two-columns">
                                        <el-form-item label="流程定义">
                                            <el-select
                                                v-model="form.selectedDefinitionId"
                                                placeholder="请选择流程定义"
                                                filterable
                                                style="width: 100%;"
                                                @change="handleDefinitionChange">
                                                <el-option
                                                    v-for="definition in availableDefinitions"
                                                    :key="definition.processDefinitionId"
                                                    :label="resolveDefinitionLabel(definition)"
                                                    :value="definition.processDefinitionId">
                                                </el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="流程分类">
                                            <el-input :value="resolveDefinitionCategoryById(form.selectedDefinitionId)" disabled></el-input>
                                        </el-form-item>
                                        <el-form-item label="流程标识">
                                            <el-input :value="form.processDefinitionKey" disabled></el-input>
                                        </el-form-item>
                                        <el-form-item label="任务名称">
                                            <el-input v-model.trim="form.taskName" placeholder="请输入任务名称"></el-input>
                                        </el-form-item>
                                    </div>

                                    <div v-if="dynamicFormFields.length" class="form-grid two-columns dynamic-process-form-grid">
                                        <dynamic-process-form-field
                                            v-for="field in dynamicFormFields"
                                            :key="field.fieldKey"
                                            :field="field"
                                            :form-values="formValues">
                                        </dynamic-process-form-field>
                                    </div>

                                    <div class="empty-panel process-schema-empty" v-else>
                                        当前流程未解析到可渲染的动态表单字段，请先在流程定义中配置表单元数据。
                                    </div>

                                    <div class="process-action-bar">
                                        <el-button
                                            v-for="button in visibleActionButtons"
                                            :key="button.actionKey"
                                            :type="button.buttonType === 'default' ? '' : button.buttonType"
                                            @click="submitByAction(button)">
                                            {{ button.label }}
                                        </el-button>
                                    </div>
                                </el-form>
                            </div>

                            <div class="process-builder-preview">
                                <div class="process-detail-shell" v-if="selectedDetail">
                                    <div class="process-detail-head">
                                        <div>
                                            <div class="section-kicker">流程图动态结构</div>
                                            <h3>{{ selectedDetail.processDefinitionName || selectedDetail.processDefinitionKey }}</h3>
                                        </div>
                                        <el-tag size="small" effect="plain">第{{ selectedDetail.version || 1 }}版</el-tag>
                                    </div>

                                    <div class="process-stat-grid">
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">部署</span>
                                            <strong>{{ selectedDetail.deploymentName || "-" }}</strong>
                                        </div>
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">节点数</span>
                                            <strong>{{ selectedDetail.nodeCount || 0 }}</strong>
                                        </div>
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">用户任务</span>
                                            <strong>{{ selectedDetail.userTaskCount || 0 }}</strong>
                                        </div>
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">连线数</span>
                                            <strong>{{ selectedDetail.sequenceFlowCount || 0 }}</strong>
                                        </div>
                                    </div>

                                    <div class="process-meta-list">
                                        <span class="mini-tag">流程分类：{{ selectedDetail.category || "未分类" }}</span>
                                        <span class="mini-tag">流程标识：{{ selectedDetail.processDefinitionKey }}</span>
                                        <span class="mini-tag">资源名称：{{ selectedDetail.resourceName || "-" }}</span>
                                    </div>

                                    <div class="process-schema-panel" v-if="dynamicFormFields.length || visibleActionButtons.length">
                                        <div class="schema-chip-list" v-if="dynamicFormFields.length">
                                            <span
                                                v-for="field in dynamicFormFields"
                                                :key="'chip-' + field.fieldKey"
                                                class="schema-chip">
                                                {{ field.label }} / {{ resolveFieldTypeLabel(field) }}
                                            </span>
                                        </div>
                                        <div class="schema-chip-list" v-if="visibleActionButtons.length">
                                            <span
                                                v-for="button in visibleActionButtons"
                                                :key="'button-' + button.actionKey"
                                                class="schema-chip schema-chip-action">
                                                {{ button.label }}
                                            </span>
                                        </div>
                                    </div>

                                    <process-diagram ref="definitionDiagram" :detail="selectedDetail"></process-diagram>

                                    <div class="process-stage-list" v-if="orderedNodes.length">
                                        <div
                                            v-for="node in orderedNodes"
                                            :key="node.elementId"
                                            class="process-stage-card">
                                            <div class="process-stage-index">{{ resolveNodeIndex(node.elementId, orderedNodes) }}</div>
                                            <div class="process-stage-main">
                                                <div class="process-stage-title">{{ node.elementName || node.elementId }}</div>
                                                <div class="process-stage-meta">
                                                    <el-tag size="mini" :type="resolveNodeTagType(node.elementType)" effect="plain">
                                                        {{ resolveNodeTypeLabel(node.elementType) }}
                                                    </el-tag>
                                                    <span>入口 {{ node.incomingCount || 0 }} / 出口 {{ node.outgoingCount || 0 }}</span>
                                                </div>
                                                <div class="helper-text" v-if="node.documentation">{{ node.documentation }}</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="helper-text" v-if="sequenceFlowSummaries.length">
                                        当前路径：{{ sequenceFlowSummaries.join(" → ") }}
                                    </div>
                                </div>
                                <div class="empty-panel" v-else>
                                    请选择流程定义，以便根据流程图渲染表单字段和操作按钮。
                                </div>
                            </div>
                        </div>

                        <el-dialog v-if="false"
                            title="流程详情"
                            :visible.sync="processDetailDialogVisible"
                            width="960px"
                            top="5vh"
                            @closed="handleProcessDetailDialogClosed">
                            <div class="process-detail-shell" v-if="selectedProcessListDetail && selectedProcessRow">
                                <div class="process-detail-head">
                                    <div>
                                        <div class="section-kicker">流程实例图谱</div>
                                        <h3>{{ selectedProcessRow.taskName || selectedProcessRow.businessTitle || selectedProcessRow.processNo }}</h3>
                                    </div>
                                    <el-tag size="small" :type="resolveStatusType(selectedProcessRow.status)" effect="plain">
                                        {{ resolveStatusLabel(selectedProcessRow.status) }}
                                    </el-tag>
                                </div>

                                <div class="process-stat-grid">
                                    <div class="process-stat-card">
                                        <span class="process-stat-label">流程编号</span>
                                        <strong>{{ selectedProcessRow.processNo || "-" }}</strong>
                                    </div>
                                    <div class="process-stat-card">
                                        <span class="process-stat-label">当前任务</span>
                                        <strong>{{ selectedProcessRow.currentTaskName || "无" }}</strong>
                                    </div>
                                    <div class="process-stat-card">
                                        <span class="process-stat-label">节点数</span>
                                        <strong>{{ selectedProcessListDetail.nodeCount || 0 }}</strong>
                                    </div>
                                    <div class="process-stat-card">
                                        <span class="process-stat-label">激活节点</span>
                                        <strong>{{ activeProcessNodeIds.length }}</strong>
                                    </div>
                                </div>

                                <div class="process-meta-list">
                                    <span class="mini-tag">流程定义：{{ selectedProcessListDetail.processDefinitionName || selectedProcessListDetail.processDefinitionKey }}</span>
                                    <span class="mini-tag">流程分类：{{ selectedProcessListDetail.category || "未分类" }}</span>
                                    <span class="mini-tag">发起人：{{ selectedProcessRow.starter || "-" }}</span>
                                    <span class="mini-tag">创建时间：{{ formatDateTime(selectedProcessRow.createTime) }}</span>
                                    <span class="mini-tag">更新时间：{{ formatDateTime(selectedProcessRow.updateTime) }}</span>
                                </div>

                                <div class="helper-text" v-if="selectedProcessRow.businessTitle">
                                    标题：{{ selectedProcessRow.businessTitle }}
                                </div>

                                <div class="process-stage-list" v-if="orderedProcessNodes.length">
                                    <div
                                        v-for="node in orderedProcessNodes"
                                        :key="node.elementId"
                                        :class="['process-stage-card', { 'is-active': isActiveProcessNode(node.elementId) }]">
                                        <div class="process-stage-index">{{ resolveNodeIndex(node.elementId, orderedProcessNodes) }}</div>
                                        <div class="process-stage-main">
                                            <div class="process-stage-title">
                                                {{ node.elementName || node.elementId }}
                                                <el-tag v-if="isActiveProcessNode(node.elementId)" size="mini" type="success" effect="dark">当前步骤</el-tag>
                                            </div>
                                            <div class="process-stage-meta">
                                                <el-tag size="mini" :type="resolveNodeTagType(node.elementType)" effect="plain">
                                                    {{ resolveNodeTypeLabel(node.elementType) }}
                                                </el-tag>
                                                <span>入口 {{ node.incomingCount || 0 }} / 出口 {{ node.outgoingCount || 0 }}</span>
                                            </div>
                                            <div class="helper-text" v-if="node.documentation">{{ node.documentation }}</div>
                                        </div>
                                    </div>
                                </div>

                                <div class="helper-text" v-if="processSequenceFlowSummaries.length">
                                    当前路径：{{ processSequenceFlowSummaries.join(" → ") }}
                                </div>
                            </div>
                            <div class="empty-panel" v-if="!selectedProcessListDetail">
                                未加载到流程详情，请稍后重试。
                            </div>
                        </el-dialog>
                    </el-tab-pane>

                    <el-tab-pane v-if="$root.hasTab('process:tab:list')" label="流程列表" name="list">
                    <!--class="process-builder-grid"-->
                        <div >
                            <div class="process-list-panel">
                                <el-form inline class="process-filter-form" @submit.native.prevent="handleQuery">
                                    <el-form-item label="编号">
                                        <el-input v-model.trim="$root.processFilters.processNo" placeholder="请输入流程编号"></el-input>
                                    </el-form-item>
                                    <el-form-item label="发起人">
                                        <el-input v-model.trim="$root.processFilters.starter" placeholder="请输入发起人"></el-input>
                                    </el-form-item>
                                    <el-form-item label="标题">
                                        <el-input v-model.trim="$root.processFilters.businessTitle" placeholder="请输入标题"></el-input>
                                    </el-form-item>
                                    <el-form-item label="流程分类">
                                        <el-select v-model="$root.processFilters.category" clearable filterable placeholder="请选择流程分类">
                                            <el-option
                                                v-for="item in availableCategories"
                                                :key="'category-' + item"
                                                :label="item"
                                                :value="item">
                                            </el-option>
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="流程定义">
                                        <el-select v-model="$root.processFilters.processDefinitionKey" clearable filterable placeholder="请选择流程定义">
                                            <el-option
                                                v-for="definition in availableDefinitions"
                                                :key="'filter-' + definition.processDefinitionId"
                                                :label="resolveDefinitionLabel(definition)"
                                                :value="definition.processDefinitionKey">
                                            </el-option>
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="状态">
                                        <el-select v-model="$root.processFilters.status" clearable placeholder="请选择状态">
                                            <el-option
                                                v-for="item in processStatusOptions"
                                                :key="item.value"
                                                :label="item.label"
                                                :value="item.value">
                                            </el-option>
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item>
                                        <el-button type="primary" @click="handleQuery">查询</el-button>
                                        <el-button @click="handleResetQuery">重置</el-button>
                                    </el-form-item>
                                </el-form>

                                <div class="helper-text process-table-tip">点击“查看”按钮，在弹框中查看流程图。</div>

                                <el-table
                                    :data="processTableData"
                                    stripe
                                    highlight-current-row
                                    :row-class-name="resolveProcessRowClass"
                                    @row-click="handleProcessRowClick">
                                    <el-table-column prop="processNo" label="编号" min-width="160"></el-table-column>
                                    <el-table-column prop="starter" label="发起人" min-width="120">
                                        <template slot-scope="scope">
                                            {{ scope.row.starter || "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="taskName" label="任务名称" min-width="160">
                                        <template slot-scope="scope">
                                            {{ scope.row.taskName || "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="businessTitle" label="标题" min-width="180">
                                        <template slot-scope="scope">
                                            {{ scope.row.businessTitle || "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="流程定义" min-width="180">
                                        <template slot-scope="scope">
                                            {{ resolveDefinitionName(scope.row.processDefinitionKey) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="流程分类" min-width="140">
                                        <template slot-scope="scope">
                                            {{ resolveDefinitionCategory(scope.row.processDefinitionKey) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="状态" min-width="120">
                                        <template slot-scope="scope">
                                            <el-tag :type="resolveStatusType(scope.row.status)" effect="plain">
                                                {{ resolveStatusLabel(scope.row.status) }}
                                            </el-tag>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="currentTaskName" label="当前任务" min-width="140">
                                        <template slot-scope="scope">
                                            {{ scope.row.currentTaskName || "-" }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="操作" min-width="180" fixed="right">
                                        <template slot-scope="scope">
                                            <div class="table-operations">
                                                <el-button
                                                    v-if="$root.hasButton('process:view')"
                                                    type="text"
                                                    @click.stop="openProcessDetail(scope.row)">
                                                    查看
                                                </el-button>
                                                <el-button
                                                    v-if="scope.row.status === 'DRAFT' && $root.hasButton('process:submit')"
                                                    type="text"
                                                    @click.stop="submitDraft(scope.row.id)">
                                                    提交
                                                </el-button>
                                                <el-button
                                                    v-if="$root.hasButton('process:delete')"
                                                    type="text"
                                                    style="color: #f56c6c;"
                                                    @click.stop="handleDelete(scope.row.id)">
                                                    删除
                                                </el-button>
                                            </div>
                                        </template>
                                    </el-table-column>
                                </el-table>

                                <div class="process-pagination">
                                    <el-pagination
                                        background
                                        layout="total, sizes, prev, pager, next"
                                        :current-page="$root.processPageNum"
                                        :page-size="$root.processPageSize"
                                        :page-sizes="[10, 20, 50, 100]"
                                        :total="$root.processTotal"
                                        @current-change="handleProcessPageChange"
                                        @size-change="handleProcessSizeChange">
                                    </el-pagination>
                                </div>
                            </div>

                            <div v-if="false" class="process-builder-preview">
                                <div class="process-detail-shell" v-if="selectedProcessListDetail">
                                    <div class="process-detail-head">
                                        <div>
                                            <div class="section-kicker">流程实例图谱</div>
                                            <h3>{{ selectedProcessRow ? (selectedProcessRow.taskName || selectedProcessRow.businessTitle || selectedProcessRow.processNo) : "流程详情" }}</h3>
                                        </div>
                                        <el-tag size="small" :type="resolveStatusType(selectedProcessRow && selectedProcessRow.status)" effect="plain">
                                            {{ resolveStatusLabel(selectedProcessRow && selectedProcessRow.status) }}
                                        </el-tag>
                                    </div>

                                    <div class="process-stat-grid">
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">流程编号</span>
                                            <strong>{{ selectedProcessRow ? selectedProcessRow.processNo : "-" }}</strong>
                                        </div>
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">当前任务</span>
                                            <strong>{{ selectedProcessRow && selectedProcessRow.currentTaskName ? selectedProcessRow.currentTaskName : "无" }}</strong>
                                        </div>
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">节点数</span>
                                            <strong>{{ selectedProcessListDetail.nodeCount || 0 }}</strong>
                                        </div>
                                        <div class="process-stat-card">
                                            <span class="process-stat-label">激活节点</span>
                                            <strong>{{ activeProcessNodeIds.length }}</strong>
                                        </div>
                                    </div>

                                    <div class="process-meta-list">
                                        <span class="mini-tag">流程定义：{{ selectedProcessListDetail.processDefinitionName || selectedProcessListDetail.processDefinitionKey }}</span>
                                        <span class="mini-tag">流程分类：{{ selectedProcessListDetail.category || "未分类" }}</span>
                                        <span class="mini-tag">发起人：{{ selectedProcessRow ? (selectedProcessRow.starter || "-") : "-" }}</span>
                                    </div>

                                    <div class="helper-text" v-if="selectedProcessRow && selectedProcessRow.businessTitle">
                                        标题：{{ selectedProcessRow.businessTitle }}
                                    </div>

                                    <div class="process-stage-list" v-if="orderedProcessNodes.length">
                                        <div
                                            v-for="node in orderedProcessNodes"
                                            :key="node.elementId"
                                            :class="['process-stage-card', { 'is-active': isActiveProcessNode(node.elementId) }]">
                                            <div class="process-stage-index">{{ resolveNodeIndex(node.elementId, orderedProcessNodes) }}</div>
                                            <div class="process-stage-main">
                                                <div class="process-stage-title">
                                                    {{ node.elementName || node.elementId }}
                                                    <el-tag v-if="isActiveProcessNode(node.elementId)" size="mini" type="success" effect="dark">当前步骤</el-tag>
                                                </div>
                                                <div class="process-stage-meta">
                                                    <el-tag size="mini" :type="resolveNodeTagType(node.elementType)" effect="plain">
                                                        {{ resolveNodeTypeLabel(node.elementType) }}
                                                    </el-tag>
                                                    <span>入口 {{ node.incomingCount || 0 }} / 出口 {{ node.outgoingCount || 0 }}</span>
                                                </div>
                                                <div class="helper-text" v-if="node.documentation">{{ node.documentation }}</div>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="helper-text" v-if="processSequenceFlowSummaries.length">
                                        当前路径：{{ processSequenceFlowSummaries.join(" → ") }}
                                    </div>
                                </div>
                                <div class="empty-panel" v-else>
                                    点击左侧流程列表项后，这里会展示流程图和当前步骤节点。
                                </div>
                            </div>
                        </div>

                        <el-dialog
                            title="流程图"
                            :visible.sync="processDetailDialogVisible"
                            width="2000px"
                            top="5vh"
                            @opened="renderProcessCanvas"
                            @closed="handleProcessDetailDialogClosed">
                            <process-diagram ref="processDiagram" :detail="selectedProcessListDetail" :active-node-ids="activeProcessNodeIds"></process-diagram>
<!--                            <div class="empty-panel" v-else>-->
<!--                                未加载到流程图数据，请稍后重试。-->
<!--                            </div>-->
                        </el-dialog>
                    </el-tab-pane>
                </el-tabs>
            </section>
        </section>
    `,
    data: function () {
        return {
            activeTab: "create",
            processStatusOptions: [
                { value: "DRAFT", label: "草稿" },
                { value: "PROCESSING", label: "审批中" },
                { value: "APPROVED", label: "已通过" },
                { value: "REJECTED", label: "已驳回" },
                { value: "CANCELLED", label: "已取消" },
                { value: "TERMINATED", label: "已终止" },
                { value: "COMPLETED", label: "已完成" }
            ],
            form: {
                selectedDefinitionId: "",
                processDefinitionKey: "",
                taskName: ""
            },
            formValues: {},
            selectedProcessId: null,
            processDetailDialogVisible: false
        };
    },
    computed: {
        availableDefinitions: function () {
            return (this.$root.processDefinitions || []).filter(function (definition) {
                return !definition.suspended;
            });
        },
        availableCategories: function () {
            var categoryMap = {};
            for (var i = 0; i < this.availableDefinitions.length; i += 1) {
                var category = (this.availableDefinitions[i].category || "").trim();
                if (category) {
                    categoryMap[category] = true;
                }
            }
            return Object.keys(categoryMap).sort();
        },
        selectedDetail: function () {
            return this.$root.selectedProcessDefinitionDetail;
        },
        processTableData: function () {
            if (Array.isArray(this.$root.processList)) {
                return this.$root.processList;
            }
            if (this.$root.processList && Array.isArray(this.$root.processList.records)) {
                return this.$root.processList.records;
            }
            return [];
        },
        dynamicFormFields: function () {
            return this.resolveSortedFields(this.selectedDetail);
        },
        actionButtons: function () {
            return this.resolveSortedButtons(this.selectedDetail);
        },
        visibleActionButtons: function () {
            var self = this;
            return this.actionButtons.filter(function (button) {
                return self.$root.hasButton("process:action:" + button.actionKey);
            });
        },
        primaryActionButton: function () {
            return this.visibleActionButtons.length ? this.visibleActionButtons[0] : null;
        },
        orderedNodes: function () {
            return this.resolveOrderedNodes(this.selectedDetail);
        },
        sequenceFlowSummaries: function () {
            return this.resolveSequenceFlowSummaries(this.selectedDetail, this.orderedNodes);
        },
        selectedProcessRow: function () {
            var rows = this.processTableData;
            for (var i = 0; i < rows.length; i += 1) {
                if (rows[i].id === this.selectedProcessId) {
                    return rows[i];
                }
            }
            return null;
        },
        selectedProcessListDetail: function () {
            return this.$root.selectedProcessRequestDiagramDetail;
        },
        orderedProcessNodes: function () {
            return this.resolveOrderedNodes(this.selectedProcessListDetail);
        },
        activeProcessNodeIds: function () {
            var detail = this.selectedProcessListDetail;
            return detail && Array.isArray(detail.activeNodeIds) ? detail.activeNodeIds : [];
        },
        processSequenceFlowSummaries: function () {
            return this.resolveSequenceFlowSummaries(this.selectedProcessListDetail, this.orderedProcessNodes);
        }
    },
    methods: {
        resolveStatusType: window.AppService.resolveStatusType,
        resolveStatusLabel: window.AppService.resolveStatusLabel,
        findDefinitionById: function (processDefinitionId) {
            var definitions = this.$root.processDefinitions || [];
            for (var i = 0; i < definitions.length; i += 1) {
                if (definitions[i].processDefinitionId === processDefinitionId) {
                    return definitions[i];
                }
            }
            return null;
        },
        handleDefinitionChange: async function (processDefinitionId) {
            var selectedDefinition = this.findDefinitionById(processDefinitionId);
            this.form.processDefinitionKey = selectedDefinition ? selectedDefinition.processDefinitionKey : "";
            if (!processDefinitionId) {
                this.$root.selectedProcessDefinitionDetail = null;
                this.formValues = {};
                return;
            }
            try {
                await this.$root.loadProcessDefinitionDetail(processDefinitionId);
                this.applyDynamicDefaults();
                this.$nextTick(this.renderDefinitionCanvas);
            } catch (error) {
                this.$root.showError(error.message || "加载流程图失败");
            }
        },
        applyRouteDefinitionSelection: async function () {
            var query = this.$route && this.$route.query ? this.$route.query : {};
            var processDefinitionId = query.processDefinitionId || "";
            if (!processDefinitionId) {
                return false;
            }
            var selectedDefinition = this.findDefinitionById(processDefinitionId);
            if (!selectedDefinition) {
                this.$root.showError("未查询到跳转指定的流程定义");
                return false;
            }
            this.activeTab = "create";
            this.form.selectedDefinitionId = processDefinitionId;
            this.form.processDefinitionKey = selectedDefinition.processDefinitionKey || "";
            await this.handleDefinitionChange(processDefinitionId);
            return true;
        },
        handleProcessRowClick: function (row) {
            this.selectedProcessId = row.id;
        },
        openProcessDetail: async function (row) {
            this.selectedProcessId = row.id;
            try {
                await this.$root.loadProcessRequestDiagramDetail(row.id);
                this.processDetailDialogVisible = true;
                this.$nextTick(this.renderProcessCanvas);
            } catch (error) {
                this.$root.showError(error.message || "加载流程图失败");
            }
        },
        handleProcessDetailDialogClosed: function () {
            this.$root.selectedProcessRequestDiagramDetail = null;
        },
        renderDefinitionCanvas: function () {
            if (this.$refs.definitionDiagram && typeof this.$refs.definitionDiagram.renderCanvas === 'function') {
                this.$refs.definitionDiagram.renderCanvas();
                return;
            }
            this.renderDiagramCanvas(this.$refs.definitionProcessCanvas, this.selectedDetail, []);
        },
        renderProcessCanvas: function () {
            if (this.$refs.processDiagram && typeof this.$refs.processDiagram.renderCanvas === 'function') {
                this.$refs.processDiagram.renderCanvas();
                return;
            }
            this.renderDiagramCanvas(this.$refs.processCanvas, this.selectedProcessListDetail, this.activeProcessNodeIds);
        },
        renderDiagramCanvas: function (canvas, detail, activeNodeIds) {
            if (!canvas || !detail || !Array.isArray(detail.nodes) || !detail.nodes.length) {
                return;
            }
            var nodes = detail.nodes.slice();
            var padding = 40;
            var minX = Number.MAX_SAFE_INTEGER;
            var minY = Number.MAX_SAFE_INTEGER;
            var maxX = 0;
            var maxY = 0;
            for (var i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                var nodeX = typeof node.x === "number" ? node.x : 0;
                var nodeY = typeof node.y === "number" ? node.y : 0;
                var nodeWidth = typeof node.width === "number" ? node.width : 120;
                var nodeHeight = typeof node.height === "number" ? node.height : 60;
                minX = Math.min(minX, nodeX);
                minY = Math.min(minY, nodeY);
                maxX = Math.max(maxX, nodeX + nodeWidth);
                maxY = Math.max(maxY, nodeY + nodeHeight);
            }
            var logicalWidth = Math.max(600, Math.ceil(maxX - minX + padding * 2));
            var logicalHeight = Math.max(320, Math.ceil(maxY - minY + padding * 2));
            var pixelRatio = window.devicePixelRatio || 1;
            canvas.width = logicalWidth * pixelRatio;
            canvas.height = logicalHeight * pixelRatio;
            canvas.style.height = logicalHeight + "px";
            var context = canvas.getContext("2d");
            context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
            context.clearRect(0, 0, logicalWidth, logicalHeight);
            context.fillStyle = "#f8fafc";
            context.fillRect(0, 0, logicalWidth, logicalHeight);
            this.drawProcessEdges(context, detail.sequenceFlows || [], nodes, minX, minY, padding);
            this.drawProcessNodes(context, nodes, minX, minY, padding, activeNodeIds || []);
        },
        drawProcessEdges: function (context, sequenceFlows, nodes, minX, minY, padding) {
            if (!sequenceFlows || !sequenceFlows.length) {
                return;
            }
            var nodeMap = {};
            for (var i = 0; i < nodes.length; i += 1) {
                nodeMap[nodes[i].elementId] = nodes[i];
            }
            context.save();
            context.strokeStyle = "#94a3b8";
            context.fillStyle = "#94a3b8";
            context.lineWidth = 2;
            for (var j = 0; j < sequenceFlows.length; j += 1) {
                var edge = sequenceFlows[j];
                var sourceNode = nodeMap[edge.sourceRef];
                var targetNode = nodeMap[edge.targetRef];
                if (!sourceNode || !targetNode) {
                    continue;
                }
                if (Array.isArray(edge.waypoints) && edge.waypoints.length >= 2) {
                    var waypoints = this.normalizeEdgeWaypoints(edge.waypoints, minX, minY, padding);
                    this.drawEdgePathByWaypoints(context, waypoints);
                    var lastPoint = waypoints[waypoints.length - 1];
                    this.drawArrowHead(context, lastPoint.x, lastPoint.y);
                } else {
                    var startX = (sourceNode.x || 0) - minX + padding + (sourceNode.width || 120);
                    var startY = (sourceNode.y || 0) - minY + padding + (sourceNode.height || 60) / 2;
                    var endX = (targetNode.x || 0) - minX + padding;
                    var endY = (targetNode.y || 0) - minY + padding + (targetNode.height || 60) / 2;
                    var midX = startX + (endX - startX) / 2;
                    context.beginPath();
                    context.moveTo(startX, startY);
                    context.lineTo(midX, startY);
                    context.lineTo(midX, endY);
                    context.lineTo(endX, endY);
                    context.stroke();
                    this.drawArrowHead(context, endX, endY);
                }
            }
            context.restore();
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
        drawArrowHead: function (context, x, y) {
            context.beginPath();
            context.moveTo(x, y);
            context.lineTo(x - 10, y - 5);
            context.lineTo(x - 10, y + 5);
            context.closePath();
            context.fill();
        },
        drawProcessNodes: function (context, nodes, minX, minY, padding, activeNodeIds) {
            var currentActiveNodeIds = activeNodeIds || [];
            for (var i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                var x = (node.x || 0) - minX + padding;
                var y = (node.y || 0) - minY + padding;
                var width = node.width || 120;
                var height = node.height || 60;
                var isActive = currentActiveNodeIds.indexOf(node.elementId) >= 0;
                context.save();
                context.fillStyle = isActive ? "#dcfce7" : "#ffffff";
                context.strokeStyle = isActive ? "#16a34a" : "#2563eb";
                context.lineWidth = isActive ? 3 : 2;
                if (node.elementType === "StartEvent" || node.elementType === "EndEvent") {
                    context.beginPath();
                    context.arc(x + width / 2, y + height / 2, Math.min(width, height) / 2, 0, Math.PI * 2);
                    context.fill();
                    context.stroke();
                } else {
                    context.beginPath();
                    context.rect(x, y, width, height);
                    context.fill();
                    context.stroke();
                }
                context.fillStyle = "#0f172a";
                context.font = "14px sans-serif";
                context.textAlign = "center";
                context.textBaseline = "middle";
                this.drawNodeText(context, node.elementName || node.elementId, x + width / 2, y + height / 2, width - 16);
                context.restore();
            }
        },
        drawNodeText: function (context, text, centerX, centerY, maxWidth) {
            var safeText = text || "";
            var chars = safeText.split("");
            var lines = [];
            var line = "";
            for (var i = 0; i < chars.length; i += 1) {
                var nextLine = line + chars[i];
                if (context.measureText(nextLine).width > maxWidth && line) {
                    lines.push(line);
                    line = chars[i];
                    continue;
                }
                line = nextLine;
            }
            if (line) {
                lines.push(line);
            }
            var lineHeight = 18;
            var startY = centerY - ((lines.length - 1) * lineHeight) / 2;
            for (var j = 0; j < lines.length; j += 1) {
                context.fillText(lines[j], centerX, startY + j * lineHeight);
            }
        },
        resolveDefinitionLabel: function (definition) {
            var name = definition.processDefinitionName || definition.processDefinitionKey;
            var version = definition.version ? "第" + definition.version + "版" : "";
            return version ? name + "（" + version + "）" : name;
        },
        resolveDefinitionName: function (processDefinitionKey) {
            if (!processDefinitionKey) {
                return "-";
            }
            var definitions = this.$root.processDefinitions || [];
            for (var i = 0; i < definitions.length; i += 1) {
                if (definitions[i].processDefinitionKey === processDefinitionKey) {
                    return this.resolveDefinitionLabel(definitions[i]);
                }
            }
            return processDefinitionKey;
        },
        resolveDefinitionCategory: function (processDefinitionKey) {
            if (!processDefinitionKey) {
                return "-";
            }
            var definitions = this.$root.processDefinitions || [];
            for (var i = 0; i < definitions.length; i += 1) {
                if (definitions[i].processDefinitionKey === processDefinitionKey) {
                    return definitions[i].category || "未分类";
                }
            }
            return "-";
        },
        resolveDefinitionCategoryById: function (processDefinitionId) {
            var definition = this.findDefinitionById(processDefinitionId);
            return definition && definition.category ? definition.category : "未分类";
        },
        resolveNodeTypeLabel: function (elementType) {
            var mapping = {
                StartEvent: "开始节点",
                EndEvent: "结束节点",
                UserTask: "用户任务",
                ManualTask: "人工任务",
                ServiceTask: "服务任务",
                ExclusiveGateway: "排他网关",
                ParallelGateway: "并行网关",
                InclusiveGateway: "包容网关"
            };
            return mapping[elementType] || elementType;
        },
        resolveFieldTypeLabel: function (field) {
            var mapping = {
                input: "输入框",
                textarea: "文本域",
                number: "数字",
                select: "下拉选择",
                checkbox: "复选框",
                switch: "开关",
                radio: "单选框",
                date: "日期",
                table: "表格"
            };
            return mapping[field.componentType] || field.componentType || field.dataType || "字段";
        },
        resolveNodeTagType: function (elementType) {
            if (elementType === "StartEvent") {
                return "success";
            }
            if (elementType === "EndEvent") {
                return "info";
            }
            if (elementType && elementType.indexOf("Gateway") >= 0) {
                return "warning";
            }
            return "";
        },
        resolveNodeIndex: function (elementId, nodes) {
            var targetNodes = nodes || [];
            for (var i = 0; i < targetNodes.length; i += 1) {
                if (targetNodes[i].elementId === elementId) {
                    return i + 1;
                }
            }
            return "-";
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
            for (var i = 0; i < orderedNodes.length; i += 1) {
                nodeNameMap[orderedNodes[i].elementId] = orderedNodes[i].elementName || orderedNodes[i].elementId;
            }
            return detail.sequenceFlows.map(function (sequenceFlow) {
                return (nodeNameMap[sequenceFlow.sourceRef] || sequenceFlow.sourceRef)
                    + " → "
                    + (nodeNameMap[sequenceFlow.targetRef] || sequenceFlow.targetRef);
            });
        },
        resolveSortedFields: function (detail) {
            if (!detail || !detail.formFields) {
                return [];
            }
            return detail.formFields.slice().sort(function (left, right) {
                return (left.sortOrder || 0) - (right.sortOrder || 0);
            });
        },
        resolveSortedButtons: function (detail) {
            if (!detail || !detail.actionButtons) {
                return [];
            }
            return detail.actionButtons.slice().sort(function (left, right) {
                return (left.sortOrder || 0) - (right.sortOrder || 0);
            });
        },
        isActiveProcessNode: function (nodeId) {
            return this.activeProcessNodeIds.indexOf(nodeId) >= 0;
        },
        resolveProcessRowClass: function (payload) {
            return payload.row && payload.row.id === this.selectedProcessId ? "is-selected-process-row" : "";
        },
        isInputField: function (field) {
            return !field.componentType || field.componentType === "input";
        },
        isTextareaField: function (field) {
            return field.componentType === "textarea";
        },
        isNumberField: function (field) {
            return field.componentType === "number";
        },
        isSelectField: function (field) {
            return field.componentType === "select" || field.componentType === "radio";
        },
        isCheckboxField: function (field) {
            return field.componentType === "checkbox";
        },
        isSwitchField: function (field) {
            return field.componentType === "switch";
        },
        isDateField: function (field) {
            return field.componentType === "date";
        },
        applyDynamicDefaults: function () {
            var nextValues = {};
            var fields = this.flattenDynamicFormFields(this.dynamicFormFields);
            for (var i = 0; i < fields.length; i += 1) {
                var field = fields[i];
                if (field.componentType === "checkbox" || field.componentType === "switch") {
                    nextValues[field.fieldKey] = field.defaultValue === true || field.defaultValue === "true";
                    continue;
                }
                if (field.componentType === "number") {
                    nextValues[field.fieldKey] = field.defaultValue !== null && field.defaultValue !== undefined && field.defaultValue !== ""
                        ? Number(field.defaultValue)
                        : null;
                    continue;
                }
                nextValues[field.fieldKey] = field.defaultValue !== null && field.defaultValue !== undefined
                    ? field.defaultValue
                    : "";
            }
            this.formValues = nextValues;
        },
        flattenDynamicFormFields: function (fields) {
            var results = [];
            for (var i = 0; i < (fields || []).length; i += 1) {
                var field = fields[i];
                if (!field) {
                    continue;
                }
                if (field.componentType === "table" || field.type === "table") {
                    var cells = field.children || [];
                    for (var cellIndex = 0; cellIndex < cells.length; cellIndex += 1) {
                        results = results.concat(this.flattenDynamicFormFields(cells[cellIndex].fields || []));
                    }
                    continue;
                }
                if (field.componentType === "group" || field.type === "group") {
                    results = results.concat(this.flattenDynamicFormFields(field.children || []));
                    continue;
                }
                if (field.componentType === "button" || field.type === "button" || !field.fieldKey) {
                    continue;
                }
                results.push(field);
            }
            return results;
        },
        resetForm: function () {
            this.form.taskName = "";
            this.form.selectedDefinitionId = this.availableDefinitions.length ? this.availableDefinitions[0].processDefinitionId : "";
            this.form.processDefinitionKey = this.availableDefinitions.length ? this.availableDefinitions[0].processDefinitionKey : "";
            if (this.form.selectedDefinitionId) {
                this.handleDefinitionChange(this.form.selectedDefinitionId);
            } else {
                this.$root.selectedProcessDefinitionDetail = null;
                this.formValues = {};
            }
        },
        validateDynamicForm: function () {
            if (!this.form.processDefinitionKey) {
                return "流程定义不能为空";
            }
            if (!this.form.taskName) {
                return "任务名称不能为空";
            }
            var fields = this.flattenDynamicFormFields(this.dynamicFormFields);
            for (var i = 0; i < fields.length; i += 1) {
                var field = fields[i];
                if (!field.required) {
                    continue;
                }
                var value = this.formValues[field.fieldKey];
                if (field.componentType === "checkbox") {
                    continue;
                }
                if (value === null || value === undefined || value === "") {
                    return field.label + "不能为空";
                }
            }
            return "";
        },
        buildCreatePayload: function (button) {
            return {
                processDefinitionKey: this.form.processDefinitionKey,
                taskName: this.form.taskName,
                formData: Object.assign({}, this.formValues),
                submit: !!(button && button.submit)
            };
        },
        submitByAction: async function (button) {
            if (!button) {
                return;
            }
            if (button.actionKey === "reset") {
                this.form.taskName = "";
                this.applyDynamicDefaults();
                return;
            }
            var validationMessage = this.validateDynamicForm();
            if (validationMessage) {
                this.$root.showError(validationMessage);
                return;
            }
            try {
                await this.$root.createProcess(this.buildCreatePayload(button));
                this.form.taskName = "";
                this.applyDynamicDefaults();
            } catch (error) {
                this.$root.showError(error.message || "流程申请创建失败");
            }
        },
        submitDraft: async function (id) {
            try {
                await this.$root.submitProcessById(id);
                if (this.selectedProcessId === id) {
                    await this.$root.loadProcessRequestDiagramDetail(id);
                }
            } catch (error) {
                this.$root.showError(error.message || "流程申请提交失败");
            }
        },
        handleDelete: function (id) {
            var self = this;
            this.$confirm("删除后将同时清除流程申请及关联流程、任务数据，是否继续？", "删除流程数据", {
                type: "warning",
                confirmButtonText: "确定删除",
                cancelButtonText: "取消"
            }).then(async function () {
                await self.$root.deleteProcessById(id);
                if (self.selectedProcessId === id) {
                    self.selectedProcessId = null;
                    self.$root.selectedProcessRequestDiagramDetail = null;
                }
                self.selectFirstProcessRow();
            }).catch(function () {});
        },
        handleRefresh: async function () {
            if (this.activeTab === "list") {
                await this.$root.loadProcesses(this.$root.processPageNum, this.$root.processPageSize);
                this.selectFirstProcessRow();
                return;
            }
            await Promise.all([
                this.$root.loadDefinitions(),
                this.$root.loadProcesses(this.$root.processPageNum, this.$root.processPageSize)
            ]);
        },
        handleProcessPageChange: async function (pageNum) {
            await this.$root.loadProcesses(pageNum, this.$root.processPageSize);
            this.selectFirstProcessRow();
        },
        handleProcessSizeChange: async function (pageSize) {
            await this.$root.loadProcesses(1, pageSize);
            this.selectFirstProcessRow();
        },
        handleQuery: async function () {
            await this.$root.loadProcesses(1, this.$root.processPageSize);
            this.selectFirstProcessRow();
        },
        handleResetQuery: async function () {
            this.$root.processFilters.processNo = "";
            this.$root.processFilters.starter = "";
            this.$root.processFilters.businessTitle = "";
            this.$root.processFilters.category = "";
            this.$root.processFilters.processDefinitionKey = "";
            this.$root.processFilters.status = "";
            await this.$root.loadProcesses(1, this.$root.processPageSize);
            this.selectFirstProcessRow();
        },
        selectFirstProcessRow: function () {
            var rows = this.processTableData;
            if (!rows.length) {
                this.selectedProcessId = null;
                this.$root.selectedProcessRequestDiagramDetail = null;
                return;
            }
            this.selectedProcessId = rows[0].id;
        }
    },
    mounted: async function () {
        await Promise.all([
            this.$root.loadDefinitions(),
            this.$root.loadProcesses(this.$root.processPageNum, this.$root.processPageSize)
        ]);
        var routeApplied = await this.applyRouteDefinitionSelection();
        if (!routeApplied) {
            this.resetForm();
        }
        this.selectFirstProcessRow();
    },
    watch: {
        selectedDetail: function () {
            this.$nextTick(this.renderDefinitionCanvas);
        },
        availableDefinitions: function (definitions) {
            if (!definitions.length) {
                this.form.selectedDefinitionId = "";
                this.form.processDefinitionKey = "";
                this.$root.selectedProcessDefinitionDetail = null;
                this.formValues = {};
                return;
            }
            if (this.$route && this.$route.query && this.$route.query.processDefinitionId) {
                this.applyRouteDefinitionSelection();
                return;
            }
            if (!this.form.selectedDefinitionId) {
                this.form.selectedDefinitionId = definitions[0].processDefinitionId;
                this.form.processDefinitionKey = definitions[0].processDefinitionKey;
                this.handleDefinitionChange(this.form.selectedDefinitionId);
            }
        },
        processTableData: function () {
            if (!this.processTableData.length) {
                this.selectedProcessId = null;
                this.$root.selectedProcessRequestDiagramDetail = null;
                return;
            }
            if (!this.selectedProcessRow) {
                this.selectFirstProcessRow();
            }
        },
        "$route.query.processDefinitionId": function () {
            this.applyRouteDefinitionSelection();
        }
    }
};
