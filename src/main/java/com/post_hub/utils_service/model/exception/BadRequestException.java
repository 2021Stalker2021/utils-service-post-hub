package com.post_hub.utils_service.model.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}