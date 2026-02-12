package org.hadiroyan.retailhub.exception;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String resource, String field, Object value) {
        super(String.format("%s with %s '%s' already exists", resource, field, value));
    }
}
