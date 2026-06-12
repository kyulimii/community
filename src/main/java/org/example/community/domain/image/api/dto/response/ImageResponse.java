package org.example.community.domain.image.api.dto.response;

import org.example.community.domain.image.Image;

public record ImageResponse(
        Long id,
        String jpgPath,
        String webpPath,
        String originalName
) {
    public static ImageResponse from(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getJpgPath(),
                image.getWebpPath(),
                image.getOriginalName()
        );
    }
}
