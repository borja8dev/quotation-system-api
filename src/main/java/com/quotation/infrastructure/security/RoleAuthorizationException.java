package com.quotation.infrastructure.security;

public class RoleAuthorizationException extends RuntimeException {

    public RoleAuthorizationException(String message) {
        super(message);
    }
}
