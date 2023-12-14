package com.thingspire.thingspire.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class LoginDTO {
    @NotNull
    private String loginId;
    @NotNull
    private String password;
}
