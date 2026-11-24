package com.example.erp.auth.controller;

import com.example.erp.auth.dto.AuthDtos.LoginRequest;
import com.example.erp.auth.dto.AuthDtos.LogoutRequest;
import com.example.erp.auth.dto.AuthDtos.MeVO;
import com.example.erp.auth.dto.AuthDtos.RefreshRequest;
import com.example.erp.auth.dto.AuthDtos.TokenRequest;
import com.example.erp.auth.dto.AuthDtos.TokenVO;
import com.example.erp.auth.service.AuthService;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import com.example.erp.common.core.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/token")
    public R<TokenVO> token(@Valid @RequestBody TokenRequest request) {
        return R.ok(authService.token(request.getClientId(), request.getClientSecret()));
    }

    @PostMapping("/refresh")
    public R<TokenVO> refresh(@Valid @RequestBody RefreshRequest request) {
        return R.ok(authService.refresh(request.getRefreshToken()));
    }

    @GetMapping("/me")
    public R<MeVO> me() {
        UserContextHolder.UserContext context = UserContextHolder.get();
        if (context == null || context.userId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return R.ok(authService.me(context.userId()));
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                          @RequestBody(required = false) LogoutRequest request) {
        String token = null;
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = authorization.substring(7).trim();
        }
        if ((token == null || token.isBlank()) && request != null) {
            token = request.getToken();
        }
        authService.logout(token);
        return R.ok();
    }
}
