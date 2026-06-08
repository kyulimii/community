package org.example.community.domain.user.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.community.domain.user.User;

@Getter
@Builder
public class UserInfoDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;

    public static UserInfoDto from(User user) {
        return new UserInfoDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage()
        );
    }
}
