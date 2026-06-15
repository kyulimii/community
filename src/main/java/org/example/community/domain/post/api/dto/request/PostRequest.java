package org.example.community.domain.post.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.community.global.exception.ConstraintConstants;

public record PostRequest(

        @NotBlank(message = ConstraintConstants.POST_BLANK_MESSAGE)
        @Size(max = ConstraintConstants.TITLE_MAX, message = ConstraintConstants.TITLE_MAX_MESSAGE)
        String title,

        @NotBlank(message = ConstraintConstants.POST_BLANK_MESSAGE)
        String content,

        String postImageUrl
) {
}
