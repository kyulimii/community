package org.example.community.domain.post.comment.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.comment.Comment;

public record CommentDetailResponse(
        Long id,
        String content,
        String author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CommentDetailResponse from(Comment comment, String nickname) {
        return new CommentDetailResponse(
                comment.getId(),
                comment.getContent(),
                nickname,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

}
