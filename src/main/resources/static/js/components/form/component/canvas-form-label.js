/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "label",
        label: "标签",
        icon: "el-icon-thumb",
        width: 120,
        height: 32,
        supportDefaultValue: true,
        disableCommonActionFields: true,
        schemaFields: ["defaultValue"],
        defaultProps: function () {
            return {
                defaultValue: "标签文本"
            };
        },
        propertyFields: [
            { prop: "defaultValue", label: "标签文本", editor: "input" },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" }
        ],
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            context.fillStyle = field.color || "#303133";
            context.font = "14px Microsoft YaHei, PingFang SC, sans-serif";
            context.textBaseline = "middle";
            context.fillText(field.defaultValue || "标签文本", actualX, actualY + Math.max(field.height || 32, 32) / 2);
            context.textBaseline = "alphabetic";
        },
        buildPreviewFieldHtml: function (designer, field, offsetX, offsetY) {
            var style = designer.buildPreviewPositionStyle(field, offsetX, offsetY);
            if (field.color) {
                style += "color:" + designer.escapeHtml(field.color) + ";";
            }
            return [
                "<div class=\"canvas-form-preview-field\" style=\"", style, "display:flex;align-items:center;\">",
                designer.escapeHtml(field.defaultValue || "标签文本"),
                "</div>"
            ].join("");
        }
    });
})();
