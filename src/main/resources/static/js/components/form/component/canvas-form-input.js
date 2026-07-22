/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "input",
        label: "普通输入框",
        icon: "el-icon-edit-outline",
        width: 320,
        height: 72,
        supportPlaceholder: true,
        supportDefaultValue: true,
        schemaFields: ["fieldKey", "placeholder", "required", "readOnly", "defaultValue"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                placeholder: "请输入" + label,
                required: false,
                readOnly: false,
                defaultValue: ""
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "placeholder", label: "占位提示", editor: "input" },
            { prop: "defaultValue", label: "默认值", editor: "input" },
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
            return "<div class=\"el-input\"><input class=\"el-input__inner\" type=\"text\" name=\"" + name + "\" placeholder=\"" + designer.escapeHtml(field.placeholder || "请输入") + "\" value=\"" + designer.escapeHtml(field.defaultValue || "") + "\"" + style + "></div>";
        }
    });
})();
