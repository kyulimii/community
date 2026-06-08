package org.example.community.domain.auth.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.community.domain.auth.api.dto.request.LoginRequestDto;
import org.example.community.domain.auth.api.dto.response.LoginResponse;
import org.example.community.domain.auth.application.AuthService;
import org.example.community.domain.auth.application.LoginResult;
import org.example.community.global.config.JwtProperties;
import org.example.community.global.jwt.TokenInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletResponse httpResponse
    ) {
        LoginResult result = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpSeconds())
                .sameSite("Strict")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(result.getResponse());
    }

    // 로그아웃
    @DeleteMapping
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenInfo> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authService.refresh(refreshToken));
    }
}