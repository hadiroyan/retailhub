package org.hadiroyan.retailhub.exception;

public class InvalidTokenException extends UnauthorizedException {
    public InvalidTokenException() {
        super("Invalid or expired authentication token");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
