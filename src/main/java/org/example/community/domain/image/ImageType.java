package org.example.community.domain.image;

import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;

public enum ImageType {
    USER, POST;

    public static ImageType from(String type) {
        try {
            return ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.IMAGE_INVALID_TYPE);
        }
    }
}
