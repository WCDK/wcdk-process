(function () {
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

    function unwrapResult(response, result) {
        if (!response.ok || (typeof result.code === "number" && result.code >= 400)) {
            throw new Error((result && result.message) || "请求失败");
        }
        return result;
    }

    async function request(url, options) {
        var response = await fetch(url, options || {});
        var result = await readResponseBody(response);
        if (!result) {
            result = {
                code: response.ok ? 200 : response.status,
                message: response.ok
                    ? "处理成功"
                    : ("请求失败，协议状态码：" + response.status),
                data: null
            };
        }
        return unwrapResult(response, result);
    }

    function requestJson(url, options) {
        var requestOptions = options || {};
        var headers = new Headers(requestOptions.headers || {});
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

    window.AppService = {
        request: request,
        requestJson: requestJson,
        formatDateTime: formatDateTime,
        resolveStatusType: resolveStatusType,
        resolveStatusLabel: resolveStatusLabel
    };
})();
