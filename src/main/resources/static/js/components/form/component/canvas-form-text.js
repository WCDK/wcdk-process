/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "text",
        label: "说明文本",
        icon: "el-icon-tickets",
        width: 420,
        height: 64,
        supportDefaultValue: true,
        schemaFields: ["fieldKey", "defaultValue"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                defaultValue: ""
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "defaultValue", label: "默认值", editor: "input" },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" }
        ],
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            context.fillStyle = field.color || "#5f718a";
            context.font = "13px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillText(field.defaultValue || "说明文本内容", actualX, actualY + 22);
        },
        buildPreviewControlHtml: function (designer, field) {
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var style = field.color ? " style=\"border-color:" + designer.escapeHtml(field.color) + ";color:" + designer.escapeHtml(field.color) + ";\"" : "";
            return "<div class=\"el-input\"><input class=\"el-input__inner\" type=\"text\" name=\"" + name + "\" value=\"" + designer.escapeHtml(field.defaultValue || "说明文本内容") + "\"" + style + "></div>";
        }
    });
})();
