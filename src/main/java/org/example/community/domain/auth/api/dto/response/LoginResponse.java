package org.example.community.domain.auth.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.community.domain.user.api.dto.response.UserInfoDto;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private long expiresIn;
    private UserInfoDto user;

    public static LoginResponse of(UserInfoDto user, String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, expiresIn, user);
    }
}