package org.example.community.domain.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.community.global.exception.ConstraintConstants;

public record UserUpdateRequest(
        @NotBlank(message = ConstraintConstants.NICKNAME_BLANK_MESSAGE)
        @Size(max = ConstraintConstants.NICKNAME_MAX, message = ConstraintConstants.NICKNAME_MAX_MESSAGE)
        @Pattern(
                regexp = ConstraintConstants.NICKNAME_FORMAT,
                message = ConstraintConstants.NICKNAME_FORMAT_MESSAGE
        )
        String nickname
) {
}
