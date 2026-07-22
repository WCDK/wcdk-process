/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "select",
        label: "下拉选择",
        icon: "el-icon-arrow-down",
        width: 320,
        height: 72,
        supportPlaceholder: true,
        supportOptions: true,
        schemaFields: ["fieldKey", "placeholder", "required", "readOnly", "defaultValue", "options", "dataSourceType", "presetOptionsText", "dataSourceUrl", "dataSourceMethod", "dataSourceScript"],
        defaultProps: function (label, index, designer) {
            return {
                fieldKey: "field_" + (index + 1),
                placeholder: "请选择" + label,
                required: false,
                readOnly: false,
                defaultValue: "",
                options: designer.defaultOptions("select"),
                dataSourceType: "preset",
                presetOptionsText: "[{\"label\":\"男\",\"value\":\"1\"},{\"label\":\"女\",\"value\":\"0\"}]",
                dataSourceUrl: "",
                dataSourceMethod: "GET",
                dataSourceScript: "return data;"
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "placeholder", label: "占位提示", editor: "input" },
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
            designer.drawInputLikePreview(context, field, actualX, actualY, "请选择", function (controlX, controlY, controlWidth) {
                context.fillText("⌄", controlX + controlWidth - 20, controlY + 19);
            });
        },
        buildPreviewControlHtml: function (designer, field) {
            var options = field.options && field.options.length ? field.options : [];
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var style = field.color ? " style=\"border-color:" + designer.escapeHtml(field.color) + ";\"" : "";
            var defaultValue = field.defaultValue || "";
            var dataSourceAttrs = field.dataSourceType === "request" && field.dataSourceUrl
                ? [
                    " data-source-url=\"", designer.escapeHtml(field.dataSourceUrl), "\"",
                    " data-source-method=\"", designer.escapeHtml(field.dataSourceMethod || "GET"), "\"",
                    " data-source-script=\"", designer.escapeHtml(field.dataSourceScript || ""), "\"",
                    " data-default-value=\"", designer.escapeHtml(defaultValue), "\""
                ].join("")
                : "";
            var html = ["<div class=\"el-select\"><select class=\"el-input__inner el-select__inner canvas-form-preview-select\" name=\"", name, "\"", dataSourceAttrs, style, ">"];
            html.push("<option value=\"\">", designer.escapeHtml(field.placeholder || "请选择"), "</option>");
            for (var index = 0; index < options.length; index += 1) {
                var value = options[index].value || options[index].label || "";
                html.push("<option value=\"", designer.escapeHtml(value), "\"", String(value) === String(defaultValue) ? " selected" : "", ">", designer.escapeHtml(options[index].label || value), "</option>");
            }
            html.push("</select></div>");
            return html.join("");
        }
    });
})();
