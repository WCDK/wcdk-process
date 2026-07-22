/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
(function () {
    window.CanvasFormDesignerComponentRegistry.register({
        type: "dataTable",
        label: "数据表格",
        icon: "el-icon-menu",
        width: 900,
        height: 180,
        schemaFields: ["fieldKey", "label", "tableHeaderText", "tableDataText", "paginationText"],
        defaultProps: function (label, index) {
            return {
                fieldKey: "field_" + (index + 1),
                tableHeaderText: "账号\n姓名\n状态",
                tableDataText: "zhangsan|张三|启用\nlisi|李四|停用",
                paginationText: ""
            };
        },
        propertyFields: [
            { prop: "label", label: "表格标题", editor: "input" },
            { prop: "fieldKey", label: "绑定字段", editor: "input" },
            { prop: "tableHeaderText", label: "表格列名", editor: "textarea", rows: 3, placeholder: "每行一个列名，例如：账号" },
            { prop: "tableDataText", label: "示例数据", editor: "textarea", rows: 5, placeholder: "每行一条记录，列之间使用竖线分隔" },
            { prop: "paginationText", label: "分页文案", editor: "input" },
            { prop: "width", label: "组件宽度", editor: "number" },
            { prop: "height", label: "组件高度", editor: "number" }
        ],
        buildPreviewFieldHtml: function (designer, field, offsetX, offsetY) {
            var style = designer.buildPreviewPositionStyle(field, offsetX, offsetY);
            var headers = this.parseTableHeaderText(field.tableHeaderText);
            var rows = this.parseTableDataText(field.tableDataText);
            var html = [
                "<div class=\"el-table canvas-form-preview-action\" style=\"", style, field.color ? "border-color:" + designer.escapeHtml(field.color) + ";" : "", "\"", designer.buildCommonPreviewAttrs(field), ">",
                "<table><thead><tr>"
            ];
            for (var headerIndex = 0; headerIndex < headers.length; headerIndex += 1) {
                html.push("<th>", designer.escapeHtml(headers[headerIndex]), "</th>");
            }
            html.push("</tr></thead><tbody>");
            if (!rows.length) {
                html.push("<tr><td class=\"el-table__empty-text\" colspan=\"", headers.length, "\">暂无数据</td></tr>");
            }
            for (var rowIndex = 0; rowIndex < rows.length; rowIndex += 1) {
                html.push("<tr>");
                for (var columnIndex = 0; columnIndex < headers.length; columnIndex += 1) {
                    html.push("<td>", designer.escapeHtml(rows[rowIndex][columnIndex] || "-"), "</td>");
                }
                html.push("</tr>");
            }
            html.push("</tbody></table>");
            if (field.paginationText) {
                html.push("<div class=\"el-pagination\">", designer.escapeHtml(field.paginationText), "</div>");
            }
            html.push("</div>");
            return html.join("");
        },
        drawField: function (designer, context, field, actualX, actualY, selected, hovered) {
            designer.drawRoundRect(context, actualX, actualY, field.width, field.height, 4, "#ffffff", selected ? "#3477f6" : (hovered ? "#58b9ff" : (field.color || "#c7d8f0")));
            var headers = this.parseTableHeaderText(field.tableHeaderText);
            var rows = this.parseTableDataText(field.tableDataText);
            var tableHeight = field.paginationText ? field.height - 30 : field.height;
            var rowHeight = Math.max(28, Math.min(38, tableHeight / (rows.length + 1)));
            var columnWidth = field.width / headers.length;
            context.fillStyle = "#f3f7ff";
            context.fillRect(actualX, actualY, field.width, rowHeight);
            context.strokeStyle = field.color || "#d4e2f4";
            context.lineWidth = 1;
            context.font = "700 12px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillStyle = "#333333";
            for (var headerIndex = 0; headerIndex < headers.length; headerIndex += 1) {
                var headerX = actualX + headerIndex * columnWidth;
                context.strokeRect(headerX, actualY, columnWidth, rowHeight);
                context.fillText(headers[headerIndex], headerX + 8, actualY + 19);
            }
            context.font = "12px Microsoft YaHei, PingFang SC, sans-serif";
            context.fillStyle = "#5f718a";
            for (var rowIndex = 0; rowIndex < rows.length; rowIndex += 1) {
                var rowY = actualY + rowHeight * (rowIndex + 1);
                for (var columnIndex = 0; columnIndex < headers.length; columnIndex += 1) {
                    var cellX = actualX + columnIndex * columnWidth;
                    context.strokeRect(cellX, rowY, columnWidth, rowHeight);
                    context.fillText(rows[rowIndex][columnIndex] || "-", cellX + 8, rowY + 19);
                }
            }
            if (field.paginationText) {
                context.fillStyle = "#f8fbff";
                context.fillRect(actualX, actualY + field.height - 30, field.width, 30);
                context.strokeStyle = "#d4e2f4";
                context.strokeRect(actualX, actualY + field.height - 30, field.width, 30);
                context.fillStyle = "#7a8ca6";
                context.fillText(field.paginationText, actualX + 8, actualY + field.height - 11);
            }
            if (selected) {
                context.fillStyle = "#3477f6";
                context.fillRect(actualX + field.width - 10, actualY + field.height - 10, 8, 8);
            }
        },
        parseTableHeaderText: function (text) {
            var headers = String(text || "").split("\n").map(function (line) {
                return line.trim();
            }).filter(function (line) {
                return !!line;
            });
            return headers.length ? headers : ["列一", "列二"];
        },
        parseTableDataText: function (text) {
            return String(text || "").split("\n").map(function (line) {
                return line.split("|").map(function (cell) {
                    return cell.trim();
                });
            }).filter(function (row) {
                return row.length && row.join("");
            });
        }
    });
})();
