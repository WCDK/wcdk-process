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
        schemaFields: ["fieldKey", "required", "readOnly", "defaultValue", "activeText", "inactiveText", "valueType", "activeValue", "inactiveValue"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                required: false,
                readOnly: false,
                defaultValue: false,
                activeText: "启用",
                inactiveText: "关闭",
                valueType: "boolean",
                activeValue: true,
                inactiveValue: false
            };
        },
        propertyFields: [
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "activeText", label: "开启显示内容", editor: "input", placeholder: "启用" },
            { prop: "inactiveText", label: "关闭显示内容", editor: "input", placeholder: "关闭" },
            { prop: "valueType", label: "返回类型", editor: "select", options: [
                { label: "布尔", value: "boolean" },
                { label: "字符串", value: "string" },
                { label: "数字", value: "number" }
            ] },
            { prop: "activeValue", label: "开启返回值", editor: "input", placeholder: "true" },
            { prop: "inactiveValue", label: "关闭返回值", editor: "input", placeholder: "false" },
            { prop: "defaultValue", label: "默认值", editor: "input", placeholder: "填写开启或关闭返回值" },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" },
            { prop: "state", label: "", editor: "checkboxes", options: [
                { prop: "required", label: "必填" },
                { prop: "readOnly", label: "只读" }
            ] }
        ],
        resolveSwitchOptions: function (field) {
            var activeText = field.activeText || "启用";
            var inactiveText = field.inactiveText || "关闭";
            var activeValue = typeof field.activeValue === "undefined" || field.activeValue === null ? true : field.activeValue;
            var inactiveValue = typeof field.inactiveValue === "undefined" || field.inactiveValue === null ? false : field.inactiveValue;
            var defaultValue = typeof field.defaultValue === "undefined" || field.defaultValue === null ? inactiveValue : field.defaultValue;
            var checked = String(defaultValue) === String(activeValue) || defaultValue === true && String(activeValue) === "true";
            return {
                activeText: activeText,
                inactiveText: inactiveText,
                activeValue: activeValue,
                inactiveValue: inactiveValue,
                valueType: field.valueType || "boolean",
                checked: checked
            };
        },
        drawControlPreview: function (designer, context, field, actualX, actualY) {
            var controlX = actualX;
            var controlY = actualY;
            var options = this.resolveSwitchOptions(field);
            var color = options.checked ? (field.color || "#10a37f") : "#dcdfe6";
            designer.drawRoundRect(context, controlX, controlY, 48, 24, 12, color, color);
            context.fillStyle = "#ffffff";
            context.beginPath();
            context.arc(controlX + (options.checked ? 35 : 13), controlY + 12, 9, 0, Math.PI * 2);
            context.fill();
            context.fillStyle = "#606266";
            context.font = "14px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillText(options.checked ? options.activeText : options.inactiveText, controlX + 58, controlY + 17);
        },
        buildPreviewControlHtml: function (designer, field) {
            var name = designer.escapeHtml(field.fieldKey || field.id || "");
            var options = this.resolveSwitchOptions(field);
            var style = field.color ? " style=\"background:" + designer.escapeHtml(field.color) + ";\"" : "";
            var activeText = designer.escapeHtml(options.activeText);
            var inactiveText = designer.escapeHtml(options.inactiveText);
            var activeValue = designer.escapeHtml(String(options.activeValue));
            var inactiveValue = designer.escapeHtml(String(options.inactiveValue));
            return [
                "<label class=\"el-switch\">",
                "<input type=\"hidden\" name=\"", name, "\" value=\"", options.checked ? activeValue : inactiveValue,
                "\" data-value-type=\"", designer.escapeHtml(options.valueType),
                "\" data-active-value=\"", activeValue, "\" data-inactive-value=\"", inactiveValue, "\">",
                "<input type=\"checkbox\" value=\"", activeValue, "\"", options.checked ? " checked" : "",
                " data-active-text=\"", activeText, "\" data-inactive-text=\"", inactiveText,
                "\" data-active-value=\"", activeValue, "\" data-inactive-value=\"", inactiveValue,
                "\" onchange=\"this.previousElementSibling.value=this.checked?this.getAttribute('data-active-value'):this.getAttribute('data-inactive-value');this.nextElementSibling.nextElementSibling.textContent=this.checked?this.getAttribute('data-active-text'):this.getAttribute('data-inactive-text');\">",
                "<span class=\"el-switch__core\"", style, "></span>",
                "<span>", options.checked ? activeText : inactiveText, "</span>",
                "</label>"
            ].join("");
        }
    });
})();
