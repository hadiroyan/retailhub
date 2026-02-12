package org.hadiroyan.retailhub.exception;

public class ResourceNotFoundException extends NotFoundException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource, id);
    }
}
