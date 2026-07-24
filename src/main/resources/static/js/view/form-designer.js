/**
 * @auther WCDK
 * @date 2026/7/20
 * @version 1.0
 **/
window.FormDesigner = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">动态表单配置</div>
                        <h2>表单设计</h2>
                    </div>
                    <div class="header-actions">
                        <el-button v-if="activeTab === 'design' && $root.hasButton('form:save')" type="primary" @click="saveSchema">保存方案</el-button>
<!--                        <el-button v-if="activeTab === 'design' && $root.hasButton('form:load')" @click="loadLatestSchema">载入方案</el-button>-->
                        <el-button v-if="activeTab === 'design' && $root.hasButton('form:export')" @click="exportSchema">导出JSON</el-button>
                        <el-button v-if="activeTab === 'design' && $root.hasButton('form:import')" @click="importDialogVisible = true">导入JSON</el-button>
                        <el-button v-if="activeTab === 'design' && $root.hasButton('form:jump:designer')" @click="goProcessDesigner">流程设计</el-button>
                        <el-button v-if="activeTab === 'list'" @click="refreshFormList">刷新</el-button>
                    </div>
                </div>

                <el-tabs v-model="activeTab" class="center-tabs" @tab-click="handleTabClick">
                    <el-tab-pane label="表单设计" name="design">
                        <el-form label-position="top" class="panel-form" @submit.native.prevent="saveSchema">
                            <div class="form-grid two-columns">
                                <el-form-item label="表单标识">
                                    <el-input v-model.trim="formMeta.formKey" placeholder="请输入表单标识"></el-input>
                                </el-form-item>
                                <el-form-item label="表单名称">
                                    <el-input v-model.trim="formMeta.formName" placeholder="请输入表单名称"></el-input>
                                </el-form-item>
                            </div>
                        </el-form>

                        <div class="process-schema-panel">
                            <div class="schema-chip-list">
                                <span class="schema-chip">字段数量：{{ formFields.length }}</span>
