package com.youguocang.common;

/**
 * 三端角色。
 *
 * <p>{@code code} 与数据库中约定的角色编号保持一致，便于写入 JWT 与日志。
 */
public enum RoleType {

    USER(1, "用户", "/api/user"),
    MERCHANT(2, "商家", "/api/merchant"),
    ADMIN(3, "管理员", "/api/admin");

    private final int code;
    private final String label;
    private final String pathPrefix;

    RoleType(int code, String label, String pathPrefix) {
        this.code = code;
        this.label = label;
        this.pathPrefix = pathPrefix;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public static RoleType fromCode(int code) {
        for (RoleType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知角色编号: " + code);
    }

    public static RoleType fromName(String name) {
        for (RoleType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知角色名称: " + name);
    }
}
