package com.thingspire.thingspire.auth.jwt.dto;

import lombok.Getter;

@Getter
public class LoginDTO {
    private String loginId;
    private String password;
}
