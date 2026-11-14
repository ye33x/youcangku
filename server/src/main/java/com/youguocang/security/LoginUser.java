package com.youguocang.security;

import com.youguocang.common.RoleType;

/**
 * 登录态，由 JWT 解析得到并放入 {@link UserContext}。
 *
 * <p>注意：不包含密码等敏感字段。
 */
public class LoginUser {

    private final Long id;
    private final String name;
    private final RoleType role;

    public LoginUser(Long id, String name, RoleType role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RoleType getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "LoginUser{id=" + id + ", name='" + name + "', role=" + role + '}';
    }
}
