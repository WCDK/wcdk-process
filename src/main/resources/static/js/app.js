/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
if (!window.__wcdkF12Blocked) {
    window.__wcdkF12Blocked = true;
    var devtoolsOverlay = document.createElement("div");
    var devtoolsStyle = document.createElement("style");
    var devtoolsLocked = false;
    var devtoolsDestroyed = false;
    devtoolsOverlay.id = "wcdk-devtools-overlay";
    devtoolsOverlay.innerHTML = "<div class=\"wcdk-devtools-overlay__content\">检测到开发者工具已打开，当前页面已禁止操作</div>";
    devtoolsStyle.textContent = ""
        + "#wcdk-devtools-overlay{position:fixed;inset:0;display:none;align-items:center;justify-content:center;background:rgba(120,120,120,0.45);backdrop-filter:grayscale(1);z-index:999999;pointer-events:auto;}"
        + "#wcdk-devtools-overlay .wcdk-devtools-overlay__content{padding:20px 28px;border-radius:12px;background:rgba(255,255,255,0.96);color:#303133;font-size:16px;font-weight:600;box-shadow:0 12px 32px rgba(0,0,0,0.16);}"
        + "html.wcdk-devtools-locked body>*:not(#wcdk-devtools-overlay){filter:grayscale(1);}"
        + "html.wcdk-devtools-locked{overflow:hidden;}";
    document.head.appendChild(devtoolsStyle);
    document.addEventListener("DOMContentLoaded", function () {
        if (!document.getElementById("wcdk-devtools-overlay")) {
            document.body.appendChild(devtoolsOverlay);
        }
    });

    function setDevtoolsLocked(locked) {
        if (devtoolsLocked === locked) {
            return;
        }
        devtoolsLocked = locked;
        if (!document.getElementById("wcdk-devtools-overlay") && document.body) {
            document.body.appendChild(devtoolsOverlay);
        }
        document.documentElement.classList.toggle("wcdk-devtools-locked", locked);
        devtoolsOverlay.style.display = locked ? "flex" : "none";
        if (locked) {
            destroyFrontendResources();
            console.log("请访问以下地址获取源码：");
            console.log("https://github.com/WCDK/wcdk-process");
        }
    }

    function detectDevtoolsOpened() {
        var widthGap = window.outerWidth - window.innerWidth;
        var heightGap = window.outerHeight - window.innerHeight;
        return widthGap > 160 || heightGap > 160;
    }

    function destroyFrontendResources() {
        if (devtoolsDestroyed) {
            return;
        }
        devtoolsDestroyed = true;
        Array.prototype.slice.call(document.querySelectorAll("link[rel='stylesheet'], style")).forEach(function (node) {
            if (node !== devtoolsStyle && node.parentNode) {
                node.parentNode.removeChild(node);
            }
        });
        Array.prototype.slice.call(document.querySelectorAll("script")).forEach(function (node) {
            if (node.parentNode) {
                node.parentNode.removeChild(node);
            }
        });
        if (document.body) {
            Array.prototype.slice.call(document.body.children).forEach(function (node) {
                if (node !== devtoolsOverlay) {
                    node.remove();
                }
            });
            if (!document.getElementById("wcdk-devtools-overlay")) {
                document.body.appendChild(devtoolsOverlay);
            }
        }
    }

    document.addEventListener("keydown", function (event) {
        var key = (event.key || "").toUpperCase();
        var blockedByFunctionKey = key === "F12" || event.keyCode === 123;
        var blockedByShortcut = event.ctrlKey && ((event.shiftKey && (key === "I" || key === "J" || key === "C")) || key === "U");
        if (devtoolsLocked || blockedByFunctionKey || blockedByShortcut) {
            event.preventDefault();
            event.stopPropagation();
        }
    }, true);
    document.addEventListener("click", function (event) {
        if (devtoolsLocked) {
            event.preventDefault();
            event.stopPropagation();
        }
    }, true);
    document.addEventListener("contextmenu", function (event) {
        event.preventDefault();
    });
    window.addEventListener("resize", function () {
        setDevtoolsLocked(detectDevtoolsOpened());
    });
    setDevtoolsLocked(detectDevtoolsOpened());
    window.setInterval(function () {
        setDevtoolsLocked(detectDevtoolsOpened());
    }, 1000);
}

Vue.use(ELEMENT);
Vue.use(VueRouter);

