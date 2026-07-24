/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.PermissionCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">权限节点维护</div>
                        <h2>权限管理</h2>
                    </div>
                    <el-button v-if="$root.hasButton('sys:permission:add')" type="primary" @click="openCreate">新增权限</el-button>
                </div>
                <el-form inline class="process-filter-form" @submit.native.prevent="queryList">
                    <el-form-item label="权限名称">
                        <el-input v-model.trim="filters.permissionName" placeholder="请输入权限名称"></el-input>
                    </el-form-item>
                    <el-form-item label="类型">
                        <el-select v-model="filters.permissionType" clearable placeholder="请选择类型">
                            <el-option label="菜单" value="MENU"></el-option>
                            <el-option label="按钮" value="BUTTON"></el-option>
                            <el-option label="标签页" value="TAB"></el-option>
                            <el-option label="标签" value="TAG"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="父级权限">
                        <el-select
                            v-model="filters.parentId"
                            clearable
                            filterable
                            placeholder="请输入或选择父级权限">
                            <el-option
                                v-for="item in $root.permissionOptions"
                                :key="item.id"
                                :label="item.permissionName"
                                :value="item.id">
                            </el-option>
                        </el-select>
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
                <el-table
                    :data="records"
                    stripe>
                    <el-table-column prop="permissionCode" label="权限编码" min-width="180"></el-table-column>
                    <el-table-column prop="permissionName" label="权限名称" min-width="180"></el-table-column>
                    <el-table-column prop="permissionType" label="类型" width="100"></el-table-column>
                    <el-table-column prop="icon" label="图标" min-width="140"></el-table-column>
                    <el-table-column prop="sortNo" label="排序号" width="100"></el-table-column>
                    <el-table-column prop="routePath" label="路由地址" min-width="180"></el-table-column>
                    <el-table-column prop="parentPermissionName" label="父级权限" min-width="160"></el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                        <template slot-scope="scope">
                            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" min-width="180" fixed="right">
                        <template slot-scope="scope">
                            <div class="table-operations">
                                <el-button v-if="$root.hasButton('sys:permission:edit')" type="text" @click="openEdit(scope.row)">编辑</el-button>
                                <el-button v-if="$root.hasButton('sys:permission:delete')" type="text" style="color:#f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
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
                <el-dialog :title="editingId ? '编辑权限' : '新增权限'" :visible.sync="dialogVisible" width="720px" @closed="resetForm">
                    <el-form label-position="top">
                        <div class="form-grid two-columns">
                            <el-form-item label="权限编码">
                                <el-input v-model.trim="form.permissionCode" placeholder="请输入权限编码"></el-input>
                            </el-form-item>
                            <el-form-item label="权限名称">
                                <el-input v-model.trim="form.permissionName" placeholder="请输入权限名称"></el-input>
                            </el-form-item>
                            <el-form-item label="权限类型">
                                <el-select v-model="form.permissionType">
                                    <el-option label="菜单" value="MENU"></el-option>
                                    <el-option label="按钮" value="BUTTON"></el-option>
                                    <el-option label="标签页" value="TAB"></el-option>
                                    <el-option label="标签" value="TAG"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="父级权限">
                            
                            <el-select
                            v-model="filters.parentId"
                            clearable
                            filterable
                            placeholder="请输入或选择父级权限">
                             <el-option v-for="item in $root.permissionOptions" :key="item.id" :label="item.permissionName" :value="item.id"></el-option>

                        </el-select>
                            
<!--                                <el-select v-model="form.parentId" clearable placeholder="请选择父级权限">-->
<!--                                    <el-option v-for="item in $root.permissionOptions" :key="item.id" :label="item.permissionName" :value="item.id"></el-option>-->
<!--                                </el-select>-->
                            </el-form-item>
                            <el-form-item label="排序号">
                                <el-input v-model.number="form.sortNo" type="number" placeholder="请输入排序号"></el-input>
                            </el-form-item>
                            <el-form-item label="路由地址">
                                <el-input v-model.trim="form.routePath" placeholder="菜单权限可填写路由"></el-input>
                            </el-form-item>
                            <el-form-item label="图标">
                                <el-input v-model.trim="form.icon" placeholder="菜单图标，例如 el-icon-house"></el-input>
                            </el-form-item>
                            <el-form-item label="状态">
                                <el-select v-model="form.status">
                                    <el-option label="启用" :value="1"></el-option>
                                    <el-option label="停用" :value="0"></el-option>
                                </el-select>
                            </el-form-item>
                        </div>
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
            filters: {
                permissionName: "",
                permissionType: "",
                parentId: "",
                status: ""
            },
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
            dialogVisible: false,
            editingId: null,
            form: {
                permissionCode: "",
                permissionName: "",
                permissionType: "MENU",
                parentId: "",
                routePath: "",
                icon: "",
                sortNo: 0,
                status: 1,
                remark: ""
            }
        };
    },
    mounted: async function () {
        await this.$root.loadPermissionsOption();
        this.queryList();
    },
    methods: {
        queryList: async function () {
            var query = "?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize;
            if (this.filters.permissionName) {
                query += "&permissionName=" + encodeURIComponent(this.filters.permissionName);
            }
            if (this.filters.permissionType) {
                query += "&permissionType=" + encodeURIComponent(this.filters.permissionType);
            }
            if (this.filters.parentId) {
                query += "&parentId=" + encodeURIComponent(this.filters.parentId);
            }
            if (this.filters.status !== "" && this.filters.status !== null) {
                query += "&status=" + encodeURIComponent(this.filters.status);
            }
            var result = await window.AppService.request("/sys/permission/list" + query);
            var pageData = result.data || {};
            this.records = pageData.records || [];
            this.total = Number(pageData.total || 0);
        },
        resetQuery: function () {
            this.filters = {
                permissionName: "",
                permissionType: "",
                parentId: "",
                status: ""
            };
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
        },
        openEdit: function (row) {
            this.editingId = row.id;
            this.form = {
                permissionCode: row.permissionCode,
                permissionName: row.permissionName,
                permissionType: row.permissionType,
                parentId: row.parentId,
                routePath: row.routePath,
                icon: row.icon,
                sortNo: row.sortNo,
                status: row.status,
                remark: row.remark
            };
            this.dialogVisible = true;
        },
        resetForm: function () {
            this.form = {
                permissionCode: "",
                permissionName: "",
                permissionType: "MENU",
                parentId: "",
                routePath: "",
                icon: "",
                sortNo: 0,
                status: 1,
                remark: ""
            };
            this.editingId = null;
        },
        submitForm: async function () {
            var url = this.editingId ? ("/sys/permission/" + this.editingId) : "/sys/permission";
            var method = this.editingId ? "PUT" : "POST";
            await window.AppService.requestJson(url, {
                method: method,
                body: JSON.stringify(this.form)
            });
            this.$root.showSuccess(this.editingId ? "权限修改成功" : "权限新增成功");
            await this.$root.loadPermissionsOption();
            this.dialogVisible = false;
            this.resetForm();
            this.queryList();
        },
        handleDelete: function (row) {
            var self = this;
            this.$confirm("删除权限后不可恢复，是否继续？", "删除权限", { type: "warning" }).then(async function () {
                await window.AppService.request("/sys/permission/" + row.id, { method: "DELETE" });
                self.$root.showSuccess("权限删除成功");
                await self.$root.loadPermissionsOption();
                self.queryList();
            }).catch(function () {});
        }
    }
};
