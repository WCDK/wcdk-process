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
            throw new Error((result && result.message) || "\u8bf7\u6c42\u5931\u8d25");
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
                    ? "\u5904\u7406\u6210\u529f"
                    : ("\u8bf7\u6c42\u5931\u8d25\uff0c\u534f\u8bae\u72b6\u6001\u7801\uff1a" + response.status),
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
            DRAFT: "\u8349\u7a3f",
            PROCESSING: "\u5ba1\u6279\u4e2d",
            APPROVED: "\u5df2\u901a\u8fc7",
            REJECTED: "\u5df2\u9a73\u56de",
            CANCELLED: "\u5df2\u53d6\u6d88",
            CANCELED: "\u5df2\u53d6\u6d88",
            TERMINATED: "\u5df2\u7ec8\u6b62",
            COMPLETED: "\u5df2\u5b8c\u6210"
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
