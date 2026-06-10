package org.example.community.domain.user.api.dto.response;

import org.example.community.domain.user.User;

public record UserInfoResponse(
        Long id,
        String email,
        String nickname,
        String profileImage
) {

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage()
        );
    }
}
