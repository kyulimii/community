package org.example.community.domain.post.comment.api.dto.response;

import java.time.LocalDateTime;
import org.example.community.domain.post.comment.Comment;
import org.example.community.domain.user.User;

public record CommentDetailResponse(
        Long id,
        String content,
        CommentAuthorResponse author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CommentDetailResponse from(Comment comment, User user) {
        return new CommentDetailResponse(
                comment.getId(),
                comment.getContent(),
                CommentAuthorResponse.from(user),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

}
