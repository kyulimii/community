package org.example.community.domain.post.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PostRequestDto {

    @NotBlank(message = "제목, 내용을 모두 작성해주세요")
    @Size(max = 26, message = "제목은 최대 26자까지 작성 가능합니다.")
    private String title;

    @NotBlank(message = "제목, 내용을 모두 작성해주세요")
    private String content;
}
