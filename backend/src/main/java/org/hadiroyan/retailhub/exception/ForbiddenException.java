package org.hadiroyan.retailhub.exception;

public class ForbiddenException extends AppException{
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String resource, String action) {
        super(String.format("You don't have permission to %s %s", action, resource));
    }
}
