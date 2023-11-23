package com.thingspire.thingspire.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.PostLoad;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.time.LocalDateTime;

public class MemberDTO {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Post{
        @NotBlank(message = "아이디를 입력하세요")
        private String loginId;
        @Email
        @NotBlank(message = "이메일을 입력하세요")
        private String email;
        @NotBlank(message = "이름을 입력하세요")
        private String name;
        @NotBlank(message="비밀번호를 입력하세요")
        private String password;
        @Pattern(regexp = "^010-\\d{3,4}-\\d{4}$",
                message = "휴대폰 번호는 010으로 시작하는 11자리 숫자와 '-'로 구성되어야 합니다.")
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
        private String eamil;
        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class PatchPassword{
        private long memberId;
        private String nowPassword; // 현재 비밀번호
        private String newPassword; // 바꿀 비밀번호
        private String passwordConfirm; // 비밀번호 확인 newPassword값과 같아야 한다.
    }

    // 비밀번호 까먹었을때 비밀번호 초기화?
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ResetPassword {
        private long memberId;
        private String loginId;
        private String email;
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
        private LocalDateTime createdTime;
        private LocalDateTime modifiedTime;
        private String createdBy;
        private String modifiedBy;
        private String departmentType;
        private String factoryCode;
    }
}
