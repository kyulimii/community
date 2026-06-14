package org.example.community.domain.image.api.dto.response;

import org.example.community.domain.image.PostImage;

public record PostImageResponse(
        Long id,
        String jpgPath,
        String webpPath,
        String originalName
) {
    public static PostImageResponse from(PostImage image) {
        return new PostImageResponse(
                image.getId(),
                image.getJpgPath(),
                image.getWebpPath(),
                image.getOriginalName()
        );
    }
}
