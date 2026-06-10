package org.example.community.domain.post.comment.api.dto.response;

public record CommentCreateResponse(
        Long id
) {
    public static CommentCreateResponse from(Long id) {
        return new CommentCreateResponse(id);
    }
}
