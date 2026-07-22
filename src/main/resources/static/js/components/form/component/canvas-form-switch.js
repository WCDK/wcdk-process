/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "switch",
        label: "开关",
        icon: "el-icon-open",
        width: 260,
        height: 72,
        schemaFields: ["fieldKey", "required", "readOnly", "defaultValue"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                required: false,
                readOnly: false,
                defaultValue: false
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "defaultValue", label: "默认状态", editor: "select", options: [
                { label: "关闭", value: false },
                { label: "启用", value: true }
            ] },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" },
            { prop: "state", label: "", editor: "checkboxes", options: [
                { prop: "required", label: "必填" },
                { prop: "readOnly", label: "只读" }
            ] }
        ],
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            var controlX = actualX;
            var controlY = actualY;
            var checked = field.defaultValue === true || field.defaultValue === "true";
            var color = checked ? (field.color || "#10a37f") : "#dcdfe6";
            designer.drawRoundRect(context, controlX, controlY, 48, 24, 12, color, color);
            context.fillStyle = "#ffffff";
            context.beginPath();
            context.arc(controlX + (checked ? 35 : 13), controlY + 12, 9, 0, Math.PI * 2);
            context.fill();
            context.fillStyle = "#606266";
            context.font = "14px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillText(checked ? "启用" : "关闭", controlX + 58, controlY + 17);
        },
        buildPreviewControlHtml: function (designer, field) {
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var checked = field.defaultValue === true || field.defaultValue === "true";
            var style = field.color ? " style=\"background:" + designer.escapeHtml(field.color) + ";\"" : "";
            return [
                "<label class=\"el-switch\">",
                "<input type=\"hidden\" name=\"", name, "\" value=\"", checked ? "true" : "false", "\">",
                "<input type=\"checkbox\" value=\"true\"", checked ? " checked" : "",
                " onchange=\"this.previousElementSibling.value=this.checked?'true':'false';this.nextElementSibling.nextElementSibling.textContent=this.checked?'启用':'关闭';\">",
                "<span class=\"el-switch__core\"", style, "></span>",
                "<span>", checked ? "启用" : "关闭", "</span>",
                "</label>"
            ].join("");
        }
    });
})();
