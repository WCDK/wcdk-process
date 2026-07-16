package com.wcdk.process.service;

import com.wcdk.process.context.AuthenticatedUser;
import com.wcdk.process.dto.CurrentUserResponse;
import com.wcdk.process.dto.LoginRequest;
import com.wcdk.process.dto.LoginResponse;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String token);

    AuthenticatedUser authenticate(String token);

    CurrentUserResponse getCurrentUser();
}
