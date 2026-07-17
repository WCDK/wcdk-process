/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.DeployCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">流程部署管理</div>
                        <h2>部署中心</h2> <div class="section-kicker">(同一个流程文件反复上传 会覆盖旧的部署信息保)</div>
                    </div>
                    <el-button @click="handleRefresh">刷新</el-button>
                </div>

                <el-tabs v-model="activeTab" class="center-tabs">
                    <el-tab-pane label="创建部署" name="create">
                        <el-form label-position="top" @submit.native.prevent="submitDeployment">
                            <div class="form-grid">
                                <el-form-item label="流程名称">
                                    <el-input
                                        v-model.trim="form.deploymentName"
                                        maxlength="50"
                                        show-word-limit
                                        placeholder="示例：请假流程第一版">
                                    </el-input>
                                    <div class="upload-tip">命名规则：四到五十个字符，支持中文、字母、数字、点、减号、下划线和中英文括号。</div>
                                </el-form-item>
                                 <el-form-item label="流程分类">
                                     <el-input v-model.trim="form.category" placeholder="示例：人事流程"></el-input>
                                 </el-form-item>
                                <el-form-item label="客户端">
                                    <el-select
                                        v-model="form.clientId"
                                        clearable
                                        filterable
                                        allow-create
                                        default-first-option
                                        placeholder="请选择或输入客户端">
                                        <el-option
                                            v-for="client in clientOptions"
                                            :key="client.clientId"
                                            :label="formatClientOptionLabel(client)"
                                            :value="client.clientId">
                                        </el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="流程处理器">
                                    <el-select
                                        v-model="form.processBeanName"
                                        clearable
                                        filterable
                                        allow-create
                                        default-first-option
                                        :loading="processBeanLoading"
                                        placeholder="请选择或输入流程处理器">
                                        <el-option
                                            v-for="processBeanName in processBeanOptions"
                                            :key="processBeanName"
                                            :label="processBeanName"
                                            :value="processBeanName">
                                        </el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="流程定义文件">
                                    <el-upload
                                        ref="upload"
                                        action="#"
                                        :auto-upload="false"
                                        :limit="1"
                                        :file-list="fileList"
                                        accept=".xml,.bpmn,.bpmn20.xml"
                                        :on-change="handleFileChange"
                                        :on-remove="handleFileRemove"
                                        :on-exceed="handleFileExceed">
                                        <el-button type="primary" size="small">选择流程定义文件</el-button>
                                        <div slot="tip" class="upload-tip">仅支持流程定义文件，扩展名可为 .xml、.bpmn、.bpmn20.xml。</div>
                                    </el-upload>
                                </el-form-item>
                                 <el-form-item label="流程描述">
                                    <el-input
                                        v-model.trim="form.description"
                                        type="textarea"
                                        :rows="4"
                                        placeholder="请输入流程描述">
                                    </el-input>
                                </el-form-item>
                            </div>
                            <div class="form-actions">
                                <el-button type="primary" @click="submitDeployment">提交部署</el-button>
                                <el-button @click="resetForm">重置表单</el-button>
                            </div>
                        </el-form>
                    </el-tab-pane>

                    <el-tab-pane label="流程列表" name="list">
                        <div class="process-list-panel">
                            <el-form inline class="process-filter-form" @submit.native.prevent="handleQuery">
                                <el-form-item label="流程部署名称">
                                    <el-input v-model.trim="filters.deploymentName" placeholder="请输入流程部署名称"></el-input>
                                </el-form-item>
                                <el-form-item label="流程分类">
                                    <el-input v-model.trim="filters.category" placeholder="请输入流程分类"></el-input>
                                </el-form-item>
                                <el-form-item label="所属客户端">
                                    <el-select
                                        v-model="filters.clientId"
                                        clearable
                                        filterable
                                        allow-create
                                        default-first-option
                                        placeholder="请选择或输入客户端">
                                        <el-option
                                            v-for="client in clientOptions"
                                            :key="'filter-' + client.clientId"
                                            :label="formatClientOptionLabel(client)"
                                            :value="client.clientId">
                                        </el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="handleQuery">查询</el-button>
                                    <el-button @click="handleResetQuery">重置</el-button>
                                </el-form-item>
                            </el-form>
                        </div>

                            <el-table
                                :data="pagedDeployments"
                                stripe
                                @sort-change="handleSortChange">
                                <el-table-column prop="deploymentId" label="流程部署ID" min-width="180"></el-table-column>
                                <el-table-column prop="deploymentName" label="流程部署名称" min-width="180" sortable="custom"></el-table-column>
                                <el-table-column prop="fileName" label="文件名称" min-width="220" sortable="custom">
                                    <template slot-scope="scope">
                                        {{ scope.row.fileName || "-" }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="category" label="流程分类" min-width="140" sortable="custom">
                                    <template slot-scope="scope">
                                        {{ scope.row.category || "-" }}
                                    </template>
                                </el-table-column>
                                <el-table-column label="所属客户端" min-width="180">
                                    <template slot-scope="scope">
                                        {{ formatListText(scope.row.clientNames) }}
                                    </template>
                                </el-table-column>
                                <el-table-column label="绑定处理器" min-width="180">
                                    <template slot-scope="scope">
                                        {{ formatListText(scope.row.processBeanNames) }}
                                    </template>
                                </el-table-column>
                                <el-table-column prop="deployTime" label="部署时间" min-width="180" sortable="custom">
                                    <template slot-scope="scope">
                                        {{ formatDateTime(scope.row.deployTime) }}
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" min-width="160" fixed="right">
                                    <template slot-scope="scope">
                                        <el-button type="text" @click.stop="handleStartApproval(scope.row)">发起审批</el-button>
                                        <el-button type="text" @click.stop="handleEdit(scope.row)">编辑</el-button>
                                        <el-button type="text" @click.stop="handleView(scope.row)">查看</el-button>
                                        <el-button type="text" @click.stop="handlePreview(scope.row)">预览</el-button>
                                        <el-button type="text" @click.stop="handleBindingEdit(scope.row)">修改绑定</el-button>
                                        <el-button type="text" @click.stop="handleDelete(scope.row.deploymentId)">删除</el-button>
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
                                    :total="filteredDeployments.length"
                                    @current-change="handlePageChange"
                                    @size-change="handleSizeChange">
                                </el-pagination>
                            </div>
                        </div>
                    </el-tab-pane>
                </el-tabs>

                <el-dialog
                    title="部署详情"
                    :visible.sync="detailDialogVisible"
                    width="720px">
                    <div v-if="selectedDeployment" class="center-detail-shell">
                        <div class="center-detail-head">
                            <div>
                                <div class="section-kicker">部署详情</div>
                                <h3>{{ selectedDeployment.deploymentName || "未命名部署" }}</h3>
                            </div>
                            <el-tag size="small" effect="plain">流程分类</el-tag>
                        </div>

                        <div class="detail-stat-grid">
                            <div class="detail-stat-card">
                                <span class="detail-stat-label">部署ID</span>
                                <strong>{{ selectedDeployment.deploymentId || "-" }}</strong>
                            </div>
                            <div class="detail-stat-card">
                                <span class="detail-stat-label">关联流程定义数</span>
                                <strong>{{ relatedDefinitions.length }}</strong>
                            </div>
                        </div>

                        <div class="detail-meta-list">
                            <span class="mini-tag">文件名: {{ selectedDeployment.fileName || "-" }}</span>
                            <span class="mini-tag">分类: {{ selectedDeployment.category || "无分类" }}</span>
                            <span class="mini-tag">部署时间: {{ formatDateTime(selectedDeployment.deployTime) }}</span>
                        </div>

                        <div class="detail-section" v-if="relatedDefinitions.length">
                            <div class="detail-section-title">关联流程定义</div>
                            <div class="schema-chip-list">
                                <span
                                    v-for="definition in relatedDefinitions"
                                    :key="definition.processDefinitionId"
                                    class="schema-chip">
                                    {{ resolveDefinitionLabel(definition) }}
                                </span>
                            </div>
                        </div>
                    </div>
                    <div v-else class="empty-panel center-empty-panel">
                        未查询到部署详情。
                    </div>
                </el-dialog>
                <el-dialog
                    title="修改绑定"
                    :visible.sync="bindingDialogVisible"
                    width="520px"
                    @close="resetBindingForm">
                    <el-form label-position="top" @submit.native.prevent="submitBinding">
                        <el-form-item label="流程部署">
                            <el-input :value="bindingForm.deploymentName || '-'" disabled></el-input>
                        </el-form-item>
                        <el-form-item label="客户端">
                            <el-select
                                v-model="bindingForm.clientId"
                                clearable
                                filterable
                                placeholder="请选择客户端">
                                <el-option
                                    v-for="client in clientOptions"
                                    :key="'binding-client-' + client.clientId"
                                    :label="formatClientOptionLabel(client)"
                                    :value="client.clientId">
                                </el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item label="processName">
                            <el-select
                                v-model="bindingForm.processBeanName"
                                clearable
                                filterable
                                :loading="bindingProcessBeanLoading"
                                placeholder="请选择processName">
                                <el-option
                                    v-for="processBeanName in bindingProcessBeanOptions"
                                    :key="'binding-process-' + processBeanName"
                                    :label="processBeanName"
                                    :value="processBeanName">
                                </el-option>
                            </el-select>
                        </el-form-item>
                    </el-form>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="bindingDialogVisible = false">取消</el-button>
                        <el-button type="primary" :loading="bindingSaving" @click="submitBinding">保存</el-button>
                    </span>
                </el-dialog>
                <el-dialog
                    title="流程图预览"
                    :visible.sync="previewDialogVisible"
                    width="960px"
                    top="5vh"
                    @opened="renderPreviewCanvas">
                    <div v-if="previewDetail" class="process-detail-shell">
                        <div class="process-detail-head">
                            <div>
                                <div class="section-kicker">流程定义</div>
                                <h3>{{ previewDetail.processDefinitionName || previewDetail.processDefinitionKey || "未知流程" }}</h3>
                            </div>
                            <el-tag size="small" effect="plain">版本: {{ previewDetail.version || 1 }}</el-tag>
                        </div>

                        <div class="process-stat-grid">
                            <div class="process-stat-card">
                                <span class="process-stat-label">部署名称</span>
                                <strong>{{ previewDetail.deploymentName || "-" }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">节点数量</span>
                                <strong>{{ previewDetail.nodeCount || 0 }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">用户任务数量</span>
                                <strong>{{ previewDetail.userTaskCount || 0 }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">流程连线数量</span>
                                <strong>{{ previewDetail.sequenceFlowCount || 0 }}</strong>
                            </div>
                        </div>

                        <div class="process-meta-list">
                            <span class="mini-tag">流程标识：{{ previewDetail.processDefinitionKey || "-" }}</span>
                            <span class="mini-tag">流程分类：{{ previewDetail.category || "未分类" }}</span>
                            <span class="mini-tag">资源名称：{{ previewDetail.resourceName || "-" }}</span>
                            <span class="mini-tag" v-if="previewSourceLabel">绘制依据：{{ previewSourceLabel }}</span>
                        </div>

                        <div class="detail-section" v-if="previewDefinitions.length > 1">
                            <div class="detail-section-title">流程定义</div>
                            <div class="schema-chip-list">
                                <span
                                    v-for="definition in previewDefinitions"
                                    :key="'preview-' + definition.processDefinitionId"
                                    :class="['schema-chip', { 'schema-chip-action': definition.processDefinitionId === selectedPreviewDefinitionId }]"
                                    @click="handlePreviewDefinitionChange(definition.processDefinitionId)">
                                    {{ resolveDefinitionLabel(definition) }}
                                </span>
                            </div>
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
<!--                    <div v-else class="empty-panel center-empty-panel">-->
<!--                        未加载到流程图数据，请稍后重试。-->
<!--                    </div>-->
                </el-dialog>
            </section>
        </section>
    `,
    data: function () {
        return {
            activeTab: "create",
            form: {
                deploymentName: "",
                category: "",
                clientId: "",
                processBeanName: ""
            },
            selectedFile: null,
            fileList: [],
            clientOptions: [],
            processBeanOptions: [],
            processBeanLoading: false,
            filters: {
                deploymentName: "",
                category: "",
                clientId: ""
            },
            pageNum: 1,
            pageSize: 10,
            sortProp: "deployTime",
            sortOrder: "descending",
            selectedDeploymentId: "",
            detailDialogVisible: false,
            bindingDialogVisible: false,
            bindingSaving: false,
            bindingProcessBeanLoading: false,
            bindingProcessBeanOptions: [],
            bindingForm: {
                deploymentId: "",
                deploymentName: "",
                clientId: "",
                processBeanName: ""
            },
            previewDialogVisible: false,
            previewDetail: null,
            previewDeploymentId: "",
            selectedPreviewDefinitionId: ""
        };
    },
    computed: {
        filteredDeployments: function () {
            return this.sortItems(this.$root.deployments || []);
        },
        pagedDeployments: function () {
            var startIndex = (this.pageNum - 1) * this.pageSize;
            return this.filteredDeployments.slice(startIndex, startIndex + this.pageSize);
        },
        selectedDeployment: function () {
            var deploymentId = this.selectedDeploymentId;
            var deployments = this.$root.deployments || [];
            for (var index = 0; index < deployments.length; index += 1) {
                if (deployments[index].deploymentId === deploymentId) {
                    return deployments[index];
                }
            }
            return deployments.length ? deployments[0] : null;
        },
        relatedDefinitions: function () {
            if (!this.selectedDeployment) {
                return [];
            }
            return (this.$root.processDefinitions || []).filter(function (definition) {
                return definition.deploymentId === this.selectedDeployment.deploymentId;
            }, this);
        },
        previewDefinitions: function () {
            var deploymentId = this.previewDeploymentId;
            if (!deploymentId) {
                return [];
            }
            return (this.$root.processDefinitions || []).filter(function (definition) {
                return definition.deploymentId === deploymentId;
            });
        },
        previewOrderedNodes: function () {
            return this.resolveOrderedNodes(this.previewDetail);
        },
        previewSequenceFlowSummaries: function () {
            return this.resolveSequenceFlowSummaries(this.previewDetail, this.previewOrderedNodes);
        },
        previewSourceLabel: function () {
            if (!this.previewDeploymentId) {
                return "";
            }
            return this.findModelByDeploymentId(this.previewDeploymentId) ? "MODEL_SOURCE" : "DEPLOYED_DEFINITION";
        },
        selectedClient: function () {
            var clientId = this.form.clientId;
            if (!clientId) {
                return null;
            }
            for (var index = 0; index < this.clientOptions.length; index += 1) {
                if (this.clientOptions[index].clientId === clientId) {
                    return this.clientOptions[index];
                }
            }
            return null;
        }
    },
    methods: {
        formatDateTime: window.AppService.formatDateTime,
        formatClientOptionLabel: function (client) {
            if (!client) {
                return "";
            }
            return client.clientName ? client.clientId + "（" + client.clientName + "）" : client.clientId;
        },
        formatListText: function (values) {
            var list = (values || []).filter(function (value) {
                return !!value;
            });
            return list.length ? list.join("、") : "-";
        },
        resolveDeploymentDefinitions: function (deploymentId) {
            if (!deploymentId) {
                return [];
            }
            return (this.$root.processDefinitions || []).filter(function (definition) {
                return definition.deploymentId === deploymentId;
            });
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
        handleClientChange: async function () {
            await this.loadProcessBeanOptionsByClient();
            this.syncProcessBeanByClient();
        },
        loadProcessBeanOptionsByClient: async function () {
            var clientId = (this.form.clientId || "").trim();
            this.processBeanOptions = [];
            if (!clientId) {
                this.form.processBeanName = "";
                return;
            }
            this.processBeanLoading = true;
            try {
                var result = await window.AppService.request("/flowable/deploy/client/" + encodeURIComponent(clientId) + "/process-bean/list");
                if ((this.form.clientId || "").trim() !== clientId) {
                    return;
                }
                this.processBeanOptions = Array.isArray(result.data) ? result.data : [];
            } catch (error) {
                if ((this.form.clientId || "").trim() !== clientId) {
                    return;
                }
                this.processBeanOptions = [];
                this.$root.showError(error.message || "加载流程处理器失败");
            } finally {
                if ((this.form.clientId || "").trim() === clientId) {
                    this.processBeanLoading = false;
                }
            }
        },
        syncProcessBeanByClient: function () {
            if (!this.form.processBeanName) {
                return;
            }
            if (this.processBeanOptions.indexOf(this.form.processBeanName) < 0) {
                this.form.processBeanName = "";
            }
        },
        handleBindingEdit: async function (row) {
            if (!row || !row.deploymentId) {
                this.$root.showError("未查询到部署信息");
                return;
            }
            this.bindingForm.deploymentId = row.deploymentId;
            this.bindingForm.deploymentName = row.deploymentName || row.deploymentId;
            this.bindingForm.clientId = Array.isArray(row.clientIds) && row.clientIds.length ? row.clientIds[0] : "";
            this.bindingForm.processBeanName = Array.isArray(row.processBeanNames) && row.processBeanNames.length ? row.processBeanNames[0] : "";
            this.bindingDialogVisible = true;
            if (!this.clientOptions.length) {
                await this.loadClientOptions();
            }
            await this.loadBindingProcessBeanOptionsByClient();
            this.syncBindingProcessBeanByClient();
        },
        loadBindingProcessBeanOptionsByClient: async function () {
            var clientId = (this.bindingForm.clientId || "").trim();
            this.bindingProcessBeanOptions = [];
            if (!clientId) {
                this.bindingForm.processBeanName = "";
                return;
            }
            this.bindingProcessBeanLoading = true;
            try {
                var result = await window.AppService.request("/flowable/deploy/client/" + encodeURIComponent(clientId) + "/process-bean/list");
                if ((this.bindingForm.clientId || "").trim() !== clientId) {
                    return;
                }
                this.bindingProcessBeanOptions = Array.isArray(result.data) ? result.data : [];
            } catch (error) {
                if ((this.bindingForm.clientId || "").trim() !== clientId) {
                    return;
                }
                this.bindingProcessBeanOptions = [];
                this.$root.showError(error.message || "加载processName失败");
            } finally {
                if ((this.bindingForm.clientId || "").trim() === clientId) {
                    this.bindingProcessBeanLoading = false;
                }
            }
        },
        syncBindingProcessBeanByClient: function () {
            if (!this.bindingForm.processBeanName) {
                return;
            }
            if (this.bindingProcessBeanOptions.indexOf(this.bindingForm.processBeanName) < 0) {
                this.bindingForm.processBeanName = "";
            }
        },
        resetBindingForm: function () {
            this.bindingSaving = false;
            this.bindingProcessBeanLoading = false;
            this.bindingProcessBeanOptions = [];
            this.bindingForm.deploymentId = "";
            this.bindingForm.deploymentName = "";
            this.bindingForm.clientId = "";
            this.bindingForm.processBeanName = "";
        },
        submitBinding: async function () {
            if (!this.bindingForm.deploymentId) {
                this.$root.showError("未查询到部署信息");
                return;
            }
            if (!this.bindingForm.clientId) {
                this.$root.showError("请选择客户端");
                return;
            }
            if (!this.bindingForm.processBeanName) {
                this.$root.showError("请选择processName");
                return;
            }
            this.bindingSaving = true;
            try {
                await this.$root.updateDeploymentBinding(this.bindingForm.deploymentId, {
                    clientId: this.bindingForm.clientId,
                    processBeanName: this.bindingForm.processBeanName
                });
                this.bindingDialogVisible = false;
                this.selectFirstFromFiltered();
            } catch (error) {
                this.$root.showError(error.message || "修改绑定失败");
            } finally {
                this.bindingSaving = false;
            }
        },
        resolveDefinitionLabel: function (definition) {
            var name = definition.processDefinitionName || definition.processDefinitionKey || "Unnamed Process";
            return name + " V" + (definition.version || 1);
        },
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
                if (prop === "deployTime") {
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
        handleFileChange: function (file, fileList) {
            this.selectedFile = file && file.raw ? file.raw : null;
            this.fileList = fileList.slice(-1);
        },
        handleFileRemove: function () {
            this.selectedFile = null;
            this.fileList = [];
        },
        handleFileExceed: function () {
            this.$root.showError("Only one file can be uploaded at a time");
        },
        validateDeploymentName: function () {
            var deploymentName = (this.form.deploymentName || "").trim();
            var deploymentNamePattern = /^(?=.*[A-Za-z0-9\u4e00-\u9fa5])[A-Za-z0-9\u4e00-\u9fa5._\-()]{4,50}$/;
            if (!deploymentName) {
                return "Deployment name is required";
            }
            if (deploymentName.length < 4 || deploymentName.length > 50) {
                return "Deployment name length must be between 4 and 50 characters";
            }
            if (!deploymentNamePattern.test(deploymentName)) {
                return "Deployment name only supports Chinese, letters, numbers, dots, hyphens, underscores and parentheses";
            }
            return "";
        },
        resetForm: function () {
            this.form.deploymentName = "";
            this.form.category = "";
            this.form.clientId = "";
            this.form.processBeanName = "";
            this.processBeanOptions = [];
            this.selectedFile = null;
            this.fileList = [];
            if (this.$refs.upload) {
                this.$refs.upload.clearFiles();
            }
        },
        submitDeployment: async function () {
            var deploymentNameErrorMessage = this.validateDeploymentName();
            if (deploymentNameErrorMessage) {
                this.$root.showError(deploymentNameErrorMessage);
                return;
            }
            if (!this.selectedFile) {
                this.$root.showError("Please select a process definition file");
                return;
            }
            try {
                var formData = new FormData();
                formData.append("deploymentName", this.form.deploymentName.trim());
                formData.append("category", this.form.category);
                formData.append("clientId", this.form.clientId || "");
                formData.append("processBeanName", this.form.processBeanName || "");
                formData.append("file", this.selectedFile);
                await this.$root.createDeployment(formData);
                this.resetForm();
                this.selectFirstDeployment();
            } catch (error) {
                this.$root.showError(error.message || "部署失败");
            }
        },
        handleDelete: function (deploymentId) {
            var self = this;
            this.$confirm("删除此部署也将删除相关的流程定义和运行时数据，是否继续删除？", "删除部署", {
                type: "warning",
                confirmButtonText: "删除",
                cancelButtonText: "取消"
            }).then(async function () {
                await self.$root.deleteDeployment(deploymentId);
                if (self.selectedDeploymentId === deploymentId) {
                    self.detailDialogVisible = false;
                }
                self.selectFirstDeployment();
            }).catch(function () {});
        },
        handleStartApproval: function (row) {
            var targetDefinition = this.resolveLatestDefinition(row);
            if (!targetDefinition || !targetDefinition.processDefinitionKey) {
                this.$root.showError("未查询到可发起的流程定义");
                return;
            }
            this.$router.push({
                path: "/process",
                query: {
                    processDefinitionId: targetDefinition.processDefinitionId
                }
            });
        },
        resolveLatestDefinition: function (row) {
            var definitions = this.findDefinitionsByDeploymentId(row && row.deploymentId ? row.deploymentId : "");
            if (!definitions.length) {
                return null;
            }
            return definitions.slice().sort(function (left, right) {
                return Number(right.version || 0) - Number(left.version || 0);
            })[0];
        },
        handleRefresh: async function () {
            await Promise.all([
                this.$root.loadDeployments(this.filters),
                this.$root.loadDefinitions(),
                this.loadClientOptions()
            ]);
            this.selectFirstDeployment();
        },
        handleQuery: async function () {
            this.pageNum = 1;
            await Promise.all([
                this.$root.loadDeployments(this.filters),
                this.$root.loadDefinitions()
            ]);
            this.selectFirstFromFiltered();
        },
        handleResetQuery: async function () {
            this.filters.deploymentName = "";
            this.filters.category = "";
            this.filters.clientId = "";
            this.pageNum = 1;
            this.sortProp = "deployTime";
            this.sortOrder = "descending";
            await Promise.all([
                this.$root.loadDeployments(this.filters),
                this.$root.loadDefinitions()
            ]);
            this.selectFirstDeployment();
        },
        handlePageChange: function (pageNum) {
            this.pageNum = pageNum;
        },
        handleSizeChange: function (pageSize) {
            this.pageSize = pageSize;
            this.pageNum = 1;
        },
        handleSortChange: function (payload) {
            this.sortProp = payload.prop || "deployTime";
            this.sortOrder = payload.order || "descending";
            this.pageNum = 1;
        },
        handleView: function (row) {
            this.selectedDeploymentId = row.deploymentId;
            this.detailDialogVisible = true;
        },
        handleEdit: function (row) {
            var targetDeploymentId = row && row.deploymentId ? row.deploymentId : "";
            var definitions = this.findDefinitionsByDeploymentId(targetDeploymentId);
            if (!definitions.length) {
                this.$root.showError("No related process definition found for this deployment");
                return;
            }
            var targetDefinition = definitions.slice().sort(function (left, right) {
                return Number(right.version || 0) - Number(left.version || 0);
            })[0];
            this.$router.push({
                path: "/designer",
                query: {
                    deploymentId: targetDeploymentId,
                    processDefinitionId: targetDefinition.processDefinitionId || ""
                }
            });
        },
        handlePreview: async function (row) {
            var targetDeploymentId = row && row.deploymentId ? row.deploymentId : "";
            var definitions = this.findDefinitionsByDeploymentId(targetDeploymentId);
            if (!definitions.length) {
                this.$root.showError("No related process definition found for this deployment");
                return;
            }
            this.previewDeploymentId = targetDeploymentId;
            this.selectedPreviewDefinitionId = definitions[0].processDefinitionId;
            this.previewDialogVisible = true;
            await this.loadPreviewDetail(this.selectedPreviewDefinitionId);
        },
        handlePreviewDefinitionChange: async function (processDefinitionId) {
            if (!processDefinitionId || processDefinitionId === this.selectedPreviewDefinitionId) {
                return;
            }
            this.selectedPreviewDefinitionId = processDefinitionId;
            await this.loadPreviewDetail(processDefinitionId);
        },
        loadPreviewDetail: async function (processDefinitionId) {
            try {
                this.previewDetail = await this.$root.fetchProcessDefinitionDetail(processDefinitionId);
                this.$nextTick(this.renderPreviewCanvas);
            } catch (error) {
                this.previewDetail = null;
                this.$root.showError(error.message || "Failed to load process preview");
            }
        },
        findDefinitionsByDeploymentId: function (deploymentId) {
            if (!deploymentId) {
                return [];
            }
            return (this.$root.processDefinitions || []).filter(function (definition) {
                return definition.deploymentId === deploymentId;
            });
        },
        findModelByDeploymentId: function (deploymentId) {
            var models = this.$root.models || [];
            for (var index = 0; index < models.length; index += 1) {
                if (models[index].deploymentId === deploymentId) {
                    return models[index];
                }
            }
            return null;
        },
        renderPreviewCanvas: function () {
            if (this.$refs.previewDiagram && typeof this.$refs.previewDiagram.renderCanvas === 'function') {
                this.$refs.previewDiagram.renderCanvas();
                return;
            }
            this.renderProcessCanvas(this.$refs.previewProcessCanvas, this.previewDetail, []);
        },

        renderProcessCanvas: function (canvas, detail, activeNodeIds) {
            if (!canvas || !detail || !Array.isArray(detail.nodes) || !detail.nodes.length) {
                return;
            }
            var nodes = detail.nodes.slice();
            var minX = Number.MAX_SAFE_INTEGER;
            var minY = Number.MAX_SAFE_INTEGER;
            var maxX = 0;
            var maxY = 0;
            for (var i = 0; i < nodes.length; i += 1) {
                var node = nodes[i];
                var nodeX = typeof node.x === "number" ? node.x : 0;
                var nodeY = typeof node.y === "number" ? node.y : 0;
                var width = node.width || 120;
                var height = node.height || 60;
                minX = Math.min(minX, nodeX);
                minY = Math.min(minY, nodeY);
                maxX = Math.max(maxX, nodeX + width);
                maxY = Math.max(maxY, nodeY + height);
            }
            if (!Number.isFinite(minX)) {
                minX = 0;
            }
            if (!Number.isFinite(minY)) {
                minY = 0;
            }
            var padding = 48;
            var logicalWidth = Math.max(720, Math.ceil(maxX - minX + padding * 2));
            var logicalHeight = Math.max(280, Math.ceil(maxY - minY + padding * 2));
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
                    + " -> "
                    + (nodeNameMap[sequenceFlow.targetRef] || sequenceFlow.targetRef);
            });
        },
        resolveNodeTypeLabel: function (elementType) {
            var mapping = {
                StartEvent: "Start Event",
                EndEvent: "End Event",
                UserTask: "User Task",
                ManualTask: "Manual Task",
                ServiceTask: "Service Task",
                ExclusiveGateway: "Exclusive Gateway",
                ParallelGateway: "Parallel Gateway",
                InclusiveGateway: "Inclusive Gateway"
            };
            return mapping[elementType] || elementType;
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
        selectFirstDeployment: function () {
            var deployments = this.$root.deployments || [];
            this.selectedDeploymentId = deployments.length ? deployments[0].deploymentId : "";
        },
        selectFirstFromFiltered: function () {
            var list = this.filteredDeployments;
            this.selectedDeploymentId = list.length ? list[0].deploymentId : "";
        }
    },
    mounted: async function () {
        await Promise.all([
            this.$root.loadDeployments(this.filters),
            this.$root.loadDefinitions(),
            this.$root.loadModels(),
            this.loadClientOptions()
        ]);
        this.selectFirstDeployment();
    },
    watch: {
        "$root.deployments": function () {
            if (!this.selectedDeploymentId) {
                this.selectFirstDeployment();
                return;
            }
            if (!this.selectedDeployment) {
                this.selectFirstDeployment();
            }
        },
        previewDetail: function () {
            this.$nextTick(this.renderPreviewCanvas);
        },
        "form.clientId": function () {
            this.handleClientChange();
        },
        "bindingForm.clientId": function () {
            var self = this;
            this.loadBindingProcessBeanOptionsByClient().then(function () {
                self.syncBindingProcessBeanByClient();
            });
        }
    }
};
