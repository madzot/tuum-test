package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class InsufficientFundsException extends ApplicationException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
