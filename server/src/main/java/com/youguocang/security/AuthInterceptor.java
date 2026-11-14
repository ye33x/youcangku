package com.youguocang.security;

import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.common.RoleType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 三端鉴权拦截器。
 *
 * <p>校验逻辑由「路径前缀」与「token 中的角色」共同决定，避免为每个接口单独写注解：
 * <ul>
 *   <li>{@code /api/public/**} 放行</li>
 *   <li>{@code /api/user/**}   需要 USER</li>
 *   <li>{@code /api/merchant/**} 需要 MERCHANT</li>
 *   <li>{@code /api/admin/**}  需要 ADMIN</li>
 * </ul>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/public/") || "/api/public".equals(path)
                || "/actuator/health".equals(path) || path.startsWith("/uploads/")) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            throw new BizException(BizCode.UNAUTHORIZED);
        }

        LoginUser loginUser = jwtUtil.parse(header.substring(BEARER_PREFIX.length()).trim());
        if (loginUser == null) {
            throw new BizException(BizCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }

        RoleType required = resolveRequiredRole(path);
        if (required != null && required != loginUser.getRole()) {
            log.warn("越权访问: path={}, user={}, required={}", path, loginUser, required);
            throw new BizException(BizCode.FORBIDDEN);
        }

        UserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private RoleType resolveRequiredRole(String path) {
        for (RoleType role : RoleType.values()) {
            if (path.startsWith(role.getPathPrefix() + "/") || path.equals(role.getPathPrefix())) {
                return role;
            }
        }
        return null;
    }
}
