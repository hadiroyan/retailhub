package org.hadiroyan.retailhub.exception;

public class InvalidTenantAccessException extends ForbiddenException {
    public InvalidTenantAccessException() {
        super("You don't have access to this tenant's resources");
    }

    public InvalidTenantAccessException(Long tenantId) {
        super(String.format("You don't have access to tenant %d", tenantId));
    }
}
