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
                        <el-button v-if="$root.hasButton('form:save')" type="primary" @click="saveSchema">保存方案</el-button>
                        <el-button v-if="$root.hasButton('form:load')" @click="loadSchema">载入方案</el-button>
                        <el-button v-if="$root.hasButton('form:export')" @click="exportSchema">导出JSON</el-button>
                        <el-button v-if="$root.hasButton('form:import')" @click="importDialogVisible = true">导入JSON</el-button>
                        <el-button v-if="$root.hasButton('form:jump:designer')" @click="goProcessDesigner">流程设计</el-button>
                    </div>
                </div>

                <div class="process-schema-panel">
                    <div class="schema-chip-list">
                        <span class="schema-chip">字段数量：{{ formFields.length }}</span>
                        <span class="schema-chip schema-chip-action">保存位置：浏览器本地缓存</span>
                    </div>
                    <div class="helper-text">
                        表单设计器用于生成动态字段结构，后续可复制 JSON 到流程表单配置或业务扩展接口中使用。
                    </div>
                </div>

                <canvas-form-designer v-model="formFields" :height="620"></canvas-form-designer>

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
            storageKey: "wcdk_process_form_designer_schema",
            formFields: [],
            importDialogVisible: false,
            exportDialogVisible: false,
            importText: "",
            exportText: ""
        };
    },
    methods: {
        saveSchema: function () {
            window.localStorage.setItem(this.storageKey, JSON.stringify(this.formFields || []));
            this.$root.showSuccess("表单方案保存成功");
        },
        loadSchema: function () {
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
        exportSchema: function () {
            this.exportText = JSON.stringify(this.formFields || [], null, 2);
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
        goProcessDesigner: function () {
            this.$router.push("/designer");
        }
    },
    mounted: function () {
        this.loadSchema();
    }
};
