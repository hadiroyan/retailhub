package org.hadiroyan.retailhub.exception;

public class TenantNotFoundException extends NotFoundException {
    public TenantNotFoundException(Long tenantId) {
        super("Tenant", tenantId);
    }
}
