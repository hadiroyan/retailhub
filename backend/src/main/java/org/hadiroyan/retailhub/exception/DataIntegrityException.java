package org.hadiroyan.retailhub.exception;

public class DataIntegrityException extends AppException {
    public DataIntegrityException(String message) {
        super(message);
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}
