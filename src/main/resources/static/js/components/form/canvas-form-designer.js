/**
 * @auther WCDK
 * @date 2026/7/20
 * @version 1.0
 **/
(function () {
    var canvasFormDesignerStyleId = "canvas-form-designer-inline-style";
    var canvasFormDesignerComponentFiles = [
        "component/canvas-form-group.js",
        "component/canvas-form-label.js",
        "component/canvas-form-input.js",
        "component/canvas-form-textarea.js",
        "component/canvas-form-number.js",
        "component/canvas-form-date.js",
        "component/canvas-form-select.js",
        "component/canvas-form-radio.js",
        "component/canvas-form-checkbox.js",
        "component/canvas-form-switch.js",
        "component/canvas-form-upload.js",
        "component/canvas-form-text.js",
        "component/canvas-form-button.js",
        "component/canvas-form-data-table.js",
    ];

    window.CanvasFormDesignerComponentRegistry = window.CanvasFormDesignerComponentRegistry || {
        items: {},
        order: [],
        defaults: {
            label: "普通输入框",
            width: 320,
            height: 72,
            defaultProps: {},
            propertyFields: [],
            schemaFields: []
        },
        commonDefaultProps: {
            color: "",
            triggerMode: "click",
            function: "",
            httpMethod: "",
            param: ""
        },
        commonPropertyFields: [
            { prop: "color", label: "组件颜色", editor: "color" },
            { prop: "triggerMode", label: "触发方式", editor: "select", placeholder: "点击触发", options: [
                { label: "click", value: "click" },
                { label: "onchange", value: "onchange" }
            ] },
            { prop: "function", label: "触发事件", editor: "input", placeholder: "函数名或请求地址" },
            { prop: "httpMethod", label: "请求方法", editor: "select", placeholder: "不请求", options: [
                { label: "不请求", value: "" },
                { label: "GET", value: "GET" },
                { label: "POST", value: "POST" },
                { label: "PUT", value: "PUT" },
                { label: "DELETE", value: "DELETE" }
            ] },
            { prop: "param", label: "请求参数", editor: "textarea", rows: 3, placeholder: "JSON 或查询字符串" }
        ],
        commonSchemaFields: ["color", "triggerMode", "function", "httpMethod", "param"],
        commonActionProps: ["triggerMode", "function", "httpMethod", "param"],
        register: function (config) {
            if (!config || !config.type) {
                return;
            }
            if (!this.items[config.type]) {
                this.order.push(config.type);
            }
            this.items[config.type] = config;
        },
        get: function (type) {
            return this.items[type] || this.items.input;
        },
        resolve: function (type) {
            return Object.assign({}, this.defaults, this.get(type) || {});
        },
        list: function () {
            var self = this;
            return this.order.map(function (type) {
                return self.items[type];
            }).filter(function (item) {
                return !!item;
            });
        }
    };

    function resolveCanvasFormDesignerBasePath() {
        var scripts = document.getElementsByTagName("script");
        for (var index = scripts.length - 1; index >= 0; index -= 1) {
            var source = scripts[index].getAttribute("src") || "";
            var markerIndex = source.indexOf("canvas-form-designer.js");
            if (markerIndex >= 0) {
                return source.substring(0, markerIndex);
            }
        }
        return "/js/components/form/";
    }

    function loadCanvasFormDesignerComponents() {
        var basePath = resolveCanvasFormDesignerBasePath();
        for (var index = 0; index < canvasFormDesignerComponentFiles.length; index += 1) {
            var fileName = canvasFormDesignerComponentFiles[index];
            if (window.CanvasFormDesignerComponentRegistry.loaded && window.CanvasFormDesignerComponentRegistry.loaded[fileName]) {
                continue;
            }
            var request = new XMLHttpRequest();
            var componentUrl = basePath + fileName;
            request.open("GET", componentUrl, false);
            request.send(null);
            if ((request.status >= 200 && request.status < 300) || request.status === 0) {
                new Function(request.responseText)();
                window.CanvasFormDesignerComponentRegistry.loaded = window.CanvasFormDesignerComponentRegistry.loaded || {};
                window.CanvasFormDesignerComponentRegistry.loaded[fileName] = true;
            } else {
                throw new Error("表单组件加载失败：" + componentUrl);
            }
        }
    }

    loadCanvasFormDesignerComponents();

    window.CanvasFormDesigner = {
        name: "canvas-form-designer",
        template: `
            <div class="canvas-form-designer">
                <div class="canvas-form-sidebar">
                    <div class="canvas-form-section-title">基础组件</div>
                    <button
                        v-for="item in componentPalette"
                        :key="'palette-' + item.type"
                        class="canvas-form-palette-item"
                        type="button"
                        draggable="true"
                        @dragstart="handlePaletteDragStart(item)">
                        <i :class="item.icon"></i>
                        <span>{{ item.label }}</span>
                    </button>
                </div>

                <div class="canvas-form-workbench">
                    <div class="canvas-form-toolbar">
                        <div>
                            <div class="canvas-form-kicker">表单画布</div>
                            <strong>拖拽字段并调整布局</strong>
                        </div>
                        <div class="canvas-form-actions">
                            <el-button size="mini" type="primary" plain @click="openPreview">预览</el-button>
                            <el-button size="mini" @click="copySelectedField" :disabled="!selectedField">复制</el-button>
                            <el-button size="mini" type="danger" plain @click="deleteSelectedField" :disabled="!selectedField">删除</el-button>
                            <el-button size="mini" @click="clearFields">清空</el-button>
                        </div>
                    </div>
                    <div
                        ref="canvasWrapper"
                        class="canvas-form-canvas-wrapper"
                        tabindex="0"
                        @keydown.delete.prevent="deleteSelectedField"
                        @keydown.backspace.prevent="deleteSelectedField"
                        @dragover.prevent
                        @drop="handleCanvasDrop">
                        <canvas
                            ref="canvas"
                            class="canvas-form-canvas"
                            @mousedown="handleCanvasMouseDown"
                            @mousemove="handleCanvasMouseMove"
                            @mouseleave="handleCanvasMouseLeave">
                        </canvas>
                    </div>
                    <div class="canvas-form-helper">
                        单击选择字段，拖动移动位置，靠近其他组件边缘或中心时显示辅助线并自动吸附；表格和分组内可继续拖入基础组件，按 Delete 删除当前选中组件。
                    </div>
                </div>

                <div class="canvas-form-property">
                    <div class="canvas-form-section-title">字段属性</div>
                    <div v-if="selectedField" class="canvas-form-property-panel">
                        <el-form label-position="top" size="mini">
                            <el-form-item
                                v-for="property in selectedPropertyFields"
                                v-if="isPropertyVisible(property)"
                                :key="selectedField.id + '-' + property.prop"
                                :label="property.label">
                                <el-input
                                    v-if="property.editor === 'input'"
                                    v-model.trim="selectedField[property.prop]"
                                    :placeholder="property.placeholder || ''"
                                    @input="handleFieldPropertyChange(property)">
                                </el-input>
                                <el-input
                                    v-else-if="property.editor === 'textarea'"
                                    v-model="selectedField[property.prop]"
                                    type="textarea"
                                    :rows="property.rows || 3"
                                    :placeholder="property.placeholder || ''"
                                    @input="handleFieldPropertyChange(property)">
                                </el-input>
                                <el-input-number
                                    v-else-if="property.editor === 'number'"
                                    v-model="selectedField[property.prop]"
                                    :min="property.min"
                                    :max="property.max"
                                    @change="handleFieldPropertyChange(property)">
                                </el-input-number>
                                <el-color-picker
                                    v-else-if="property.editor === 'color'"
                                    v-model="selectedField[property.prop]"
                                    @change="handleFieldPropertyChange(property)">
                                </el-color-picker>
                                <el-input
                                    v-else-if="property.editor === 'options'"
                                    v-model="selectedOptionsText"
                                    type="textarea"
                                    :rows="property.rows || 4"
                                    :placeholder="property.placeholder || ''">
                                </el-input>
                                <el-select
                                    v-else-if="property.editor === 'select'"
                                    v-model="selectedField[property.prop]"
                                    :placeholder="property.placeholder || '请选择'"
                                    @change="handleFieldPropertyChange(property)">
                                    <el-option
                                        v-for="option in property.options || []"
                                        :key="property.prop + '-' + option.value"
                                        :label="option.label"
                                        :value="option.value">
                                    </el-option>
                                </el-select>
                                <template v-else-if="property.editor === 'checkboxes'">
                                    <el-checkbox
                                        v-for="option in property.options"
                                        :key="property.prop + '-' + option.prop"
                                        v-model="selectedField[option.prop]"
                                        @change="handleFieldPropertyChange(property)">
                                        {{ option.label }}
                                    </el-checkbox>
                                </template>
                            </el-form-item>
                        </el-form>
                    </div>
                    <div v-else class="canvas-form-empty">请选择画布中的字段</div>
                    <div class="canvas-form-section-title canvas-form-json-title">字段结构</div>
                    <pre class="canvas-form-json">{{ schemaText }}</pre>
                </div>

                <el-dialog
                    title="表单预览"
                    :visible.sync="previewDialogVisible"
                    width="80%"
                    class="canvas-form-preview-dialog"
                    append-to-body>
                    <div
                        class="canvas-form-preview-dialog-body"
                        v-html="previewHtml"
                        @click.capture="handlePreviewClick"
                        @change.capture="handlePreviewChange"
                        @submit.prevent>
                    </div>
                </el-dialog>
            </div>
        `,
        props: {
            value: {
                type: Array,
                default: function () {
                    return [];
                }
            },
            height: {
                type: Number,
                default: 620
            },
            readonly: {
                type: Boolean,
                default: false
            }
        },
        data: function () {
            return {
                componentPalette: window.CanvasFormDesignerComponentRegistry.list().map(function (item) {
                    return { type: item.type, label: item.label, icon: item.icon };
                }),
                fields: [],
                selectedId: "",
                hoverId: "",
                dragType: "",
                canvasDrag: {
                    active: false,
                    mode: "",
                    fieldId: "",
                    startX: 0,
                    startY: 0,
                    startFieldX: 0,
                    startFieldY: 0,
                    startWidth: 0,
                    startHeight: 0
                },
                alignmentGuides: [],
                selectedOptionsText: "",
                previewDialogVisible: false,
                previewHtml: ""
            };
        },
        computed: {
            selectedField: function () {
                return this.findFieldById(this.selectedId);
            },
            selectedPropertyFields: function () {
                return this.resolveFieldPropertyFields(this.selectedField);
            },
            schemaText: function () {
                return JSON.stringify(this.exportSchema(), null, 2);
            }
        },
        watch: {
            value: {
                immediate: true,
                handler: function (nextValue) {
                    var nextFields = this.normalizeFields(nextValue || []);
                    if (this.isSameSchema(nextFields, this.fields)) {
                        return;
                    }
                    this.fields = nextFields;
                    this.$nextTick(this.renderCanvas);
                }
            },
            selectedField: function (field) {
                this.selectedOptionsText = field && this.hasOptionsProperty(field) && Array.isArray(field.options)
                    ? field.options.map(function (item) { return item.label || item.value || ""; }).join("\n")
                    : "";
            },
            selectedOptionsText: function (value) {
                if (!this.selectedField || !this.hasOptionsProperty(this.selectedField)) {
                    return;
                }
                this.selectedField.options = this.parseOptionsText(value);
                this.syncSelectedField();
            }
        },
        methods: {
            ensureStyle: function () {
                if (document.getElementById(canvasFormDesignerStyleId)) {
                    return;
                }
                var style = document.createElement("style");
                style.id = canvasFormDesignerStyleId;
                style.textContent = [
                    ".canvas-form-designer{display:grid;grid-template-columns:180px minmax(0,1fr) 300px;gap:14px;min-width:0;}",
                    ".canvas-form-sidebar,.canvas-form-workbench,.canvas-form-property{min-width:0;border:1px solid #e1eaf8;border-radius:18px;background:#f8fbff;padding:14px;}",
                    ".canvas-form-sidebar{display:grid;align-content:start;gap:8px;}",
                    ".canvas-form-section-title{margin-bottom:6px;font-size:13px;font-weight:700;color:#18263f;}",
                    ".canvas-form-palette-item{height:38px;border:1px solid #dce8f8;border-radius:10px;background:#fff;color:#25364f;display:flex;align-items:center;gap:8px;padding:0 10px;cursor:grab;text-align:left;}",
                    ".canvas-form-palette-item:hover{border-color:#3477f6;color:#245fc9;background:#f4f8ff;}",
                    ".canvas-form-workbench{display:grid;grid-template-rows:auto minmax(320px,1fr) auto;gap:10px;}",
                    ".canvas-form-toolbar{display:flex;justify-content:space-between;gap:12px;align-items:center;}",
                    ".canvas-form-kicker{font-size:12px;color:#6a88b5;letter-spacing:0.12em;}",
                    ".canvas-form-actions{display:flex;gap:8px;flex-wrap:wrap;}",
                    ".canvas-form-canvas-wrapper{overflow:auto;border:1px solid #dce8f8;border-radius:14px;background:#fff;outline:none;}",
                    ".canvas-form-canvas-wrapper:focus{box-shadow:0 0 0 2px rgba(52,119,246,0.16);}",
                    ".canvas-form-canvas{display:block;max-width:none;cursor:default;}",
                    ".canvas-form-helper{font-size:12px;color:#7a8ca6;line-height:1.6;}",
                    ".canvas-form-property{display:grid;align-content:start;gap:10px;}",
                    ".canvas-form-property-panel{max-height:360px;overflow:auto;padding-right:4px;}",
                    ".canvas-form-empty{padding:24px 12px;border:1px dashed #d8e3f4;border-radius:14px;text-align:center;color:#7a8ca6;background:#fff;}",
                    ".canvas-form-json-title{margin-top:4px;}",
                    ".canvas-form-json{max-height:220px;overflow:auto;margin:0;padding:12px;border-radius:12px;border:1px solid #e1eaf8;background:#fff;color:#25364f;font-size:12px;line-height:1.6;white-space:pre-wrap;word-break:break-all;}",
                    ".canvas-form-preview-dialog .el-dialog__body{padding:0 18px 18px;background:#f2f4f8;}",
                    ".canvas-form-preview-dialog-body{max-height:72vh;overflow:auto;padding:18px;}",
                    ".canvas-form-preview-dialog-body *{box-sizing:border-box;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-stage{position:relative;min-width:720px;min-height:420px;background:#fff;border-radius:14px;box-shadow:0 10px 28px rgba(24,38,63,0.08);overflow:auto;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-field{position:absolute;overflow:hidden;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-field .el-button{width:100%;height:100%;}",
                    ".canvas-form-preview-dialog-body .el-button{height:34px;border:1px solid #409eff;border-radius:4px;background:#409eff;color:#fff;padding:0 16px;cursor:pointer;font-size:14px;}",
                    ".canvas-form-preview-dialog-body .el-button:hover{background:#66b1ff;border-color:#66b1ff;}",
                    ".canvas-form-preview-dialog-body .el-form-item{margin:0;width:100%;height:100%;}",
                    ".canvas-form-preview-dialog-body .el-input,.canvas-form-preview-dialog-body .el-select,.canvas-form-preview-dialog-body .el-textarea{position:relative;width:100%;height:100%;font-size:14px;}",
                    ".canvas-form-preview-dialog-body .el-input__inner,.canvas-form-preview-dialog-body .el-textarea__inner{display:block;width:100%;border:1px solid #dcdfe6;border-radius:4px;background:#fff;color:#606266;font-size:14px;outline:none;transition:border-color .2s;}",
                    ".canvas-form-preview-dialog-body .el-input__inner{height:100%;line-height:32px;padding:0 15px;}",
                    ".canvas-form-preview-dialog-body .el-textarea__inner{height:100%;line-height:1.5;padding:7px 15px;resize:none;}",
                    ".canvas-form-preview-dialog-body .el-input__inner:focus,.canvas-form-preview-dialog-body .el-textarea__inner:focus,.canvas-form-preview-dialog-body .el-select__inner:focus{border-color:#409eff;}",
                    ".canvas-form-preview-dialog-body .el-radio-group,.canvas-form-preview-dialog-body .el-checkbox-group{display:flex;gap:14px;flex-wrap:wrap;align-content:flex-start;min-height:100%;line-height:32px;font-size:14px;color:#606266;}",
                    ".canvas-form-preview-dialog-body .el-radio,.canvas-form-preview-dialog-body .el-checkbox{display:inline-flex;align-items:center;gap:6px;cursor:pointer;}",
                    ".canvas-form-preview-dialog-body .el-radio__input,.canvas-form-preview-dialog-body .el-checkbox__input{width:14px;height:14px;accent-color:#409eff;}",
                    ".canvas-form-preview-dialog-body .el-switch{display:inline-flex;align-items:center;gap:8px;color:#606266;font-size:14px;cursor:pointer;}",
                    ".canvas-form-preview-dialog-body .el-switch input{display:none;}",
                    ".canvas-form-preview-dialog-body .el-switch__core{width:40px;height:20px;border-radius:10px;background:#dcdfe6;position:relative;transition:.2s;}",
                    ".canvas-form-preview-dialog-body .el-switch__core::after{content:'';position:absolute;left:2px;top:2px;width:16px;height:16px;border-radius:50%;background:#fff;transition:.2s;}",
                    ".canvas-form-preview-dialog-body .el-switch input:checked+.el-switch__core{background:#409eff;}",
                    ".canvas-form-preview-dialog-body .el-switch input:checked+.el-switch__core::after{left:22px;}",
                    ".canvas-form-preview-dialog-body .el-upload{display:block;width:100%;height:100%;}",
                    ".canvas-form-preview-dialog-body .el-upload__trigger{display:flex;width:100%;height:100%;align-items:center;justify-content:center;border:1px dashed #d9d9d9;border-radius:6px;padding:8px 10px;color:#409eff;background:#fff;font-size:14px;cursor:pointer;text-align:center;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-table{position:absolute;border:1px solid #c7d8f0;border-radius:12px;background:#fff;overflow:hidden;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-table-title,.canvas-form-preview-dialog-body .canvas-form-preview-group-title{position:relative;z-index:1;height:34px;line-height:34px;padding:0 12px;font-weight:700;color:#18263f;background:#f8fbff;border-bottom:1px solid #dce8f8;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-table-grid{position:relative;margin:12px;border:1px solid #d4e2f4;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-cell{position:absolute;border-right:1px solid #d4e2f4;border-bottom:1px solid #d4e2f4;background:#fff;overflow:hidden;}",
                    ".canvas-form-preview-dialog-body .el-table{position:absolute;border:1px solid #ebeef5;background:#fff;overflow:auto;color:#606266;font-size:14px;}",
                    ".canvas-form-preview-dialog-body .el-table table{width:100%;border-collapse:collapse;}",
                    ".canvas-form-preview-dialog-body .el-table th{height:40px;background:#f5f7fa;color:#606266;text-align:left;font-weight:700;border-right:1px solid #ebeef5;border-bottom:1px solid #ebeef5;padding:0 10px;}",
                    ".canvas-form-preview-dialog-body .el-table td{height:40px;border-right:1px solid #ebeef5;border-bottom:1px solid #ebeef5;padding:0 10px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}",
                    ".canvas-form-preview-dialog-body .el-table__empty-text{text-align:center;color:#909399;}",
                    ".canvas-form-preview-dialog-body .el-pagination{height:36px;line-height:36px;padding:0 10px;color:#606266;background:#fff;border-top:1px solid #ebeef5;font-size:13px;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-group{position:absolute;border:1px solid #c7d8f0;border-radius:12px;background:#fbfdff;overflow:hidden;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-group-body{position:absolute;left:0;top:0;width:100%;height:100%;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-group-empty{margin:58px 24px 0;color:#909399;font-size:14px;}",
                    ".canvas-form-preview-dialog-body .canvas-form-preview-group-desc{font-size:12px;font-weight:400;color:#7a8ca6;margin-left:8px;}",
                    "@media (max-width:1180px){.canvas-form-designer{grid-template-columns:1fr;}.canvas-form-property-panel{max-height:none;}}"
                ].join("");
                document.head.appendChild(style);
            },
            normalizeFields: function (items) {
                var self = this;
                return (items || []).map(function (item, index) {
                    return self.normalizeField(item, index);
                });
            },
            normalizeField: function (item, index) {
                var type = item.type || item.componentType || "input";
                var config = window.CanvasFormDesignerComponentRegistry.resolve(type);
                var hasOwn = Object.prototype.hasOwnProperty;
                var label = hasOwn.call(item, "label") ? item.label : config.label;
                var defaultProps = this.resolveFieldDefaultProps(type, label, index);
                var field = Object.assign({}, defaultProps, item, {
                    id: item.id || this.createFieldId(type),
                    type: type,
                    componentType: type,
                    label: label,
                    fieldKey: hasOwn.call(item, "fieldKey") ? item.fieldKey : (defaultProps.fieldKey || ("field_" + (index + 1))),
                    x: typeof item.x === "number" ? item.x : 40,
                    y: typeof item.y === "number" ? item.y : 40 + index * 88,
                    width: typeof item.width === "number" ? item.width : config.width,
                    height: typeof item.height === "number" ? item.height : config.height,
                    sortOrder: index + 1
                });
                if (type === "table") {
                    field.tableRows = Math.max(1, field.tableRows || item.rowsCount || 2);
                    field.tableColumns = Math.max(1, field.tableColumns || item.columnsCount || 2);
                    field.children = this.normalizeTableChildren(field, item.children || []);
                } else if (type === "group") {
                    field.children = this.normalizeGroupChildren(field, item.children || []);
                } else {
                    delete field.children;
                }
                if (this.isOptionDataSourceType(type)) {
                    this.normalizeOptionDataSourceField(field, item);
                }
                return field;
            },
            isOptionDataSourceType: function (type) {
                return ["select", "radio", "checkbox"].indexOf(type) >= 0;
            },
            normalizeOptionDataSourceField: function (field, source) {
                if (!field.dataSourceType) {
                    field.dataSourceType = field.dataSourceUrl ? "request" : "preset";
                }
                if (!field.dataSourceMethod) {
                    field.dataSourceMethod = "GET";
                }
                if ((!source || !source.presetOptionsText) && source && Array.isArray(source.options) && source.options.length) {
                    field.presetOptionsText = this.stringifyPresetOptionsText(source.options);
                } else if (!field.presetOptionsText) {
                    field.presetOptionsText = this.stringifyPresetOptionsText(field.options || []);
                }
                if (field.dataSourceType === "preset") {
                    field.options = this.parsePresetOptionsText(field.presetOptionsText);
                }
            },
            normalizeGroupChildren: function (group, children) {
                var self = this;
                return (Array.isArray(children) ? children : []).map(function (child, childIndex) {
                    var normalized = self.normalizeField(child, childIndex);
                    normalized.parentId = group.id;
                    normalized.parentType = "group";
                    normalized.x = typeof child.x === "number" ? child.x : 16;
                    normalized.y = typeof child.y === "number" ? child.y : 48 + childIndex * 82;
                    return normalized;
                }).filter(function (child) {
                    return child.type !== "table" && child.type !== "group" && child.type !== "dataTable";
                });
            },
            normalizeTableChildren: function (table, children) {
                var self = this;
                var tableChildren = [];
                for (var rowIndex = 0; rowIndex < table.tableRows; rowIndex += 1) {
                    for (var columnIndex = 0; columnIndex < table.tableColumns; columnIndex += 1) {
                        var sourceCell = this.findCell(children, rowIndex, columnIndex);
                        tableChildren.push({
                            row: rowIndex,
                            column: columnIndex,
                            fields: (sourceCell && Array.isArray(sourceCell.fields) ? sourceCell.fields : []).map(function (child, childIndex) {
                                var normalized = self.normalizeField(child, childIndex);
                                normalized.parentId = table.id;
                                normalized.cellRow = rowIndex;
                                normalized.cellColumn = columnIndex;
                                normalized.x = typeof child.x === "number" ? child.x : 12;
                                normalized.y = typeof child.y === "number" ? child.y : 12 + childIndex * 76;
                                return normalized;
                            }).filter(function (child) {
                                return child.type !== "table" && child.type !== "group" && child.type !== "dataTable";
                            })
                        });
                    }
                }
                return tableChildren;
            },
            findCell: function (children, row, column) {
                for (var index = 0; index < children.length; index += 1) {
                    if (children[index] && children[index].row === row && children[index].column === column) {
                        return children[index];
                    }
                }
                return null;
            },
            resolveFieldDefaultProps: function (type, label, index) {
                var config = window.CanvasFormDesignerComponentRegistry.resolve(type);
                var defaultProps = typeof config.defaultProps === "function"
                    ? config.defaultProps(label, index, this)
                    : config.defaultProps;
                var commonDefaultProps = Object.assign({}, window.CanvasFormDesignerComponentRegistry.commonDefaultProps);
                if (config.disableCommonActionFields) {
                    (window.CanvasFormDesignerComponentRegistry.commonActionProps || []).forEach(function (prop) {
                        delete commonDefaultProps[prop];
                    });
                }
                return Object.assign({}, commonDefaultProps, defaultProps || {});
            },
            resolveFieldPropertyFields: function (field) {
                if (!field) {
                    return [];
                }
                var config = window.CanvasFormDesignerComponentRegistry.resolve(field.type);
                var commonFields = window.CanvasFormDesignerComponentRegistry.commonPropertyFields || [];
                if (config.disableCommonActionFields) {
                    var commonActionProps = window.CanvasFormDesignerComponentRegistry.commonActionProps || [];
                    commonFields = commonFields.filter(function (item) {
                        return commonActionProps.indexOf(item.prop) < 0;
                    });
                }
                return this.mergePropertyFields(config.propertyFields || [], commonFields);
            },
            isPropertyVisible: function (property) {
                if (!property || !property.visibleWhen || !this.selectedField) {
                    return true;
                }
                var rule = property.visibleWhen;
                if (Array.isArray(rule.value)) {
                    return rule.value.indexOf(this.selectedField[rule.prop]) >= 0;
                }
                return this.selectedField[rule.prop] === rule.value;
            },
            mergePropertyFields: function (baseFields, commonFields) {
                var exists = {};
                var result = [];
                (baseFields || []).forEach(function (field) {
                    if (!field || !field.prop) {
                        return;
                    }
                    exists[field.prop] = true;
                    result.push(field);
                });
                (commonFields || []).forEach(function (field) {
                    if (!field || !field.prop || exists[field.prop]) {
                        return;
                    }
                    exists[field.prop] = true;
                    result.push(field);
                });
                return result;
            },
            mergeSchemaFields: function (baseFields, config) {
                var exists = {};
                var result = [];
                var commonFields = window.CanvasFormDesignerComponentRegistry.commonSchemaFields || [];
                if (config && config.disableCommonActionFields) {
                    var commonActionProps = window.CanvasFormDesignerComponentRegistry.commonActionProps || [];
                    commonFields = commonFields.filter(function (prop) {
                        return commonActionProps.indexOf(prop) < 0;
                    });
                }
                (baseFields || []).concat(commonFields).forEach(function (prop) {
                    if (!prop || exists[prop]) {
                        return;
                    }
                    exists[prop] = true;
                    result.push(prop);
                });
                return result;
            },
            defaultOptions: function (type) {
                var config = window.CanvasFormDesignerComponentRegistry.get(type) || {};
                if (typeof config.defaultOptions === "function") {
                    return config.defaultOptions();
                }
                return Array.isArray(config.defaultOptions) ? config.defaultOptions : [];
            },
            createFieldId: function (type) {
                return type + "_" + Date.now() + "_" + Math.floor(Math.random() * 10000);
            },
            handlePaletteDragStart: function (item) {
                if (this.readonly) {
                    return;
                }
                this.dragType = item.type;
            },
            handleCanvasDrop: function (event) {
                if (this.readonly || !this.dragType) {
                    return;
                }
                var point = this.resolveCanvasPoint(event);
                var cell = this.findTableCellAt(point.x, point.y);
                var group = this.findGroupAt(point.x, point.y);
                if (cell && this.canDropIntoContainer(this.dragType)) {
                    this.addFieldToTableCell(this.dragType, cell, point);
                } else if (group && this.canDropIntoContainer(this.dragType)) {
                    this.addFieldToGroup(this.dragType, group, point);
                } else {
                    this.addField(this.dragType, point.x, point.y);
                }
                this.dragType = "";
            },
            addField: function (type, x, y) {
                var field = this.normalizeField({
                    id: this.createFieldId(type),
                    type: type,
                    x: Math.max(20, x),
                    y: Math.max(20, y)
                }, this.fields.length);
                this.fields.push(field);
                this.selectedId = field.id;
                this.syncSelectedField();
            },
            canDropIntoContainer: function (type) {
                return ["table", "group", "dataTable"].indexOf(type) < 0;
            },
            addFieldToGroup: function (type, groupHit, point) {
                var config = window.CanvasFormDesignerComponentRegistry.resolve(type);
                var field = this.normalizeField({
                    id: this.createFieldId(type),
                    type: type,
                    x: Math.max(14, point.x - groupHit.x + 8),
                    y: Math.max(44, point.y - groupHit.y + 8),
                    width: config.width,
                    height: config.height
                }, groupHit.group.children.length);
                field.parentId = groupHit.group.id;
                field.parentType = "group";
                groupHit.group.children.push(field);
                this.selectedId = field.id;
                this.syncSelectedField();
            },
            addFieldToTableCell: function (type, cellHit, point) {
                var cell = this.findCell(cellHit.table.children, cellHit.row, cellHit.column);
                if (!cell) {
                    return;
                }
                var config = window.CanvasFormDesignerComponentRegistry.resolve(type);
                var field = this.normalizeField({
                    id: this.createFieldId(type),
                    type: type,
                    x: Math.max(10, point.x - cellHit.x + 8),
                    y: Math.max(10, point.y - cellHit.y + 8),
                    width: config.width,
                    height: config.height
                }, cell.fields.length);
                field.parentId = cellHit.table.id;
                field.cellRow = cellHit.row;
                field.cellColumn = cellHit.column;
                cell.fields.push(field);
                this.selectedId = field.id;
                this.syncSelectedField();
            },
            copySelectedField: function () {
                if (!this.selectedField) {
                    return;
                }
                var source = this.cloneField(this.selectedField);
                source.id = this.createFieldId(source.type);
                source.fieldKey = source.fieldKey + "_copy";
                source.x += 24;
                source.y += 24;
                this.pushCopiedField(source);
                this.selectedId = source.id;
                this.syncSelectedField();
            },
            cloneField: function (field) {
                return JSON.parse(JSON.stringify(field));
            },
            pushCopiedField: function (field) {
                if (field.parentType === "group") {
                    var group = this.findParentGroup(this.selectedId);
                    if (group) {
                        field.parentId = group.id;
                        field.parentType = "group";
                        group.children.push(field);
                        return;
                    }
                    delete field.parentId;
                    delete field.parentType;
                }
                if (field.parentId) {
                    var cell = this.findCellByChildId(this.selectedId);
                    if (!cell) {
                        delete field.parentId;
                        delete field.parentType;
                        delete field.cellRow;
                        delete field.cellColumn;
                        this.fields.push(field);
                        return;
                    }
                    field.parentId = cell.table.id;
                    field.cellRow = cell.row;
                    field.cellColumn = cell.column;
                    cell.cell.fields.push(field);
                    return;
                }
                this.fields.push(field);
            },
            deleteSelectedField: function () {
                if (!this.selectedField) {
                    return;
                }
                var selectedId = this.selectedId;
                this.fields = this.fields.filter(function (field) {
                    return field.id !== selectedId;
                });
                this.removeChildFieldById(selectedId);
                this.selectedId = "";
                this.syncSelectedField();
            },
            removeChildFieldById: function (fieldId) {
                for (var tableIndex = 0; tableIndex < this.fields.length; tableIndex += 1) {
                    var table = this.fields[tableIndex];
                    if (table.type !== "table" || !Array.isArray(table.children)) {
                        if (table.type === "group" && Array.isArray(table.children)) {
                            table.children = table.children.filter(function (field) {
                                return field.id !== fieldId;
                            });
                        }
                        continue;
                    }
                    for (var cellIndex = 0; cellIndex < table.children.length; cellIndex += 1) {
                        table.children[cellIndex].fields = table.children[cellIndex].fields.filter(function (field) {
                            return field.id !== fieldId;
                        });
                    }
                }
            },
            clearFields: function () {
                var self = this;
                if (!this.fields.length) {
                    return;
                }
                this.$confirm("清空后将移除当前画布上的全部字段，是否继续？", "清空表单", {
                    type: "warning",
                    confirmButtonText: "确定清空",
                    cancelButtonText: "取消"
                }).then(function () {
                    self.fields = [];
                    self.selectedId = "";
                    self.syncSelectedField();
                }).catch(function () {});
            },
            openPreview: function () {
                this.previewHtml = this.buildDialogPreviewHtml();
                this.previewDialogVisible = true;
                var self = this;
                this.$nextTick(function () {
                    self.loadPreviewOptionData(document.querySelector(".canvas-form-preview-dialog-body"));
                });
            },
            handleTableSizeChange: function () {
                if (this.selectedField && this.selectedField.type === "table") {
                    this.selectedField.children = this.normalizeTableChildren(this.selectedField, this.selectedField.children || []);
                }
                this.syncSelectedField();
            },
            handleFieldPropertyChange: function (property) {
                if (property && property.onChange === "tableSize") {
                    this.handleTableSizeChange();
                    return;
                }
                if (this.selectedField && this.isOptionDataSourceType(this.selectedField.type)) {
                    if (property && property.prop === "dataSourceType" && this.selectedField.dataSourceType === "request") {
                        this.selectedField.dataSourceMethod = this.selectedField.dataSourceMethod || "GET";
                    }
                    if (!this.selectedField.dataSourceType || this.selectedField.dataSourceType === "preset" || (property && property.prop === "presetOptionsText")) {
                        this.selectedField.dataSourceType = this.selectedField.dataSourceType || "preset";
                        this.selectedField.options = this.parsePresetOptionsText(this.selectedField.presetOptionsText);
                    }
                }
                this.syncSelectedField();
            },
            syncSelectedField: function () {
                this.fields = this.fields.map(function (field, index) {
                    field.sortOrder = index + 1;
                    return field;
                });
                this.emitSchema();
                this.$nextTick(this.renderCanvas);
            },
            emitSchema: function () {
                var schema = this.exportSchema();
                this.$emit("input", schema);
                this.$emit("change", schema);
            },
            isSameSchema: function (left, right) {
                return JSON.stringify(left || []) === JSON.stringify(right || []);
            },
            exportSchema: function () {
                var self = this;
                return this.fields.map(function (field, index) {
                    return self.exportField(field, index);
                });
            },
            exportField: function (field, index) {
                var self = this;
                var config = window.CanvasFormDesignerComponentRegistry.resolve(field.type);
                var result = {
                    id: field.id,
                    type: field.type,
                    componentType: field.type,
                    x: field.x,
                    y: field.y,
                    width: field.width,
                    height: field.height,
                    sortOrder: index + 1
                };
                this.mergeSchemaFields(config.schemaFields || [], config).forEach(function (prop) {
                    result[prop] = field[prop];
                });
                if (field.type === "table") {
                    result.children = (field.children || []).map(function (cell) {
                        return {
                            row: cell.row,
                            column: cell.column,
                            fields: (cell.fields || []).map(function (child, childIndex) {
                                return self.exportField(child, childIndex);
                            })
                        };
                    });
                }
                if (field.type === "group") {
                    result.children = (field.children || []).map(function (child, childIndex) {
                        return self.exportField(child, childIndex);
                    });
                }
                return result;
            },
            buildPreviewHtml: function (fields, offsetX, offsetY) {
                var html = [];
                for (var index = 0; index < (fields || []).length; index += 1) {
                    html.push(this.buildPreviewFieldHtml(fields[index], offsetX, offsetY));
                }
                if (!html.length) {
                    return "<div class=\"canvas-form-empty\" style=\"margin:24px;\">暂无可预览字段</div>";
                }
                return html.join("");
            },
            buildStandalonePreviewHtml: function () {
                var width = this.resolveContentWidth();
                var height = this.resolveContentHeight();
                return [
                    "<!DOCTYPE html>",
                    "<html lang=\"zh-CN\">",
                    "<head>",
                    "<meta charset=\"UTF-8\">",
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">",
                    "<title>表单预览</title>",
                    "<style>", this.buildPreviewStyleText(), "</style>",
                    "</head>",
                    "<body>",
                    "<div class=\"canvas-form-preview-page\">",
                    "<div class=\"canvas-form-preview-page-header\">",
                    "<h1>表单预览</h1>",
                    "<button type=\"button\" onclick=\"window.close()\">关闭预览</button>",
                    "</div>",
                    "<form class=\"canvas-form-preview-stage\" style=\"width:", width, "px;height:", height, "px;\" onsubmit=\"return false;\">",
                    this.buildPreviewHtml(this.exportSchema(), 0, 0),
                    "</form>",
                    "</div>",
                    "<script>",
                    this.buildPreviewScriptText(),
                    "</script>",
                    "</body>",
                    "</html>"
                ].join("");
            },
            buildDialogPreviewHtml: function () {
                var width = this.resolveContentWidth();
                var height = this.resolveContentHeight();
                return [
                    "<form class=\"canvas-form-preview-stage\" style=\"width:", width, "px;height:", height, "px;\" onsubmit=\"return false;\">",
                    this.buildPreviewHtml(this.exportSchema(), 0, 0),
                    "</form>"
                ].join("");
            },
            handlePreviewClick: function (event) {
                this.handlePreviewTrigger(event, "click");
            },
            handlePreviewChange: function (event) {
                if (event.target && event.target.classList && event.target.classList.contains("canvas-form-preview-select-input")) {
                    this.syncPreviewDatalistValue(event.target);
                }
                this.handlePreviewTrigger(event, "onchange");
            },
            handlePreviewTrigger: function (event, triggerMode) {
                var target = event.target.closest && event.target.closest(".canvas-form-preview-action");
                if (!target) {
                    return;
                }
                var targetTriggerMode = target.getAttribute("data-trigger-mode") || "click";
                if (targetTriggerMode !== triggerMode) {
                    return;
                }
                var action = target.getAttribute("data-action") || "";
                var method = (target.getAttribute("data-http-method") || "").toUpperCase();
                var param = this.parseActionParam(target.getAttribute("data-param") || "");
                if (!action && !method) {
                    var previewGroup = target.closest && target.closest(".canvas-form-preview-group");
                    if (previewGroup && target.getAttribute("data-type") === "button") {
                        this.submitPreviewGroup(previewGroup);
                    }
                    return;
                }
                var isSwitch = target.getAttribute("data-type") === "switch";
                if (!isSwitch) {
                    event.preventDefault();
                }
                var self = this;
                var invokeAction = function () {
                    var detail = {
                        action: action,
                        method: method,
                        param: param,
                        fieldKey: target.getAttribute("data-field-key") || "",
                        type: target.getAttribute("data-type") || "",
                        text: target.textContent
                    };
                    window.dispatchEvent(new CustomEvent("canvas-form-component-action", { detail: detail }));
                    if (action && typeof window[action] === "function") {
                        window[action](detail);
                        return;
                    }
                    if (action && method) {
                        self.sendPreviewRequest(action, method, param);
                        return;
                    }
                    alert("已触发：" + action);
                };
                if (isSwitch) {
                    setTimeout(invokeAction, 0);
                    return;
                }
                invokeAction();
            },
            parseActionParam: function (text) {
                if (!text) {
                    return null;
                }
                try {
                    return JSON.parse(text);
                } catch (error) {
                    return text;
                }
            },
            appendGetParam: function (url, param) {
                if (!param) {
                    return url;
                }
                var query = typeof param === "string" ? param : new URLSearchParams(param).toString();
                if (!query) {
                    return url;
                }
                return url + (url.indexOf("?") >= 0 ? "&" : "?") + query;
            },
            sendPreviewRequest: function (action, method, param) {
                var requestUrl = method === "GET" ? this.appendGetParam(action, param) : action;
                var requestOptions = { method: method, headers: { "Content-Type": "application/json" } };
                if (method !== "GET" && param !== null) {
                    requestOptions.body = typeof param === "string" ? param : JSON.stringify(param);
                }
                fetch(requestUrl, requestOptions).then(function (response) {
                    alert("请求已发送：" + response.status);
                }).catch(function (error) {
                    alert("请求失败：" + error.message);
                });
            },
            loadPreviewOptionData: function (container) {
                if (!container) {
                    return;
                }
                var self = this;
                var controls = container.querySelectorAll("select[data-source-url],input[list][data-source-url],.canvas-form-preview-options[data-source-url]");
                Array.prototype.forEach.call(controls, function (control) {
                    self.loadPreviewRemoteOptions(control);
                });
            },
            loadPreviewRemoteOptions: function (control) {
                var url = control.getAttribute("data-source-url") || "";
                if (!url) {
                    return;
                }
                var method = (control.getAttribute("data-source-method") || "GET").toUpperCase();
                var requestOptions = { method: method, headers: { "Content-Type": "application/json" } };
                fetch(url, requestOptions).then(function (response) {
                    return response.text();
                }).then(function (text) {
                    var data = text;
                    try {
                        data = JSON.parse(text);
                    } catch (error) {}
                    var options = this.resolvePreviewSelectOptions(data, control.getAttribute("data-source-script") || "", control);
                    this.renderPreviewRemoteOptions(control, options);
                }.bind(this)).catch(function (error) {
                    control.setAttribute("data-source-error", error.message);
                });
            },
            resolvePreviewSelectOptions: function (data, script, control) {
                var parsed = data;
                if (script) {
                    var field = {
                        fieldKey: control.getAttribute("name") || control.getAttribute("data-field-key") || control.getAttribute("data-option-name") || "",
                        sourceUrl: control.getAttribute("data-source-url") || "",
                        method: control.getAttribute("data-source-method") || "GET"
                    };
                    try {
                        parsed = (new Function("data", "field", "return (" + script + ")(data, field);"))(data, field);
                    } catch (expressionError) {
                        parsed = (new Function("data", "field", script))(data, field);
                    }
                }
                return this.normalizePreviewSelectOptions(parsed);
            },
            renderPreviewRemoteOptions: function (control, options) {
                if (control.tagName && control.tagName.toLowerCase() === "select") {
                    this.renderPreviewSelectOptions(control, options);
                    return;
                }
                if (control.tagName && control.tagName.toLowerCase() === "input" && control.getAttribute("list")) {
                    this.renderPreviewDatalistOptions(control, options);
                    return;
                }
                this.renderPreviewChoiceOptions(control, options);
            },
            normalizePreviewSelectOptions: function (value) {
                if (value && Array.isArray(value.data)) {
                    value = value.data;
                } else if (value && Array.isArray(value.records)) {
                    value = value.records;
                } else if (value && Array.isArray(value.list)) {
                    value = value.list;
                } else if (value && value.data && Array.isArray(value.data.records)) {
                    value = value.data.records;
                }
                if (Array.isArray(value)) {
                    return value.map(function (item) {
                        if (item && typeof item === "object") {
                            var keys = Object.keys(item);
                            if (!item.label && !item.value && keys.length === 1) {
                                return { label: keys[0], value: item[keys[0]] };
                            }
                            var label = item.label || item.name || item.text || item.title || item.key || item.value || "";
                            var optionValue = item.value || item.id || item.code || item.key || label;
                            return { label: label, value: optionValue };
                        }
                        return { label: item, value: item };
                    }).filter(function (item) {
                        return item.label !== "" || item.value !== "";
                    });
                }
                if (value && typeof value === "object") {
                    return Object.keys(value).map(function (key) {
                        return { label: key, value: value[key] };
                    });
                }
                return value ? [{ label: value, value: value }] : [];
            },
            renderPreviewSelectOptions: function (select, options) {
                var placeholder = select.options.length ? select.options[0].text : "请选择";
                var currentValue = select.value || select.getAttribute("data-default-value") || "";
                select.innerHTML = "";
                select.appendChild(new Option(placeholder, ""));
                (options || []).forEach(function (item) {
                    select.appendChild(new Option(item.label, item.value));
                });
                select.value = currentValue;
            },
            renderPreviewDatalistOptions: function (input, options) {
                var listId = input.getAttribute("list") || "";
                var datalist = listId ? document.getElementById(listId) : null;
                var targetId = input.getAttribute("data-value-target") || "";
                var hidden = targetId ? document.getElementById(targetId) : null;
                var currentSubmitValue = hidden ? hidden.value : (input.getAttribute("data-default-value") || "");
                var currentDisplayValue = input.value || "";
                var matchedDisplayValue = "";
                if (!datalist) {
                    datalist = document.createElement("datalist");
                    datalist.id = listId || ((input.getAttribute("data-field-key") || "select") + "_list");
                    input.setAttribute("list", datalist.id);
                    input.parentNode.insertBefore(datalist, input.nextSibling);
                }
                datalist.innerHTML = "";
                (options || []).forEach(function (item) {
                    var value = item.value || item.label || "";
                    var label = item.label || value;
                    var option = document.createElement("option");
                    option.value = label;
                    option.setAttribute("data-value", value);
                    if (String(value) === String(currentSubmitValue)) {
                        matchedDisplayValue = label;
                    }
                    datalist.appendChild(option);
                });
                input.value = matchedDisplayValue || currentDisplayValue;
                this.syncPreviewDatalistValue(input);
            },
            syncPreviewDatalistValue: function (input) {
                if (!input || !input.getAttribute("data-value-target")) {
                    return;
                }
                var hidden = document.getElementById(input.getAttribute("data-value-target"));
                if (!hidden) {
                    return;
                }
                var datalist = document.getElementById(input.getAttribute("list") || "");
                var options = datalist ? datalist.querySelectorAll("option") : [];
                var submitValue = input.value;
                Array.prototype.some.call(options, function (option) {
                    if (option.value === input.value) {
                        submitValue = option.getAttribute("data-value") || option.value;
                        return true;
                    }
                    return false;
                });
                hidden.value = submitValue;
            },
            renderPreviewChoiceOptions: function (container, options) {
                var type = container.getAttribute("data-option-type") || "radio";
                var name = container.getAttribute("data-option-name") || "";
                var defaults = String(container.getAttribute("data-default-value") || "").split(",").map(function (item) {
                    return item.trim();
                });
                var className = type === "checkbox" ? "el-checkbox" : "el-radio";
                var inputClassName = type === "checkbox" ? "el-checkbox__input" : "el-radio__input";
                var html = [];
                (options || []).forEach(function (item) {
                    var value = item.value || item.label || "";
                    var checked = defaults.indexOf(String(value)) >= 0 ? " checked" : "";
                    html.push(
                        "<label class=\"" + className + "\"><input class=\"" + inputClassName + "\" type=\"" + type + "\" name=\"" +
                        this.escapeHtml(name) + "\" value=\"" + this.escapeHtml(value) + "\"" + checked + "><span>" +
                        this.escapeHtml(item.label || value || "-") + "</span></label>"
                    );
                }, this);
                if (!html.length) {
                    html.push("<span>暂无选项</span>");
                }
                container.innerHTML = html.join("");
            },
            parsePreviewFormControlValue: function (control) {
                var valueType = control.getAttribute("data-value-type") || "";
                var value = control.value;
                if (valueType === "boolean") {
                    return String(value) === String(control.getAttribute("data-active-value"));
                }
                if (valueType === "number") {
                    return value === "" ? null : Number(value);
                }
                return value;
            },
            collectPreviewFormData: function (container) {
                var data = {};
                var datalistInputs = container.querySelectorAll(".canvas-form-preview-select-input[data-value-target]");
                Array.prototype.forEach.call(datalistInputs, function (input) {
                    this.syncPreviewDatalistValue(input);
                }, this);
                var controls = container.querySelectorAll("input[name],textarea[name],select[name]");
                Array.prototype.forEach.call(controls, function (control) {
                    var name = control.getAttribute("name");
                    if (!name || control.disabled) {
                        return;
                    }
                    if ((control.type === "checkbox" || control.type === "radio") && !control.checked) {
                        return;
                    }
                    if (Object.prototype.hasOwnProperty.call(data, name)) {
                        if (!Array.isArray(data[name])) {
                            data[name] = [data[name]];
                        }
                        data[name].push(this.parsePreviewFormControlValue(control));
                        return;
                    }
                    data[name] = this.parsePreviewFormControlValue(control);
                }, this);
                return data;
            },
            submitPreviewGroup: function (groupElement) {
                var submitUrl = groupElement.getAttribute("data-submit-url") || "";
                if (!submitUrl) {
                    return;
                }
                var submitMethod = (groupElement.getAttribute("data-submit-method") || "POST").toUpperCase();
                var submitParamMode = groupElement.getAttribute("data-submit-param-mode") || "body";
                var data = this.collectPreviewFormData(groupElement);
                var requestUrl = (submitMethod === "GET" || submitParamMode === "requestParam")
                    ? this.appendGetParam(submitUrl, data)
                    : submitUrl;
                var requestOptions = { method: submitMethod, headers: { "Content-Type": "application/json" } };
                if (submitMethod !== "GET" && submitParamMode === "body") {
                    requestOptions.body = JSON.stringify(data);
                }
                fetch(requestUrl, requestOptions).then(function (response) {
                    alert("表单已提交：" + response.status);
                }).catch(function (error) {
                    alert("表单提交失败：" + error.message);
                });
            },
            buildPreviewScriptText: function () {
                return [
                    "(function(){",
                    "function parseActionParam(text){if(!text){return null;}try{return JSON.parse(text);}catch(error){return text;}}",
                    "function appendGetParam(url,param){if(!param){return url;}var query=typeof param==='string'?param:new URLSearchParams(param).toString();if(!query){return url;}return url+(url.indexOf('?')>=0?'&':'?')+query;}",
                    "function normalizePreviewSelectOptions(value){if(value&&Array.isArray(value.data)){value=value.data;}else if(value&&Array.isArray(value.records)){value=value.records;}else if(value&&Array.isArray(value.list)){value=value.list;}else if(value&&value.data&&Array.isArray(value.data.records)){value=value.data.records;}if(Array.isArray(value)){return value.map(function(item){if(item&&typeof item==='object'){var keys=Object.keys(item);if(!item.label&&!item.value&&keys.length===1){return{label:keys[0],value:item[keys[0]]};}var label=item.label||item.name||item.text||item.title||item.key||item.value||'';var optionValue=item.value||item.id||item.code||item.key||label;return{label:label,value:optionValue};}return{label:item,value:item};}).filter(function(item){return item.label!==''||item.value!=='';});}if(value&&typeof value==='object'){return Object.keys(value).map(function(key){return{label:key,value:value[key]};});}return value?[{label:value,value:value}]:[];}",
                    "function resolvePreviewSelectOptions(data,script,control){var parsed=data;if(script){var field={fieldKey:control.getAttribute('name')||control.getAttribute('data-field-key')||control.getAttribute('data-option-name')||'',sourceUrl:control.getAttribute('data-source-url')||'',method:control.getAttribute('data-source-method')||'GET'};try{parsed=(new Function('data','field','return ('+script+')(data, field);'))(data,field);}catch(expressionError){parsed=(new Function('data','field',script))(data,field);}}return normalizePreviewSelectOptions(parsed);}",
                    "function renderPreviewSelectOptions(select,options){var placeholder=select.options.length?select.options[0].text:'请选择';var currentValue=select.value||select.getAttribute('data-default-value')||'';select.innerHTML='';select.appendChild(new Option(placeholder,''));(options||[]).forEach(function(item){select.appendChild(new Option(item.label,item.value));});select.value=currentValue;}",
                    "function renderPreviewDatalistOptions(input,options){var listId=input.getAttribute('list')||'';var datalist=listId?document.getElementById(listId):null;var targetId=input.getAttribute('data-value-target')||'';var hidden=targetId?document.getElementById(targetId):null;var currentSubmitValue=hidden?hidden.value:(input.getAttribute('data-default-value')||'');var currentDisplayValue=input.value||'';var matchedDisplayValue='';if(!datalist){datalist=document.createElement('datalist');datalist.id=listId||((input.getAttribute('data-field-key')||'select')+'_list');input.setAttribute('list',datalist.id);input.parentNode.insertBefore(datalist,input.nextSibling);}datalist.innerHTML='';(options||[]).forEach(function(item){var value=item.value||item.label||'';var label=item.label||value;var option=document.createElement('option');option.value=label;option.setAttribute('data-value',value);if(String(value)===String(currentSubmitValue)){matchedDisplayValue=label;}datalist.appendChild(option);});input.value=matchedDisplayValue||currentDisplayValue;syncPreviewDatalistValue(input);}",
                    "function syncPreviewDatalistValue(input){if(!input||!input.getAttribute('data-value-target')){return;}var hidden=document.getElementById(input.getAttribute('data-value-target'));if(!hidden){return;}var datalist=document.getElementById(input.getAttribute('list')||'');var options=datalist?datalist.querySelectorAll('option'):[];var submitValue=input.value;Array.prototype.some.call(options,function(option){if(option.value===input.value){submitValue=option.getAttribute('data-value')||option.value;return true;}return false;});hidden.value=submitValue;}",
                    "function renderPreviewChoiceOptions(container,options){var type=container.getAttribute('data-option-type')||'radio';var name=container.getAttribute('data-option-name')||'';var defaults=String(container.getAttribute('data-default-value')||'').split(',').map(function(item){return item.trim();});var className=type==='checkbox'?'el-checkbox':'el-radio';var inputClassName=type==='checkbox'?'el-checkbox__input':'el-radio__input';var html=[];(options||[]).forEach(function(item){var value=item.value||item.label||'';var checked=defaults.indexOf(String(value))>=0?' checked':'';html.push('<label class=\"'+className+'\"><input class=\"'+inputClassName+'\" type=\"'+type+'\" name=\"'+escapeHtml(name)+'\" value=\"'+escapeHtml(value)+'\"'+checked+'><span>'+escapeHtml(item.label||value||'-')+'</span></label>');});if(!html.length){html.push('<span>暂无选项</span>');}container.innerHTML=html.join('');}",
                    "function renderPreviewRemoteOptions(control,options){if(control.tagName&&control.tagName.toLowerCase()==='select'){renderPreviewSelectOptions(control,options);return;}if(control.tagName&&control.tagName.toLowerCase()==='input'&&control.getAttribute('list')){renderPreviewDatalistOptions(control,options);return;}renderPreviewChoiceOptions(control,options);}",
                    "function loadPreviewRemoteOptions(control){var url=control.getAttribute('data-source-url')||'';if(!url){return;}var method=(control.getAttribute('data-source-method')||'GET').toUpperCase();fetch(url,{method:method,headers:{'Content-Type':'application/json'}}).then(function(response){return response.text();}).then(function(text){var data=text;try{data=JSON.parse(text);}catch(error){}var options=resolvePreviewSelectOptions(data,control.getAttribute('data-source-script')||'',control);renderPreviewRemoteOptions(control,options);}).catch(function(error){control.setAttribute('data-source-error',error.message);});}",
                    "function loadPreviewOptionData(container){var controls=(container||document).querySelectorAll('select[data-source-url],input[list][data-source-url],.canvas-form-preview-options[data-source-url]');Array.prototype.forEach.call(controls,function(control){loadPreviewRemoteOptions(control);});}",
                    "function escapeHtml(value){return String(value||'').replace(/[&<>\"']/g,function(item){return{'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',\"'\":'&#39;'}[item];});}",
                    "function parsePreviewFormControlValue(control){var valueType=control.getAttribute('data-value-type')||'';var value=control.value;if(valueType==='boolean'){return String(value)===String(control.getAttribute('data-active-value'));}if(valueType==='number'){return value===''?null:Number(value);}return value;}",
                    "function collectPreviewFormData(container){var data={};var datalistInputs=container.querySelectorAll('.canvas-form-preview-select-input[data-value-target]');Array.prototype.forEach.call(datalistInputs,function(input){syncPreviewDatalistValue(input);});var controls=container.querySelectorAll('input[name],textarea[name],select[name]');Array.prototype.forEach.call(controls,function(control){var name=control.getAttribute('name');if(!name||control.disabled){return;}if((control.type==='checkbox'||control.type==='radio')&&!control.checked){return;}var value=parsePreviewFormControlValue(control);if(Object.prototype.hasOwnProperty.call(data,name)){if(!Array.isArray(data[name])){data[name]=[data[name]];}data[name].push(value);return;}data[name]=value;});return data;}",
                    "function submitPreviewGroup(groupElement){var submitUrl=groupElement.getAttribute('data-submit-url')||'';if(!submitUrl){return;}var submitMethod=(groupElement.getAttribute('data-submit-method')||'POST').toUpperCase();var submitParamMode=groupElement.getAttribute('data-submit-param-mode')||'body';var data=collectPreviewFormData(groupElement);var requestUrl=(submitMethod==='GET'||submitParamMode==='requestParam')?appendGetParam(submitUrl,data):submitUrl;var requestOptions={method:submitMethod,headers:{'Content-Type':'application/json'}};if(submitMethod!=='GET'&&submitParamMode==='body'){requestOptions.body=JSON.stringify(data);}fetch(requestUrl,requestOptions).then(function(response){alert('表单已提交：'+response.status);}).catch(function(error){alert('表单提交失败：'+error.message);});}",
                    "function handlePreviewTrigger(event,triggerMode){",
                    "var target=event.target.closest&&event.target.closest('.canvas-form-preview-action');",
                    "if(!target){return;}",
                    "var targetTriggerMode=target.getAttribute('data-trigger-mode')||'click';",
                    "if(targetTriggerMode!==triggerMode){return;}",
                    "var action=target.getAttribute('data-action')||'';",
                    "var method=(target.getAttribute('data-http-method')||'').toUpperCase();",
                    "var param=parseActionParam(target.getAttribute('data-param')||'');",
                    "if(!action&&!method){var previewGroup=target.closest&&target.closest('.canvas-form-preview-group');if(previewGroup&&target.getAttribute('data-type')==='button'){submitPreviewGroup(previewGroup);}return;}",
                    "var isSwitch=target.getAttribute('data-type')==='switch';",
                    "if(!isSwitch){event.preventDefault();}",
                    "var invokeAction=function(){",
                    "var detail={action:action,method:method,param:param,fieldKey:target.getAttribute('data-field-key')||'',type:target.getAttribute('data-type')||'',text:target.textContent};",
                    "window.dispatchEvent(new CustomEvent('canvas-form-component-action',{detail:detail}));",
                    "if(action&&typeof window[action]==='function'){window[action](detail);return;}",
                    "if(action&&method){",
                    "var requestUrl=method==='GET'?appendGetParam(action,param):action;",
                    "var requestOptions={method:method,headers:{'Content-Type':'application/json'}};",
                    "if(method!=='GET'&&param!==null){requestOptions.body=typeof param==='string'?param:JSON.stringify(param);}",
                    "fetch(requestUrl,requestOptions).then(function(response){alert('请求已发送：'+response.status);}).catch(function(error){alert('请求失败：'+error.message);});",
                    "return;",
                    "}",
                    "alert('已触发：'+action);",
                    "};",
                    "if(isSwitch){setTimeout(invokeAction,0);return;}",
                    "invokeAction();",
                    "}",
                    "document.addEventListener('click',function(event){handlePreviewTrigger(event,'click');});",
                    "document.addEventListener('change',function(event){if(event.target&&event.target.classList&&event.target.classList.contains('canvas-form-preview-select-input')){syncPreviewDatalistValue(event.target);}handlePreviewTrigger(event,'onchange');});",
                    "if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',function(){loadPreviewOptionData(document);});}else{loadPreviewOptionData(document);}",
                    "})();"
                ].join("");
            },
            buildPreviewStyleText: function () {
                return [
                    "*{box-sizing:border-box;}",
                    "body{margin:0;background:#f2f4f8;color:#18263f;font-family:'Microsoft YaHei','PingFang SC',sans-serif;}",
                    ".canvas-form-preview-page{padding:18px;}",
                    ".canvas-form-preview-page-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px;}",
                    ".canvas-form-preview-page-header h1{margin:0;font-size:18px;font-weight:700;}",
                    ".canvas-form-preview-page-header button,.el-button{height:34px;border:1px solid #409eff;border-radius:4px;background:#409eff;color:#fff;padding:0 16px;cursor:pointer;font-size:14px;}",
                    ".canvas-form-preview-field .el-button{width:100%;height:100%;}",
                    ".el-button:hover{background:#66b1ff;border-color:#66b1ff;}",
                    ".canvas-form-preview-stage{position:relative;min-width:720px;min-height:420px;background:#fff;border-radius:14px;box-shadow:0 10px 28px rgba(24,38,63,0.08);overflow:auto;}",
                    ".canvas-form-preview-field{position:absolute;overflow:hidden;}",
                    ".el-form-item{margin:0;width:100%;height:100%;}",
                    ".el-input,.el-select,.el-textarea{position:relative;width:100%;height:100%;font-size:14px;}",
                    ".el-input__inner,.el-textarea__inner{display:block;width:100%;border:1px solid #dcdfe6;border-radius:4px;background:#fff;color:#606266;font-size:14px;outline:none;transition:border-color .2s;}",
                    ".el-input__inner{height:100%;line-height:32px;padding:0 15px;}",
                    ".el-textarea__inner{height:100%;line-height:1.5;padding:7px 15px;resize:none;}",
                    ".el-input__inner:focus,.el-textarea__inner:focus,.el-select__inner:focus{border-color:#409eff;}",
                    ".el-radio-group,.el-checkbox-group{display:flex;gap:14px;flex-wrap:wrap;align-content:flex-start;min-height:100%;line-height:32px;font-size:14px;color:#606266;}",
                    ".el-radio,.el-checkbox{display:inline-flex;align-items:center;gap:6px;cursor:pointer;}",
                    ".el-radio__input,.el-checkbox__input{width:14px;height:14px;accent-color:#409eff;}",
                    ".el-switch{display:inline-flex;align-items:center;gap:8px;color:#606266;font-size:14px;cursor:pointer;}",
                    ".el-switch input{display:none;}",
                    ".el-switch__core{width:40px;height:20px;border-radius:10px;background:#dcdfe6;position:relative;transition:.2s;}",
                    ".el-switch__core::after{content:'';position:absolute;left:2px;top:2px;width:16px;height:16px;border-radius:50%;background:#fff;transition:.2s;}",
                    ".el-switch input:checked+.el-switch__core{background:#409eff;}",
                    ".el-switch input:checked+.el-switch__core::after{left:22px;}",
                    ".el-upload{display:block;width:100%;height:100%;}",
                    ".el-upload__trigger{display:flex;width:100%;height:100%;align-items:center;justify-content:center;border:1px dashed #d9d9d9;border-radius:6px;padding:8px 10px;color:#409eff;background:#fff;font-size:14px;cursor:pointer;text-align:center;}",
                    ".canvas-form-preview-table{position:absolute;border:1px solid #c7d8f0;border-radius:12px;background:#fff;overflow:hidden;}",
                    ".canvas-form-preview-table-title,.canvas-form-preview-group-title{position:relative;z-index:1;height:34px;line-height:34px;padding:0 12px;font-weight:700;color:#18263f;background:#f8fbff;border-bottom:1px solid #dce8f8;}",
                    ".canvas-form-preview-table-grid{position:relative;margin:12px;border:1px solid #d4e2f4;}",
                    ".canvas-form-preview-cell{position:absolute;border-right:1px solid #d4e2f4;border-bottom:1px solid #d4e2f4;background:#fff;overflow:hidden;}",
                    ".el-table{position:absolute;border:1px solid #ebeef5;background:#fff;overflow:auto;color:#606266;font-size:14px;}",
                    ".el-table table{width:100%;border-collapse:collapse;}",
                    ".el-table th{height:40px;background:#f5f7fa;color:#606266;text-align:left;font-weight:700;border-right:1px solid #ebeef5;border-bottom:1px solid #ebeef5;padding:0 10px;}",
                    ".el-table td{height:40px;border-right:1px solid #ebeef5;border-bottom:1px solid #ebeef5;padding:0 10px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}",
                    ".el-table__empty-text{text-align:center;color:#909399;}",
                    ".el-pagination{height:36px;line-height:36px;padding:0 10px;color:#606266;background:#fff;border-top:1px solid #ebeef5;font-size:13px;}",
                    ".canvas-form-preview-group{position:absolute;border:1px solid #c7d8f0;border-radius:12px;background:#fbfdff;overflow:hidden;}",
                    ".canvas-form-preview-group-body{position:absolute;left:0;top:0;width:100%;height:100%;}",
                    ".canvas-form-preview-group-empty{margin:58px 24px 0;color:#909399;font-size:14px;}",
                    ".canvas-form-preview-group-desc{font-size:12px;font-weight:400;color:#7a8ca6;margin-left:8px;}"
                ].join("");
            },
            buildPreviewFieldHtml: function (field, offsetX, offsetY) {
                var config = window.CanvasFormDesignerComponentRegistry.get(field.type) || {};
                if (typeof config.buildPreviewFieldHtml === "function") {
                    return config.buildPreviewFieldHtml(this, field, offsetX, offsetY);
                }
                return this.buildPreviewNormalFieldHtml(field, offsetX, offsetY);
            },
            buildPreviewNormalFieldHtml: function (field, offsetX, offsetY) {
                var style = this.buildPreviewPositionStyle(field, offsetX, offsetY);
                return [
                    "<div class=\"canvas-form-preview-field canvas-form-preview-action\" style=\"", style, "\"", this.buildCommonPreviewAttrs(field), ">",
                    "<div class=\"el-form-item\">",
                    this.buildPreviewControlHtml(field),
                    "</div></div>"
                ].join("");
            },
            buildCommonPreviewAttrs: function (field) {
                return [
                    " data-trigger-mode=\"", this.escapeHtml(field.triggerMode || "click"), "\"",
                    " data-action=\"", this.escapeHtml(field.function || ""), "\"",
                    " data-http-method=\"", this.escapeHtml(field.httpMethod || ""), "\"",
                    " data-param=\"", this.escapeHtml(field.param || ""), "\"",
                    " data-field-key=\"", this.escapeHtml(field.fieldKey || field.id || ""), "\"",
                    " data-type=\"", this.escapeHtml(field.type || ""), "\""
                ].join("");
            },
            buildPreviewPositionStyle: function (field, offsetX, offsetY) {
                return [
                    "left:", offsetX + field.x, "px;",
                    "top:", offsetY + field.y, "px;",
                    "width:", field.width, "px;",
                    "height:", field.height, "px;"
                ].join("");
            },
            buildPreviewControlHtml: function (field) {
                var config = window.CanvasFormDesignerComponentRegistry.get(field.type) || {};
                if (typeof config.buildPreviewControlHtml === "function") {
                    return config.buildPreviewControlHtml(this, field);
                }
                var name = this.escapeHtml(field.fieldKey || field.id || "");
                return "<div class=\"el-input\"><input class=\"el-input__inner\" type=\"text\" name=\"" + name + "\" placeholder=\"" + this.escapeHtml(field.placeholder || "请输入") + "\" value=\"" + this.escapeHtml(field.defaultValue || "") + "\"></div>";
            },
            escapeHtml: function (value) {
                return String(value || "").replace(/[&<>"']/g, function (item) {
                    return {
                        "&": "&amp;",
                        "<": "&lt;",
                        ">": "&gt;",
                        "\"": "&quot;",
                        "'": "&#39;"
                    }[item];
                });
            },
            renderCanvas: function () {
                var canvas = this.$refs.canvas;
                var wrapper = this.$refs.canvasWrapper;
                if (!canvas || !wrapper) {
                    return;
                }
                var pixelRatio = window.devicePixelRatio || 1;
                var contentWidth = Math.max(wrapper.clientWidth - 2, this.resolveContentWidth());
                var contentHeight = Math.max(this.height, this.resolveContentHeight());
                canvas.width = contentWidth * pixelRatio;
                canvas.height = contentHeight * pixelRatio;
                canvas.style.width = contentWidth + "px";
                canvas.style.height = contentHeight + "px";
                var context = canvas.getContext("2d");
                context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0);
                context.clearRect(0, 0, contentWidth, contentHeight);
                this.drawCanvasBackground(context, contentWidth, contentHeight);
                for (var index = 0; index < this.fields.length; index += 1) {
                    this.drawField(context, this.fields[index], 0, 0, null);
                }
                this.drawAlignmentGuides(context, contentWidth, contentHeight);
                if (!this.fields.length) {
                    this.drawEmptyState(context, contentWidth, contentHeight);
                }
            },
            resolveContentWidth: function () {
                var maxWidth = 900;
                for (var index = 0; index < this.fields.length; index += 1) {
                    maxWidth = Math.max(maxWidth, this.fields[index].x + this.fields[index].width + 48);
                }
                return maxWidth;
            },
            resolveContentHeight: function () {
                var maxHeight = this.height;
                for (var index = 0; index < this.fields.length; index += 1) {
                    maxHeight = Math.max(maxHeight, this.fields[index].y + this.fields[index].height + 48);
                }
                return maxHeight;
            },
            drawCanvasBackground: function (context, width, height) {
                context.fillStyle = "#ffffff";
                context.fillRect(0, 0, width, height);
                context.strokeStyle = "#eef3f9";
                context.lineWidth = 1;
                for (var x = 0; x <= width; x += 24) {
                    context.beginPath();
                    context.moveTo(x, 0);
                    context.lineTo(x, height);
                    context.stroke();
                }
                for (var y = 0; y <= height; y += 24) {
                    context.beginPath();
                    context.moveTo(0, y);
                    context.lineTo(width, y);
                    context.stroke();
                }
            },
            drawField: function (context, field, offsetX, offsetY, clipRect) {
                var actualX = offsetX + field.x;
                var actualY = offsetY + field.y;
                var selected = field.id === this.selectedId;
                var hovered = field.id === this.hoverId;
                if (clipRect) {
                    context.save();
                    context.beginPath();
                    context.rect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
                    context.clip();
                }
                var config = window.CanvasFormDesignerComponentRegistry.get(field.type) || {};
                if (typeof config.drawField === "function") {
                    config.drawField(this, context, field, actualX, actualY, selected, hovered);
                } else {
                    this.drawNormalField(context, field, actualX, actualY, selected, hovered);
                }
                if (clipRect) {
                    context.restore();
                }
            },
            drawNormalField: function (context, field, actualX, actualY, selected, hovered) {
                if (selected || hovered) {
                    context.strokeStyle = selected ? "#3477f6" : "#58b9ff";
                    context.lineWidth = 1;
                    context.setLineDash([4, 3]);
                    context.strokeRect(actualX, actualY, field.width, field.height);
                    context.setLineDash([]);
                }
                this.drawControlPreview(context, field, actualX, actualY);
                if (selected) {
                    context.fillStyle = "#3477f6";
                    context.fillRect(actualX + field.width - 10, actualY + field.height - 10, 8, 8);
                }
            },
            resolveTableGrid: function (field) {
                var config = window.CanvasFormDesignerComponentRegistry.resolve(field.type);
                if (config && typeof config.resolveTableGrid === "function") {
                    return config.resolveTableGrid(field);
                }
                return { top: 0, cellWidth: field.width, cellHeight: field.height };
            },
            drawControlPreview: function (context, field, actualX, actualY) {
                var config = window.CanvasFormDesignerComponentRegistry.get(field.type) || {};
                if (typeof config.drawControlPreview === "function") {
                    config.drawControlPreview(this, context, field, actualX, actualY);
                    return;
                }
                this.drawInputLikePreview(context, field, actualX, actualY, field.placeholder || "请输入");
            },
            drawInputLikePreview: function (context, field, actualX, actualY, text, afterDraw) {
                var controlX = actualX;
                var controlY = actualY;
                var controlWidth = field.width;
                var controlHeight = Math.max(26, field.height);
                this.drawRoundRect(context, controlX, controlY, controlWidth, controlHeight, 8, "#f8fbff", field.color || "#dce8f8");
                context.fillStyle = "#9aa9bd";
                context.font = "12px Microsoft YaHei, PingFang SC, sans-serif";
                if (typeof afterDraw === "function") {
                    afterDraw(controlX, controlY, controlWidth, controlHeight);
                }
                context.fillText(text || "请输入", controlX + 12, controlY + 19);
            },
            drawEmptyState: function (context, width, height) {
                context.fillStyle = "#7a8ca6";
                context.font = "14px Microsoft YaHei, PingFang SC, sans-serif";
                context.textAlign = "center";
                context.fillText("从左侧拖拽基础组件到画布", width / 2, Math.min(height / 2, 240));
                context.textAlign = "left";
            },
            drawAlignmentGuides: function (context, width, height) {
                if (!this.canvasDrag.active || !this.alignmentGuides.length) {
                    return;
                }
                context.save();
                context.strokeStyle = "#ff7a45";
                context.lineWidth = 1;
                context.setLineDash([5, 5]);
                for (var index = 0; index < this.alignmentGuides.length; index += 1) {
                    var guide = this.alignmentGuides[index];
                    context.beginPath();
                    if (guide.direction === "vertical") {
                        context.moveTo(guide.position, 0);
                        context.lineTo(guide.position, height);
                    } else {
                        context.moveTo(0, guide.position);
                        context.lineTo(width, guide.position);
                    }
                    context.stroke();
                }
                context.restore();
            },
            drawRoundRect: function (context, x, y, width, height, radius, fillStyle, strokeStyle) {
                context.beginPath();
                context.moveTo(x + radius, y);
                context.lineTo(x + width - radius, y);
                context.quadraticCurveTo(x + width, y, x + width, y + radius);
                context.lineTo(x + width, y + height - radius);
                context.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
                context.lineTo(x + radius, y + height);
                context.quadraticCurveTo(x, y + height, x, y + height - radius);
                context.lineTo(x, y + radius);
                context.quadraticCurveTo(x, y, x + radius, y);
                context.closePath();
                context.fillStyle = fillStyle;
                context.fill();
                context.strokeStyle = strokeStyle;
                context.lineWidth = 1.5;
                context.stroke();
            },
            handleCanvasMouseDown: function (event) {
                if (this.readonly || event.button !== 0) {
                    return;
                }
                var wrapper = this.$refs.canvasWrapper;
                if (wrapper) {
                    wrapper.focus();
                }
                var point = this.resolveCanvasPoint(event);
                var hit = this.findFieldAt(point.x, point.y);
                if (!hit) {
                    this.selectedId = "";
                    this.renderCanvas();
                    return;
                }
                this.selectedId = hit.field.id;
                var mode = this.isPointInResizeHandle(hit.field, hit.localX, hit.localY) ? "resize" : "move";
                this.canvasDrag = {
                    active: true,
                    mode: mode,
                    fieldId: hit.field.id,
                    startX: point.x,
                    startY: point.y,
                    startFieldX: hit.field.x,
                    startFieldY: hit.field.y,
                    startWidth: hit.field.width,
                    startHeight: hit.field.height
                };
                this.renderCanvas();
                event.preventDefault();
            },
            handleCanvasMouseMove: function (event) {
                var point = this.resolveCanvasPoint(event);
                if (this.canvasDrag.active) {
                    this.updateCanvasDrag(point);
                    return;
                }
                var hit = this.findFieldAt(point.x, point.y);
                var nextHoverId = hit ? hit.field.id : "";
                var canvas = this.$refs.canvas;
                if (canvas) {
                    canvas.style.cursor = hit ? (this.isPointInResizeHandle(hit.field, hit.localX, hit.localY) ? "nwse-resize" : "move") : "default";
                }
                if (nextHoverId !== this.hoverId) {
                    this.hoverId = nextHoverId;
                    this.renderCanvas();
                }
            },
            handleCanvasMouseLeave: function () {
                if (this.hoverId) {
                    this.hoverId = "";
                    this.renderCanvas();
                }
            },
            handleDocumentMouseMove: function (event) {
                if (!this.canvasDrag.active) {
                    return;
                }
                this.updateCanvasDrag(this.resolveCanvasPoint(event));
            },
            handleDocumentMouseUp: function () {
                if (!this.canvasDrag.active) {
                    return;
                }
                this.canvasDrag.active = false;
                this.syncSelectedField();
            },
            updateCanvasDrag: function (point) {
                var field = this.findFieldById(this.canvasDrag.fieldId);
                if (!field) {
                    return;
                }
                var deltaX = point.x - this.canvasDrag.startX;
                var deltaY = point.y - this.canvasDrag.startY;
                if (this.canvasDrag.mode === "resize") {
                    field.width = this.canvasDrag.startWidth + deltaX;
                    field.height = this.canvasDrag.startHeight + deltaY;
                    this.alignmentGuides = [];
                } else {
                    var snapped = this.resolveSnappedPosition(field, this.canvasDrag.startFieldX + deltaX, this.canvasDrag.startFieldY + deltaY);
                    field.x = Math.max(field.parentId ? 6 : 12, snapped.x);
                    field.y = Math.max(field.parentType === "group" ? 44 : (field.parentId ? 6 : 12), snapped.y);
                    this.keepChildFieldInCell(field);
                    this.keepChildFieldInGroup(field);
                }
                this.renderCanvas();
            },
            keepChildFieldInCell: function (field) {
                if (!field.parentId) {
                    return;
                }
                var cellHit = this.findCellByChildId(field.id);
                if (!cellHit) {
                    return;
                }
                var table = cellHit.table;
                var grid = this.resolveTableGrid(table);
                field.x = Math.max(6, Math.min(field.x, grid.cellWidth - field.width - 6));
                field.y = Math.max(6, Math.min(field.y, grid.cellHeight - field.height - 6));
            },
            keepChildFieldInGroup: function (field) {
                if (field.parentType !== "group") {
                    return;
                }
                var group = this.findParentGroup(field.id);
                if (!group) {
                    return;
                }
                field.x = Math.max(14, Math.min(field.x, group.width - field.width - 14));
                field.y = Math.max(54, Math.min(field.y, group.height - field.height - 14));
            },
            resolveSnappedPosition: function (field, nextX, nextY) {
                var threshold = 6;
                var guides = [];
                var current = this.resolveFieldBounds(field, nextX, nextY);
                var candidates = this.collectAlignmentCandidates(field);
                var bestX = null;
                var bestY = null;
                for (var index = 0; index < candidates.length; index += 1) {
                    var target = candidates[index];
                    var xPairs = [
                        { source: current.left, target: target.left, adjust: target.left - current.left },
                        { source: current.left, target: target.right, adjust: target.right - current.left },
                        { source: current.centerX, target: target.centerX, adjust: target.centerX - current.centerX },
                        { source: current.right, target: target.left, adjust: target.left - current.right },
                        { source: current.right, target: target.right, adjust: target.right - current.right }
                    ];
                    var yPairs = [
                        { source: current.top, target: target.top, adjust: target.top - current.top },
                        { source: current.top, target: target.bottom, adjust: target.bottom - current.top },
                        { source: current.centerY, target: target.centerY, adjust: target.centerY - current.centerY },
                        { source: current.bottom, target: target.top, adjust: target.top - current.bottom },
                        { source: current.bottom, target: target.bottom, adjust: target.bottom - current.bottom }
                    ];
                    bestX = this.pickCloserGuide(bestX, xPairs, threshold, "vertical");
                    bestY = this.pickCloserGuide(bestY, yPairs, threshold, "horizontal");
                }
                if (bestX) {
                    nextX += bestX.adjust;
                    guides.push({ direction: "vertical", position: bestX.position });
                }
                if (bestY) {
                    nextY += bestY.adjust;
                    guides.push({ direction: "horizontal", position: bestY.position });
                }
                this.alignmentGuides = guides;
                return { x: nextX, y: nextY };
            },
            pickCloserGuide: function (current, pairs, threshold, direction) {
                for (var index = 0; index < pairs.length; index += 1) {
                    var pair = pairs[index];
                    var distance = Math.abs(pair.source - pair.target);
                    if (distance <= threshold && (!current || distance < current.distance)) {
                        current = {
                            distance: distance,
                            adjust: pair.adjust,
                            position: pair.target,
                            direction: direction
                        };
                    }
                }
                return current;
            },
            resolveFieldBounds: function (field, x, y) {
                var offset = this.resolveFieldOffset(field);
                var actualX = offset.x + x;
                var actualY = offset.y + y;
                return {
                    left: actualX,
                    right: actualX + field.width,
                    centerX: actualX + field.width / 2,
                    top: actualY,
                    bottom: actualY + field.height,
                    centerY: actualY + field.height / 2
                };
            },
            collectAlignmentCandidates: function (field) {
                var candidates = [];
                var offset = this.resolveFieldOffset(field);
                var siblings = this.resolveFieldSiblings(field);
                for (var index = 0; index < siblings.length; index += 1) {
                    if (siblings[index].id === field.id) {
                        continue;
                    }
                    var bounds = this.resolveFieldBounds(siblings[index], siblings[index].x, siblings[index].y);
                    candidates.push(bounds);
                }
                if (field.parentType === "group") {
                    var group = this.findParentGroup(field.id);
                    if (group) {
                        candidates.push(this.resolveFieldBounds(group, group.x, group.y));
                    }
                } else if (!field.parentId) {
                    candidates.push({ left: 24, right: 24, centerX: 24, top: 24, bottom: 24, centerY: 24 });
                }
                return candidates;
            },
            resolveFieldOffset: function (field) {
                if (field.parentType === "group") {
                    var group = this.findParentGroup(field.id);
                    return group ? { x: group.x, y: group.y } : { x: 0, y: 0 };
                }
                if (field.parentId) {
                    var cellHit = this.findCellByChildId(field.id);
                    if (cellHit) {
                        var grid = this.resolveTableGrid(cellHit.table);
                        return {
                            x: cellHit.table.x + 12 + cellHit.column * grid.cellWidth,
                            y: cellHit.table.y + grid.top + cellHit.row * grid.cellHeight
                        };
                    }
                }
                return { x: 0, y: 0 };
            },
            resolveFieldSiblings: function (field) {
                if (field.parentType === "group") {
                    var group = this.findParentGroup(field.id);
                    return group ? group.children || [] : [];
                }
                if (field.parentId) {
                    var cellHit = this.findCellByChildId(field.id);
                    return cellHit ? cellHit.cell.fields || [] : [];
                }
                return this.fields;
            },
            resolveCanvasPoint: function (event) {
                var canvas = this.$refs.canvas;
                var wrapper = this.$refs.canvasWrapper;
                if (!canvas || !wrapper) {
                    return { x: 0, y: 0 };
                }
                var rect = canvas.getBoundingClientRect();
                return {
                    x: event.clientX - rect.left + wrapper.scrollLeft,
                    y: event.clientY - rect.top + wrapper.scrollTop
                };
            },
            findFieldAt: function (x, y) {
                for (var index = this.fields.length - 1; index >= 0; index -= 1) {
                    var field = this.fields[index];
                    if (x < field.x || x > field.x + field.width || y < field.y || y > field.y + field.height) {
                        continue;
                    }
                    if (field.type === "table") {
                        var childHit = this.findTableChildAt(field, x, y);
                        if (childHit) {
                            return childHit;
                        }
                    } else if (field.type === "group") {
                        var groupChildHit = this.findGroupChildAt(field, x, y);
                        if (groupChildHit) {
                            return groupChildHit;
                        }
                    }
                    return { field: field, localX: x - field.x, localY: y - field.y };
                }
                return null;
            },
            findGroupChildAt: function (group, x, y) {
                var fields = group.children || [];
                for (var index = fields.length - 1; index >= 0; index -= 1) {
                    var field = fields[index];
                    var actualX = group.x + field.x;
                    var actualY = group.y + field.y;
                    if (x >= actualX && x <= actualX + field.width && y >= actualY && y <= actualY + field.height) {
                        return { field: field, localX: x - actualX, localY: y - actualY };
                    }
                }
                return null;
            },
            findGroupAt: function (x, y) {
                for (var index = this.fields.length - 1; index >= 0; index -= 1) {
                    var group = this.fields[index];
                    if (group.type !== "group") {
                        continue;
                    }
                    var left = group.x + 12;
                    var top = group.y + 52;
                    var right = group.x + group.width - 12;
                    var bottom = group.y + group.height - 12;
                    if (x >= left && x <= right && y >= top && y <= bottom) {
                        return {
                            group: group,
                            x: group.x,
                            y: group.y,
                            width: group.width,
                            height: group.height
                        };
                    }
                }
                return null;
            },
            findTableChildAt: function (table, x, y) {
                var cellHit = this.findTableCellAt(x, y, table);
                if (!cellHit) {
                    return null;
                }
                var cell = this.findCell(table.children || [], cellHit.row, cellHit.column);
                var fields = cell && cell.fields ? cell.fields : [];
                for (var index = fields.length - 1; index >= 0; index -= 1) {
                    var field = fields[index];
                    var actualX = cellHit.x + field.x;
                    var actualY = cellHit.y + field.y;
                    if (x >= actualX && x <= actualX + field.width && y >= actualY && y <= actualY + field.height) {
                        return { field: field, localX: x - actualX, localY: y - actualY };
                    }
                }
                return null;
            },
            findTableCellAt: function (x, y, targetTable) {
                for (var index = this.fields.length - 1; index >= 0; index -= 1) {
                    var table = targetTable || this.fields[index];
                    if (table.type !== "table") {
                        if (targetTable) {
                            break;
                        }
                        continue;
                    }
                    var grid = this.resolveTableGrid(table);
                    var left = table.x + 12;
                    var top = table.y + grid.top;
                    var right = table.x + table.width - 12;
                    var bottom = table.y + table.height - 12;
                    if (x >= left && x <= right && y >= top && y <= bottom) {
                        var column = Math.min(table.tableColumns - 1, Math.floor((x - left) / grid.cellWidth));
                        var row = Math.min(table.tableRows - 1, Math.floor((y - top) / grid.cellHeight));
                        return {
                            table: table,
                            row: row,
                            column: column,
                            x: left + column * grid.cellWidth,
                            y: top + row * grid.cellHeight,
                            width: grid.cellWidth,
                            height: grid.cellHeight
                        };
                    }
                    if (targetTable) {
                        break;
                    }
                }
                return null;
            },
            findFieldById: function (fieldId) {
                for (var index = 0; index < this.fields.length; index += 1) {
                    if (this.fields[index].id === fieldId) {
                        return this.fields[index];
                    }
                    var child = this.findChildFieldById(this.fields[index], fieldId);
                    if (child) {
                        return child;
                    }
                }
                return null;
            },
            findChildFieldById: function (table, fieldId) {
                if ((table.type !== "table" && table.type !== "group") || !Array.isArray(table.children)) {
                    return null;
                }
                if (table.type === "group") {
                    for (var groupFieldIndex = 0; groupFieldIndex < table.children.length; groupFieldIndex += 1) {
                        if (table.children[groupFieldIndex].id === fieldId) {
                            return table.children[groupFieldIndex];
                        }
                    }
                    return null;
                }
                for (var cellIndex = 0; cellIndex < table.children.length; cellIndex += 1) {
                    var fields = table.children[cellIndex].fields || [];
                    for (var fieldIndex = 0; fieldIndex < fields.length; fieldIndex += 1) {
                        if (fields[fieldIndex].id === fieldId) {
                            return fields[fieldIndex];
                        }
                    }
                }
                return null;
            },
            findParentGroup: function (fieldId) {
                for (var index = 0; index < this.fields.length; index += 1) {
                    var group = this.fields[index];
                    if (group.type !== "group" || !Array.isArray(group.children)) {
                        continue;
                    }
                    for (var childIndex = 0; childIndex < group.children.length; childIndex += 1) {
                        if (group.children[childIndex].id === fieldId) {
                            return group;
                        }
                    }
                }
                return null;
            },
            findCellByChildId: function (fieldId) {
                for (var tableIndex = 0; tableIndex < this.fields.length; tableIndex += 1) {
                    var table = this.fields[tableIndex];
                    if (table.type !== "table" || !Array.isArray(table.children)) {
                        continue;
                    }
                    for (var cellIndex = 0; cellIndex < table.children.length; cellIndex += 1) {
                        var cell = table.children[cellIndex];
                        var fields = cell.fields || [];
                        for (var fieldIndex = 0; fieldIndex < fields.length; fieldIndex += 1) {
                            if (fields[fieldIndex].id === fieldId) {
                                return { table: table, cell: cell, row: cell.row, column: cell.column };
                            }
                        }
                    }
                }
                return null;
            },
            isPointInResizeHandle: function (field, x, y) {
                return x >= field.width - 16 && y >= field.height - 16;
            },
            hasOptionsProperty: function (field) {
                return this.resolveFieldPropertyFields(field).some(function (property) {
                    return property.editor === "options";
                });
            },
            stringifyPresetOptionsText: function (options) {
                var source = Array.isArray(options) && options.length ? options : this.defaultOptions("select");
                return JSON.stringify(source.map(function (item) {
                    return {
                        label: item && (item.label || item.value) ? (item.label || item.value) : "",
                        value: item && (item.value || item.label) ? (item.value || item.label) : ""
                    };
                }));
            },
            parsePresetOptionsText: function (text) {
                var source = String(text || "").trim();
                if (!source) {
                    return [];
                }
                return this.parsePresetOptionsJson(source);
            },
            parsePresetOptionsJson: function (text) {
                try {
                    var parsed = JSON.parse(text);
                    return Array.isArray(parsed) ? this.normalizePreviewSelectOptions(parsed) : [];
                } catch (error) {
                    return [];
                }
            },
            parseOptionsText: function (text) {
                return String(text || "").split("\n").map(function (line) {
                    return line.trim();
                }).filter(function (line) {
                    return !!line;
                }).map(function (line) {
                    return { label: line, value: line };
                });
            }
        },
        mounted: function () {
            this.ensureStyle();
            document.addEventListener("mousemove", this.handleDocumentMouseMove);
            document.addEventListener("mouseup", this.handleDocumentMouseUp);
            window.addEventListener("resize", this.renderCanvas);
            this.$nextTick(this.renderCanvas);
        },
        beforeDestroy: function () {
            document.removeEventListener("mousemove", this.handleDocumentMouseMove);
            document.removeEventListener("mouseup", this.handleDocumentMouseUp);
            window.removeEventListener("resize", this.renderCanvas);
        }
    };

    if (window && window.Vue && typeof window.Vue.component === "function") {
        window.Vue.component("canvas-form-designer", window.CanvasFormDesigner);
    }
})();
