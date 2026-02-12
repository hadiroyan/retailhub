package org.hadiroyan.retailhub.exception;

public class AccountLockedException extends UnauthorizedException {
    private final String email;
    private final int lockDurationMinutes;

    public AccountLockedException(String email, int lockDurationMinutes) {
        super(String.format(
                "Account temporarily locked due to multiple failed login attempts. Try again in %d minutes.",
                lockDurationMinutes));
        this.email = email;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    public String getEmail() {
        return email;
    }

    public int getLockDurationMinutes() {
        return lockDurationMinutes;
    }
}