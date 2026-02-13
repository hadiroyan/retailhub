package org.hadiroyan.retailhub.exception;

import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.jboss.logging.Logger;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        // Authentication errors (401)
        if (exception instanceof AccountLockedException) {
            AccountLockedException ale = (AccountLockedException) exception;
            LOG.warnf("Account locked: %s", ale.getEmail());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(401, exception.getMessage()))
                    .header("Retry-After", ale.getLockDurationMinutes() * 60) // in seconds
                    .build();
        }

        if (exception instanceof AccountDisabledException) {
            LOG.warnf("Disabled account login attempt: %s", exception.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.unauthorized(exception.getMessage()))
                    .build();
        }

        if (exception instanceof InvalidTokenException) {
            LOG.debugf("Invalid token: %s", exception.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.unauthorized(exception.getMessage()))
                    .build();
        }

        if (exception instanceof UnauthorizedException) {
            LOG.debugf("Unauthorized: %s", exception.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.unauthorized(exception.getMessage()))
                    .build();
        }

        // Forbidden (403)
        if (exception instanceof InvalidTenantAccessException) {
            LOG.warnf("Invalid tenant access: %s", exception.getMessage());
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(403, exception.getMessage()))
                    .build();
        }

        if (exception instanceof ForbiddenException) {
            LOG.warnf("Forbidden: %s", exception.getMessage());
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(403, exception.getMessage()))
                    .build();
        }

        // Not found errors (404)
        if (exception instanceof UserNotFoundException) {
            LOG.debugf("User not found: %s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        if (exception instanceof RoleNotFoundException) {
            LOG.errorf("Role not found (system config issue): %s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        if (exception instanceof TenantNotFoundException) {
            LOG.warnf("Tenant not found: %s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        if (exception instanceof NotFoundException) {
            LOG.debugf("Resource not found: %s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        // Validation Errors (400)
        if (exception instanceof InvalidEmailFormatException ||
                exception instanceof WeakPasswordException) {
            LOG.debugf("Validation error: %s", exception.getMessage());
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.badRequest(exception.getMessage()))
                    .build();
        }

        if (exception instanceof ValidationException) {
            LOG.debugf("Validation error: %s", exception.getMessage());
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.badRequest(exception.getMessage()))
                    .build();
        }

        if (exception instanceof BadRequestException) {
            LOG.debugf("Bad request: %s", exception.getMessage());
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.badRequest(exception.getMessage()))
                    .build();
        }

        // Bean validation errors (400)
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) exception;
            String message = cve.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .findFirst()
                    .orElse("Validation failed");

            LOG.debugf("Constraint violation: %s", message);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.badRequest(message))
                    .build();
        }

        // Conflict (409 Conflict)
        if (exception instanceof EmailAlreadyExistsException || exception instanceof ConflictException) {
            LOG.debugf("Conflict: %s", exception.getMessage());
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(ApiResponse.error(409, exception.getMessage()))
                    .build();
        }

        // Rate Limiting (429)
        if (exception instanceof RateLimitExceededException) {
            RateLimitExceededException rle = (RateLimitExceededException) exception;
            LOG.warnf("Rate limit exceeded: %s", exception.getMessage());
            return Response
                    .status(429) // Too Many Requests
                    .entity(ApiResponse.error(429, exception.getMessage()))
                    .header("Retry-After", rle.getRetryAfterSeconds())
                    .build();
        }

        // Data Integrity
        if (exception instanceof DataIntegrityException) {
            LOG.error("Data integrity violation", exception);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error(500, "Data integrity error occurred"))
                    .build();
        }

        // Default: Internal server error (500)
        LOG.error("Unhandled exception", exception);
        String message = "An unexpected error occurred. Please try again later.";

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error(500, message))
                .build();
    }
}
