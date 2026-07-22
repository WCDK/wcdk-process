/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "radio",
        label: "单选框",
        icon: "el-icon-success",
        width: 360,
        height: 80,
        supportOptions: true,
        schemaFields: ["fieldKey", "required", "readOnly", "defaultValue", "options", "dataSourceType", "presetOptionsText", "dataSourceUrl", "dataSourceMethod", "dataSourceScript"],
        defaultProps: function (label, index, designer) {
            return {
                fieldKey: "field_" + (index + 1),
                required: false,
                readOnly: false,
                defaultValue: "",
                options: designer.defaultOptions("radio"),
                dataSourceType: "preset",
                presetOptionsText: "[{\"label\":\"选项一\",\"value\":\"选项一\"},{\"label\":\"选项二\",\"value\":\"选项二\"}]",
                dataSourceUrl: "",
                dataSourceMethod: "GET",
                dataSourceScript: "return data;"
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "defaultValue", label: "默认值", editor: "input" },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" },
            { prop: "dataSourceType", label: "数据源", editor: "select", options: [
                { label: "预设", value: "preset" },
                { label: "请求", value: "request" }
            ] },
            { prop: "presetOptionsText", label: "预设选项", editor: "textarea", rows: 4, placeholder: "JSON 数组，例如：[{\"label\":\"男\",\"value\":\"1\"},{\"label\":\"女\",\"value\":\"0\"}]", visibleWhen: { prop: "dataSourceType", value: "preset" } },
            { prop: "dataSourceUrl", label: "请求地址", editor: "input", placeholder: "例如：/api/options", visibleWhen: { prop: "dataSourceType", value: "request" } },
            { prop: "dataSourceMethod", label: "请求方式", editor: "select", placeholder: "GET", visibleWhen: { prop: "dataSourceType", value: "request" }, options: [
                { label: "GET", value: "GET" },
                { label: "POST", value: "POST" }
            ] },
            { prop: "dataSourceScript", label: "解析脚本", editor: "textarea", rows: 5, placeholder: "return data.records.map(function(item){ return { label: item.name, value: item.id }; });", visibleWhen: { prop: "dataSourceType", value: "request" } },
            { prop: "state", label: "", editor: "checkboxes", options: [
                { prop: "required", label: "必填" },
                { prop: "readOnly", label: "只读" }
            ] }
        ],
        defaultOptions: function () {
            return [
            ];
        },
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            var options = field.options && field.options.length ? field.options : designer.defaultOptions(field.type);
            var cursorX = actualX;
            context.font = "12px Microsoft YaHei, PingFang SC, sans-serif";
            for (var index = 0; index < options.length; index += 1) {
                var option = options[index];
                context.strokeStyle = field.color || "#9db2cc";
                context.lineWidth = 1;
                context.beginPath();
                context.arc(cursorX + 7, actualY + 10, 6, 0, Math.PI * 2);
                context.stroke();
                context.fillStyle = "#5f718a";
                context.fillText(option.label || option.value || "-", cursorX + 20, actualY + 15);
                cursorX += Math.max(74, context.measureText(option.label || option.value || "-").width + 38);
            }
        },
        buildPreviewControlHtml: function (designer, field) {
            var options = field.options && field.options.length ? field.options : [];
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var accentStyle = field.color ? " style=\"accent-color:" + designer.escapeHtml(field.color) + ";\"" : "";
            var defaultValue = field.defaultValue || "";
            var dataSourceAttrs = field.dataSourceType === "request" && field.dataSourceUrl
                ? [
                    " data-source-url=\"", designer.escapeHtml(field.dataSourceUrl), "\"",
                    " data-source-method=\"", designer.escapeHtml(field.dataSourceMethod || "GET"), "\"",
                    " data-source-script=\"", designer.escapeHtml(field.dataSourceScript || ""), "\"",
                    " data-default-value=\"", designer.escapeHtml(defaultValue), "\""
                ].join("")
                : "";
            var html = ["<div class=\"el-radio-group canvas-form-preview-options\" data-option-type=\"radio\" data-option-name=\"", name, "\"", dataSourceAttrs, ">"];
            for (var index = 0; index < options.length; index += 1) {
                var value = options[index].value || options[index].label || "";
                html.push(
                    "<label class=\"el-radio\"><input class=\"el-radio__input\" type=\"radio\" name=\"", name, "\" value=\"",
                    designer.escapeHtml(value), "\"", String(value) === String(defaultValue) ? " checked" : "", accentStyle, "><span>", designer.escapeHtml(options[index].label || value || "-"), "</span></label>"
                );
            }
            if (!options.length) {
                html.push("<span>暂无选项</span>");
            }
            html.push("</div>");
            return html.join("");
        }
    });
})();
