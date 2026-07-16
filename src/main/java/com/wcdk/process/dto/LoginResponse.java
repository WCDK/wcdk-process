package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
public class LoginResponse {

    private String token;

    private LocalDateTime expireTime;

    private CurrentUserResponse currentUser;
}
