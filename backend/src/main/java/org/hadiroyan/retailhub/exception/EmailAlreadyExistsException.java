package org.hadiroyan.retailhub.exception;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' is already registered", email));
    }
}
