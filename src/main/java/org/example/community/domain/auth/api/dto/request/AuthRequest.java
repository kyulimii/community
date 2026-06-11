package org.example.community.domain.auth.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.example.community.global.exception.ConstraintConstants;

public record AuthRequest(
        @NotBlank(message = ConstraintConstants.EMAIL_BLANK_MESSAGE)
        @Email(message = ConstraintConstants.EMAIL_FORMAT_MESSAGE)
        String email,

        @NotBlank(message = ConstraintConstants.PASSWORD_BLANK_MESSAGE)
        @Pattern(
                regexp = ConstraintConstants.PASSWORD_FORMAT,
                message = ConstraintConstants.PASSWORD_FORMAT_MESSAGE
        )
        String password
) {
}
