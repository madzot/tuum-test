package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class CustomerNotFoundException extends ApplicationException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
