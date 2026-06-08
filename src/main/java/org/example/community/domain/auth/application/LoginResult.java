package org.example.community.domain.auth.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.community.domain.auth.api.dto.response.LoginResponse;

@Getter
@AllArgsConstructor
public class LoginResult {

    private LoginResponse response; // 응답 바디용
    private String refreshToken;    // 쿠키용
}