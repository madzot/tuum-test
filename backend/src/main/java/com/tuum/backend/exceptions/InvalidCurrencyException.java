package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class InvalidCurrencyException extends ApplicationException {
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
