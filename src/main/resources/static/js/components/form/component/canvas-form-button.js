/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "button",
        label: "操作按钮",
        icon: "el-icon-thumb",
        width: 96,
        height: 42,
        supportDefaultValue: true,
        schemaFields: ["defaultValue", "horizontalAlign", "verticalAlign"],
        defaultProps: function (label, index) {
            return {
                defaultValue: "",
                horizontalAlign: "center",
                verticalAlign: "middle",
                color: "#1abc9c"
            };
        },
        propertyFields: [
            { prop: "defaultValue", label: "按钮文案", editor: "input" },
            { prop: "horizontalAlign", label: "水平位置", editor: "select", options: [
                { label: "居左", value: "left" },
                { label: "居中", value: "center" },
                { label: "居右", value: "right" }
            ] },
            { prop: "verticalAlign", label: "垂直位置", editor: "select", options: [
                { label: "居上", value: "top" },
                { label: "居中", value: "middle" },
                { label: "居下", value: "bottom" }
            ] },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" }
        ],
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            var controlX = actualX;
            var buttonHeight = Math.max(28, field.height);
            var text = field.defaultValue || field.label || "按钮";
            var horizontalAlign = field.horizontalAlign || "center";
            var verticalAlign = field.verticalAlign || "middle";
            designer.drawRoundRect(context, controlX, actualY, field.width, buttonHeight, 6, field.color || "#1abc9c", field.color || "#1abc9c");
            context.fillStyle = "#ffffff";
            context.font = "700 13px Microsoft YaHei, PingFang SC, sans-serif";
            context.textAlign = horizontalAlign;
            context.textBaseline = "middle";
            context.fillText(text, this.resolveTextX(controlX, field.width, horizontalAlign), this.resolveTextY(actualY, buttonHeight, verticalAlign));
            context.textAlign = "left";
            context.textBaseline = "alphabetic";
        },
        buildPreviewControlHtml: function (designer, field) {
            var style = [
                "display:flex;",
                "align-items:", this.resolveCssVerticalAlign(field.verticalAlign), ";",
                "justify-content:", this.resolveCssHorizontalAlign(field.horizontalAlign), ";"
            ];
            if (field.color) {
                style.push("background:", designer.escapeHtml(field.color), ";border-color:", designer.escapeHtml(field.color), ";");
            }
            return [
                "<button class=\"el-button canvas-form-preview-button\" type=\"button\" style=\"", style.join(""), "\">",
                designer.escapeHtml(field.defaultValue || field.label || "按钮"),
                "</button>"
            ].join("");
        },
        resolveTextX: function (left, width, align) {
            if (align === "left") {
                return left + 14;
            }
            if (align === "right") {
                return left + width - 14;
            }
            return left + width / 2;
        },
        resolveTextY: function (top, height, align) {
            if (align === "top") {
                return top + 14;
            }
            if (align === "bottom") {
                return top + height - 14;
            }
            return top + height / 2;
        },
        resolveCssHorizontalAlign: function (align) {
            if (align === "left") {
                return "flex-start";
            }
            if (align === "right") {
                return "flex-end";
            }
            return "center";
        },
        resolveCssVerticalAlign: function (align) {
            if (align === "top") {
                return "flex-start";
            }
            if (align === "bottom") {
                return "flex-end";
            }
            return "center";
        }
    });
})();
