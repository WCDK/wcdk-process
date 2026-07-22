/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "textarea",
        label: "多行输入框",
        icon: "el-icon-document",
        width: 420,
        height: 120,
        supportPlaceholder: true,
        supportDefaultValue: true,
        schemaFields: ["fieldKey", "placeholder", "required", "readOnly", "defaultValue", "rows"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                placeholder: "请输入" + label,
                required: false,
                readOnly: false,
                defaultValue: "",
                rows: 3
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "placeholder", label: "占位提示", editor: "input" },
            { prop: "defaultValue", label: "默认值", editor: "input" },
            { prop: "rows", label: "显示行数", editor: "number", min: 2, max: 8 },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" },
            { prop: "state", label: "", editor: "checkboxes", options: [
                { prop: "required", label: "必填" },
                { prop: "readOnly", label: "只读" }
            ] }
        ],
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            designer.drawInputLikePreview(context, field, actualX, actualY, field.placeholder || "请输入");
        },
        buildPreviewControlHtml: function (designer, field) {
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var style = field.color ? " style=\"border-color:" + designer.escapeHtml(field.color) + ";\"" : "";
            return "<div class=\"el-textarea\"><textarea class=\"el-textarea__inner\" name=\"" + name + "\" placeholder=\"" + designer.escapeHtml(field.placeholder || "请输入") + "\"" + style + ">" + designer.escapeHtml(field.defaultValue || "") + "</textarea></div>";
        }
    });
})();
