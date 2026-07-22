/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "checkbox",
        label: "多选框",
        icon: "el-icon-finished",
        width: 380,
        height: 88,
        supportOptions: true,
        schemaFields: ["fieldKey", "required", "readOnly", "defaultValue", "options", "dataSourceType", "presetOptionsText", "dataSourceUrl", "dataSourceMethod", "dataSourceScript"],
        defaultProps: function (label, index, designer) {
            return {
                fieldKey: "field_" + (index + 1),
                required: false,
                readOnly: false,
                defaultValue: "",
                options: designer.defaultOptions("checkbox"),
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
            { prop: "presetOptionsText", label: "预设选项", editor: "textarea", rows: 4, placeholder: "JSON 数组，例如：[{\"label\":\"同意\",\"value\":\"agree\"},{\"label\":\"拒绝\",\"value\":\"reject\"}]", visibleWhen: { prop: "dataSourceType", value: "preset" } },
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
                context.strokeRect(cursorX + 1, actualY + 4, 12, 12);
                context.fillStyle = "#5f718a";
                context.fillText(option.label || option.value || "-", cursorX + 20, actualY + 15);
                cursorX += Math.max(74, context.measureText(option.label || option.value || "-").width + 38);
            }
        },
        buildPreviewControlHtml: function (designer, field) {
            var options = field.options && field.options.length ? field.options : [];
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var accentStyle = field.color ? " style=\"accent-color:" + designer.escapeHtml(field.color) + ";\"" : "";
            var defaultValues = String(field.defaultValue || "").split(",").map(function (item) {
                return item.trim();
            });
            var dataSourceAttrs = field.dataSourceType === "request" && field.dataSourceUrl
                ? [
                    " data-source-url=\"", designer.escapeHtml(field.dataSourceUrl), "\"",
                    " data-source-method=\"", designer.escapeHtml(field.dataSourceMethod || "GET"), "\"",
                    " data-source-script=\"", designer.escapeHtml(field.dataSourceScript || ""), "\"",
                    " data-default-value=\"", designer.escapeHtml(field.defaultValue || ""), "\""
                ].join("")
                : "";
            var html = ["<div class=\"el-checkbox-group canvas-form-preview-options\" data-option-type=\"checkbox\" data-option-name=\"", name, "\"", dataSourceAttrs, ">"];
            for (var index = 0; index < options.length; index += 1) {
                var value = options[index].value || options[index].label || "";
                html.push(
                    "<label class=\"el-checkbox\"><input class=\"el-checkbox__input\" type=\"checkbox\" name=\"", name, "\" value=\"",
                    designer.escapeHtml(value), "\"", defaultValues.indexOf(String(value)) >= 0 ? " checked" : "", accentStyle, "><span>", designer.escapeHtml(options[index].label || value || "-"), "</span></label>"
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
