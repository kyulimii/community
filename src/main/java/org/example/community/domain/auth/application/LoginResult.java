package org.example.community.domain.auth.application;

import org.example.community.domain.auth.api.dto.response.AuthResponse;

public record LoginResult(

        AuthResponse response,  // 응답 바디용
        String refreshToken    // 쿠키용
) {
}