package com.wcdk.process.context;

import com.wcdk.process.exception.ForbiddenException;
import com.wcdk.process.exception.UnauthorizedException;

import java.util.Set;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public final class AuthContextHolder {

    private static final ThreadLocal<AuthenticatedUser> HOLDER = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthenticatedUser authenticatedUser) {
        HOLDER.set(authenticatedUser);
    }

    public static AuthenticatedUser get() {
        return HOLDER.get();
    }

    public static AuthenticatedUser requireUser() {
        AuthenticatedUser authenticatedUser = HOLDER.get();
        if (authenticatedUser == null) {
            throw new UnauthorizedException("��ǰ��¼�ѹ��ڻ���δ��¼");
        }
        return authenticatedUser;
    }

    public static void requirePermission(String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) {
            return;
        }
        AuthenticatedUser authenticatedUser = requireUser();
        Set<String> permissionCodes = authenticatedUser.getPermissionCodes();
        if (permissionCodes == null
                || !(permissionCodes.contains(permissionCode)
                || permissionCodes.contains("*")
                || permissionCodes.contains("*:*:*"))) {
            throw new ForbiddenException("��ǰ�û���Ȩ��ִ�иò���");
        }
    }

    public static void clear() {
        HOLDER.remove();
    }
}
