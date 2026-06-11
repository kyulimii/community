package org.example.community.domain.user.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.community.global.exception.ConstraintConstants;

public record UserCreateRequest(

        @NotBlank(message = ConstraintConstants.EMAIL_BLANK_MESSAGE)
        @Email(message = ConstraintConstants.EMAIL_FORMAT_MESSAGE)
        String email,

        @NotBlank(message = ConstraintConstants.PASSWORD_BLANK_MESSAGE)
        @Pattern(
                regexp = ConstraintConstants.PASSWORD_FORMAT,
                message = ConstraintConstants.PASSWORD_FORMAT_MESSAGE
        )
        String password,

        @NotBlank(message = ConstraintConstants.CHECK_PASSWORD_BLANK_MESSAGE)
        @Pattern(
                regexp = ConstraintConstants.PASSWORD_FORMAT,
                message = ConstraintConstants.PASSWORD_FORMAT_MESSAGE
        )
        String checkPassword,

        @NotBlank(message = ConstraintConstants.NICKNAME_BLANK_MESSAGE)
        @Size(max = ConstraintConstants.NICKNAME_MAX, message = ConstraintConstants.NICKNAME_BLANK_MESSAGE)
        @Pattern(
                regexp = ConstraintConstants.NICKNAME_FORMAT,
                message = ConstraintConstants.NICKNAME_FORMAT_MESSAGE
        )
        String nickname
) {
}
