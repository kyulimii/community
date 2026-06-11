package org.example.community.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        boolean isSuccess,
        String message,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(false, message, LocalDateTime.now());
    }
}