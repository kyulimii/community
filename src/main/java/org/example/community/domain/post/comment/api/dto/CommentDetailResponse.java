package org.example.community.domain.post.comment.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.community.domain.post.comment.Comment;

@Getter
@AllArgsConstructor
public class CommentDetailResponse {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
