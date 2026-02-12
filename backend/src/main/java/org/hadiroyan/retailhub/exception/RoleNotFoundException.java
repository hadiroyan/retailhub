package org.hadiroyan.retailhub.exception;

public class RoleNotFoundException extends NotFoundException {
    public RoleNotFoundException(String roleName) {
        super(String.format("Role '%s' not found in the system", roleName));
    }
}
