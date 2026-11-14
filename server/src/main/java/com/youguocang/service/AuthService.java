package com.youguocang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.common.RoleType;
import com.youguocang.entity.Admin;
import com.youguocang.entity.Merchant;
import com.youguocang.entity.User;
import com.youguocang.mapper.AdminMapper;
import com.youguocang.mapper.MerchantMapper;
import com.youguocang.mapper.UserMapper;
import com.youguocang.security.JwtUtil;
import com.youguocang.security.LoginUser;
import com.youguocang.security.PasswordHasher;
import org.springframework.stereotype.Service;

/**
 * 三端登录与注册。
 *
 * <p>三个角色分表存储，登录时按 role 选择对应的表，天然避免「商家账号能登用户端」这类越权。
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, MerchantMapper merchantMapper,
                       AdminMapper adminMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.merchantMapper = merchantMapper;
        this.adminMapper = adminMapper;
        this.jwtUtil = jwtUtil;
    }

    public LoginResult login(RoleType role, String account, String password) {
        if (account == null || account.isBlank() || password == null || password.isBlank()) {
            throw new BizException(BizCode.BAD_REQUEST, "账号和密码不能为空");
        }
        return switch (role) {
            case USER -> loginUser(account, password);
            case MERCHANT -> loginMerchant(account, password);
            case ADMIN -> loginAdmin(account, password);
        };
    }

    private LoginResult loginUser(String account, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .or()
                .eq(User::getPhone, account));
        verify(user == null ? null : user.getPassword(), password, user == null ? null : user.getStatus());
        return issue(user.getId(), displayName(user.getNickname(), user.getUsername()), RoleType.USER);
    }

    private LoginResult loginMerchant(String account, String password) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, account)
                .or()
                .eq(Merchant::getPhone, account));
        verify(merchant == null ? null : merchant.getPassword(), password,
                merchant == null ? null : merchant.getStatus());
        return issue(merchant.getId(), merchant.getUsername(), RoleType.MERCHANT);
    }

    private LoginResult loginAdmin(String account, String password) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, account));
        verify(admin == null ? null : admin.getPassword(), password,
                admin == null ? null : admin.getStatus());
        return issue(admin.getId(), admin.getRealName() == null ? admin.getUsername() : admin.getRealName(),
                RoleType.ADMIN);
    }

    private void verify(String storedPassword, String rawPassword, Integer status) {
        if (storedPassword == null || !PasswordHasher.matches(rawPassword, storedPassword)) {
            throw new BizException(BizCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (status != null && status == 0) {
            throw new BizException(BizCode.FORBIDDEN, "账号已被禁用，请联系平台");
        }
    }

    private LoginResult issue(Long id, String name, RoleType role) {
        LoginUser loginUser = new LoginUser(id, name, role);
        return new LoginResult(jwtUtil.generate(loginUser), id, name, role.name(), role.getLabel());
    }

    /** 用户注册：用户名、手机号、密码。 */
    public void registerUser(String username, String phone, String password) {
        if (username == null || username.isBlank() || phone == null || phone.isBlank()
                || password == null || password.length() < 6) {
            throw new BizException(BizCode.BAD_REQUEST, "用户名、手机号必填，密码至少 6 位");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .or()
                .eq(User::getPhone, phone));
        if (exists != null && exists > 0) {
            throw new BizException(BizCode.CONFLICT, "用户名或手机号已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setNickname(username);
        user.setPassword(PasswordHasher.hash(password));
        user.setStatus(1);
        userMapper.insert(user);
    }

    /** 商家入驻注册。 */
    public void registerMerchant(String username, String phone, String password) {
        if (username == null || username.isBlank() || phone == null || phone.isBlank()
                || password == null || password.length() < 6) {
            throw new BizException(BizCode.BAD_REQUEST, "用户名、手机号必填，密码至少 6 位");
        }
        Long exists = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, username)
                .or()
                .eq(Merchant::getPhone, phone));
        if (exists != null && exists > 0) {
            throw new BizException(BizCode.CONFLICT, "商家账号已存在");
        }
        Merchant merchant = new Merchant();
        merchant.setUsername(username);
        merchant.setPhone(phone);
        merchant.setPassword(PasswordHasher.hash(password));
        merchant.setStatus(1);
        merchantMapper.insert(merchant);
    }

    private static String displayName(String nickname, String username) {
        return nickname == null || nickname.isBlank() ? username : nickname;
    }

    /** 登录结果（token + 基本身份信息，前端据此决定可访问的菜单）。 */
    public record LoginResult(String token, Long id, String name, String role, String roleLabel) {
    }
}
