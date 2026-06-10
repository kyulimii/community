package org.example.community.domain.post.api.dto.response;

public record PostCreateResponse(
        Long id
) {
    public static PostCreateResponse from(Long id) {
        return new PostCreateResponse(id);
    }
}
