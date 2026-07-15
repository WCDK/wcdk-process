package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.context.AuthContextHolder;
import com.wcdk.proces.dto.CurrentUserResponse;
import com.wcdk.proces.dto.LoginRequest;
import com.wcdk.proces.dto.LoginResponse;
import com.wcdk.proces.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@RestController
@RequestMapping("/auth")
@Tag(name = "登录认证", description = "提供登录、退出和当前登录用户信息查询能力")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名和密码完成登录并返回令牌")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "根据当前登录令牌退出系统")
    public ApiResponse<Void> logout() {
        authService.logout(AuthContextHolder.requireUser().getToken());
        return ApiResponse.success("退出成功", null);
    }

    @GetMapping("/me")
    @Operation(summary = "查询当前用户", description = "返回当前登录用户的部门、角色和权限信息")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(authService.getCurrentUser());
    }
}
