package com.tuum.backend.exceptions;

import com.tuum.backend.errors.ApplicationException;

public class DescriptionMissingException extends ApplicationException {
    public DescriptionMissingException(String message) {
        super(message);
    }
}
