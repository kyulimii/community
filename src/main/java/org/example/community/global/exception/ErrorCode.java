package org.example.community.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // ------- AUTH --------
    MISMATCH_LOGIN(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호를 확인해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // ------- USER --------
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    MISMATCH_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 다릅니다."),
    DUPLICATION_EMAIL(HttpStatus.BAD_REQUEST, "중복된 이메일입니다."),
    DUPLICATION_NICKNAME(HttpStatus.BAD_REQUEST, "중복된 닉네임입니다."),

    // ------- POST --------
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "작성자만 가능합니다."),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 기준입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "유효하지 않은 커서입니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글입니다."),
    NOT_LIKED(HttpStatus.BAD_REQUEST, "좋아요를 누르지 않았습니다."),

    // ------- IMAGE --------
    IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "프로필 사진을 추가해주세요."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 처리 중 오류가 발생했습니다."),
    IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 크기는 5MB 이하여야 합니다."),
    IMAGE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다."),

    // ------- COMMENT --------
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
