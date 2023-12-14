package com.thingspire.thingspire.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class MemberDTO {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Post{

        @NotBlank(message = "아이디를 입력하세요")
        private String loginId;

//        @Null(message = "이메일은 null이거나 유효한 이메일 주소 형식이어야 합니다")
//        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "유효한 이메일 주소 형식이 아닙니다")
        private String email;

        @NotBlank(message = "이름을 입력하세요")
        private String name;

        @NotBlank(message="비밀번호를 입력하세요")
        private String password;

//        @Null(message = "전화번호는 null이거나 유효한 형식이어야 합니다.")
//        @Pattern(regexp = "^010-\\d{3,4}-\\d{4}$",
//                message = "휴대폰 번호는 010으로 시작하는 11자리 숫자와 '-'로 구성되어야 합니다.")
        private String phoneNumber;

        @NotBlank
        private String department; // 회사명

        @Pattern(regexp = "^(Admin|User)$", message = "authority는 'Admin' 또는 'User'만 가능합니다.")
        private String authority;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Patch{
        private long memberId;
        private String phoneNumber;
        private String department;
        private String authority;
        private String name;
        private String loginId;
        private String email;
        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class PatchPassword{
        private long memberId;
        private String newPassword; // 바꿀 비밀번호
    }

    // 비밀번호 까먹었을때 비밀번호 초기화?
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ResetPassword {
        private long memberId;
        public void setMemberId(long memberId){
            this.memberId = memberId;}
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private long memberId;
        private String loginId;
        private String email;
        private String password;
        private String name;
        private String authority;
        private String phoneNumber;
        private String department;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime createdTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime modifiedTime;
        private String createdBy;
        private String modifiedBy;
        private String departmentType;
        private String factoryCode;
    }
}
