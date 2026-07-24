/**
 * @auther WCDK
 * @date 2026/7/17
 * @version 1.0
 **/
window.ProcessDiagramNodePropertySupport = {
    normalizeNodeProperties: function (node) {
        var result = {};
        var merge = function (source) {
            if (!source) {
                return;
            }
            if (typeof source === "string") {
                try {
                    source = JSON.parse(source);
                } catch (error) {
                    return;
                }
            }
            if (Array.isArray(source)) {
                for (var index = 0; index < source.length; index += 1) {
                    var item = source[index] || {};
                    var key = item.name || item.key || item.code || "";
                    if (key) {
                        result[key] = item.value;
                    }
                }
                return;
            }
            if (typeof source !== "object") {
                return;
            }
            for (var key in source) {
                if (Object.prototype.hasOwnProperty.call(source, key) && typeof source[key] !== "undefined" && source[key] !== null && source[key] !== "") {
                    result[key] = source[key];
                }
            }
        };
        merge(node && node.properties);
        merge(node && node.attributes);
        merge(node && node.extensionProperties);
        merge(node && node.flowableProperties);
        merge(node && node.propertyMap);
        merge(node && node.config);
        var directKeys = [
            "initiator", "formKey", "boundFormKeys", "boundForms", "assignee", "candidateUsers", "candidateGroups", "dueDate", "priority",
            "approvalResult", "approvalResultText", "approvalAssignee", "approvalComment",
            "className", "delegateExpression", "expression", "resultVariable", "scriptFormat", "script",
            "calledElement", "messageRef", "timerDefinition", "signalRef", "errorRef", "eventDefinitionType",
            "collection", "elementVariable", "completionCondition", "skipExpression", "async", "exclusive",
            "type", "to", "subject", "text"
        ];
        for (var index = 0; index < directKeys.length; index += 1) {
            var directKey = directKeys[index];
            if (node && typeof node[directKey] !== "undefined" && node[directKey] !== null && node[directKey] !== "") {
                result[directKey] = node[directKey];
            }
        }
        if (!result.className && result["class"]) {
            result.className = result["class"];
        }
        if (Array.isArray(result.boundForms) && result.boundForms.length) {
            result.boundFormNames = result.boundForms.map(function (form) {
                var formKey = form && form.formKey ? form.formKey : "";
                var formName = form && form.formName ? form.formName : formKey;
                return formKey ? formName + "（" + formKey + "）" : "";
            }).filter(function (item) {
                return !!item;
            }).join("、");
        } else if (Array.isArray(result.boundFormKeys) && result.boundFormKeys.length) {
            result.boundFormNames = result.boundFormKeys.join("、");
        }
        if (typeof result.async === "string") {
            result.async = result.async === "true";
        }
        if (typeof result.exclusive === "string") {
            result.exclusive = result.exclusive !== "false";
        }
        if (typeof result.multiInstanceEnabled === "string") {
            result.multiInstanceEnabled = result.multiInstanceEnabled === "true";
        }
        return result;
    },
    formatValue: function (value) {
        if (Array.isArray(value)) {
            value = value.join("，");
        }
        if (value && typeof value === "object") {
            value = JSON.stringify(value);
        }
        if (value === true) {
            return "是";
        }
        if (value === false) {
            return "否";
        }
        return String(value);
    },
    pushRow: function (rows, label, value) {
        if (value === null || typeof value === "undefined" || value === "") {
            return;
        }
        rows.push({
            label: label,
            value: this.formatValue(value)
        });
    }
};

window.ProcessDiagramNodeDetailMixin = {
    props: {
        node: {
            type: Object,
            required: true
        },
        properties: {
            type: Object,
            default: function () {
                return {};
            }
        }
    },
    methods: {
        buildRows: function (pairs) {
            var rows = [];
            for (var index = 0; index < pairs.length; index += 1) {
                window.ProcessDiagramNodePropertySupport.pushRow(rows, pairs[index].label, pairs[index].value);
            }
            return rows;
        }
    }
};

