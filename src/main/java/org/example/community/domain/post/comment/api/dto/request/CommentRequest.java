package org.example.community.domain.post.comment.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.example.community.global.exception.ConstraintConstants;

public record CommentRequest(
        @NotBlank(message = ConstraintConstants.CONTENT_BLANK_MESSAGE)
        String content
) {
}
