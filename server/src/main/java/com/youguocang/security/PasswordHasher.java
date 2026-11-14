package com.youguocang.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * 密码哈希工具：PBKDF2-HMAC-SHA256。
 *
 * <p>存储格式：{@code pbkdf2_sha256$<iterations>$<saltBase64>$<hashBase64>}
 *
 * <p>选择 PBKDF2 而非 BCrypt 的原因：JDK 原生支持，无需额外依赖，
 * 且算法参数写在哈希串里，日后提升迭代次数不会让老密码失效。
 * {@code tools/gen_seed.py} 用同样的参数生成演示数据，两边可互相校验。
 */
public final class PasswordHasher {

    private static final String PREFIX = "pbkdf2_sha256";
    private static final int ITERATIONS = 310_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null || stored.isBlank()) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(rawPassword, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static byte[] pbkdf2(String rawPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("密码哈希计算失败", ex);
        }
    }

    /** 提供一个可运行的自检入口：java -cp ... com.youguocang.security.PasswordHasher */
    public static void main(String[] args) {
        String raw = args.length > 0 ? args[0] : "123456";
        String hashed = hash(raw);
        System.out.println(hashed);
        System.out.println("verify=" + matches(raw, hashed));
        System.out.println("bytes=" + hashed.getBytes(StandardCharsets.UTF_8).length);
    }
}
