package com.quotation.domain.exception;

public class PasswordValidationException extends RuntimeException {

    public PasswordValidationException() {
        super("Password must be at least 8 characters with 1 uppercase, 1 number, and 1 special character");
    }
}
