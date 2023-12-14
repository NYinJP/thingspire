package com.thingspire.thingspire.exception;

import lombok.Getter;

public class PasswordInputException extends RuntimeException{
    @Getter
    private ExceptionCode exceptionCode;

    public PasswordInputException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
