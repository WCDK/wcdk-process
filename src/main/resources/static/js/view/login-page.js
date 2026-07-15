/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.LoginPage = {
    template: `
        <section class="login-page">
            <div class="login-panel">
                <div class="login-brand">
                    <div class="brand-mark">W</div>
                    <div>
                        <div class="brand-title">流程平台</div>
                        <div class="brand-subtitle">用户登录与权限入口</div>
                    </div>
                </div>
                <div class="login-title">账号登录</div>
                <div class="login-desc">请使用系统分配的账号密码进入流程工作台和权限管理模块。</div>
                <el-form @submit.native.prevent="submitLogin">
                    <el-form-item label="用户名">
                        <el-input v-model.trim="form.username" placeholder="请输入用户名"></el-input>
                    </el-form-item>
                    <el-form-item label="密码">
                        <el-input v-model.trim="form.password" show-password placeholder="请输入密码"></el-input>
                    </el-form-item>
                    <div class="form-actions">
                        <el-button type="primary" :loading="submitting" @click="submitLogin">登录系统</el-button>
                    </div>
                </el-form>
                <div class="helper-text">默认管理员账号：admin，默认密码：admin123。</div>
            </div>
        </section>
    `,
    data: function () {
        return {
            form: {
                username: "admin",
                password: "admin123"
            },
            submitting: false
        };
    },
    methods: {
        submitLogin: async function () {
            if (!this.form.username || !this.form.password) {
                this.$message.error("请输入用户名和密码");
                return;
            }
            this.submitting = true;
            try {
                var result = await window.AppService.requestJson("/auth/login", {
                    method: "POST",
                    body: JSON.stringify(this.form)
                });
                await this.$root.handleLoginSuccess(result.data || {});
                this.$message.success(result.message || "登录成功");
            } catch (error) {
                this.$message.error(error.message || "登录失败");
            } finally {
                this.submitting = false;
            }
        }
    }
};