window.ProcessDiagramCommonNodeDetail = {
    name: "process-diagram-common-node-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: `
        <div>
            <div
                v-for="item in rows"
                :key="item.label"
                class="process-node-detail-row">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
            </div>
        </div>
    `,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "表单标识", value: this.properties.formKey },
                { label: "绑定表单", value: this.properties.boundFormNames },
                { label: "办理人", value: this.properties.assignee },
                { label: "候选用户", value: this.properties.candidateUsers },
                { label: "候选组", value: this.properties.candidateGroups },
                { label: "实现类", value: this.properties.className },
                { label: "委托表达式", value: this.properties.delegateExpression },
                { label: "执行表达式", value: this.properties.expression },
                { label: "结果变量", value: this.properties.resultVariable },
                { label: "事件定义", value: this.properties.eventDefinitionType },
                { label: "消息引用", value: this.properties.messageRef },
                { label: "多实例", value: this.properties.multiInstanceEnabled },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramStartEventDetail = {
    name: "process-diagram-start-event-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "发起人变量", value: this.properties.initiator },
                { label: "表单标识", value: this.properties.formKey },
                { label: "事件定义", value: this.properties.eventDefinitionType },
                { label: "消息引用", value: this.properties.messageRef },
                { label: "定时表达式", value: this.properties.timerDefinition },
                { label: "信号引用", value: this.properties.signalRef },
                { label: "错误引用", value: this.properties.errorRef }
            ]);
        }
    }
};

window.ProcessDiagramUserTaskDetail = {
    name: "process-diagram-user-task-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "审批结果", value: this.properties.approvalResultText },
                { label: "审批人", value: this.properties.approvalAssignee },
                { label: "审批意见", value: this.properties.approvalComment },
                { label: "表单标识", value: this.properties.formKey },
                { label: "绑定表单", value: this.properties.boundFormNames },
                { label: "办理人", value: this.properties.assignee },
                { label: "候选用户", value: this.properties.candidateUsers },
                { label: "候选组", value: this.properties.candidateGroups },
                { label: "到期时间", value: this.properties.dueDate },
                { label: "优先级", value: this.properties.priority },
                { label: "集合变量", value: this.properties.collection },
                { label: "元素变量", value: this.properties.elementVariable },
                { label: "完成条件", value: this.properties.completionCondition },
                { label: "跳过表达式", value: this.properties.skipExpression },
                { label: "多实例", value: this.properties.multiInstanceEnabled },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramServiceTaskDetail = {
    name: "process-diagram-service-task-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "实现类", value: this.properties.className },
                { label: "委托表达式", value: this.properties.delegateExpression },
                { label: "执行表达式", value: this.properties.expression },
                { label: "结果变量", value: this.properties.resultVariable },
                { label: "跳过表达式", value: this.properties.skipExpression },
                { label: "多实例", value: this.properties.multiInstanceEnabled },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramScriptTaskDetail = {
    name: "process-diagram-script-task-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "脚本格式", value: this.properties.scriptFormat },
                { label: "结果变量", value: this.properties.resultVariable },
                { label: "脚本内容", value: this.properties.script },
                { label: "跳过表达式", value: this.properties.skipExpression },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramMailTaskDetail = {
    name: "process-diagram-mail-task-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "调用类型", value: this.properties.type },
                { label: "收件人", value: this.properties.to },
                { label: "邮件主题", value: this.properties.subject },
                { label: "邮件正文", value: this.properties.text },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramReceiveTaskDetail = {
    name: "process-diagram-receive-task-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "消息引用", value: this.properties.messageRef },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramBusinessRuleTaskDetail = {
    name: "process-diagram-business-rule-task-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "规则输入变量", value: this.properties.ruleVariablesInput },
                { label: "结果变量", value: this.properties.resultVariable },
                { label: "跳过表达式", value: this.properties.skipExpression },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramCallActivityDetail = {
    name: "process-diagram-call-activity-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "调用流程", value: this.properties.calledElement },
                { label: "继承变量", value: this.properties.inheritVariables },
                { label: "多实例", value: this.properties.multiInstanceEnabled },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramGatewayDetail = {
    name: "process-diagram-gateway-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "默认分支", value: this.node.defaultFlowId },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramEventDetail = {
    name: "process-diagram-event-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "事件定义", value: this.properties.eventDefinitionType },
                { label: "消息引用", value: this.properties.messageRef },
                { label: "定时表达式", value: this.properties.timerDefinition },
                { label: "信号引用", value: this.properties.signalRef },
                { label: "错误引用", value: this.properties.errorRef },
                { label: "挂载活动", value: this.properties.attachedToRef },
                { label: "中断活动", value: this.properties.cancelActivity }
            ]);
        }
    }
};

window.ProcessDiagramSubProcessDetail = {
    name: "process-diagram-sub-process-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "事件子流程", value: this.properties.triggeredByEvent },
                { label: "集合变量", value: this.properties.collection },
                { label: "元素变量", value: this.properties.elementVariable },
                { label: "完成条件", value: this.properties.completionCondition },
                { label: "多实例", value: this.properties.multiInstanceEnabled },
                { label: "异步执行", value: this.properties.async },
                { label: "排他执行", value: this.properties.exclusive }
            ]);
        }
    }
};

window.ProcessDiagramAnnotationDetail = {
    name: "process-diagram-annotation-detail",
    mixins: [window.ProcessDiagramNodeDetailMixin],
    template: window.ProcessDiagramCommonNodeDetail.template,
    computed: {
        rows: function () {
            return this.buildRows([
                { label: "注释内容", value: this.properties.text }
            ]);
        }
    }
};

window.ProcessDiagramNodeDetail = {
    name: "process-diagram-node-detail",
    template: `
        <div class="process-node-detail">
            <div class="process-node-detail-row">
                <span>节点名称</span>
                <strong>{{ node.elementName || node.elementId || "-" }}</strong>
            </div>
            <div class="process-node-detail-row">
                <span>节点标识</span>
                <strong>{{ node.elementId || "-" }}</strong>
            </div>
            <div class="process-node-detail-row">
                <span>节点类型</span>
                <strong>{{ resolveNodeTypeLabel(node.elementType) || node.elementType || "-" }}</strong>
            </div>
            <component :is="detailComponentName" :node="node" :properties="properties"></component>
            <div class="process-node-bound-form-list" v-if="boundForms.length">
                <div class="process-node-bound-form-head">
                    <span>绑定表单</span>
                    <strong>{{ boundForms.length }} 个</strong>
                </div>
                <div
                    v-for="form in boundForms"
                    :key="form.formKey"
                    class="process-node-bound-form-item">
                    <div class="process-node-bound-form-name">
                        <strong>{{ form.formName || form.formKey }}</strong>
                        <span>{{ form.formKey }}<template v-if="form.formVersion"> / v{{ form.formVersion }}</template></span>
                    </div>
                    <el-button size="mini" type="text" @click="$emit('view-bound-form', form)">查看表单</el-button>
                </div>
            </div>
            <div class="process-node-detail-row">
                <span>入口数量</span>
                <strong>{{ node.incomingCount || 0 }}</strong>
            </div>
            <div class="process-node-detail-row">
                <span>出口数量</span>
                <strong>{{ node.outgoingCount || 0 }}</strong>
            </div>
            <div class="process-node-detail-desc" v-if="node.documentation">
                <span>节点说明</span>
                <p>{{ node.documentation }}</p>
            </div>
        </div>
    `,
    props: {
        node: {
            type: Object,
            required: true
        },
        resolveNodeTypeLabel: {
            type: Function,
            required: true
        }
    },
    computed: {
        properties: function () {
            return window.ProcessDiagramNodePropertySupport.normalizeNodeProperties(this.node);
        },
        boundForms: function () {
            var forms = Array.isArray(this.properties.boundForms) ? this.properties.boundForms : [];
            if (forms.length) {
                return this.normalizeBoundForms(forms);
            }
            var formKeys = Array.isArray(this.properties.boundFormKeys)
                ? this.properties.boundFormKeys
                : String(this.properties.formKey || "").split(",");
            return this.normalizeBoundForms(formKeys.map(function (formKey) {
                var key = String(formKey || "").trim();
                return key ? { formKey: key, formName: key } : null;
            }));
        },
        detailComponentName: function () {
            var mapping = {
                StartEvent: "process-diagram-start-event-detail",
                EndEvent: "process-diagram-event-detail",
                BoundaryEvent: "process-diagram-event-detail",
                IntermediateCatchEvent: "process-diagram-event-detail",
                IntermediateThrowEvent: "process-diagram-event-detail",
                UserTask: "process-diagram-user-task-detail",
                ManualTask: "process-diagram-common-node-detail",
                ServiceTask: "process-diagram-service-task-detail",
                ScriptTask: "process-diagram-script-task-detail",
                MailTask: "process-diagram-mail-task-detail",
                ReceiveTask: "process-diagram-receive-task-detail",
                BusinessRuleTask: "process-diagram-business-rule-task-detail",
                CallActivity: "process-diagram-call-activity-detail",
                ExclusiveGateway: "process-diagram-gateway-detail",
                ParallelGateway: "process-diagram-gateway-detail",
                InclusiveGateway: "process-diagram-gateway-detail",
                EventGateway: "process-diagram-gateway-detail",
                SubProcess: "process-diagram-sub-process-detail",
                TextAnnotation: "process-diagram-annotation-detail"
            };
            return mapping[this.node.elementType] || "process-diagram-common-node-detail";
        }
    },
    methods: {
        normalizeBoundForms: function (forms) {
            var results = [];
            var usedKeys = {};
            for (var index = 0; index < (forms || []).length; index += 1) {
                var form = forms[index] || {};
                var formKey = String(form.formKey || "").trim();
                if (!formKey || usedKeys[formKey]) {
                    continue;
                }
                usedKeys[formKey] = true;
                results.push(Object.assign({}, form, {
                    formKey: formKey,
                    formName: form.formName || formKey
                }));
            }
            return results;
        }
    }
};

if (window && window.Vue && typeof window.Vue.component === "function") {
    window.Vue.component("process-diagram-common-node-detail", window.ProcessDiagramCommonNodeDetail);
    window.Vue.component("process-diagram-start-event-detail", window.ProcessDiagramStartEventDetail);
    window.Vue.component("process-diagram-user-task-detail", window.ProcessDiagramUserTaskDetail);
    window.Vue.component("process-diagram-service-task-detail", window.ProcessDiagramServiceTaskDetail);
    window.Vue.component("process-diagram-script-task-detail", window.ProcessDiagramScriptTaskDetail);
    window.Vue.component("process-diagram-mail-task-detail", window.ProcessDiagramMailTaskDetail);
    window.Vue.component("process-diagram-receive-task-detail", window.ProcessDiagramReceiveTaskDetail);
    window.Vue.component("process-diagram-business-rule-task-detail", window.ProcessDiagramBusinessRuleTaskDetail);
    window.Vue.component("process-diagram-call-activity-detail", window.ProcessDiagramCallActivityDetail);
    window.Vue.component("process-diagram-gateway-detail", window.ProcessDiagramGatewayDetail);
    window.Vue.component("process-diagram-event-detail", window.ProcessDiagramEventDetail);
    window.Vue.component("process-diagram-sub-process-detail", window.ProcessDiagramSubProcessDetail);
    window.Vue.component("process-diagram-annotation-detail", window.ProcessDiagramAnnotationDetail);
    window.Vue.component("process-diagram-node-detail", window.ProcessDiagramNodeDetail);
}
