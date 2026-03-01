package com.post_hub.utils_service.model.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}