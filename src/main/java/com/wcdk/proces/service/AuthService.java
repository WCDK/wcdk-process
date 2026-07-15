package com.wcdk.proces.service;

import com.wcdk.proces.context.AuthenticatedUser;
import com.wcdk.proces.dto.CurrentUserResponse;
import com.wcdk.proces.dto.LoginRequest;
import com.wcdk.proces.dto.LoginResponse;
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
