package org.example.community.global.exception;

public final class ConstraintConstants {
    // EMAIL
    public static final String EMAIL_BLANK_MESSAGE = "이메일은 필수입니다.";
    public static final String EMAIL_FORMAT_MESSAGE = "올바른 이메일 주소 형식을 입력해주세요. 예) example@example.com";

    // PASSWORD
    public static final String PASSWORD_BLANK_MESSAGE = "비밀번호를 입력해주세요.";
    public static final String PASSWORD_FORMAT = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$";
    public static final String PASSWORD_FORMAT_MESSAGE = "최소 8자 최대 20자이며 대문자, 소문자, 숫자, 특수문자 각각 최소 1개 포함해야 합니다.";
    public static final String CHECK_PASSWORD_BLANK_MESSAGE = "비밀번호를 한 번 더 입력해주세요.";


    // NICKNAME
    public static final String NICKNAME_BLANK_MESSAGE = "닉네임을 입력해주세요.";
    public static final int NICKNAME_MAX = 10;
    public static final String NICKNAME_MAX_MESSAGE = "닉네임은 최대 10자 까지 작성 가능합니다.";
    public static final String NICKNAME_FORMAT = "^\\S+$";
    public static final String NICKNAME_FORMAT_MESSAGE ="띄어쓰기를 없애주세요.";

    // POST
    public static final String POST_BLANK_MESSAGE = "제목, 내용을 모두 작성해주세요";
    public static final int TITLE_MAX = 26;
    public static final String TITLE_MAX_MESSAGE = "제목은 최대 26자까지 작성 가능합니다.";

    // Comment
    public static final String CONTENT_BLANK_MESSAGE = "댓글을 작성해주세요.";
}
