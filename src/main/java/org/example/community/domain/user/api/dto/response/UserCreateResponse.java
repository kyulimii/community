package org.example.community.domain.user.api.dto.response;

public record UserCreateResponse(Long id) {
    public static UserCreateResponse from(Long id) {
        return new UserCreateResponse(id);
    }
}