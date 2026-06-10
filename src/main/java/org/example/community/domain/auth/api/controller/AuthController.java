package org.example.community.domain.auth.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.auth.api.dto.request.AuthRequest;
import org.example.community.domain.auth.api.dto.response.AuthResponse;
import org.example.community.domain.auth.application.AuthService;
import org.example.community.domain.auth.application.LoginResult;
import org.example.community.global.config.JwtProperties;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.example.community.global.jwt.TokenInfo;
import org.example.community.global.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    // 로그인
    @PostMapping
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody AuthRequest loginRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = authService.login(loginRequest.email(), loginRequest.password());

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpSeconds())
                .sameSite("Strict")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.ok(result.response());
    }

    // 로그아웃
    @DeleteMapping
    public ApiResponse<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        if (refreshToken == null) {
            return ApiResponse.fail(new CustomException(ErrorCode.INVALID_TOKEN));
        }

        authService.logout(refreshToken);

        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ApiResponse.ok(null);
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ApiResponse<TokenInfo> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            return ApiResponse.fail(new CustomException(ErrorCode.INVALID_TOKEN));
        }

        return ApiResponse.ok(authService.refresh(refreshToken));
    }
}