new Vue({
    el: "#app",
    router: window.AppRouter,
    data: function () {
        return {
            navMenus: [
                { path: "/home", label: "首页", icon: "el-icon-house", description: "快速访问和概览", permissionCode: "menu:home" },
                { path: "/deploy", label: "部署中心", icon: "el-icon-upload2", description: "上传流程定义文件并部署流程", permissionCode: "menu:deploy" },
                { path: "/model", label: "模型中心", icon: "el-icon-collection", description: "管理模型创建和流程模板资源", permissionCode: "menu:model" },
                { path: "/designer", label: "流程设计", icon: "el-icon-edit-outline", description: "进入流程设计工作区", permissionCode: "menu:designer" },
                { path: "/process", label: "流程中心", icon: "el-icon-s-operation", description: "创建并发布流程实例", permissionCode: "menu:process" },
                { path: "/task", label: "任务中心", icon: "el-icon-s-check", description: "查询并处理待办任务", permissionCode: "menu:task" },
                { path: "/system/user", label: "用户管理", icon: "el-icon-user", description: "维护用户、角色归属和状态", permissionCode: "menu:sys:user" },
                { path: "/system/role", label: "角色管理", icon: "el-icon-s-custom", description: "维护角色和权限绑定关系", permissionCode: "menu:sys:role" },
                { path: "/system/permission", label: "权限管理", icon: "el-icon-key", description: "维护菜单和按钮权限", permissionCode: "menu:sys:permission" },
                { path: "/system/dept", label: "部门管理", icon: "el-icon-office-building", description: "维护部门信息和层级关系", permissionCode: "menu:sys:dept" }
            ],
            currentUser: window.AppService.getCurrentUserCache(),
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
            roleOptions: [],
            permissionOptions: [],
            deptOptions: [],
            guideVisible: false,
            xmlVisible: false,
            currentXml: "",
            authReady: false
        };
    },
    computed: {
        isLoginRoute: function () {
            return this.$route.path === "/login";
        },
        visibleNavMenus: function () {
            var self = this;
            return this.navMenus.filter(function (item) {
                return self.hasPermission(item.permissionCode);
            });
        }
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
        hasPermission: function (permissionCode) {
            return window.AppService.hasPermission(permissionCode, this.currentUser);
        },
        handleLoginSuccess: async function (loginResult) {
            window.AppService.setToken(loginResult.token);
            window.AppService.setCurrentUserCache(loginResult.currentUser);
            this.currentUser = loginResult.currentUser;
            await this.loadBaseOptions();
            await this.reloadAll();
            this.authReady = true;
            this.$router.push("/home");
        },
        logout: async function () {
            try {
                await window.AppService.requestJson("/auth/logout", { method: "POST" });
            } catch (error) {
            }
            window.AppService.clearAuth();
            this.currentUser = null;
            this.authReady = true;
            this.$router.push("/login");
        },
        initializeAuth: async function () {
            var token = window.AppService.getToken();
            if (!token) {
                this.currentUser = null;
                this.authReady = true;
                if (!this.isLoginRoute) {
                    this.$router.replace("/login");
                }
                return;
            }
            try {
                var result = await window.AppService.request("/auth/me");
                this.currentUser = result.data || null;
                window.AppService.setCurrentUserCache(this.currentUser);
                await this.loadBaseOptions();
                await this.reloadAll();
                this.authReady = true;
                if (this.isLoginRoute) {
                    this.$router.replace("/home");
                }
            } catch (error) {
                window.AppService.clearAuth();
                this.currentUser = null;
                this.authReady = true;
                this.$router.replace("/login");
            }
        },
        reloadAll: async function () {
            if (!this.currentUser) {
                return;
            }
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
        loadBaseOptions: async function () {
            if (!this.currentUser) {
                return;
            }
            var tasks = [];
            if (this.hasPermission("sys:role:view")) {
                tasks.push(this.loadRolesOption());
            }
            if (this.hasPermission("sys:permission:view")) {
                tasks.push(this.loadPermissionsOption());
            }
            if (this.hasPermission("sys:dept:view")) {
                tasks.push(this.loadDeptsOption());
            }
            await Promise.all(tasks);
        },
        loadRolesOption: async function () {
            var result = await window.AppService.request("/sys/role/list?pageNum=1&pageSize=500");
            this.roleOptions = (result.data && result.data.records) || [];
        },
        loadPermissionsOption: async function () {
            var result = await window.AppService.request("/sys/permission/list?pageNum=1&pageSize=500");
            this.permissionOptions = (result.data && result.data.records) || [];
        },
        loadDeptsOption: async function () {
            var result = await window.AppService.request("/sys/dept/list?pageNum=1&pageSize=500");
            this.deptOptions = (result.data && result.data.records) || [];
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
            return result.data || null;
        },
        loadModels: async function () {
            var result = await window.AppService.request("/flowable/model/list");
            this.models = result.data || [];
            this.overview.modelCount = this.models.length;
        },
        loadProcesses: async function (pageNum, pageSize) {
            var nextPageNum = pageNum || this.processPageNum || 1;
            var nextPageSize = pageSize || this.processPageSize || 10;
            var query = "?pageNum=" + encodeURIComponent(nextPageNum) + "&pageSize=" + encodeURIComponent(nextPageSize);
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
            this.processPageNum = Number(pageData.pageNum || nextPageNum);
            this.processPageSize = Number(pageData.pageSize || nextPageSize);
            this.processTotal = Number(pageData.total || 0);
            this.processList = Array.isArray(pageData.records) ? pageData.records : [];
            this.overview.processCount = this.processTotal;
        },
        loadTasks: async function (assignee) {
            var query = assignee ? "?assignee=" + encodeURIComponent(assignee) : "";
            var result = await window.AppService.request("/flowable/process/task/list" + query);
            this.taskList = result.data || [];
            this.overview.taskCount = this.taskList.length;
        },
        createDeployment: async function (payload) {
            var result = await window.AppService.request("/flowable/deploy/process", { method: "POST", body: payload });
            this.showSuccess(result.message || "部署创建成功");
            await Promise.all([this.loadDeployments(), this.loadDefinitions()]);
        },
        deleteDeployment: async function (deploymentId) {
            var result = await window.AppService.request("/flowable/deploy?deploymentId=" + encodeURIComponent(deploymentId) + "&cascade=true", { method: "DELETE" });
            this.showSuccess(result.message || "部署删除成功");
            await Promise.all([this.loadDeployments(), this.loadDefinitions(), this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        createModel: async function (payload) {
            var result = await window.AppService.requestJson("/flowable/model", { method: "POST", body: JSON.stringify(payload) });
            this.showSuccess(result.message || "模型创建成功");
            await this.loadModels();
        },
        updateModel: async function (modelId, payload) {
            var result = await window.AppService.requestJson("/flowable/model/" + encodeURIComponent(modelId), { method: "PUT", body: JSON.stringify(payload) });
            this.showSuccess(result.message || "模型更新成功");
            await this.loadModels();
        },
        deleteModel: async function (modelId) {
            var result = await window.AppService.request("/flowable/model/" + encodeURIComponent(modelId), { method: "DELETE" });
            this.showSuccess(result.message || "模型删除成功");
            await this.loadModels();
        },
        deployModel: async function (modelId) {
            var result = await window.AppService.requestJson("/flowable/model/" + encodeURIComponent(modelId) + "/deploy", { method: "POST" });
            this.showSuccess(result.message || "模型部署成功");
            await Promise.all([this.loadModels(), this.loadDeployments(), this.loadDefinitions()]);
        },
        viewModelXml: async function (modelId) {
            var result = await window.AppService.request("/flowable/model/" + encodeURIComponent(modelId) + "/xml");
            this.currentXml = result.data || "";
            this.xmlVisible = true;
        },
        createProcess: async function (payload) {
            var result = await window.AppService.requestJson("/process/request", { method: "POST", body: JSON.stringify(payload) });
            this.showSuccess(result.message || "流程申请创建成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        submitProcessById: async function (id) {
            var result = await window.AppService.requestJson("/process/request/" + id + "/submit", { method: "POST" });
            this.showSuccess(result.message || "流程申请提交成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        deleteProcessById: async function (id) {
            var result = await window.AppService.request("/process/request/" + encodeURIComponent(id), { method: "DELETE" });
            this.showSuccess(result.message || "流程申请删除成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        approveTask: async function (payload) {
            var result = await window.AppService.requestJson("/process/request/approve", { method: "POST", body: JSON.stringify(payload) });
            this.showSuccess(result.message || "流程任务审批成功");
            await Promise.all([this.loadProcesses(), this.loadTasks(this.taskAssignee)]);
        },
        deleteTaskById: async function (taskId) {
            var result = await window.AppService.request("/flowable/process/task/" + encodeURIComponent(taskId), { method: "DELETE" });
            this.showSuccess(result.message || "流程任务删除成功");
            await Promise.all([this.loadTasks(this.taskAssignee), this.loadProcesses()]);
        }
    },
    mounted: function () {
        this.initializeAuth();
    }
});
