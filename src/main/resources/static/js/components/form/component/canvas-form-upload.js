/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "upload",
        label: "附件上传",
        icon: "el-icon-upload2",
        width: 360,
        height: 88,
        schemaFields: ["fieldKey", "required", "readOnly"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                required: false,
                readOnly: false
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
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
            var controlWidth = field.width;
            designer.drawRoundRect(context, controlX, controlY, controlWidth, Math.max(30, field.height), 8, "#f4f8ff", field.color || "#cddcf0");
            context.fillStyle = field.color || "#3477f6";
            context.font = "12px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillText("点击上传附件", controlX + 18, controlY + 20);
        },
        buildPreviewControlHtml: function (designer, field) {
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var style = field.color ? " style=\"border-color:" + designer.escapeHtml(field.color) + ";color:" + designer.escapeHtml(field.color) + ";\"" : "";
            return "<label class=\"el-upload\"><span class=\"el-upload__trigger\"" + style + ">点击上传附件</span><input style=\"display:none;\" type=\"file\" name=\"" + name + "\"></label>";
        }
    });
})();
