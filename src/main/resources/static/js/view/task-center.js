/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.TaskCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">任务审批处理</div>
                        <h2>任务中心</h2>
                    </div>
                    <el-button v-if="$root.hasButton('task:refresh')" @click="refreshTasks">刷新</el-button>
                </div>

                <div class="">
                    <div class="process-list-panel">
                        <el-form inline class="process-filter-form" @submit.native.prevent="queryTasks">
                            <el-form-item label="办理人">
                                <el-input v-model.trim="filters.assignee" placeholder="请输入办理人"></el-input>
                            </el-form-item>
                            <el-form-item label="任务名称">
                                <el-input v-model.trim="filters.taskName" placeholder="请输入任务名称"></el-input>
                            </el-form-item>
                            <el-form-item label="任务编号">
                                <el-input v-model.trim="filters.taskId" placeholder="请输入任务编号"></el-input>
                            </el-form-item>
                            <el-form-item label="流程实例编号">
                                <el-input v-model.trim="filters.processInstanceId" placeholder="请输入流程实例编号"></el-input>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="queryTasks">查询</el-button>
                                <el-button @click="resetQuery">重置</el-button>
                            </el-form-item>
                        </el-form>

                        <el-table
                            :data="pagedTasks"
                            stripe
                            @row-click="handleRowClick"
                            @sort-change="handleSortChange">
                            <el-table-column prop="taskId" label="任务编号" min-width="180"></el-table-column>
                            <el-table-column prop="taskName" label="任务名称" min-width="180" sortable="custom"></el-table-column>
                            <el-table-column prop="currentTaskName" label="当前节点名称" min-width="180" sortable="custom">
                                <template slot-scope="scope">
                                    {{ scope.row.currentTaskName || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column label="并行待办" min-width="220">
                                <template slot-scope="scope">
                                    <div class="schema-chip-list" v-if="resolveParallelTasks(scope.row).length">
                                        <el-tag
                                            v-for="task in resolveParallelTasks(scope.row)"
                                            :key="task.taskId"
                                            size="mini"
                                            effect="plain"
                                            :type="task.taskId === scope.row.taskId ? 'warning' : ''">
                                            {{ task.taskName || task.taskDefinitionKey || task.taskId }}
                                        </el-tag>
                                    </div>
                                    <span v-else>-</span>
                                </template>
                            </el-table-column>
                            <el-table-column prop="assignee" label="办理人" min-width="120" sortable="custom">
                                <template slot-scope="scope">
                                    {{ scope.row.assignee || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column prop="processInstanceId" label="流程实例编号" min-width="220"></el-table-column>
                            <el-table-column prop="processDefinitionId" label="流程定义编号" min-width="220"></el-table-column>
                            <el-table-column label="操作" min-width="180" fixed="right">
                                <template slot-scope="scope">
                                    <div class="table-operations">
                                        <el-button v-if="$root.hasButton('task:view')" type="text" @click.stop="openTaskDetail(scope.row)">查看</el-button>
                                        <el-button v-if="$root.hasButton('task:approve')" type="text" @click.stop="openApproval(scope.row)">办理</el-button>
                                        <el-button v-if="$root.hasButton('task:delete')" type="text" style="color: #f56c6c;" @click.stop="handleDelete(scope.row.taskId)">删除</el-button>
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
                                :total="filteredTasks.length"
                                @current-change="handlePageChange"
                                @size-change="handleSizeChange">
                            </el-pagination>
                        </div>
                    </div>
                </div>

                <el-dialog
                    title="任务办理"
                    :visible.sync="approvalDialogVisible"
                    width="640px"
                    @closed="handleApprovalDialogClosed">
                    <div v-if="approvalTask" class="center-detail-shell">
                        <div class="detail-stat-grid">
                            <div class="detail-stat-card">
                                <span class="detail-stat-label">任务编号</span>
                                <strong>{{ approvalTask.taskId || "-" }}</strong>
                            </div>
                            <div class="detail-stat-card">
                                <span class="detail-stat-label">任务名称</span>
                                <strong>{{ approvalTask.taskName || "待办任务" }}</strong>
                            </div>
                        </div>

                        <div class="detail-stat-grid">
                            <div class="detail-stat-card">
                                <span class="detail-stat-label">当前节点</span>
                                <strong>{{ approvalTask.currentTaskName || "-" }}</strong>
                            </div>
                            <div class="detail-stat-card">
                                <span class="detail-stat-label">办理人</span>
                                <strong>{{ approvalTask.assignee || "未指派" }}</strong>
                            </div>
                        </div>

                        <div class="detail-section">
                            <div class="detail-section-title">关联信息</div>
                            <div class="detail-kv-list">
                                <div class="detail-kv-item">
                                    <span>流程实例编号</span>
                                    <strong>{{ approvalTask.processInstanceId || "-" }}</strong>
                                </div>
                            </div>
                        </div>

                        <el-form label-position="top" @submit.native.prevent="submitApproval">
                            <el-form-item label="任务编号">
                                <el-input v-model.trim="form.taskId" disabled></el-input>
                            </el-form-item>
                            <el-form-item label="审批意见">
                                <el-input
                                    v-model="form.comment"
                                    type="textarea"
                                    :rows="4"
                                    placeholder="请输入审批意见">
                                </el-input>
                            </el-form-item>
                            <el-form-item label="审批结果">
                                <el-radio-group v-model="form.approvalAction">
                                    <el-radio label="PASS">通过</el-radio>
                                    <el-radio label="FAIL">不通过</el-radio>
                                    <el-radio label="REJECT">驳回</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <div class="form-actions">
                                <el-button v-if="$root.hasButton('task:approve')" type="primary" @click="submitApproval">{{ submitButtonText }}</el-button>
                                <el-button @click="approvalDialogVisible = false">取消</el-button>
                            </div>
                        </el-form>
                    </div>
                    <div v-if="!approvalTask" class="empty-panel center-empty-panel">
                        未查询到待办理任务，请关闭后重试。
                    </div>
                </el-dialog>

                <el-dialog
                    title="流程图"
                    :visible.sync="detailDialogVisible"
                    width="2000px"
                    top="5vh"
                    @opened="renderTaskDetailDiagram"
                    @closed="handleDetailDialogClosed">
                    <div v-if="selectedTask && taskDetail" class="process-detail-shell">
                        <div class="process-detail-head">
                            <div>
                                <div class="section-kicker">流程实例图谱</div>
                                <h3>{{ selectedTask.taskName || selectedTask.currentTaskName || selectedTask.taskId }}</h3>
                            </div>
                            <el-tag size="small" type="warning" effect="plain">待办任务</el-tag>
                        </div>
                        <div class="process-stat-grid">
                            <div class="process-stat-card">
                                <span class="process-stat-label">任务编号</span>
                                <strong>{{ selectedTask.taskId || "-" }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">当前任务</span>
                                <strong>{{ selectedTask.currentTaskName || selectedTask.taskName || "-" }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">节点数</span>
                                <strong>{{ taskDetail.nodeCount || 0 }}</strong>
                            </div>
                            <div class="process-stat-card">
                                <span class="process-stat-label">激活节点</span>
                                <strong>{{ activeTaskNodeIds.length }}</strong>
                            </div>
                        </div>
                        <div class="process-meta-list">
                            <span class="mini-tag">流程定义：{{ taskDetail.processDefinitionName || taskDetail.processDefinitionKey || "-" }}</span>
                            <span class="mini-tag">流程分类：{{ taskDetail.category || "未分类" }}</span>
                            <span class="mini-tag">办理人：{{ selectedTask.assignee || "未指派" }}</span>
                            <span class="mini-tag">流程实例：{{ selectedTask.processInstanceId || "-" }}</span>
                        </div>
                        <process-diagram ref="taskDetailDiagram" :detail="taskDetail" :active-node-ids="activeTaskNodeIds"></process-diagram>
                    </div>
                    <div v-else class="empty-panel center-empty-panel">
                        未加载到流程图数据，请稍后重试。
                    </div>
                </el-dialog>
            </section>
        </section>
    `,
    data: function () {
        return {
            filters: {
                assignee: "",
                taskName: "",
                taskId: "",
                processInstanceId: ""
            },
            form: {
                taskId: "",
                comment: "",
                approvalAction: "PASS"
            },
            pageNum: 1,
            pageSize: 10,
            sortProp: "taskName",
            sortOrder: "ascending",
            selectedTaskId: "",
            approvalTaskId: "",
            approvalDialogVisible: false,
            detailDialogVisible: false
        };
    },
    computed: {
        filteredTasks: function () {
            var assignee = (this.filters.assignee || "").trim().toLowerCase();
            var taskName = (this.filters.taskName || "").trim().toLowerCase();
            var taskId = (this.filters.taskId || "").trim().toLowerCase();
            var processInstanceId = (this.filters.processInstanceId || "").trim().toLowerCase();
            var list = (this.$root.taskList || []).filter(function (item) {
                var matchesAssignee = !assignee || ((item.assignee || "").toLowerCase().indexOf(assignee) >= 0);
                var matchesTaskName = !taskName || ((item.taskName || "").toLowerCase().indexOf(taskName) >= 0);
                var matchesTaskId = !taskId || ((item.taskId || "").toLowerCase().indexOf(taskId) >= 0);
                var matchesProcessInstance = !processInstanceId || ((item.processInstanceId || "").toLowerCase().indexOf(processInstanceId) >= 0);
                return matchesAssignee && matchesTaskName && matchesTaskId && matchesProcessInstance;
            });
            return this.sortItems(list);
        },
        pagedTasks: function () {
            var startIndex = (this.pageNum - 1) * this.pageSize;
            return this.filteredTasks.slice(startIndex, startIndex + this.pageSize);
        },
        selectedTask: function () {
            var taskId = this.selectedTaskId;
            var list = this.$root.taskList || [];
            for (var index = 0; index < list.length; index += 1) {
                if (list[index].taskId === taskId) {
                    return list[index];
                }
            }
            return list.length ? list[0] : null;
        },
        approvalTask: function () {
            var taskId = this.approvalTaskId;
            var list = this.$root.taskList || [];
            for (var index = 0; index < list.length; index += 1) {
                if (list[index].taskId === taskId) {
                    return list[index];
                }
            }
            return null;
        },
        taskDetail: function () {
            return this.$root.selectedProcessRequestDiagramDetail;
        },
        activeTaskNodeIds: function () {
            var detail = this.taskDetail;
            return detail && Array.isArray(detail.activeNodeIds) ? detail.activeNodeIds : [];
        },
        submitButtonText: function () {
            if (this.form.approvalAction === "FAIL") {
                return "提交不通过";
            }
            if (this.form.approvalAction === "REJECT") {
                return "提交驳回";
            }
            return "提交通过";
        }
    },
    mounted: async function () {
        this.filters.assignee = this.$root.taskAssignee || "";
        await this.$root.loadTasks(this.filters.assignee);
        this.selectFirstTask();
    },
    methods: {
        sortItems: function (list) {
            var prop = this.sortProp;
            var order = this.sortOrder;
            if (!prop || !order) {
                return list.slice();
            }
            var direction = order === "ascending" ? 1 : -1;
            return list.slice().sort(function (left, right) {
                var leftValue = left[prop] ? String(left[prop]).toLowerCase() : "";
                var rightValue = right[prop] ? String(right[prop]).toLowerCase() : "";
                if (leftValue === rightValue) {
                    return 0;
                }
                return leftValue > rightValue ? direction : -direction;
            });
        },
        resolveParallelTasks: function (row) {
            return row && Array.isArray(row.parallelTasks) ? row.parallelTasks : [];
        },
        queryTasks: async function () {
            try {
                this.$root.taskAssignee = this.filters.assignee;
                await this.$root.loadTasks(this.filters.assignee);
                this.pageNum = 1;
                this.selectFirstFromFiltered();
            } catch (error) {
                this.$root.showError(error.message || "任务查询失败");
            }
        },
        refreshTasks: async function () {
            await this.queryTasks();
        },
        resetQuery: async function () {
            this.filters.assignee = "";
            this.filters.taskName = "";
            this.filters.taskId = "";
            this.filters.processInstanceId = "";
            this.$root.taskAssignee = "";
            this.pageNum = 1;
            this.sortProp = "taskName";
            this.sortOrder = "ascending";
            await this.$root.loadTasks("");
            this.selectFirstTask();
        },
        submitApproval: async function () {
            if (!this.form.taskId) {
                this.$root.showError("任务编号不能为空");
                return;
            }
            try {
                await this.$root.approveTask({
                    taskId: this.form.taskId,
                    comment: this.form.comment,
                    approved: this.form.approvalAction === "PASS",
                    approvalAction: this.form.approvalAction
                });
                this.resetApprovalForm();
                this.approvalDialogVisible = false;
                this.approvalTaskId = "";
                this.selectFirstTask();
            } catch (error) {
                this.$root.showError(error.message || "审批处理失败");
            }
        },
        resetApprovalForm: function () {
            this.form.taskId = "";
            this.form.comment = "";
            this.form.approvalAction = "PASS";
        },
        openApproval: function (row) {
            this.selectedTaskId = row.taskId;
            this.approvalTaskId = row.taskId || "";
            this.form.taskId = row.taskId || "";
            this.form.comment = "";
            this.form.approvalAction = "PASS";
            this.approvalDialogVisible = true;
        },
        openTaskDetail: async function (row) {
            this.selectedTaskId = row.taskId;
            if (!row.processRequestId) {
                this.$root.showError("未查询到任务关联的流程申请，无法查看流程图");
                return;
            }
            try {
                await this.$root.loadProcessRequestDiagramDetail(row.processRequestId);
                this.detailDialogVisible = true;
                this.$nextTick(this.renderTaskDetailDiagram);
            } catch (error) {
                this.$root.showError(error.message || "加载流程图失败");
            }
        },
        renderTaskDetailDiagram: function () {
            if (this.$refs.taskDetailDiagram && typeof this.$refs.taskDetailDiagram.renderCanvas === "function") {
                this.$refs.taskDetailDiagram.renderCanvas();
            }
        },
        handleDetailDialogClosed: function () {
            this.$root.selectedProcessRequestDiagramDetail = null;
        },
        handleApprovalDialogClosed: function () {
            this.approvalTaskId = "";
            this.resetApprovalForm();
        },
        handleDelete: function (taskId) {
            var self = this;
            this.$confirm("删除任务后不可恢复，是否继续？", "删除任务", {
                type: "warning",
                confirmButtonText: "确定删除",
                cancelButtonText: "取消"
            }).then(async function () {
                await self.$root.deleteTaskById(taskId);
                self.selectFirstTask();
            }).catch(function (error) {
                if (error === "cancel" || error === "close") {
                    return;
                }
                self.$root.showError((error && error.message) || "任务删除失败");
            });
        },
        handleRowClick: function (row) {
            this.selectedTaskId = row.taskId;
        },
        handlePageChange: function (pageNum) {
            this.pageNum = pageNum;
        },
        handleSizeChange: function (pageSize) {
            this.pageSize = pageSize;
            this.pageNum = 1;
        },
        handleSortChange: function (payload) {
            this.sortProp = payload.prop || "taskName";
            this.sortOrder = payload.order || "ascending";
            this.pageNum = 1;
        },
        selectFirstTask: function () {
            var list = this.$root.taskList || [];
            this.selectedTaskId = list.length ? list[0].taskId : "";
        },
        selectFirstFromFiltered: function () {
            var list = this.filteredTasks;
            this.selectedTaskId = list.length ? list[0].taskId : "";
        }
    },
    watch: {
        "$root.taskList": function () {
            if (!this.selectedTaskId) {
                this.selectFirstTask();
                return;
            }
            if (!this.selectedTask) {
                this.selectFirstTask();
            }
            if (this.approvalTaskId && !this.approvalTask) {
                this.approvalDialogVisible = false;
                this.approvalTaskId = "";
                this.resetApprovalForm();
            }
            if (this.detailDialogVisible && !this.selectedTask) {
                this.detailDialogVisible = false;
                this.$root.selectedProcessRequestDiagramDetail = null;
            }
        }
    }
};
