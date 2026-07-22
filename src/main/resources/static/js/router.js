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
        { path: "/home", component: window.HomePage, meta: { permissionCode: "menu:home" } },
        { path: "/deploy", component: window.DeployCenter, meta: { permissionCode: "menu:deploy" } },
        { path: "/model", component: window.ModelCenter, meta: { permissionCode: "menu:model" } },
        { path: "/designer", component: window.ProcessDesigner, meta: { permissionCode: "menu:designer" } },
        { path: "/form-designer", component: window.FormDesigner, meta: { permissionCode: "menu:form" } },
        { path: "/process", component: window.ProcessCenter, meta: { permissionCode: "menu:process" } },
        { path: "/task", component: window.TaskCenter, meta: { permissionCode: "menu:task" } },
        { path: "/client", component: window.ClientCenter, meta: { permissionCode: "client:view" } },
        { path: "/system/user", component: window.UserCenter, meta: { permissionCode: "menu:sys:user" } },
        { path: "/system/role", component: window.RoleCenter, meta: { permissionCode: "menu:sys:role" } },
        { path: "/system/permission", component: window.PermissionCenter, meta: { permissionCode: "menu:sys:permission" } },
        { path: "/system/dept", component: window.DeptCenter, meta: { permissionCode: "menu:sys:dept" } }
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
    if (to.meta && to.meta.permissionCode && !window.AppService.hasPermission(to.meta.permissionCode)) {
        var menus = window.AppService.getPermissionResources("MENU")
            .filter(function (item) {
                return item.routePath && window.AppService.hasPermission(item.permissionCode);
            })
            .sort(function (left, right) {
                return Number(left.sortNo || 0) - Number(right.sortNo || 0);
            });
        next(menus.length ? menus[0].routePath : "/login");
        return;
    }
    next();
});
