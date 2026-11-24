package com.example.erp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public final class AuthDtos {

    private AuthDtos() {
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class TokenRequest {
        @NotBlank(message = "clientId 不能为空")
        private String clientId;
        @NotBlank(message = "clientSecret 不能为空")
        private String clientSecret;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank(message = "refreshToken 不能为空")
        private String refreshToken;
    }

    @Data
    public static class LogoutRequest {
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenVO {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeVO {
        private Long userId;
        private String username;
        private String nickname;
        private List<String> roles;
        private List<String> perms;
    }
}
