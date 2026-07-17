/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.ModelCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">流程模型管理</div>
                        <h2>模型中心</h2>
                    </div>
                    <el-button @click="handleRefresh">刷新</el-button>
                </div>

                <el-tabs v-model="activeTab" class="center-tabs">
                    <el-tab-pane label="创建模型" name="create">
                        <div >
                            <div>
                                <el-form label-position="top" @submit.native.prevent="submitModel">
                                    <el-form-item label="模型名称">
                                        <el-input v-model.trim="form.modelName" placeholder="请输入模型名称"></el-input>
                                    </el-form-item>
                                    <div class="form-grid two-columns">
                                        <el-form-item label="模型标识">
                                            <el-input v-model.trim="form.modelKey" placeholder="例如：请假流程"></el-input>
                                        </el-form-item>
                                        <el-form-item label="模型分类">
                                            <el-input v-model.trim="form.category" placeholder="请输入模型分类"></el-input>
                                        </el-form-item>
                                    </div>
                                    <el-form-item label="模型源码">
                                        <el-input
                                            v-model="form.bpmnXml"
                                            type="textarea"
                                            :rows="12"
                                            placeholder="请输入完整流程模型源码">
                                        </el-input>
                                    </el-form-item>
                                    <div class="form-actions">
                                        <el-button type="primary" @click="submitModel">{{ editingModelId ? "更新模型" : "创建模型" }}</el-button>
                                        <el-button @click="handleDraftPreview">预览</el-button>
                                        <el-button @click="resetForm">{{ editingModelId ? "取消编辑" : "重置" }}</el-button>
                                    </div>
                                </el-form>
                            </div>
                        </div>
                    </el-tab-pane>

                    <el-tab-pane label="模型列表" name="list">
                        <div class="process-list-panel">
                            <el-form inline class="process-filter-form" @submit.native.prevent="handleQuery">
                                <el-form-item label="模型名称">
                                    <el-input v-model.trim="filters.modelName" placeholder="请输入模型名称"></el-input>
                                </el-form-item>
                                <el-form-item label="模型标识">
                                    <el-input v-model.trim="filters.modelKey" placeholder="请输入模型标识"></el-input>
                                </el-form-item>
                                <el-form-item label="模型分类">
                                    <el-input v-model.trim="filters.category" placeholder="请输入模型分类"></el-input>
                                </el-form-item>
                                <el-form-item label="部署状态">
                                    <el-select v-model="filters.deployed" clearable placeholder="请选择部署状态">
                                        <el-option label="已部署" value="deployed"></el-option>
                                        <el-option label="未部署" value="undeployed"></el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="handleQuery">查询</el-button>
                                    <el-button @click="handleResetQuery">重置</el-button>
                                </el-form-item>
                            </el-form>

                            <el-table
                                :data="pagedModels"
                                stripe
                                @row-click="handleRowClick"
                                @sort-change="handleSortChange">
                                <el-table-column prop="modelName" label="模型名称" min-width="180" sortable="custom"></el-table-column>
                                <el-table-column prop="modelKey" label="模型标识" min-width="180" sortable="custom"></el-table-column>
                                <el-table-column prop="category" label="模型分类" min-width="140" sortable="custom">
                                    <template slot-scope="scope">
                                        {{ scope.row.category || "-" }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="version" label="版本号" width="100" sortable="custom"></el-table-column>
                                <el-table-column prop="lastUpdateTime" label="更新时间" min-width="180" sortable="custom">
                                    <template slot-scope="scope">
                                        {{ formatDateTime(scope.row.lastUpdateTime) }}
                                    </template>
                                </el-table-column>
                                <el-table-column label="部署状态" width="120">
                                    <template slot-scope="scope">
                                        <el-tag :type="scope.row.deploymentId ? 'success' : 'info'" effect="plain">
                                            {{ scope.row.deploymentId ? "已部署" : "未部署" }}
                                        </el-tag>
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" min-width="220" fixed="right">
                                    <template slot-scope="scope">
                                        <div class="table-operations">
                                            <el-button type="text" @click.stop="handleEdit(scope.row)">修改</el-button>
                                            <el-button type="text" @click.stop="handlePreview(scope.row)">预览</el-button>
                                            <el-button type="text" @click.stop="handleDeploy(scope.row)">部署</el-button>
                                            <el-button type="text" style="color: #f56c6c;" @click.stop="handleDelete(scope.row.modelId)">删除</el-button>
                                        </div>
                                    </template>
                                </el-table-column>
                            </el-table>

                            <div class="process-pagination">
                                <el-pagination
                                    background
                                    layout="total, sizes, prev, pager, next"
                                    :current-page="pageNum"
                                    :page-size="pageSize"
                                    :page-sizes="[10, 20, 50, 100]"
                                    :total="filteredModels.length"
                                    @current-change="handlePageChange"
                                    @size-change="handleSizeChange">
                                </el-pagination>
                            </div>
                        </div>
                    </el-tab-pane>
                </el-tabs>

                <el-dialog
                    title="部署模型"
                    :visible.sync="deployDialogVisible"
                    width="520px"
                    @close="resetDeployForm">
                    <el-form label-position="top" @submit.native.prevent="submitDeploy">
                        <el-form-item label="模型名称">
                            <el-input :value="deployForm.modelName || '-'" disabled></el-input>
                        </el-form-item>
                        <el-form-item label="客户端">
                            <el-select
                                v-model="deployForm.clientId"
                                clearable
                                filterable
                                placeholder="请选择客户端">
                                <el-option
                                    v-for="client in clientOptions"
                                    :key="'model-deploy-client-' + client.clientId"
                                    :label="formatClientOptionLabel(client)"
                                    :value="client.clientId">
                                </el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item label="processName">
                            <el-select
                                v-model="deployForm.processBeanName"
                                clearable
                                filterable
                                :loading="deployProcessBeanLoading"
                                placeholder="请选择processName">
                                <el-option
                                    v-for="processBeanName in deployProcessBeanOptions"
                                    :key="'model-deploy-process-' + processBeanName"
                                    :label="processBeanName"
                                    :value="processBeanName">
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-form>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="deployDialogVisible = false">取消</el-button>
                        <el-button type="primary" :loading="deploySaving" @click="submitDeploy">部署</el-button>
                    </span>
                </el-dialog>

                <el-dialog
                    title="模型流程图预览"
                    :visible.sync="previewDialogVisible"
                    width="960px"
                    top="5vh"
                    @opened="renderPreviewCanvas">
                    <div v-if="previewDetail" class="process-detail-shell">
                        <div class="process-detail-head">
                            <div>
                                <div class="section-kicker">模型源码绘制预览</div>
                                <h3>{{ previewDetail.modelName || previewDetail.processName || previewDetail.processKey || "未命名模型" }}</h3>
                            </div>
                            <el-tag :type="previewDetail.deploymentId ? 'success' : 'info'" size="small" effect="plain">
                                {{ previewDetail.deploymentId ? "已部署" : "未部署" }}
                            </el-tag>
                        </div>

                        <div class="process-stat-grid">
                            <div class="process-stat-card">
                                <span class="process-stat-label">模型标识</span>
                                <strong>{{ previewDetail.modelKey || "-" }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">节点数</span>
                                <strong>{{ previewDetail.nodeCount || 0 }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">用户任务</span>
                                <strong>{{ previewDetail.userTaskCount || 0 }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">连线数</span>
                                <strong>{{ previewDetail.sequenceFlowCount || 0 }}</strong>
                            </div>
                        </div>

                        <div class="process-meta-list">
                            <span class="mini-tag">流程标识：{{ previewDetail.processKey || previewDetail.modelKey || "-" }}</span>
                            <span class="mini-tag">流程分类：{{ previewDetail.category || "未分类" }}</span>
                            <span class="mini-tag">更新时间：{{ formatDateTime(previewDetail.lastUpdateTime) }}</span>
                            <span class="mini-tag">绘制依据：模型源码</span>
                        </div>

                        <process-diagram ref="previewDiagram" :detail="previewDetail"></process-diagram>

                        <div class="process-stage-list" v-if="previewOrderedNodes.length">
                            <div
                                v-for="node in previewOrderedNodes"
                                :key="'preview-node-' + node.elementId"
                                class="process-stage-card">
                                <div class="process-stage-index">{{ resolveNodeIndex(node.elementId, previewOrderedNodes) }}</div>
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

                        <div class="helper-text" v-if="previewSequenceFlowSummaries.length">
                            当前路径：{{ previewSequenceFlowSummaries.join(" → ") }}
                        </div>
                    </div>
                    <div v-if="!previewDetail" class="empty-panel center-empty-panel">
                        未加载到模型流程图数据，请稍后重试。
                    </div>
                </el-dialog>
            </section>
        </section>
    `,
    data: function () {
        return {
            activeTab: "create",
            form: {
                modelName: "",
                modelKey: "",
                category: "",
                bpmnXml: ""
            },
            filters: {
                modelName: "",
                modelKey: "",
                category: "",
                deployed: ""
            },
            pageNum: 1,
            pageSize: 10,
            sortProp: "lastUpdateTime",
            sortOrder: "descending",
            editingModelId: "",
            selectedModelId: "",
            clientOptions: [],
            deployDialogVisible: false,
            deploySaving: false,
            deployProcessBeanLoading: false,
            deployProcessBeanOptions: [],
            deployForm: {
                modelId: "",
                modelName: "",
                clientId: "",
                processBeanName: ""
            },
            previewDialogVisible: false,
            previewDetail: null
        };
    },
    computed: {
        filteredModels: function () {
            return this.sortItems(this.$root.models || []);
        },
        pagedModels: function () {
            var startIndex = (this.pageNum - 1) * this.pageSize;
            return this.filteredModels.slice(startIndex, startIndex + this.pageSize);
        },
        selectedModel: function () {
            var modelId = this.selectedModelId;
            var list = this.$root.models || [];
            for (var index = 0; index < list.length; index += 1) {
                if (list[index].modelId === modelId) {
                    return list[index];
                }
            }
            return list.length ? list[0] : null;
        },
        previewOrderedNodes: function () {
            return this.resolveOrderedNodes(this.previewDetail);
        },
        previewSequenceFlowSummaries: function () {
            return this.resolveSequenceFlowSummaries(this.previewDetail, this.previewOrderedNodes);
        }
    },
    methods: {
        formatDateTime: window.AppService.formatDateTime,
        sortItems: function (list) {
            var prop = this.sortProp;
            var order = this.sortOrder;
            if (!prop || !order) {
                return list.slice();
            }
            var direction = order === "ascending" ? 1 : -1;
            return list.slice().sort(function (left, right) {
                var leftValue = left[prop];
                var rightValue = right[prop];
                if (prop === "version") {
                    leftValue = Number(leftValue || 0);
                    rightValue = Number(rightValue || 0);
                } else if (prop === "createTime" || prop === "lastUpdateTime") {
                    leftValue = leftValue ? new Date(leftValue).getTime() : 0;
                    rightValue = rightValue ? new Date(rightValue).getTime() : 0;
                } else {
                    leftValue = leftValue ? String(leftValue).toLowerCase() : "";
                    rightValue = rightValue ? String(rightValue).toLowerCase() : "";
                }
                if (leftValue === rightValue) {
                    return 0;
                }
                return leftValue > rightValue ? direction : -direction;
            });
        },
        resetForm: function () {
            this.editingModelId = "";
            this.form.modelName = "";
            this.form.modelKey = "";
            this.form.category = "";
            this.form.bpmnXml = "";
        },
        submitModel: async function () {
            if (!this.form.modelName || !this.form.modelKey || !this.form.bpmnXml) {
                this.$root.showError("模型名称、模型标识和模型源码不能为空");
                return;
            }
            try {
                var payload = {
                    modelName: this.form.modelName,
                    modelKey: this.form.modelKey,
                    category: this.form.category,
                    bpmnXml: this.form.bpmnXml
                };
                if (this.editingModelId) {
                    await this.$root.updateModel(this.editingModelId, payload);
                } else {
                    await this.$root.createModel(payload);
                }
                this.resetForm();
                this.activeTab = "list";
                this.selectFirstModel();
            } catch (error) {
                this.$root.showError(error.message || (this.editingModelId ? "模型更新失败" : "模型创建失败"));
            }
        },
        handleEdit: async function (model) {
            if (!model || !model.modelId) {
                this.$root.showError("未查询到模型信息，无法修改");
                return;
            }
            try {
                var result = await window.AppService.request("/flowable/model/" + encodeURIComponent(model.modelId) + "/xml");
                this.editingModelId = model.modelId;
                this.form.modelName = model.modelName || "";
                this.form.modelKey = model.modelKey || "";
                this.form.category = model.category || "";
                this.form.bpmnXml = result.data || "";
                this.selectedModelId = model.modelId;
                this.activeTab = "create";
            } catch (error) {
                this.$root.showError(error.message || "模型回显失败");
            }
        },
        handleDraftPreview: function () {
            if (!this.form.bpmnXml) {
                this.$root.showError("请先输入模型源码后再预览");
                return;
            }
            try {
                this.previewDetail = this.buildPreviewDetailFromXml({
                    modelName: this.form.modelName,
                    modelKey: this.form.modelKey,
                    category: this.form.category,
                    lastUpdateTime: new Date().toISOString()
                }, this.form.bpmnXml);
                this.previewDialogVisible = true;
                this.$nextTick(this.renderPreviewCanvas);
            } catch (error) {
                this.previewDetail = null;
                this.$root.showError(error.message || "模型流程图预览失败");
            }
        },
        handlePreview: async function (model) {
            if (!model || !model.modelId) {
                this.$root.showError("未查询到模型信息，无法预览");
                return;
            }
            try {
                var result = await window.AppService.request("/flowable/model/" + encodeURIComponent(model.modelId) + "/xml");
                this.previewDetail = this.buildPreviewDetailFromXml(model, result.data || "");
                this.previewDialogVisible = true;
                this.$nextTick(this.renderPreviewCanvas);
            } catch (error) {
                this.previewDetail = null;
                this.$root.showError(error.message || "模型流程图预览失败");
            }
        },
        handleDeploy: async function (model) {
            try {
                if (!model || !model.modelId) {
                    this.$root.showError("未查询到模型信息");
                    return;
                }
                this.deployForm.modelId = model.modelId;
                this.deployForm.modelName = model.modelName || model.modelId;
                this.deployForm.clientId = "";
                this.deployForm.processBeanName = "";
                this.deployProcessBeanOptions = [];
                this.deployDialogVisible = true;
                if (!this.clientOptions.length) {
                    await this.loadClientOptions();
                }
            } catch (error) {
                this.$root.showError(error.message || "模型部署失败");
            }
        },
        loadClientOptions: async function () {
            try {
                var result = await window.AppService.request("/flowable/deploy/client/list?pageNum=1&pageSize=500");
                var pageData = result.data || {};
                this.clientOptions = Array.isArray(pageData.records) ? pageData.records : [];
            } catch (error) {
                this.clientOptions = [];
                this.$root.showError(error.message || "加载客户端列表失败");
            }
        },
        formatClientOptionLabel: function (client) {
            if (!client) {
                return "";
            }
            return client.clientName ? client.clientId + " (" + client.clientName + ")" : client.clientId;
        },
        loadDeployProcessBeanOptionsByClient: async function () {
            var clientId = (this.deployForm.clientId || "").trim();
            this.deployProcessBeanOptions = [];
            if (!clientId) {
                this.deployForm.processBeanName = "";
                return;
            }
            this.deployProcessBeanLoading = true;
            try {
                var result = await window.AppService.request("/flowable/deploy/client/" + encodeURIComponent(clientId) + "/process-bean/list");
                if ((this.deployForm.clientId || "").trim() !== clientId) {
                    return;
                }
                this.deployProcessBeanOptions = Array.isArray(result.data) ? result.data : [];
            } catch (error) {
                if ((this.deployForm.clientId || "").trim() !== clientId) {
                    return;
                }
                this.deployProcessBeanOptions = [];
                this.$root.showError(error.message || "加载processName失败");
            } finally {
                if ((this.deployForm.clientId || "").trim() === clientId) {
                    this.deployProcessBeanLoading = false;
                }
            }
        },
        syncDeployProcessBeanByClient: function () {
            if (!this.deployForm.processBeanName) {
                return;
            }
            if (this.deployProcessBeanOptions.indexOf(this.deployForm.processBeanName) < 0) {
                this.deployForm.processBeanName = "";
            }
        },
        resetDeployForm: function () {
            this.deploySaving = false;
            this.deployProcessBeanLoading = false;
            this.deployProcessBeanOptions = [];
            this.deployForm.modelId = "";
            this.deployForm.modelName = "";
            this.deployForm.clientId = "";
            this.deployForm.processBeanName = "";
        },
        submitDeploy: async function () {
            if (!this.deployForm.modelId) {
                this.$root.showError("未查询到模型信息");
                return;
            }
            if ((this.deployForm.clientId && !this.deployForm.processBeanName)
                || (!this.deployForm.clientId && this.deployForm.processBeanName)) {
                this.$root.showError("客户端与processName需要同时选择，或都不选择");
                return;
            }
            this.deploySaving = true;
            try {
                await this.$root.deployModel(this.deployForm.modelId, {
                    clientId: this.deployForm.clientId,
                    processBeanName: this.deployForm.processBeanName
                });
                var deployedModelId = this.deployForm.modelId;
                this.deployDialogVisible = false;
                this.selectCurrentOrFirst(deployedModelId);
            } catch (error) {
                this.$root.showError(error.message || "模型部署失败");
            } finally {
                this.deploySaving = false;
            }
        },
        handleDelete: function (modelId) {
            var self = this;
            this.$confirm("删除模型后将清除该模型及其编辑数据，是否继续？", "删除模型", {
                type: "warning",
                confirmButtonText: "确定删除",
                cancelButtonText: "取消"
            }).then(async function () {
                await self.$root.deleteModel(modelId);
                self.selectFirstModel();
            }).catch(function () {});
        },
        handleRefresh: async function () {
            await this.$root.loadModels(this.filters);
            this.selectFirstModel();
        },
        handleQuery: async function () {
            this.pageNum = 1;
            await this.$root.loadModels(this.filters);
            this.selectFirstFromFiltered();
        },
        handleResetQuery: async function () {
            this.filters.modelName = "";
            this.filters.modelKey = "";
            this.filters.category = "";
            this.filters.deployed = "";
            this.pageNum = 1;
            this.sortProp = "lastUpdateTime";
            this.sortOrder = "descending";
            await this.$root.loadModels(this.filters);
            this.selectFirstModel();
        },
        handlePageChange: function (pageNum) {
            this.pageNum = pageNum;
        },
        handleSizeChange: function (pageSize) {
            this.pageSize = pageSize;
            this.pageNum = 1;
        },
        handleSortChange: function (payload) {
            this.sortProp = payload.prop || "lastUpdateTime";
            this.sortOrder = payload.order || "descending";
            this.pageNum = 1;
        },
        handleRowClick: function (row) {
            this.selectedModelId = row.modelId;
        },
        selectCurrentOrFirst: function (modelId) {
            this.selectedModelId = modelId || "";
            if (!this.selectedModel) {
                this.selectFirstModel();
            }
        },
        selectFirstModel: function () {
            var list = this.$root.models || [];
            this.selectedModelId = list.length ? list[0].modelId : "";
        },
        selectFirstFromFiltered: function () {
            var list = this.filteredModels;
            this.selectedModelId = list.length ? list[0].modelId : "";
        },
        renderPreviewCanvas: function () {
            if (this.$refs.previewDiagram && typeof this.$refs.previewDiagram.renderCanvas === "function") {
                this.$refs.previewDiagram.renderCanvas();
            }
        },
        buildPreviewDetailFromXml: function (model, xmlText) {
            var parser = new DOMParser();
            var documentNode = parser.parseFromString(xmlText || "", "text/xml");
            var parseError = documentNode.getElementsByTagName("parsererror");
            if (parseError && parseError.length) {
                throw new Error("模型源码格式不正确");
            }
            var processElement = this.findFirstElementByLocalName(documentNode, "process");
            if (!processElement) {
                throw new Error("模型源码中未找到流程定义");
            }
            var shapeMap = this.buildShapeMap(documentNode);
            var edgeWaypointMap = this.buildEdgeWaypointMap(documentNode);
            var nodes = [];
            var sequenceFlows = [];
            var supportedNodeTypes = {
                startEvent: "StartEvent",
                endEvent: "EndEvent",
                userTask: "UserTask",
                manualTask: "ManualTask",
                serviceTask: "ServiceTask",
                subProcess: "SubProcess",
                exclusiveGateway: "ExclusiveGateway",
                parallelGateway: "ParallelGateway",
                inclusiveGateway: "InclusiveGateway"
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
        toNumber: function (value, fallback) {
            var nextValue = Number(value);
            return Number.isFinite(nextValue) ? nextValue : fallback;
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
            for (var index = 0; index < targetNodes.length; index += 1) {
                if (targetNodes[index].elementId === elementId) {
                    return index + 1;
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
            for (var index = 0; index < orderedNodes.length; index += 1) {
                nodeNameMap[orderedNodes[index].elementId] = orderedNodes[index].elementName || orderedNodes[index].elementId;
            }
            return detail.sequenceFlows.map(function (sequenceFlow) {
                return (nodeNameMap[sequenceFlow.sourceRef] || sequenceFlow.sourceRef)
                    + " → "
                    + (nodeNameMap[sequenceFlow.targetRef] || sequenceFlow.targetRef);
            });
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
        }
    },
    mounted: async function () {
        await this.$root.loadModels(this.filters);
        this.selectFirstModel();
    },
    watch: {
        "$root.models": function () {
            if (!this.selectedModelId) {
                this.selectFirstModel();
                return;
            }
            if (!this.selectedModel) {
                this.selectFirstModel();
            }
        },
        "deployForm.clientId": function () {
            var self = this;
            this.loadDeployProcessBeanOptionsByClient().then(function () {
                self.syncDeployProcessBeanByClient();
            });
        }
    }
};
