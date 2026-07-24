/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.RoleCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">角色授权维护</div>
                        <h2>角色管理</h2>
                    </div>
                    <el-button v-if="$root.hasButton('sys:role:add')" type="primary" @click="openCreate">新增角色</el-button>
                </div>
                <el-form inline class="process-filter-form" @submit.native.prevent="queryList">
                    <el-form-item label="角色名称">
                        <el-input v-model.trim="filters.roleName" placeholder="请输入角色名称"></el-input>
                    </el-form-item>
                    <el-form-item label="状态">
                        <el-select v-model="filters.status" clearable placeholder="请选择状态">
                            <el-option label="启用" :value="1"></el-option>
                            <el-option label="停用" :value="0"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="queryList">查询</el-button>
                        <el-button @click="resetQuery">重置</el-button>
                    </el-form-item>
                </el-form>
                <el-table :data="records" stripe>
                    <el-table-column prop="roleCode" label="角色编码" min-width="140"></el-table-column>
                    <el-table-column prop="roleName" label="角色名称" min-width="140"></el-table-column>
                    <el-table-column prop="permissionNames" label="绑定权限" min-width="280">
                        <template slot-scope="scope">{{ (scope.row.permissionNames || []).join('、') || '-' }}</template>
                    </el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                        <template slot-scope="scope">
                            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" min-width="180" fixed="right">
                        <template slot-scope="scope">
                            <div class="table-operations">
                                <el-button v-if="$root.hasButton('sys:role:edit')" type="text" @click="openEdit(scope.row)">编辑</el-button>
                                <el-button v-if="$root.hasButton('sys:role:delete')" type="text" style="color:#f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
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
                        :page-sizes="[10,20,50,100]"
                        :total="total"
                        @current-change="handlePageChange"
                        @size-change="handleSizeChange">
                    </el-pagination>
                </div>
                <el-dialog :title="editingId ? '编辑角色' : '新增角色'" :visible.sync="dialogVisible" width="720px" @closed="resetForm">
                    <el-form label-position="top">
                        <div class="form-grid two-columns">
                            <el-form-item label="角色编码">
                                <el-input v-model.trim="form.roleCode" placeholder="请输入角色编码"></el-input>
                            </el-form-item>
                            <el-form-item label="角色名称">
                                <el-input v-model.trim="form.roleName" placeholder="请输入角色名称"></el-input>
                            </el-form-item>
                            <el-form-item label="状态">
                                <el-select v-model="form.status">
                                    <el-option label="启用" :value="1"></el-option>
                                    <el-option label="停用" :value="0"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="排序">
                                <el-input v-model.number="form.sortNo" type="number" placeholder="请输入排序"></el-input>
                            </el-form-item>
                        </div>
                        <el-form-item label="权限集合">
                            <el-popover
                                placement="bottom-start"
                                width="420"
                                trigger="click"
                                v-model="permissionTreeVisible">
                                <div class="permission-tree-panel">
                                    <div class="helper-text">可展开树节点并勾选权限，父子节点支持联动选择。</div>
                                    <el-tree
                                        ref="permissionTree"
                                        node-key="id"
                                        show-checkbox
                                        :data="permissionTreeData"
                                        :props="permissionTreeProps"
                                        @check="handlePermissionTreeCheck">
                                    </el-tree>
                                </div>
                                <el-input
                                    slot="reference"
                                    :value="selectedPermissionNamesText"
                                    readonly
                                    placeholder="请选择权限集合">
                                </el-input>
                            </el-popover>
                        </el-form-item>
                        <el-form-item label="备注">
                            <el-input v-model.trim="form.remark" type="textarea" :rows="3" placeholder="请输入备注"></el-input>
                        </el-form-item>
                        <div class="form-actions">
                            <el-button type="primary" @click="submitForm">保存</el-button>
                            <el-button @click="dialogVisible = false">取消</el-button>
                        </div>
                    </el-form>
                </el-dialog>
            </section>
        </section>
    `,
    data: function () {
        return {
            filters: { roleName: "", status: "" },
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
            dialogVisible: false,
            editingId: null,
            permissionTreeVisible: false,
            permissionTreeProps: {
                label: "permissionName",
                children: "children"
            },
            form: {
                roleCode: "",
                roleName: "",
                sortNo: 0,
                status: 1,
                remark: "",
                permissionIds: []
            }
        };
    },
    computed: {
        permissionTreeData: function () {
            var options = this.$root.permissionOptions || [];
            var nodeMap = {};
            var tree = [];
            options.forEach(function (item) {
                nodeMap[item.id] = {
                    id: item.id,
                    parentId: item.parentId,
                    permissionName: item.permissionName + "（" + item.permissionCode + "）",
                    children: []
                };
            });
            Object.keys(nodeMap).forEach(function (key) {
                var node = nodeMap[key];
                if (node.parentId && nodeMap[node.parentId]) {
                    nodeMap[node.parentId].children.push(node);
                } else {
                    tree.push(node);
                }
            });
            return tree;
        },
        selectedPermissionNamesText: function () {
            var permissionIds = this.form.permissionIds || [];
            var options = this.$root.permissionOptions || [];
            var selectedNames = options.filter(function (item) {
                return permissionIds.indexOf(item.id) >= 0;
            }).map(function (item) {
                return item.permissionName;
            });
            return selectedNames.join("、");
        }
    },
    mounted: async function () {
        await this.$root.loadPermissionsOption();
        this.queryList();
    },
    methods: {
        queryList: async function () {
            var query = "?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize;
            if (this.filters.roleName) {
                query += "&roleName=" + encodeURIComponent(this.filters.roleName);
            }
            if (this.filters.status !== "" && this.filters.status !== null) {
                query += "&status=" + encodeURIComponent(this.filters.status);
            }
            var result = await window.AppService.request("/sys/role/list" + query);
            var pageData = result.data || {};
            this.records = pageData.records || [];
            this.total = Number(pageData.total || 0);
        },
        resetQuery: function () {
            this.filters = { roleName: "", status: "" };
            this.pageNum = 1;
            this.queryList();
        },
        handlePageChange: function (pageNum) {
            this.pageNum = pageNum;
            this.queryList();
        },
        handleSizeChange: function (pageSize) {
            this.pageSize = pageSize;
            this.pageNum = 1;
            this.queryList();
        },
        openCreate: function () {
            this.editingId = null;
            this.dialogVisible = true;
            this.permissionTreeVisible = false;
            this.$nextTick(this.syncPermissionTreeCheckedKeys);
        },
        openEdit: function (row) {
            this.editingId = row.id;
            this.form = {
                roleCode: row.roleCode,
                roleName: row.roleName,
                sortNo: row.sortNo,
                status: row.status,
                remark: row.remark,
                permissionIds: (row.permissionIds || []).slice()
            };
            this.dialogVisible = true;
            this.permissionTreeVisible = false;
            this.$nextTick(this.syncPermissionTreeCheckedKeys);
        },
        resetForm: function () {
            this.form = {
                roleCode: "",
                roleName: "",
                sortNo: 0,
                status: 1,
                remark: "",
                permissionIds: []
            };
            this.editingId = null;
            this.permissionTreeVisible = false;
            if (this.$refs.permissionTree) {
                this.$refs.permissionTree.setCheckedKeys([]);
            }
        },
        syncPermissionTreeCheckedKeys: function () {
            if (this.$refs.permissionTree) {
                this.$refs.permissionTree.setCheckedKeys(this.form.permissionIds || []);
            }
        },
        handlePermissionTreeCheck: function () {
            if (!this.$refs.permissionTree) {
                return;
            }
            this.form.permissionIds = this.$refs.permissionTree.getCheckedKeys();
        },
        submitForm: async function () {
            var url = this.editingId ? ("/sys/role/" + this.editingId) : "/sys/role";
            var method = this.editingId ? "PUT" : "POST";
            await window.AppService.requestJson(url, { method: method, body: JSON.stringify(this.form) });
            this.$root.showSuccess(this.editingId ? "角色修改成功" : "角色新增成功");
            await this.$root.loadRolesOption();
            this.dialogVisible = false;
            this.resetForm();
            this.queryList();
        },
        handleDelete: function (row) {
            var self = this;
            this.$confirm("删除角色后不可恢复，是否继续？", "删除角色", { type: "warning" }).then(async function () {
                await window.AppService.request("/sys/role/" + row.id, { method: "DELETE" });
                self.$root.showSuccess("角色删除成功");
                await self.$root.loadRolesOption();
                self.queryList();
            }).catch(function () {});
        }
    },
    watch: {
        dialogVisible: function (visible) {
            if (visible) {
                this.$nextTick(this.syncPermissionTreeCheckedKeys);
            }
        },
        "$root.permissionOptions": function () {
            this.$nextTick(this.syncPermissionTreeCheckedKeys);
        }
    }
};
