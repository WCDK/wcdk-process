/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "group",
        label: "表单",
        icon: "el-icon-folder-opened",
        width: 520,
        height: 240,
        disableCommonActionFields: true,
        schemaFields: ["label", "groupDescription", "submitUrl", "submitMethod", "submitParamMode"],
        defaultProps: function (label, index) {
            return {
                groupDescription: "",
                submitUrl: "",
                submitMethod: "POST",
                submitParamMode: "body"
            };
        },
        propertyFields: [
            { prop: "label", label: "表单标题", editor: "input" },
            { prop: "groupDescription", label: "表单说明", editor: "input" },
            { prop: "submitUrl", label: "提交地址", editor: "input", placeholder: "请输入表单提交地址" },
            { prop: "submitMethod", label: "请求方式", editor: "select", options: [
                { label: "GET", value: "GET" },
                { label: "POST", value: "POST" },
                { label: "PUT", value: "PUT" },
                { label: "DELETE", value: "DELETE" }
            ] },
            { prop: "submitParamMode", label: "提交方式", editor: "select", options: [
                { label: "body", value: "body" },
                { label: "requestParam", value: "requestParam" }
            ] },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" }
        ],
        buildPreviewFieldHtml: function (designer, field, offsetX, offsetY) {
            var style = designer.buildPreviewPositionStyle(field, offsetX, offsetY);
            var description = field.groupDescription ? "<span class=\"canvas-form-preview-group-desc\">" + designer.escapeHtml(field.groupDescription) + "</span>" : "";
            var children = field.children || [];
            var childrenHtml = children.length
                ? designer.buildPreviewHtml(children, 0, 0)
                : "<div class=\"canvas-form-preview-group-empty\">暂无可预览字段</div>";
            var submitAttrs = [
                " data-submit-url=\"", designer.escapeHtml(field.submitUrl || ""), "\"",
                " data-submit-method=\"", designer.escapeHtml(field.submitMethod || "POST"), "\"",
                " data-submit-param-mode=\"", designer.escapeHtml(field.submitParamMode || "body"), "\""
            ].join("");
            return [
                "<div class=\"canvas-form-preview-group\" style=\"", style, field.color ? "border-color:" + designer.escapeHtml(field.color) + ";" : "", "\"", submitAttrs, ">",
                "<div class=\"canvas-form-preview-group-title\"", field.color ? " style=\"color:" + designer.escapeHtml(field.color) + ";\"" : "", ">", designer.escapeHtml(field.label || "表单"), description, "</div>",
                "<div class=\"canvas-form-preview-group-body\">",
                childrenHtml,
                "</div>",
                "</div>"
            ].join("");
        },
        drawField: function (designer, context, field, actualX, actualY, selected, hovered) {
            designer.drawRoundRect(context, actualX, actualY, field.width, field.height, 12, "#fbfdff", selected ? "#3477f6" : (hovered ? "#58b9ff" : (field.color || "#c7d8f0")));
            context.fillStyle = field.color || "#18263f";
            context.font = "700 13px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillText(field.label || "表单", actualX + 14, actualY + 22);
            context.fillStyle = "#7a8ca6";
            context.font = "12px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillText(field.groupDescription || "拖入组件形成表单", actualX + 14, actualY + 42);
            context.strokeStyle = field.color || "#d4e2f4";
            context.setLineDash([6, 4]);
            context.strokeRect(actualX + 12, actualY + 52, field.width - 24, field.height - 64);
            context.setLineDash([]);
            this.drawGroupChildren(designer, context, field, actualX, actualY);
            if (selected) {
                context.fillStyle = "#3477f6";
                context.fillRect(actualX + field.width - 10, actualY + field.height - 10, 8, 8);
            }
        },
        drawGroupChildren: function (designer, context, group, actualX, actualY) {
            var clipRect = {
                x: actualX + 12,
                y: actualY + 52,
                width: Math.max(20, group.width - 24),
                height: Math.max(20, group.height - 64)
            };
            var fields = group.children || [];
            if (!fields.length) {
                context.fillStyle = "#a2b1c4";
                context.font = "12px Microsoft YaHei, PingFang SC, sans-serif";
                context.fillText("拖入组件", actualX + 24, actualY + 78);
            }
            for (var index = 0; index < fields.length; index += 1) {
                designer.drawField(context, fields[index], actualX, actualY, clipRect);
            }
        }
    });
})();
