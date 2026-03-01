package com.post_hub.utils_service.model.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.security.sasl.AuthenticationException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthException extends AuthenticationException {

    public AuthException(String message) {
        super(message);
    }

}
