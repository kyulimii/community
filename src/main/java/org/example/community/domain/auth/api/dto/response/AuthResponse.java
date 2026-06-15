package org.example.community.domain.auth.api.dto.response;

import org.example.community.domain.user.api.dto.response.UserInfoResponse;

public record AuthResponse(
        String accessToken,
        long expiresIn,
        UserInfoResponse user
) {

    public static AuthResponse of(UserInfoResponse user, String accessToken, long expiresIn) {
        return new AuthResponse(accessToken, expiresIn, user);
    }
}