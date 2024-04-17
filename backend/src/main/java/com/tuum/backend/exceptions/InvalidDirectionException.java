package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class InvalidDirectionException extends ApplicationException {
    public InvalidDirectionException(String message) {
        super(message);
    }
}
