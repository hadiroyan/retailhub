package org.hadiroyan.retailhub.exception;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String message) {
        super(String.format("%s: %s", field, message));
    }
}
