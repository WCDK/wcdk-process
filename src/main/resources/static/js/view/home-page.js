window.HomePage = {
    template: `
        <section class="route-section">
            <section class="overview-grid">
                <div class="overview-card accent-blue">
                    <div class="overview-label">流程定义总数</div>
                    <div class="overview-value">{{ $root.overview.definitionCount }}</div>
                    <div class="overview-foot">已部署的最新流程定义数量</div>
                </div>
                <div class="overview-card accent-orange">
                    <div class="overview-label">流程模型总数</div>
                    <div class="overview-value">{{ $root.overview.modelCount }}</div>
                    <div class="overview-foot">支持查看模型源码并一键部署</div>
                </div>
                <div class="overview-card accent-green">
                    <div class="overview-label">流程单总数</div>
                    <div class="overview-value">{{ $root.overview.processCount }}</div>
                    <div class="overview-foot">业务单据与流程实例实时联动</div>
                </div>
                <div class="overview-card accent-dark">
                    <div class="overview-label">当前待办任务</div>
                    <div class="overview-value">{{ $root.overview.taskCount }}</div>
                    <div class="overview-foot">支持按办理人筛选查询</div>
                </div>
            </section>

            <section class="quick-board">
                <div class="board-head">
                    <div>
                        <div class="section-kicker">快捷入口</div>
                        <h2>常用操作面板</h2>
                    </div>
                </div>
                <div class="quick-grid">
                    <button
                        v-for="item in quickMenus"
                        :key="item.path"
                        class="quick-card"
                        type="button"
                        @click="$root.navigate(item.path)">
                        <div class="quick-icon">
                            <i :class="item.icon"></i>
                        </div>
                        <div class="quick-title">{{ item.label }}</div>
                        <div class="quick-text">{{ item.description }}</div>
                    </button>
                </div>
            </section>
        </section>
    `,
    computed: {
        quickMenus: function () {
            return this.$root.navMenus.filter(function (item) {
                return item.path !== "/home";
            });
        }
    },
    mounted: function () {
        this.$root.reloadAll();
    }
};
