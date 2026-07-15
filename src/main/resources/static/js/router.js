/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
window.AppRouter = new VueRouter({
    mode: "hash",
    routes: [
        { path: "/", redirect: "/home" },
        { path: "/login", component: window.LoginPage, meta: { publicPage: true } },
        { path: "/home", component: window.HomePage },
        { path: "/deploy", component: window.DeployCenter },
        { path: "/model", component: window.ModelCenter },
        { path: "/designer", component: window.ProcessDesigner },
        { path: "/process", component: window.ProcessCenter },
        { path: "/task", component: window.TaskCenter },
        { path: "/system/user", component: window.UserCenter },
        { path: "/system/role", component: window.RoleCenter },
        { path: "/system/permission", component: window.PermissionCenter },
        { path: "/system/dept", component: window.DeptCenter }
    ]
});

window.AppRouter.beforeEach(function (to, from, next) {
    var token = window.AppService.getToken();
    if (to.meta && to.meta.publicPage) {
        if (token && to.path === "/login") {
            next("/home");
            return;
        }
        next();
        return;
    }
    if (!token) {
        next("/login");
        return;
    }
    next();
});
