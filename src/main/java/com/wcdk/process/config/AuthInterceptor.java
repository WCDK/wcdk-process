package com.wcdk.process.config;

import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.context.AuthenticatedUser;
import com.wcdk.process.exception.UnauthorizedException;
import com.wcdk.process.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("���ȵ�¼���ٷ���");
        }
        AuthenticatedUser authenticatedUser = authService.authenticate(token);
        AuthContextHolder.set(authenticatedUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }
        return request.getHeader("X-Auth-Token");
    }
}
