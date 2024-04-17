package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class InvalidAmountException extends ApplicationException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
