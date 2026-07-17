/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.UserCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">用户账号维护</div>
                        <h2>用户管理</h2>
                    </div>
                    <el-button v-if="$root.hasButton('sys:user:add')" type="primary" @click="openCreate">新增用户</el-button>
                </div>
                <el-form inline class="process-filter-form" @submit.native.prevent="queryList">
                    <el-form-item label="用户名"><el-input v-model.trim="filters.username" placeholder="请输入用户名"></el-input></el-form-item>
                    <el-form-item label="姓名"><el-input v-model.trim="filters.realName" placeholder="请输入姓名"></el-input></el-form-item>
                    <el-form-item label="部门">
                        <el-select v-model="filters.deptId" clearable placeholder="请选择部门">
                            <el-option v-for="item in $root.deptOptions" :key="item.id" :label="item.deptName" :value="item.id"></el-option>
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
                <el-table :data="records" stripe>
                    <el-table-column prop="username" label="用户名" min-width="140"></el-table-column>
                    <el-table-column prop="realName" label="姓名" min-width="120"></el-table-column>
                    <el-table-column prop="deptName" label="部门" min-width="140"></el-table-column>
                    <el-table-column prop="roleNames" label="角色" min-width="220">
                        <template slot-scope="scope">{{ (scope.row.roleNames || []).join('、') || '-' }}</template>
                    </el-table-column>
                    <el-table-column prop="mobile" label="手机号" min-width="140"></el-table-column>
                    <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
                    <el-table-column prop="status" label="状态" width="100">
                        <template slot-scope="scope"><el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag></template>
                    </el-table-column>
                    <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="180">
                        <template slot-scope="scope">{{ formatDateTime(scope.row.lastLoginTime) }}</template>
                    </el-table-column>
                    <el-table-column label="操作" min-width="180" fixed="right">
                        <template slot-scope="scope">
                            <div class="table-operations">
                                <el-button v-if="$root.hasButton('sys:user:edit')" type="text" @click="openEdit(scope.row)">编辑</el-button>
                                <el-button v-if="$root.hasButton('sys:user:delete')" type="text" style="color:#f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="process-pagination">
                    <el-pagination background layout="total, sizes, prev, pager, next" :current-page="pageNum" :page-size="pageSize" :page-sizes="[10,20,50,100]" :total="total" @current-change="handlePageChange" @size-change="handleSizeChange"></el-pagination>
                </div>
                <el-dialog :title="editingId ? '编辑用户' : '新增用户'" :visible.sync="dialogVisible" width="720px" @closed="resetForm">
                    <el-form label-position="top">
                        <div class="form-grid two-columns">
                            <el-form-item label="用户名"><el-input v-model.trim="form.username" placeholder="请输入用户名"></el-input></el-form-item>
                            <el-form-item label="姓名"><el-input v-model.trim="form.realName" placeholder="请输入姓名"></el-input></el-form-item>
                            <el-form-item label="密码"><el-input v-model.trim="form.password" show-password :placeholder="editingId ? '不修改请留空' : '请输入初始密码'"></el-input></el-form-item>
                            <el-form-item label="部门">
                                <el-select v-model="form.deptId" clearable placeholder="请选择部门">
                                    <el-option v-for="item in $root.deptOptions" :key="item.id" :label="item.deptName" :value="item.id"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="手机号"><el-input v-model.trim="form.mobile" placeholder="请输入手机号"></el-input></el-form-item>
                            <el-form-item label="邮箱"><el-input v-model.trim="form.email" placeholder="请输入邮箱"></el-input></el-form-item>
                            <el-form-item label="状态">
                                <el-select v-model="form.status" placeholder="请选择状态">
                                    <el-option label="启用" :value="1"></el-option>
                                    <el-option label="停用" :value="0"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="角色">
                                <el-select v-model="form.roleIds" multiple collapse-tags placeholder="请选择角色">
                                    <el-option v-for="item in $root.roleOptions" :key="item.id" :label="item.roleName" :value="item.id"></el-option>
                                </el-select>
                            </el-form-item>
                        </div>
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
            filters: { username: "", realName: "", deptId: "", status: "" },
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
            dialogVisible: false,
            editingId: null,
            form: { username: "", realName: "", password: "", deptId: "", mobile: "", email: "", status: 1, roleIds: [] }
        };
    },
    mounted: function () { this.queryList(); },
    methods: {
        queryList: async function () {
            var query = "?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize;
            if (this.filters.username) { query += "&username=" + encodeURIComponent(this.filters.username); }
            if (this.filters.realName) { query += "&realName=" + encodeURIComponent(this.filters.realName); }
            if (this.filters.deptId) { query += "&deptId=" + encodeURIComponent(this.filters.deptId); }
            if (this.filters.status !== "" && this.filters.status !== null) { query += "&status=" + encodeURIComponent(this.filters.status); }
            var result = await window.AppService.request("/sys/user/list" + query);
            var pageData = result.data || {};
            this.records = pageData.records || [];
            this.total = Number(pageData.total || 0);
        },
        resetQuery: function () {
            this.filters = { username: "", realName: "", deptId: "", status: "" };
            this.pageNum = 1;
            this.queryList();
        },
        handlePageChange: function (pageNum) { this.pageNum = pageNum; this.queryList(); },
        handleSizeChange: function (pageSize) { this.pageSize = pageSize; this.pageNum = 1; this.queryList(); },
        openCreate: function () { this.editingId = null; this.dialogVisible = true; },
        openEdit: function (row) {
            this.editingId = row.id;
            this.form = { username: row.username, realName: row.realName, password: "", deptId: row.deptId, mobile: row.mobile, email: row.email, status: row.status, roleIds: (row.roleIds || []).slice() };
            this.dialogVisible = true;
        },
        resetForm: function () {
            this.form = { username: "", realName: "", password: "", deptId: "", mobile: "", email: "", status: 1, roleIds: [] };
            this.editingId = null;
        },
        submitForm: async function () {
            var url = this.editingId ? ("/sys/user/" + this.editingId) : "/sys/user";
            var method = this.editingId ? "PUT" : "POST";
            await window.AppService.requestJson(url, { method: method, body: JSON.stringify(this.form) });
            this.$root.showSuccess(this.editingId ? "用户修改成功" : "用户新增成功");
            this.dialogVisible = false;
            this.resetForm();
            this.queryList();
        },
        handleDelete: function (row) {
            var self = this;
            this.$confirm("删除用户后不可恢复，是否继续？", "删除用户", { type: "warning" }).then(async function () {
                await window.AppService.request("/sys/user/" + row.id, { method: "DELETE" });
                self.$root.showSuccess("用户删除成功");
                self.queryList();
            }).catch(function () {});
        },
        formatDateTime: function (value) {
            return window.AppService.formatDateTime(value);
        }
    }
};
