package com.youguocang.common;

/**
 * 统一响应体。
 *
 * <p>约定：{@code code == 200} 表示成功，其余为业务或系统错误码。
 */
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    public ApiResult() {
    }

    public ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(BizCode.OK.getCode(), "success", null);
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(BizCode.OK.getCode(), "success", data);
    }

    public static <T> ApiResult<T> fail(BizCode code) {
        return new ApiResult<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> ApiResult<T> fail(BizCode code, String message) {
        return new ApiResult<>(code.getCode(), message, null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
