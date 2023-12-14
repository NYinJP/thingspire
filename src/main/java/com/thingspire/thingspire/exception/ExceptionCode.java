package com.thingspire.thingspire.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_POST_ERROR(400,"유효하지 않은 회원정보입니다."),
    MEMBER_NOT_FOUND(400, "회원 정보가 없습니다"),
    MEMBER_EXISTS(400, "이미 존재하는 회원입니다"),
    MEMBER_UNAUTHORIZED(401, "회원 권한이 없습니다"),
    LOGIN_ID_FAILED(400, "잘못된 로그인 아이디입니다"),
    JWT_TOKEN_UNAUTHORIZED(400, "JWT 토큰이 유효하지 않습니다"),
    Email_NOT_VALID(400, "이메일 정보가 유효하지 않습니다"),
    PASSWORD_IS_SAME(400, "바꿀 비밀번호가 이전 비밀번호와 같습니다."),
    WRONG_EMAIL_ADDRESS(400, "잘못된 이메일 정보입니다"),
    MEMBERID_NOT_VALID(400,"회원 아이디가 유효하지 않습니다"),
    PASSWORD_EMPTY(400, "입력 패스워드가 없습니다."),
    REFRESHTOKEN_NOT_SAME(400, "리프레시 토큰 값이 다릅니다."),
    REFRESHTOKEN_NOT_VALID(402, "Request failed with status code 402");


    @Getter
    private int status;
    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
