package org.example.community.global.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomRestControllerAdvice {

    // @Valid 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> responseValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효하지 않은 입력입니다.");

        return ResponseEntity
                .badRequest()
                        .body(ApiErrorResponse.of(message));
    }

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException e) {

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiErrorResponse.of(e.getErrorCode().getMessage()));
    }
}
