package com.thingspire.thingspire.exception;

public class UsernameAlreadyUsedException extends RuntimeException{
    private static final long SerialVersionUID = 1L;

    public UsernameAlreadyUsedException(){
        super("Login name already used!");
    }
}
