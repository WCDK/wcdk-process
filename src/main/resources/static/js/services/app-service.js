/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
(function () {
    var TOKEN_KEY = "wcdk_process_token";
    var USER_KEY = "wcdk_process_current_user";

    function getSessionStorage() {
        return window.sessionStorage || window.localStorage;
    }

    function clearLegacyAuth() {
        if (getSessionStorage() === window.localStorage) {
            return;
        }
        window.localStorage.removeItem(TOKEN_KEY);
        window.localStorage.removeItem(USER_KEY);
    }

    async function readResponseBody(response) {
        var contentType = response.headers.get("Content-Type") || "";
        if (contentType.indexOf("application/json") >= 0) {
            try {
                return await response.json();
            } catch (error) {
                return null;
            }
        }
        try {
            var text = await response.text();
            if (!text) {
                return null;
            }
            try {
                return JSON.parse(text);
            } catch (error) {
                return { message: text };
            }
        } catch (error) {
            return null;
        }
    }

    function getToken() {
        return getSessionStorage().getItem(TOKEN_KEY) || window.localStorage.getItem(TOKEN_KEY) || "";
    }

    function setToken(token) {
        if (token) {
            getSessionStorage().setItem(TOKEN_KEY, token);
            clearLegacyAuth();
            return;
        }
        getSessionStorage().removeItem(TOKEN_KEY);
        window.localStorage.removeItem(TOKEN_KEY);
    }

    function getCurrentUserCache() {
        var raw = getSessionStorage().getItem(USER_KEY) || window.localStorage.getItem(USER_KEY);
        if (!raw) {
            return null;
        }
        try {
            return JSON.parse(raw);
        } catch (error) {
            return null;
        }
    }

    function setCurrentUserCache(currentUser) {
        if (currentUser) {
            getSessionStorage().setItem(USER_KEY, JSON.stringify(currentUser));
            clearLegacyAuth();
            return;
        }
        getSessionStorage().removeItem(USER_KEY);
        window.localStorage.removeItem(USER_KEY);
    }

    function clearAuth() {
        setToken("");
        setCurrentUserCache(null);
    }

    function buildHeaders(options) {
        var headers = new Headers((options && options.headers) || {});
        var token = getToken();
        if (token) {
            headers.set("X-Auth-Token", token);
        }
        return headers;
    }

    function installFetchAuthInterceptor() {
        if (!window.fetch || window.fetch.__wcdkAuthInterceptor) {
            return;
        }
        var originalFetch = window.fetch.bind(window);
        var interceptedFetch = function (input, init) {
            var requestOptions = Object.assign({}, init || {});
            var sourceHeaders = requestOptions.headers || (input && input.headers);
            requestOptions.headers = buildHeaders({ headers: sourceHeaders });
            return originalFetch(input, requestOptions);
        };
        interceptedFetch.__wcdkAuthInterceptor = true;
        window.fetch = interceptedFetch;
    }

    function handleUnauthorized(response, result) {
        if (response.status === 401 || (result && result.code === 401)) {
            clearAuth();
            if (window.location.hash !== "#/login") {
                window.location.hash = "#/login";
            }
        }
    }

    function unwrapResult(response, result) {
        handleUnauthorized(response, result);
        if (!response.ok || (result && typeof result.code === "number" && result.code >= 400)) {
            throw new Error((result && result.message) || "请求失败");
        }
        return result;
    }

    async function request(url, options) {
        var response = await fetch(url, Object.assign({}, options || {}, {
            headers: buildHeaders(options)
        }));
        var result = await readResponseBody(response);
        if (!result) {
            result = {
                code: response.ok ? 200 : response.status,
                message: response.ok ? "处理成功" : ("请求失败，状态码：" + response.status),
                data: null
            };
        }
        return unwrapResult(response, result);
    }

    function requestJson(url, options) {
        var requestOptions = options || {};
        var headers = buildHeaders(requestOptions);
        headers.set("Content-Type", "application/json;charset=UTF-8");
        return request(url, Object.assign({}, requestOptions, { headers: headers }));
    }

    function formatDateTime(value) {
        if (!value) {
            return "-";
        }
        var date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return String(value);
        }
        return date.toLocaleString("zh-CN", { hour12: false });
    }

    function resolveStatusType(status) {
        if (status === "PROCESSING") {
            return "warning";
        }
        if (status === "APPROVED") {
            return "success";
        }
        if (status === "REJECTED") {
            return "danger";
        }
        return "info";
    }

    function resolveStatusLabel(status) {
        var mapping = {
            DRAFT: "草稿",
            PROCESSING: "审批中",
            APPROVED: "已通过",
            REJECTED: "已驳回",
            CANCELLED: "已取消",
            CANCELED: "已取消",
            TERMINATED: "已终止",
            COMPLETED: "已完成"
        };
        return mapping[status] || status || "-";
    }

    function hasPermission(permissionCode, currentUser) {
        if (!permissionCode) {
            return true;
        }
        var user = currentUser || getCurrentUserCache();
        var permissionCodes = (user && user.permissionCodes) || [];
        return permissionCodes.indexOf(permissionCode) >= 0
            || permissionCodes.indexOf("*") >= 0
            || permissionCodes.indexOf("*:*:*") >= 0;
    }

    function flattenPermissionResources(resources) {
        var result = [];
        function collect(items) {
            (items || []).forEach(function (item) {
                result.push(item);
                collect(item.children || []);
            });
        }
        collect(resources || []);
        return result;
    }

    function getPermissionResources(permissionType, currentUser) {
        var user = currentUser || getCurrentUserCache();
        var resources = flattenPermissionResources((user && user.permissionResources) || []);
        if (!permissionType) {
            return resources;
        }
        return resources.filter(function (item) {
            return item.permissionType === permissionType;
        });
    }

    function hasResource(permissionCode, permissionType, currentUser) {
        if (!hasPermission(permissionCode, currentUser)) {
            return false;
        }
        var user = currentUser || getCurrentUserCache();
        var permissionCodes = (user && user.permissionCodes) || [];
        if (permissionCodes.indexOf("*") >= 0 || permissionCodes.indexOf("*:*:*") >= 0) {
            return true;
        }
        var resources = getPermissionResources(permissionType, currentUser);
        if (!resources.length) {
            return true;
        }
        return resources.some(function (item) {
            return item.permissionCode === permissionCode;
        });
    }

    installFetchAuthInterceptor();

    window.AppService = {
        request: request,
        requestJson: requestJson,
        formatDateTime: formatDateTime,
        resolveStatusType: resolveStatusType,
        resolveStatusLabel: resolveStatusLabel,
        getToken: getToken,
        setToken: setToken,
        getCurrentUserCache: getCurrentUserCache,
        setCurrentUserCache: setCurrentUserCache,
        clearAuth: clearAuth,
        hasPermission: hasPermission,
        getPermissionResources: getPermissionResources,
        hasResource: hasResource
    };
})();
