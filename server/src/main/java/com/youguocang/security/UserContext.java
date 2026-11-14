package com.youguocang.security;

import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.common.RoleType;

/**
 * 请求级登录态容器。
 *
 * <p>必须在请求结束时清理，否则线程池复用会造成身份串号（见 {@code AuthInterceptor#afterCompletion}）。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录直接抛 401。
     */
    public static LoginUser require() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BizException(BizCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static Long requireUserId() {
        return require().getId();
    }

    public static RoleType requireRole() {
        return require().getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
