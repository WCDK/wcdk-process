/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
window.ClientCenter = {
    template: `
        <section class="route-section">
            <section class="workspace-panel route-panel">
                <div class="panel-head">
                    <div>
                        <div class="section-kicker">流程客户端注册信息</div>
                        <h2>客户端管理</h2>
                    </div>
                    <el-button @click="queryList">刷新</el-button>
                </div>

                <el-form inline class="process-filter-form" @submit.native.prevent="queryList">
                    <el-form-item label="客户端标识">
                        <el-input v-model.trim="filters.clientId" placeholder="请输入客户端标识"></el-input>
                    </el-form-item>
                    <el-form-item label="客户端名称">
                        <el-input v-model.trim="filters.clientName" placeholder="请输入客户端名称"></el-input>
                    </el-form-item>
                    <el-form-item label="回调地址">
                        <el-input v-model.trim="filters.callbackUrl" placeholder="请输入回调地址"></el-input>
                    </el-form-item>
                    <el-form-item label="流程处理器">
                        <el-input v-model.trim="filters.processBeanName" placeholder="请输入processBean"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="queryList">查询</el-button>
                        <el-button @click="resetQuery">重置</el-button>
                    </el-form-item>
                </el-form>

                <el-table
                    :data="records"
                    stripe
                    @sort-change="handleSortChange">
                    <el-table-column prop="clientId" label="客户端标识" min-width="160" sortable="custom"></el-table-column>
                    <el-table-column prop="clientName" label="客户端名称" min-width="180" sortable="custom"></el-table-column>
                    <el-table-column prop="callbackUrl" label="回调地址" min-width="260">
                        <template slot-scope="scope">
                            <span>{{ scope.row.callbackUrl || "-" }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="processBeanCount" label="处理器数量" width="120"></el-table-column>
                    <el-table-column prop="processBindingCount" label="绑定流程数" width="120"></el-table-column>
                    <el-table-column prop="processBeanNames" label="流程处理器" min-width="220">
                        <template slot-scope="scope">
                            <el-tag
                                v-for="name in scope.row.processBeanNames || []"
                                :key="scope.row.clientId + '-' + name"
                                size="mini"
                                effect="plain"
                                style="margin-right: 6px; margin-bottom: 4px;">
                                {{ name }}
                            </el-tag>
                            <span v-if="!scope.row.processBeanNames || !scope.row.processBeanNames.length">-</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="processNames" label="已绑定流程" min-width="220">
                        <template slot-scope="scope">
                            <el-tag
                                v-for="name in scope.row.processNames || []"
                                :key="scope.row.clientId + '-process-' + name"
                                size="mini"
                                type="success"
                                effect="plain"
                                style="margin-right: 6px; margin-bottom: 4px;">
                                {{ name }}
                            </el-tag>
                            <span v-if="!scope.row.processNames || !scope.row.processNames.length">-</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="authFlg" label="鉴权标识" min-width="120">
                        <template slot-scope="scope">
                            {{ scope.row.authFlg || "-" }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="注册时间" min-width="180" sortable="custom">
                        <template slot-scope="scope">{{ formatDateTime(scope.row.createTime) }}</template>
                    </el-table-column>
                    <el-table-column prop="updateTime" label="更新时间" min-width="180" sortable="custom">
                        <template slot-scope="scope">{{ formatDateTime(scope.row.updateTime) }}</template>
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
            </section>
        </section>
    `,
    data: function () {
        return {
            filters: {
                clientId: "",
                clientName: "",
                callbackUrl: "",
                processBeanName: ""
            },
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
            sortProp: "updateTime",
            sortOrder: "descending"
        };
    },
    mounted: function () {
        this.queryList();
    },
    methods: {
        queryList: async function () {
            var query = "?pageNum=" + encodeURIComponent(this.pageNum)
                + "&pageSize=" + encodeURIComponent(this.pageSize);
            if (this.filters.clientId) {
                query += "&clientId=" + encodeURIComponent(this.filters.clientId);
            }
            if (this.filters.clientName) {
                query += "&clientName=" + encodeURIComponent(this.filters.clientName);
            }
            if (this.filters.callbackUrl) {
                query += "&callbackUrl=" + encodeURIComponent(this.filters.callbackUrl);
            }
            if (this.filters.processBeanName) {
                query += "&processBeanName=" + encodeURIComponent(this.filters.processBeanName);
            }
            if (this.sortProp) {
                query += "&sortProp=" + encodeURIComponent(this.sortProp);
            }
            if (this.sortOrder) {
                query += "&sortOrder=" + encodeURIComponent(this.sortOrder);
            }
            var result = await window.AppService.request("/wcdk/process/client/list" + query);
            var pageData = result.data || {};
            this.records = pageData.records || [];
            this.total = Number(pageData.total || 0);
            this.pageNum = Number(pageData.pageNum || this.pageNum);
            this.pageSize = Number(pageData.pageSize || this.pageSize);
        },
        resetQuery: function () {
            this.filters = {
                clientId: "",
                clientName: "",
                callbackUrl: "",
                processBeanName: ""
            };
            this.pageNum = 1;
            this.sortProp = "updateTime";
            this.sortOrder = "descending";
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
        handleSortChange: function (payload) {
            this.sortProp = payload.prop || "updateTime";
            this.sortOrder = payload.order || "descending";
            this.pageNum = 1;
            this.queryList();
        },
        formatDateTime: function (value) {
            return window.AppService.formatDateTime(value);
        }
    }
};
