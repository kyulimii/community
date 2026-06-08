package org.example.community.domain.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserPasswordUpdateRequestDto {

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$",
            message = "최소 8자 최대 20자이며 대문자, 소문자, 숫자, 특수문자 각각 최소 1개 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호를 한 번 더 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$",
            message = "최소 8자 최대 20자이며 대문자, 소문자, 숫자, 특수문자 각각 최소 1개 포함해야 합니다."
    )
    private String checkPassword;
}
