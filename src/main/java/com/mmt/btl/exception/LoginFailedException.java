package com.mmt.btl.exception;

public class LoginFailedException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Login failed...";

    public LoginFailedException() {
        super(DEFAULT_MESSAGE);
    }

    public LoginFailedException(String message) {
        super(message);
    }
}