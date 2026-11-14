package com.youguocang.common;

/**
 * 业务状态码。
 */
public enum BizCode {

    OK(200, "success"),
    BAD_REQUEST(400, "请求参数不合法"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "操作冲突"),
    TOO_MANY_REQUESTS(429, "操作过于频繁"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    BizCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
