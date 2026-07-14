Vue.use(ELEMENT);
Vue.use(VueRouter);

new Vue({
    el: "#app",
    router: window.AppRouter,
    data: function () {
        return {
            navMenus: [
                { path: "/home", label: "首页", icon: "el-icon-house", description: "总览与快捷入口" },
                { path: "/deploy", label: "部署中心", icon: "el-icon-upload2", description: "上传流程定义文件并部署流程" },
                { path: "/model", label: "模型中心", icon: "el-icon-collection", description: "管理模型资产与流程模型源码" },
                { path: "/process", label: "流程中心", icon: "el-icon-s-operation", description: "创建并发起流程申请" },
                { path: "/task", label: "任务中心", icon: "el-icon-s-check", description: "查询并处理审批任务" }
            ],
            overview: {
                definitionCount: 0,
                modelCount: 0,
                processCount: 0,
                taskCount: 0
            },
            deployments: [],
            models: [],
            processDefinitions: [],
            selectedProcessDefinitionDetail: null,
            selectedProcessRequestDiagramDetail: null,
            processList: [],
            processPageNum: 1,
            processPageSize: 10,
            processTotal: 0,
            processFilters: {
                processNo: "",
                starter: "",
                businessTitle: "",
                category: "",
                processDefinitionKey: "",
                status: ""
            },
            taskList: [],
            taskAssignee: "",
            guideVisible: false,
            xmlVisible: false,
            currentXml: ""
        };
    },
    mounted: function () {
        this.reloadAll();
    },
    methods: {
        isActiveMenu: function (path) {
            return this.$route.path === path;
        },
        navigate: function (path) {
            if (this.$route.path !== path) {
                this.$router.push(path);
            }
        },
        openGuide: function () {
            this.guideVisible = true;
        },
        showSuccess: function (message) {
            this.$message.success(message);
        },
        showError: function (message) {
            this.$message.error(message);
        },
        reloadAll: async function () {
            try {
                await Promise.all([
                    this.loadDeployments(),
                    this.loadDefinitions(),
                    this.loadModels(),
                    this.loadProcesses(),
                    this.loadTasks(this.taskAssignee)
                ]);
            } catch (error) {
                this.showError(error.message || "刷新数据失败");
            }
        },
        loadDeployments: async function () {
            var result = await window.AppService.request("/flowable/deploy/list");
            this.deployments = result.data || [];
        },
        loadDefinitions: async function () {
            var result = await window.AppService.request("/flowable/deploy/definition/list");
            this.processDefinitions = result.data || [];
            this.overview.definitionCount = this.processDefinitions.length;
        },
        loadProcessDefinitionDetail: async function (processDefinitionId) {
            if (!processDefinitionId) {
                this.selectedProcessDefinitionDetail = null;
                return null;
            }
            var result = await window.AppService.request("/flowable/deploy/definition/" + encodeURIComponent(processDefinitionId));
            this.selectedProcessDefinitionDetail = result.data || null;
            return this.selectedProcessDefinitionDetail;
        },
        fetchProcessDefinitionDetail: async function (processDefinitionId) {
            if (!processDefinitionId) {
                return null;
            }
            var result = await window.AppService.request("/flowable/deploy/definition/" + encodeURIComponent(processDefinitionId));
            return result.data || null;
        },
        loadProcessRequestDiagramDetail: async function (id) {
            if (!id) {
                this.selectedProcessRequestDiagramDetail = null;
                return null;
            }
            var result = await window.AppService.request("/process/request/" + encodeURIComponent(id) + "/diagram");
            this.selectedProcessRequestDiagramDetail = result.data || null;
            return this.selectedProcessRequestDiagramDetail;
        },
        loadModels: async function () {
            var result = await window.AppService.request("/flowable/model/list");
            this.models = result.data || [];
            this.overview.modelCount = this.models.length;
        },
        loadProcesses: async function (pageNum, pageSize) {
            var nextPageNum = pageNum || this.processPageNum || 1;
            var nextPageSize = pageSize || this.processPageSize || 10;
            var query = "?pageNum=" + encodeURIComponent(nextPageNum)
                + "&pageSize=" + encodeURIComponent(nextPageSize);
            if (this.processFilters.processNo) {
                query += "&processNo=" + encodeURIComponent(this.processFilters.processNo);
            }
            if (this.processFilters.starter) {
                query += "&starter=" + encodeURIComponent(this.processFilters.starter);
            }
            if (this.processFilters.businessTitle) {
                query += "&businessTitle=" + encodeURIComponent(this.processFilters.businessTitle);
            }
            if (this.processFilters.category) {
                query += "&category=" + encodeURIComponent(this.processFilters.category);
            }
            if (this.processFilters.processDefinitionKey) {
                query += "&processDefinitionKey=" + encodeURIComponent(this.processFilters.processDefinitionKey);
            }
            if (this.processFilters.status) {
                query += "&status=" + encodeURIComponent(this.processFilters.status);
            }
            var result = await window.AppService.request("/process/request/list" + query);
            var pageData = result.data || {};
            if (Array.isArray(pageData)) {
                this.processPageNum = 1;
                this.processPageSize = Number(pageData.length || nextPageSize);
                this.processTotal = Number(pageData.length);
                this.processList = pageData;
                this.overview.processCount = this.processTotal;
                return;
            }
            this.processPageNum = Number(pageData.pageNum || nextPageNum);
            this.processPageSize = Number(pageData.pageSize || nextPageSize);
            this.processTotal = Number(pageData.total || 0);
            this.processList = Array.isArray(pageData.records) ? pageData.records : [];
            this.overview.processCount = this.processTotal;
            if (!this.processList.length && this.processTotal > 0 && this.processPageNum > 1) {
                return this.loadProcesses(this.processPageNum - 1, this.processPageSize);
            }
        },
        loadTasks: async function (assignee) {
            var query = assignee ? "?assignee=" + encodeURIComponent(assignee) : "";
            var result = await window.AppService.request("/flowable/process/task/list" + query);
            this.taskList = result.data || [];
            this.overview.taskCount = this.taskList.length;
        },
        createDeployment: async function (payload) {
            var result = await window.AppService.request("/flowable/deploy/process", {
                method: "POST",
                body: payload
            });
            this.showSuccess(result.message || "部署创建成功");
            await Promise.all([this.loadDeployments(), this.loadDefinitions()]);
        },
        deleteDeployment: async function (deploymentId) {
            var result = await window.AppService.request("/flowable/deploy?deploymentId=" + encodeURIComponent(deploymentId) + "&cascade=true", {
                method: "DELETE"
            });
            this.showSuccess(result.message || "部署删除成功");
            await Promise.all([this.loadDeployments(), this.loadDefinitions(), this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        createModel: async function (payload) {
            var result = await window.AppService.requestJson("/flowable/model", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            this.showSuccess(result.message || "模型创建成功");
            await this.loadModels();
        },
        updateModel: async function (modelId, payload) {
            var result = await window.AppService.requestJson("/flowable/model/" + encodeURIComponent(modelId), {
                method: "PUT",
                body: JSON.stringify(payload)
            });
            this.showSuccess(result.message || "流程模型更新成功");
            await this.loadModels();
        },
        deleteModel: async function (modelId) {
            var result = await window.AppService.request("/flowable/model/" + encodeURIComponent(modelId), {
                method: "DELETE"
            });
            this.showSuccess(result.message || "模型删除成功");
            await this.loadModels();
        },
        deployModel: async function (modelId) {
            var result = await window.AppService.requestJson("/flowable/model/" + encodeURIComponent(modelId) + "/deploy", {
                method: "POST"
            });
            this.showSuccess(result.message || "模型部署成功");
            await Promise.all([this.loadModels(), this.loadDeployments(), this.loadDefinitions()]);
        },
        viewModelXml: async function (modelId) {
            var result = await window.AppService.request("/flowable/model/" + encodeURIComponent(modelId) + "/xml");
            this.currentXml = result.data || "";
            this.xmlVisible = true;
        },
        createProcess: async function (payload) {
            var result = await window.AppService.requestJson("/process/request", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            this.showSuccess(result.message || "流程申请创建成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        submitProcessById: async function (id) {
            var result = await window.AppService.requestJson("/process/request/" + id + "/submit", {
                method: "POST"
            });
            this.showSuccess(result.message || "流程申请提交成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        deleteProcessById: async function (id) {
            var result = await window.AppService.request("/process/request/" + encodeURIComponent(id), {
                method: "DELETE"
            });
            this.showSuccess(result.message || "流程申请删除成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        approveTask: async function (payload) {
            var result = await window.AppService.requestJson("/process/request/approve", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            this.showSuccess(result.message || "审批处理成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        deleteTaskById: async function (taskId) {
            var result = await window.AppService.request("/flowable/process/task/" + encodeURIComponent(taskId), {
                method: "DELETE"
            });
            this.showSuccess(result.message || "任务删除成功");
            await Promise.all([this.loadTasks(this.taskAssignee), this.loadProcesses()]);
        }
    }
});
