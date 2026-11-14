package com.youguocang.common;

/**
 * 业务异常，由 {@link GlobalExceptionHandler} 统一转换为 {@link ApiResult}。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(BizCode bizCode) {
        super(bizCode.getMessage());
        this.code = bizCode.getCode();
    }

    public BizException(BizCode bizCode, String message) {
        super(message);
        this.code = bizCode.getCode();
    }

    public BizException(String message) {
        super(message);
        this.code = BizCode.BAD_REQUEST.getCode();
    }

    public int getCode() {
        return code;
    }

    public static BizException of(String message) {
        return new BizException(message);
    }
}
