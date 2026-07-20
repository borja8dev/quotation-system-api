package com.quotation.infrastructure.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    private static final String PATTERN =
            "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";

    public boolean isValid(String password) {
        if (password == null) return false;
        return password.matches(PATTERN);
    }
}
