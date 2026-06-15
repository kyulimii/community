package org.example.community.domain.image.api.dto.response;

import org.example.community.domain.image.ProfileImage;

public record ProfileImageResponse(
        Long id,
        String profileImageUrl,
        String webpPath,
        String originalName
) {
    public static ProfileImageResponse from(ProfileImage image) {
        return new ProfileImageResponse(
                image.getId(),
                image.getJpgPath(),
                image.getWebpPath(),
                image.getOriginalName()
        );
    }
}
