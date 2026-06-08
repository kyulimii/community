package org.example.community.domain.post.comment.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentRequestDto {
    @NotBlank(message = "댓글을 작성해주세요.")
    private String content;
}
