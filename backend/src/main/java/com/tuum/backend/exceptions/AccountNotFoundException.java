package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class AccountNotFoundException extends ApplicationException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
