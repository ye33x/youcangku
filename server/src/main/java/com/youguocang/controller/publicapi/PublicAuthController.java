package com.youguocang.controller.publicapi;

import com.youguocang.common.ApiResult;
import com.youguocang.common.BizCode;
import com.youguocang.common.BizException;
import com.youguocang.common.RoleType;
import com.youguocang.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 免登录接口：登录与注册。
 *
 * <p>用户端、商家端、管理端共用一个登录入口，通过 {@code role} 区分，避免三套重复代码。
 */
@RestController
@RequestMapping("/api/public/auth")
public class PublicAuthController {

    private final AuthService authService;

    public PublicAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResult<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest request) {
        RoleType role = parseRole(request.role());
        return ApiResult.ok(authService.login(role, request.account(), request.password()));
    }

    @PostMapping("/register/user")
    public ApiResult<Void> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request.username(), request.phone(), request.password());
        return ApiResult.ok();
    }

    @PostMapping("/register/merchant")
    public ApiResult<Void> registerMerchant(@Valid @RequestBody RegisterRequest request) {
        authService.registerMerchant(request.username(), request.phone(), request.password());
        return ApiResult.ok();
    }

    private RoleType parseRole(String role) {
        try {
            return RoleType.valueOf(role.toUpperCase());
        } catch (RuntimeException ex) {
            throw new BizException(BizCode.BAD_REQUEST, "role 只能是 USER / MERCHANT / ADMIN");
        }
    }

    public record LoginRequest(@NotBlank String account, @NotBlank String password,
                               @NotBlank String role) {
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String phone,
                                  @NotBlank String password) {
    }
}
