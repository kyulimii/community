package org.example.community.global.exception;

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
                .body(ApiErrorResponse.of("INVALID_INPUT", message));
    }

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        String code = mapToFeCode(errorCode);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.of(code, errorCode.getMessage()));
    }

    private String mapToFeCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case DUPLICATION_EMAIL -> "ALREADY_EXIST_EMAIL";
            case DUPLICATION_NICKNAME -> "ALREADY_EXIST_NICKNAME";
            default -> errorCode.name();
        };
    }
}
