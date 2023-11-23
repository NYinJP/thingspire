package com.thingspire.thingspire.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class LoginDTO {
    private String loginId;
    private String password;
}
