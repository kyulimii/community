package org.example.community.domain.image.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class FileUploadRequestDto {

    @NotNull(message = "파일은 필수입니다.")
    private MultipartFile file;

    private String description;

    @Value("${file.size}")
    public final long MAX_FILE_SIZE;

    public boolean isFileSizeValid() {
        return file != null && file.getSize() <= MAX_FILE_SIZE;
    }
}
