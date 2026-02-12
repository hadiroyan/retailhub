package org.hadiroyan.retailhub.exception;

public class AccountDisabledException extends UnauthorizedException {
    public AccountDisabledException() {
        super("Your account has been disabled. Please contact support.");
    }

    public AccountDisabledException(String reason) {
        super("Your account has been disabled: " + reason);
    }
}
