/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.DeptCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head"><div><div class="section-kicker">部门组织维护</div><h2>部门管理</h2></div><el-button v-if="$root.hasPermission('sys:dept:add')" type="primary" @click="openCreate">新增部门</el-button></div>
                <el-form inline class="process-filter-form" @submit.native.prevent="queryList">
                    <el-form-item label="部门名称"><el-input v-model.trim="filters.deptName" placeholder="请输入部门名称"></el-input></el-form-item>
                    <el-form-item label="状态"><el-select v-model="filters.status" clearable placeholder="请选择状态"><el-option label="启用" :value="1"></el-option><el-option label="停用" :value="0"></el-option></el-select></el-form-item>
                    <el-form-item><el-button type="primary" @click="queryList">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
                </el-form>
                <el-table :data="records" stripe>
                    <el-table-column prop="deptCode" label="部门编码" min-width="140"></el-table-column>
                    <el-table-column prop="deptName" label="部门名称" min-width="160"></el-table-column>
                    <el-table-column prop="parentDeptName" label="上级部门" min-width="160"></el-table-column>
                    <el-table-column prop="sortNo" label="排序" width="100"></el-table-column>
                    <el-table-column prop="status" label="状态" width="100"><template slot-scope="scope"><el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
                    <el-table-column label="操作" min-width="180" fixed="right"><template slot-scope="scope"><div class="table-operations"><el-button v-if="$root.hasPermission('sys:dept:edit')" type="text" @click="openEdit(scope.row)">编辑</el-button><el-button v-if="$root.hasPermission('sys:dept:delete')" type="text" style="color:#f56c6c;" @click="handleDelete(scope.row)">删除</el-button></div></template></el-table-column>
                </el-table>
                <div class="process-pagination"><el-pagination background layout="total, sizes, prev, pager, next" :current-page="pageNum" :page-size="pageSize" :page-sizes="[10,20,50,100]" :total="total" @current-change="handlePageChange" @size-change="handleSizeChange"></el-pagination></div>
                <el-dialog :title="editingId ? '编辑部门' : '新增部门'" :visible.sync="dialogVisible" width="720px" @closed="resetForm">
                    <el-form label-position="top">
                        <div class="form-grid two-columns">
                            <el-form-item label="部门编码"><el-input v-model.trim="form.deptCode" placeholder="请输入部门编码"></el-input></el-form-item>
                            <el-form-item label="部门名称"><el-input v-model.trim="form.deptName" placeholder="请输入部门名称"></el-input></el-form-item>
                            <el-form-item label="上级部门"><el-select v-model="form.parentId" clearable placeholder="请选择上级部门"><el-option v-for="item in $root.deptOptions" :key="item.id" :label="item.deptName" :value="item.id"></el-option></el-select></el-form-item>
                            <el-form-item label="状态"><el-select v-model="form.status"><el-option label="启用" :value="1"></el-option><el-option label="停用" :value="0"></el-option></el-select></el-form-item>
                            <el-form-item label="排序"><el-input v-model.number="form.sortNo" type="number" placeholder="请输入排序"></el-input></el-form-item>
                        </div>
                        <el-form-item label="备注"><el-input v-model.trim="form.remark" type="textarea" :rows="3" placeholder="请输入备注"></el-input></el-form-item>
                        <div class="form-actions"><el-button type="primary" @click="submitForm">保存</el-button><el-button @click="dialogVisible = false">取消</el-button></div>
                    </el-form>
                </el-dialog>
            </section>
        </section>
    `,
    data: function () {
        return { filters: { deptName: "", status: "" }, records: [], total: 0, pageNum: 1, pageSize: 10, dialogVisible: false, editingId: null, form: { deptCode: "", deptName: "", parentId: "", sortNo: 0, status: 1, remark: "" } };
    },
    mounted: function () { this.queryList(); },
    methods: {
        queryList: async function () {
            var query = "?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize;
            if (this.filters.deptName) { query += "&deptName=" + encodeURIComponent(this.filters.deptName); }
            if (this.filters.status !== "" && this.filters.status !== null) { query += "&status=" + encodeURIComponent(this.filters.status); }
            var result = await window.AppService.request("/sys/dept/list" + query);
            var pageData = result.data || {};
            this.records = pageData.records || [];
            this.total = Number(pageData.total || 0);
        },
        resetQuery: function () { this.filters = { deptName: "", status: "" }; this.pageNum = 1; this.queryList(); },
        handlePageChange: function (pageNum) { this.pageNum = pageNum; this.queryList(); },
        handleSizeChange: function (pageSize) { this.pageSize = pageSize; this.pageNum = 1; this.queryList(); },
        openCreate: function () { this.editingId = null; this.dialogVisible = true; },
        openEdit: function (row) { this.editingId = row.id; this.form = { deptCode: row.deptCode, deptName: row.deptName, parentId: row.parentId, sortNo: row.sortNo, status: row.status, remark: row.remark }; this.dialogVisible = true; },
        resetForm: function () { this.form = { deptCode: "", deptName: "", parentId: "", sortNo: 0, status: 1, remark: "" }; this.editingId = null; },
        submitForm: async function () {
            var url = this.editingId ? ("/sys/dept/" + this.editingId) : "/sys/dept";
            var method = this.editingId ? "PUT" : "POST";
            await window.AppService.requestJson(url, { method: method, body: JSON.stringify(this.form) });
            this.$root.showSuccess(this.editingId ? "部门修改成功" : "部门新增成功");
            await this.$root.loadDeptsOption();
            this.dialogVisible = false;
            this.resetForm();
            this.queryList();
        },
        handleDelete: function (row) {
            var self = this;
            this.$confirm("删除部门后不可恢复，是否继续？", "删除部门", { type: "warning" }).then(async function () {
                await window.AppService.request("/sys/dept/" + row.id, { method: "DELETE" });
                self.$root.showSuccess("部门删除成功");
                await self.$root.loadDeptsOption();
                self.queryList();
            }).catch(function () {});
        }
    }
};
