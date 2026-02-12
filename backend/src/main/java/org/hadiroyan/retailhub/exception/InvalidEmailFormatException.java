package org.hadiroyan.retailhub.exception;

public class InvalidEmailFormatException extends ValidationException {
    public InvalidEmailFormatException(String email) {
        super(String.format("Invalid email format: %s", email));
    }
}
