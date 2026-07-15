package com.wcdk.proces.service.impl;

import com.wcdk.proces.context.AuthContextHolder;
import com.wcdk.proces.context.AuthenticatedUser;
import com.wcdk.proces.dto.CurrentUserResponse;
import com.wcdk.proces.dto.LoginRequest;
import com.wcdk.proces.dto.LoginResponse;
import com.wcdk.proces.entity.SysUser;
import com.wcdk.proces.exception.UnauthorizedException;
import com.wcdk.proces.service.AuthService;
import com.wcdk.proces.service.SysPermissionService;
import com.wcdk.proces.service.SysRoleService;
import com.wcdk.proces.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Service
public class AuthServiceImpl implements AuthService {

    private static final long TOKEN_EXPIRE_HOURS = 12L;

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;
    private final Map<String, TokenSession> tokenSessionMap = new ConcurrentHashMap<>();

    public AuthServiceImpl(SysUserService sysUserService,
                           SysRoleService sysRoleService,
                           SysPermissionService sysPermissionService) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.sysPermissionService = sysPermissionService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new UnauthorizedException("用户名或密码不能为空");
        }
        SysUser sysUser = sysUserService.getByUsername(request.getUsername().trim());
        if (sysUser == null || !Integer.valueOf(1).equals(sysUser.getStatus())) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        String passwordHash = DigestUtils.md5DigestAsHex(request.getPassword().trim().getBytes(StandardCharsets.UTF_8));
        if (!passwordHash.equalsIgnoreCase(sysUser.getPasswordHash())) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        sysUserService.updateLastLoginTime(sysUser.getId());
        LocalDateTime expireTime = LocalDateTime.now().plusHours(TOKEN_EXPIRE_HOURS);
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(sysUser, token);
        tokenSessionMap.put(token, new TokenSession(authenticatedUser, expireTime));
        return LoginResponse.builder()
                .token(token)
                .expireTime(expireTime)
                .currentUser(toCurrentUserResponse(authenticatedUser))
                .build();
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            tokenSessionMap.remove(token.trim());
        }
    }

    @Override
    public AuthenticatedUser authenticate(String token) {
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("请先登录后再访问");
        }
        TokenSession tokenSession = tokenSessionMap.get(token.trim());
        if (tokenSession == null || tokenSession.getExpireTime().isBefore(LocalDateTime.now())) {
            tokenSessionMap.remove(token);
            throw new UnauthorizedException("登录状态已过期，请重新登录");
        }
        return tokenSession.getAuthenticatedUser();
    }

    @Override
    public CurrentUserResponse getCurrentUser() {
        return toCurrentUserResponse(AuthContextHolder.requireUser());
    }

    private AuthenticatedUser buildAuthenticatedUser(SysUser sysUser, String token) {
        var userResponse = sysUserService.getUserResponse(sysUser.getId());
        Set<String> permissionCodes = sysPermissionService.listPermissionCodesByUserId(sysUser.getId());
        return AuthenticatedUser.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .deptId(userResponse.getDeptId())
                .deptName(userResponse.getDeptName())
                .token(token)
                .roleIds(userResponse.getRoleIds())
                .roleNames(userResponse.getRoleNames())
                .permissionCodes(permissionCodes)
                .build();
    }

    private CurrentUserResponse toCurrentUserResponse(AuthenticatedUser authenticatedUser) {
        return CurrentUserResponse.builder()
                .userId(authenticatedUser.getUserId())
                .username(authenticatedUser.getUsername())
                .realName(authenticatedUser.getRealName())
                .deptId(authenticatedUser.getDeptId())
                .deptName(authenticatedUser.getDeptName())
                .roleIds(authenticatedUser.getRoleIds())
                .roleNames(authenticatedUser.getRoleNames())
                .permissionCodes(authenticatedUser.getPermissionCodes())
                .build();
    }

    private static class TokenSession {

        private final AuthenticatedUser authenticatedUser;

        private final LocalDateTime expireTime;

        private TokenSession(AuthenticatedUser authenticatedUser, LocalDateTime expireTime) {
            this.authenticatedUser = authenticatedUser;
            this.expireTime = expireTime;
        }

        public AuthenticatedUser getAuthenticatedUser() {
            return authenticatedUser;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }
    }
}
