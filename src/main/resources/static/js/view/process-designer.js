/**
 * @auther WCDK
 * @date 2026/7/24
 * @version 1.0
 **/
window.ProcessDesigner = {
    name: "ProcessDesigner",
    template: `
        <wcdk-process-designer
            :process-definition-id="routeProcessDefinitionId"
            :deployment-id="routeDeploymentId"
            :model-id="routeModelId"
            :process-definition-detail="processDefinitionDetail"
            :form-bindings="formBindings"
            :form-records="formRecords"
            :form-total="formTotal"
            :form-loading="formLoading"
            :update-mode="routeUpdateMode"
            :save-handler="handleDesignerSave"
            :export-handler="handleDesignerExport"
            @form-query="handleFormQuery">
        </wcdk-process-designer>
    `,
    data: function () {
        return {
            processDefinitionDetail: null,
            formBindings: [],
            formRecords: [],
            formTotal: 0,
            formLoading: false
        };
    },
    computed: {
        routeQuery: function () {
            return this.$route && this.$route.query ? this.$route.query : {};
        },
        routeProcessDefinitionId: function () {
            return this.routeQuery.processDefinitionId || "";
        },
        routeDeploymentId: function () {
            return this.routeQuery.deploymentId || "";
        },
        routeModelId: function () {
            return this.routeQuery.modelId || "";
        },
        routeUpdateMode: function () {
            return this.routeQuery.flg === "update";
        }
    },
    watch: {
        routeProcessDefinitionId: function () {
            this.loadDesignerData();
        }
    },
    mounted: function () {
        this.loadDesignerData();
        this.loadFormRecords({ pageNum: 1, pageSize: 10, formName: "", formKey: "" });
    },
    methods: {
        loadDesignerData: async function () {
            if (!this.routeProcessDefinitionId) {
                this.processDefinitionDetail = null;
                this.formBindings = [];
                return;
            }
            try {
                var detailResult = await window.AppService.request("/flowable/deploy/definition/" + encodeURIComponent(this.routeProcessDefinitionId));
                this.processDefinitionDetail = detailResult.data || null;
                var bindingResult = await window.AppService.request("/process/form/binding/" + encodeURIComponent(this.routeProcessDefinitionId));
                this.formBindings = bindingResult.data || [];
            } catch (error) {
                this.$message.error(error && error.message ? error.message : "加载部署流程失败");
            }
        },
        handleFormQuery: function (query) {
            this.loadFormRecords(query || {});
        },
        loadFormRecords: async function (query) {
            var pageNum = query.pageNum || 1;
            var pageSize = query.pageSize || 10;
            var url = "/process/form/list?pageNum=" + encodeURIComponent(pageNum)
                + "&pageSize=" + encodeURIComponent(pageSize);
            if (query.formName) {
                url += "&formName=" + encodeURIComponent(query.formName);
            }
            if (query.formKey) {
                url += "&formKey=" + encodeURIComponent(query.formKey);
            }
            this.formLoading = true;
            try {
                var result = await window.AppService.request(url);
                var pageData = result.data || {};
                this.formRecords = pageData.records || [];
                this.formTotal = Number(pageData.total || 0);
            } catch (error) {
                this.$message.error(error && error.message ? error.message : "加载表单列表失败");
            } finally {
                this.formLoading = false;
            }
        },
        handleDesignerSave: async function (payload) {
            var modelPayload = payload.model || {};
            var result = null;
            if (payload.mode === "updateProcessDefinition" && payload.formBinding && payload.formBinding.processDefinitionId) {
                result = await window.AppService.requestJson("/flowable/deploy/definition/" + encodeURIComponent(payload.formBinding.processDefinitionId), {
                    method: "PUT",
                    body: JSON.stringify({
                        deploymentId: payload.formBinding.deploymentId || "",
                        processDefinitionId: payload.formBinding.processDefinitionId,
                        bpmnXml: modelPayload.bpmnXml
                    })
                });
                await window.AppService.requestJson("/process/form/binding", {
                    method: "POST",
                    body: JSON.stringify(payload.formBinding)
                });
                await this.loadDesignerData();
                return result;
            }
            if (this.routeModelId) {
                result = await window.AppService.requestJson("/flowable/model/" + encodeURIComponent(this.routeModelId), {
                    method: "PUT",
                    body: JSON.stringify({
                        modelName: modelPayload.modelName,
                        category: modelPayload.category,
                        bpmnXml: modelPayload.bpmnXml
                    })
                });
            } else {
                result = await window.AppService.requestJson("/flowable/model", {
                    method: "POST",
                    body: JSON.stringify(modelPayload)
                });
            }
            return result;
        },
        handleDesignerExport: async function (payload) {
            var result = await window.AppService.requestJson("/flowable/designer/export", {
                method: "POST",
                body: JSON.stringify(payload)
            });
            return result.data || null;
        }
    }
};
