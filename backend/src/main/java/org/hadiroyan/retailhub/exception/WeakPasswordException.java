package org.hadiroyan.retailhub.exception;

public class WeakPasswordException extends ValidationException {
    public WeakPasswordException() {
        super("Password does not meet security requirements");
    }

    public WeakPasswordException(String requirements) {
        super("Password does not meet security requirements: " + requirements);
    }
}
