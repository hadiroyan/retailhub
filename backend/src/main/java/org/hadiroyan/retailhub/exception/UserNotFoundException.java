package org.hadiroyan.retailhub.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super("User", userId);
    }

    public UserNotFoundException(String email) {
        super(String.format("User with email '%s' not found", email));
    }
}