<!--                                <span class="schema-chip schema-chip-action">保存位置：浏览器本地缓存</span>-->
                            </div>
                            <div class="helper-text">
                                表单设计器用于生成动态字段结构，保存后可在表单列表中载入、导出或删除。
                            </div>
                        </div>

                        <canvas-form-designer v-model="formFields" :height="620"></canvas-form-designer>
                    </el-tab-pane>

                    <el-tab-pane label="表单列表" name="list">
                        <div class="process-list-panel">
                            <el-form inline class="process-filter-form" @submit.native.prevent="handleQuery">
                                <el-form-item label="表单名称">
                                    <el-input v-model.trim="filters.formName" placeholder="请输入表单名称"></el-input>
                                </el-form-item>
                                <el-form-item label="表单标识">
                                    <el-input v-model.trim="filters.formKey" placeholder="请输入表单标识"></el-input>
                                </el-form-item>
                                <el-form-item label="已绑定流程">
                                    <el-select v-model="filters.boundProcess" clearable placeholder="请选择">
                                        <el-option label="已绑定" :value="true"></el-option>
                                        <el-option label="未绑定" :value="false"></el-option>
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="流程节点">
                                    <el-input v-model.trim="filters.processNode" placeholder="请输入节点ID或名称"></el-input>
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="handleQuery">查询</el-button>
                                    <el-button @click="handleResetQuery">重置</el-button>
                                </el-form-item>
                            </el-form>

                            <el-table
                                v-loading="listLoading"
                                :data="formRecords"
                                stripe
                                @sort-change="handleSortChange">
                                <el-table-column prop="formName" label="表单名称" min-width="180" sortable="custom"></el-table-column>
                                <el-table-column prop="formKey" label="表单标识" min-width="180" sortable="custom"></el-table-column>
                                <el-table-column prop="boundProcess" label="已绑定流程" width="120" sortable="custom">
                                    <template slot-scope="scope">
                                        <el-tag :type="scope.row.boundProcess ? 'success' : 'info'" size="mini">
                                            {{ scope.row.boundProcess ? '已绑定' : '未绑定' }}
                                        </el-tag>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="processDefinitionId" label="流程定义ID" min-width="220" sortable="custom" show-overflow-tooltip></el-table-column>
                                <el-table-column prop="processNodeId" label="流程节点ID" min-width="160" sortable="custom" show-overflow-tooltip></el-table-column>
                                <el-table-column prop="processNodeName" label="流程节点名称" min-width="160" sortable="custom" show-overflow-tooltip></el-table-column>
                                <el-table-column prop="fieldCount" label="字段数量" width="110" sortable="custom"></el-table-column>
                                <el-table-column prop="updateTime" label="更新时间" min-width="180" sortable="custom">
                                    <template slot-scope="scope">
                                        {{ formatDateTime(scope.row.updateTime) }}
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" min-width="220" fixed="right">
                                    <template slot-scope="scope">
                                        <div class="table-operations">
                                            <el-button v-if="$root.hasButton('form:load')" type="text" @click.stop="loadSchemaRecord(scope.row)">载入</el-button>
                                            <el-button v-if="$root.hasButton('form:export')" type="text" @click.stop="exportSchemaRecord(scope.row)">导出</el-button>
                                            <el-button v-if="$root.hasButton('form:save')" type="text" style="color: #f56c6c;" @click.stop="deleteSchemaRecord(scope.row)">删除</el-button>
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
                                    :total="total"
                                    @current-change="handlePageChange"
                                    @size-change="handleSizeChange">
                                </el-pagination>
                            </div>
                        </div>
                    </el-tab-pane>
                </el-tabs>

                <el-dialog title="导入表单JSON" :visible.sync="importDialogVisible" width="720px">
                    <el-input
                        v-model="importText"
                        type="textarea"
                        :rows="16"
                        placeholder="请输入字段数组JSON">
                    </el-input>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="importDialogVisible = false">取消</el-button>
                        <el-button v-if="$root.hasButton('form:import')" type="primary" @click="confirmImport">确定导入</el-button>
                    </span>
                </el-dialog>

                <el-dialog title="表单JSON" :visible.sync="exportDialogVisible" width="720px">
                    <pre class="xml-preview">{{ exportText }}</pre>
                    <span slot="footer" class="dialog-footer">
                        <el-button @click="exportDialogVisible = false">关闭</el-button>
                    </span>
                </el-dialog>
            </section>
        </section>
    `,
    data: function () {
        return {
            activeTab: "design",
            listLoaded: false,
            storageKey: "wcdk_process_form_designer_schema",
            listStorageKey: "wcdk_process_form_designer_schema_list",
            formMeta: {
                formKey: "",
                formName: ""
            },
            formFields: [],
            formRecords: [],
            filters: {
                formName: "",
                formKey: "",
                boundProcess: "",
                processNode: ""
            },
            pageNum: 1,
            pageSize: 10,
            total: 0,
            sortProp: "updateTime",
            sortOrder: "descending",
            listLoading: false,
            importDialogVisible: false,
            exportDialogVisible: false,
            importText: "",
            exportText: ""
        };
    },
    computed: {
        filteredForms: function () {
            var formName = (this.filters.formName || "").trim().toLowerCase();
            var formKey = (this.filters.formKey || "").trim().toLowerCase();
            var records = this.formRecords.filter(function (item) {
                var nameMatched = !formName || String(item.formName || "").toLowerCase().indexOf(formName) >= 0;
                var keyMatched = !formKey || String(item.formKey || "").toLowerCase().indexOf(formKey) >= 0;
                return nameMatched && keyMatched;
            });
            return this.sortItems(records);
        },
        pagedForms: function () {
            var startIndex = (this.pageNum - 1) * this.pageSize;
            return this.filteredForms.slice(startIndex, startIndex + this.pageSize);
        }
    },
    methods: {
        formatDateTime: window.AppService.formatDateTime,
        saveSchema: async function () {
            var formKey = (this.formMeta.formKey || "").trim();
            var formName = (this.formMeta.formName || "").trim();
            if (!formKey) {
                this.$root.showError("表单标识不能为空");
                return;
            }
            if (!formName) {
                this.$root.showError("表单名称不能为空");
                return;
            }
            var fields = this.cloneFields(this.formFields);
            await window.AppService.requestJson("/process/form", {
                method: "POST",
                body: JSON.stringify({
                    formKey: formKey,
                    formName: formName,
                    schema: fields
                })
            });
            window.localStorage.setItem(this.storageKey, JSON.stringify(fields));
            this.listLoaded = false;
            this.$root.showSuccess("表单方案保存成功");
            this.confirmClearCurrentSchema();
        },
        // loadLatestSchema: function () {
        //     this.refreshFormList();
        //     if (this.formRecords.length) {
        //         this.loadSchemaRecord(this.sortItems(this.formRecords)[0]);
        //         return;
        //     }
        //     this.loadLegacySchema();
        // },
        loadLegacySchema: function () {
            var rawText = window.localStorage.getItem(this.storageKey) || "[]";
            try {
                var schema = JSON.parse(rawText);
                if (!Array.isArray(schema)) {
                    throw new Error("表单方案格式不正确");
                }
                this.formFields = schema;
                this.$root.showSuccess("表单方案载入成功");
            } catch (error) {
                this.$root.showError(error.message || "表单方案载入失败");
            }
        },
        loadSchemaRecord: function (record) {
            if (!record) {
                return;
            }
            this.formMeta.formKey = record.formKey || "";
            this.formMeta.formName = record.formName || "";
            this.formFields = this.cloneFields(record.schema || []);
            window.localStorage.setItem(this.storageKey, JSON.stringify(this.formFields));
            this.activeTab = "design";
            this.$root.showSuccess("表单方案载入成功");
        },
        exportSchema: function () {
            this.exportText = JSON.stringify(this.formFields || [], null, 2);
            this.exportDialogVisible = true;
        },
        exportSchemaRecord: function (record) {
            this.exportText = JSON.stringify(record && record.schema ? record.schema : [], null, 2);
            this.exportDialogVisible = true;
        },
        confirmImport: function () {
            try {
                var schema = JSON.parse(this.importText || "[]");
                if (!Array.isArray(schema)) {
                    throw new Error("导入内容必须是字段数组");
                }
                this.formFields = schema;
                this.importDialogVisible = false;
                this.$root.showSuccess("表单方案导入成功");
            } catch (error) {
                this.$root.showError(error.message || "表单方案导入失败");
            }
        },
        deleteSchemaRecord: function (record) {
            if (!record) {
                return;
            }
            var self = this;
            this.$confirm("删除后将移除该表单方案，是否继续？", "删除表单方案", {
                type: "warning",
                confirmButtonText: "确定删除",
                cancelButtonText: "取消"
            }).then(async function () {
                await window.AppService.request("/process/form/" + record.id, { method: "DELETE" });
                await self.refreshFormList();
                self.$root.showSuccess("表单方案删除成功");
            }).catch(function () {});
        },
        refreshFormList: async function () {
            var query = "?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize;
            if (this.filters.formName) {
                query += "&formName=" + encodeURIComponent(this.filters.formName);
            }
            if (this.filters.formKey) {
                query += "&formKey=" + encodeURIComponent(this.filters.formKey);
            }
            if (this.filters.boundProcess !== "" && this.filters.boundProcess !== null && this.filters.boundProcess !== undefined) {
                query += "&boundProcess=" + encodeURIComponent(this.filters.boundProcess);
            }
            if (this.filters.processNode) {
                query += "&processNode=" + encodeURIComponent(this.filters.processNode);
            }
            this.listLoading = true;
            try {
                var result = await window.AppService.request("/process/form/list" + query);
                var pageData = result.data || {};
                this.formRecords = pageData.records || [];
                this.total = Number(pageData.total || 0);
                this.listLoaded = true;
                if (this.total > 0 && (this.pageNum - 1) * this.pageSize >= this.total) {
                    this.pageNum = 1;
                    await this.refreshFormList();
                }
            } finally {
                this.listLoading = false;
            }
        },
        handleQuery: function () {
            this.pageNum = 1;
            this.refreshFormList();
        },
        handleTabClick: function () {
            if (this.activeTab === "list" && !this.listLoaded) {
                this.refreshFormList();
            }
        },
        handleResetQuery: function () {
            this.filters.formName = "";
            this.filters.formKey = "";
            this.filters.boundProcess = "";
            this.filters.processNode = "";
            this.pageNum = 1;
            this.sortProp = "updateTime";
            this.sortOrder = "descending";
            this.refreshFormList();
        },
        handlePageChange: function (pageNum) {
            this.pageNum = pageNum;
            this.refreshFormList();
        },
        handleSizeChange: function (pageSize) {
            this.pageSize = pageSize;
            this.pageNum = 1;
            this.refreshFormList();
        },
        handleSortChange: function (payload) {
            this.sortProp = payload.prop || "updateTime";
            this.sortOrder = payload.order || "descending";
            this.pageNum = 1;
            this.formRecords = this.sortItems(this.formRecords);
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
                if (prop === "fieldCount") {
                    leftValue = Number(leftValue || 0);
                    rightValue = Number(rightValue || 0);
                } else if (prop === "boundProcess") {
                    leftValue = leftValue ? 1 : 0;
                    rightValue = rightValue ? 1 : 0;
                } else if (prop === "createTime" || prop === "updateTime") {
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
        readFormRecords: function () {
            var rawText = window.localStorage.getItem(this.listStorageKey) || "[]";
            try {
                var records = JSON.parse(rawText);
                if (!Array.isArray(records)) {
                    return [];
                }
                return records.map(function (item) {
                    var schema = Array.isArray(item.schema) ? item.schema : [];
                    return {
                        formKey: item.formKey || "",
                        formName: item.formName || "",
                        fieldCount: schema.length,
                        schema: schema,
                        createTime: item.createTime || "",
                        updateTime: item.updateTime || item.createTime || ""
                    };
                }).filter(function (item) {
                    return item.formKey;
                });
            } catch (error) {
                return [];
            }
        },
        writeFormRecords: function (records) {
            window.localStorage.setItem(this.listStorageKey, JSON.stringify(records || []));
        },
        cloneFields: function (fields) {
            return JSON.parse(JSON.stringify(fields || []));
        },
        confirmClearCurrentSchema: function () {
            var self = this;
            this.$confirm("表单方案已保存，是否清空当前页面内容？", "清空当前页面", {
                type: "warning",
                confirmButtonText: "确定清空",
                cancelButtonText: "保留内容"
            }).then(function () {
                self.formMeta.formKey = "";
                self.formMeta.formName = "";
                self.formFields = [];
                window.localStorage.setItem(self.storageKey, "[]");
            }).catch(function () {});
        },
        goProcessDesigner: function () {
            this.$router.push("/designer");
        }
    },
    mounted: function () {
        // this.loadLatestSchema();
    }
};
