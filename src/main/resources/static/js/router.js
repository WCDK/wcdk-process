window.AppRouter = new VueRouter({
    mode: "hash",
    routes: [
        {
            path: "/",
            redirect: "/home"
        },
        {
            path: "/home",
            component: window.HomePage
        },
        {
            path: "/deploy",
            component: window.DeployCenter
        },
        {
            path: "/model",
            component: window.ModelCenter
        },
        {
            path: "/designer",
            component: window.ProcessDesigner
        },
        {
            path: "/process",
            component: window.ProcessCenter
        },
        {
            path: "/task",
            component: window.TaskCenter
        }
    ]
});
