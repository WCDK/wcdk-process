<template>
  <section class="process-designer-workspace">
    <el-alert
      v-if="loadError"
      class="designer-alert"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
    />

    <div v-if="!ready" class="designer-loading">
      <i class="el-icon-loading"></i>
      <span>正在加载流程设计器...</span>
    </div>

    <wcdk-process-designer
      v-else
      :process-definition-id="processDefinitionId"
      :deployment-id="deploymentId"
      :model-id="modelId"
      :process-definition-detail="processDefinitionDetail"
      :form-bindings="formBindings"
      :form-records="visibleFormRecords"
      :form-total="filteredFormRecords.length"
      :button-permissions="buttonPermissions"
      :save-handler="handleSave"
      @form-query="handleFormQuery"
    />

    <el-dialog title="流程设计数据" :visible.sync="payloadVisible" width="860px">
      <pre class="designer-payload">{{ savedPayloadText }}</pre>
    </el-dialog>
  </section>
</template>

<script>
const DESIGNER_SCRIPT = '/wcdk-process/js/components/process-designer-widget.js';

function loadScript(src) {
  return new Promise((resolve, reject) => {
    if (window.WcdkProcessDesigner) {
      resolve();
      return;
    }
    const existing = document.querySelector(`script[data-src="${src}"]`);
    if (existing) {
      existing.addEventListener('load', resolve, { once: true });
      existing.addEventListener('error', reject, { once: true });
      return;
    }
    const script = document.createElement('script');
    script.src = src;
    script.async = false;
    script.dataset.src = src;
    script.onload = resolve;
    script.onerror = () => reject(new Error('流程设计器脚本加载失败'));
    document.body.appendChild(script);
  });
}

