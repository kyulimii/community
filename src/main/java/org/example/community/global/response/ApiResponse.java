package org.example.community.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import org.example.community.global.exception.CustomException;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean isSuccess,
        T body,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(true, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(true, data, LocalDateTime.now());
    }
}