export default {
  name: 'ProcessDesignerView',
  data() {
    return {
      bpmn:"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:flowable=\"http://flowable.org/bpmn\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" typeLanguage=\"http://www.w3.org/2001/XMLSchema\" expressionLanguage=\"http://www.w3.org/1999/XPath\" targetNamespace=\"http://flowable.org/processdef\" id=\"Definitions_1784803869027\">\n" +
          "  <process id=\"Wcdk_1784792703363\" name=\"Wcdk_1784792703363\" isExecutable=\"true\">\n" +
          "    <startEvent id=\"StartEvent_1\" name=\"启动事件1\"></startEvent>\n" +
          "    <parallelGateway id=\"ParallelGateway_2\" name=\"并行网关2\"></parallelGateway>\n" +
          "    <userTask id=\"UserTask_4\" name=\"用户任务4\"></userTask>\n" +
          "    <userTask id=\"UserTask_3\" name=\"用户任务3\"></userTask>\n" +
          "    <userTask id=\"UserTask_6\" name=\"用户任务6\"></userTask>\n" +
          "    <endEvent id=\"EndEvent_7\" name=\"结束事件7\"></endEvent>\n" +
          "    <inclusiveGateway id=\"InclusiveGateway_8\" name=\"包容网关8\"></inclusiveGateway>\n" +
          "    <sequenceFlow id=\"Flow_StartEvent_1_ParallelGateway_2\" sourceRef=\"StartEvent_1\" targetRef=\"ParallelGateway_2\"></sequenceFlow>\n" +
          "    <sequenceFlow id=\"Flow_ParallelGateway_2_UserTask_3\" sourceRef=\"ParallelGateway_2\" targetRef=\"UserTask_3\"></sequenceFlow>\n" +
          "    <sequenceFlow id=\"Flow_ParallelGateway_2_UserTask_4\" sourceRef=\"ParallelGateway_2\" targetRef=\"UserTask_4\"></sequenceFlow>\n" +
          "    <sequenceFlow id=\"Flow_UserTask_6_EndEvent_7\" sourceRef=\"UserTask_6\" targetRef=\"EndEvent_7\"></sequenceFlow>\n" +
          "    <sequenceFlow id=\"Flow_UserTask_3_designer-node-8\" sourceRef=\"UserTask_3\" targetRef=\"InclusiveGateway_8\"></sequenceFlow>\n" +
          "    <sequenceFlow id=\"Flow_UserTask_4_designer-node-8\" sourceRef=\"UserTask_4\" targetRef=\"InclusiveGateway_8\"></sequenceFlow>\n" +
          "    <sequenceFlow id=\"Flow_designer-node-8_UserTask_6\" sourceRef=\"InclusiveGateway_8\" targetRef=\"UserTask_6\"></sequenceFlow>\n" +
          "  </process>\n" +
          "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_Wcdk_1784792703363\">\n" +
          "    <bpmndi:BPMNPlane bpmnElement=\"Wcdk_1784792703363\" id=\"BPMNPlane_Wcdk_1784792703363\">\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"StartEvent_1\" id=\"BPMNShape_StartEvent_1\">\n" +
          "        <omgdc:Bounds height=\"92.0\" width=\"92.0\" x=\"657.0\" y=\"174.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"ParallelGateway_2\" id=\"BPMNShape_ParallelGateway_2\">\n" +
          "        <omgdc:Bounds height=\"112.0\" width=\"112.0\" x=\"877.0\" y=\"164.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"UserTask_4\" id=\"BPMNShape_UserTask_4\">\n" +
          "        <omgdc:Bounds height=\"86.0\" width=\"148.0\" x=\"1087.0\" y=\"274.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"UserTask_3\" id=\"BPMNShape_UserTask_3\">\n" +
          "        <omgdc:Bounds height=\"86.0\" width=\"148.0\" x=\"1090.0\" y=\"95.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"UserTask_6\" id=\"BPMNShape_UserTask_6\">\n" +
          "        <omgdc:Bounds height=\"86.0\" width=\"148.0\" x=\"1603.0\" y=\"46.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"EndEvent_7\" id=\"BPMNShape_EndEvent_7\">\n" +
          "        <omgdc:Bounds height=\"92.0\" width=\"92.0\" x=\"1645.0\" y=\"387.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNShape bpmnElement=\"InclusiveGateway_8\" id=\"BPMNShape_InclusiveGateway_8\">\n" +
          "        <omgdc:Bounds height=\"112.0\" width=\"112.0\" x=\"1297.0\" y=\"180.0\"></omgdc:Bounds>\n" +
          "      </bpmndi:BPMNShape>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_StartEvent_1_ParallelGateway_2\" id=\"BPMNEdge_Flow_StartEvent_1_ParallelGateway_2\">\n" +
          "        <omgdi:waypoint x=\"749.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"813.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"813.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"877.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_ParallelGateway_2_UserTask_3\" id=\"BPMNEdge_Flow_ParallelGateway_2_UserTask_3\">\n" +
          "        <omgdi:waypoint x=\"989.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1039.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1040.0\" y=\"138.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1090.0\" y=\"138.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_ParallelGateway_2_UserTask_4\" id=\"BPMNEdge_Flow_ParallelGateway_2_UserTask_4\">\n" +
          "        <omgdi:waypoint x=\"989.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1038.0\" y=\"220.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1038.0\" y=\"317.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1087.0\" y=\"317.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_UserTask_6_EndEvent_7\" id=\"BPMNEdge_Flow_UserTask_6_EndEvent_7\">\n" +
          "        <omgdi:waypoint x=\"1751.0\" y=\"89.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1791.0\" y=\"89.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1605.0\" y=\"433.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1645.0\" y=\"433.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_UserTask_3_designer-node-8\" id=\"BPMNEdge_Flow_UserTask_3_designer-node-8\">\n" +
          "        <omgdi:waypoint x=\"1238.0\" y=\"138.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1274.0\" y=\"138.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1261.0\" y=\"236.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1297.0\" y=\"236.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_UserTask_4_designer-node-8\" id=\"BPMNEdge_Flow_UserTask_4_designer-node-8\">\n" +
          "        <omgdi:waypoint x=\"1235.0\" y=\"317.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1271.0\" y=\"317.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1261.0\" y=\"236.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1297.0\" y=\"236.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "      <bpmndi:BPMNEdge bpmnElement=\"Flow_designer-node-8_UserTask_6\" id=\"BPMNEdge_Flow_designer-node-8_UserTask_6\">\n" +
          "        <omgdi:waypoint x=\"1409.0\" y=\"236.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1493.0\" y=\"236.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1519.0\" y=\"89.0\"></omgdi:waypoint>\n" +
          "        <omgdi:waypoint x=\"1603.0\" y=\"89.0\"></omgdi:waypoint>\n" +
          "      </bpmndi:BPMNEdge>\n" +
          "    </bpmndi:BPMNPlane>\n" +
          "  </bpmndi:BPMNDiagram>\n" +
          "</definitions>",
      ready: false,
      loadError: '',
      payloadVisible: false,
      savedPayloadText: '',
      processDefinitionId: '',
      deploymentId: '',
      modelId: '',
      processDefinitionDetail: null,
      formBindings: [],
      formQuery: {
        pageNum: 1,
        pageSize: 10,
        formName: '',
        formKey: '',
      },
      buttonPermissions: [
        'designer:canvas:center',
        'designer:canvas:reset',
        'designer:canvas:refresh',
      ],
      formRecords: [
        {
          id: 1,
          formName: '通用审批表',
          formKey: 'common_approve_form',
          version: 1,
          schema: [],
        },
        {
          id: 2,
          formName: '任务执行记录',
          formKey: 'task_execute_record',
          version: 1,
          schema: [],
        },
      ],
    };
  },
  computed: {
    filteredFormRecords() {
      const formName = (this.formQuery.formName || '').trim().toLowerCase();
      const formKey = (this.formQuery.formKey || '').trim().toLowerCase();
      return this.formRecords.filter(item => {
        const nameMatched = !formName || String(item.formName || '').toLowerCase().includes(formName);
        const keyMatched = !formKey || String(item.formKey || '').toLowerCase().includes(formKey);
        return nameMatched && keyMatched;
      });
    },
    visibleFormRecords() {
      const pageNum = Number(this.formQuery.pageNum || 1);
      const pageSize = Number(this.formQuery.pageSize || 10);
      const start = Math.max(pageNum - 1, 0) * pageSize;
      return this.filteredFormRecords.slice(start, start + pageSize);
    },
  },
  async mounted() {
    try {
      this.processDefinitionDetail = this.buildProcessDefinitionDetail(this.bpmn);
      await loadScript(DESIGNER_SCRIPT);
      this.ready = true;
    } catch (error) {
      this.loadError = error.message || '流程设计器加载失败';
      this.$emit('error', this.loadError);
    }
  },
  methods: {
    buildProcessDefinitionDetail(bpmnXml) {
      const parser = new DOMParser();
      const documentNode = parser.parseFromString(bpmnXml, 'text/xml');
      const parseError = documentNode.getElementsByTagName('parsererror');
      if (parseError && parseError.length) {
        throw new Error('BPMN XML 解析失败');
      }

      const processElement = this.findFirstElementByLocalName(documentNode, 'process');
      const processDefinitionKey = processElement ? processElement.getAttribute('id') || '' : '';
      const processDefinitionName = processElement
        ? processElement.getAttribute('name') || processDefinitionKey
        : '';
      const boundsMap = this.buildBpmnBoundsMap(documentNode);
      const nodes = this.buildBpmnNodes(processElement, boundsMap);
      const sequenceFlows = this.buildBpmnSequenceFlows(processElement);

      return {
        processDefinitionId: processDefinitionKey,
        processDefinitionKey,
        processDefinitionName,
        deploymentId: '',
        category: 'preview',
        bpmnXml,
        nodes,
        sequenceFlows,
      };
    },
    buildBpmnBoundsMap(documentNode) {
      const result = {};
      const shapes = this.findElementsByLocalName(documentNode, 'BPMNShape');
      shapes.forEach(shape => {
        const elementId = shape.getAttribute('bpmnElement') || '';
        const bounds = this.findFirstElementByLocalName(shape, 'Bounds');
        if (!elementId || !bounds) {
          return;
        }
        result[elementId] = {
          x: Number(bounds.getAttribute('x') || 0),
          y: Number(bounds.getAttribute('y') || 0),
          width: Number(bounds.getAttribute('width') || 120),
          height: Number(bounds.getAttribute('height') || 72),
        };
      });
      return result;
    },
    buildBpmnNodes(processElement, boundsMap) {
      if (!processElement) {
        return [];
      }
      const supportedTypes = [
        'startEvent',
        'endEvent',
        'boundaryEvent',
        'intermediateCatchEvent',
        'intermediateThrowEvent',
        'userTask',
        'scriptTask',
        'serviceTask',
        'mailTask',
        'manualTask',
        'receiveTask',
        'businessRuleTask',
        'callActivity',
        'subProcess',
        'parallelGateway',
        'exclusiveGateway',
        'inclusiveGateway',
        'eventBasedGateway',
        'eventGateway',
        'textAnnotation',
      ];
      return Array.from(processElement.children)
        .filter(element => supportedTypes.includes(this.localNameOf(element)))
        .map(element => {
          const elementId = element.getAttribute('id') || '';
          const bounds = boundsMap[elementId] || {};
          return {
            elementId,
            elementName: element.getAttribute('name') || elementId,
            elementType: this.localNameOf(element),
            documentation: this.readElementText(element, 'documentation'),
            defaultFlowId: element.getAttribute('default') || '',
            x: bounds.x || 0,
            y: bounds.y || 0,
            width: bounds.width || 120,
            height: bounds.height || 72,
            properties: {},
          };
        });
    },
    buildBpmnSequenceFlows(processElement) {
      if (!processElement) {
        return [];
      }
      return this.findElementsByLocalName(processElement, 'sequenceFlow').map(flow => ({
        elementId: flow.getAttribute('id') || '',
        elementName: flow.getAttribute('name') || '',
        sourceRef: flow.getAttribute('sourceRef') || '',
        targetRef: flow.getAttribute('targetRef') || '',
        conditionExpression: this.readElementText(flow, 'conditionExpression'),
      }));
    },
    readElementText(root, localName) {
      const element = this.findFirstElementByLocalName(root, localName);
      return element ? (element.textContent || '').trim() : '';
    },
    findFirstElementByLocalName(root, localName) {
      const elements = this.findElementsByLocalName(root, localName);
      return elements.length ? elements[0] : null;
    },
    findElementsByLocalName(root, localName) {
      if (!root) {
        return [];
      }
      return Array.from(root.getElementsByTagName('*')).filter(element => this.localNameOf(element) === localName);
    },
    localNameOf(element) {
      return element ? element.localName || String(element.nodeName || '').split(':').pop() : '';
    },
    handleFormQuery(query) {
      this.formQuery = Object.assign({}, this.formQuery, query || {});
    },
    async handleSave(payload) {
      this.savedPayloadText = JSON.stringify(payload, null, 2);
      this.payloadVisible = true;
      return { message: '流程设计数据已生成' };
    },
  },
};
</script>
