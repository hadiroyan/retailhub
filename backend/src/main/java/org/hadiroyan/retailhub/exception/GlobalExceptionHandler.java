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
            LOG.warnf("action=AUTH_ACCOUNT_LOCKED email=%s retryAfter=%d message=%s",
                    ale.getEmail(),
                    ale.getLockDurationMinutes(),
                    ale.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(401, exception.getMessage()))
                    .header("Retry-After", ale.getLockDurationMinutes() * 60) // in seconds
                    .build();
        }

        if (exception instanceof AccountDisabledException) {
            LOG.warnf("action=AUTH_ACCOUNT_DISABLED message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.unauthorized(exception.getMessage()))
                    .build();
        }

        if (exception instanceof InvalidTokenException) {
            LOG.debugf("action=AUTH_INVALID_TOKEN message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.unauthorized(exception.getMessage()))
                    .build();
        }

        if (exception instanceof UnauthorizedException) {
            LOG.debugf("action=AUTH_UNAUTHORIZED message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.unauthorized(exception.getMessage()))
                    .build();
        }

        // Forbidden (403)
        if (exception instanceof InvalidTenantAccessException) {
            LOG.warnf("action=FORBIDDEN_INVALID_TENANT message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(403, exception.getMessage()))
                    .build();
        }

        if (exception instanceof ForbiddenException) {
            LOG.warnf("action=FORBIDDEN_ACCESS_DENIED message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error(403, exception.getMessage()))
                    .build();
        }

        // Not found errors (404)
        if (exception instanceof UserNotFoundException) {
            LOG.debugf("action=USER_NOT_FOUND message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        if (exception instanceof RoleNotFoundException) {
            LOG.errorf("action=ROLE_NOT_FOUND message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        if (exception instanceof TenantNotFoundException) {
            LOG.warnf("action=TENANT_NOT_FOUND message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        if (exception instanceof NotFoundException) {
            LOG.debugf("action=RESOURCE_NOT_FOUND message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.notFound(exception.getMessage()))
                    .build();
        }

        // Validation Errors (400)
        if (exception instanceof InvalidEmailFormatException ||
                exception instanceof WeakPasswordException ||
                exception instanceof ValidationException ||
                exception instanceof BadRequestException) {

            LOG.debugf("action=BAD_REQUEST_VALIDATION message=%s", exception.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.badRequest(exception.getMessage()))
                    .build();
        }

        if (exception instanceof BadRequestException) {
            LOG.debugf("action=BAD_REQUEST message=%s", exception.getMessage());
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

            LOG.debugf("action=BAD_REQUEST_CONSTRAINT message=%s", message);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.badRequest(message))
                    .build();
        }

        // Conflict (409 Conflict)
        if (exception instanceof EmailAlreadyExistsException || exception instanceof ConflictException) {
            LOG.warnf("action=CONFLICT message=%s", exception.getMessage());
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(ApiResponse.error(409, exception.getMessage()))
                    .build();
        }

        // Rate Limiting (429)
        if (exception instanceof RateLimitExceededException) {
            RateLimitExceededException rle = (RateLimitExceededException) exception;
            LOG.warnf("action=RATE_LIMIT_EXCEEDED retryAfter=%d message=%s",
                    rle.getRetryAfterSeconds(),
                    rle.getMessage());

            return Response
                    .status(429) // Too Many Requests
                    .entity(ApiResponse.error(429, exception.getMessage()))
                    .header("Retry-After", rle.getRetryAfterSeconds())
                    .build();
        }

        // Data Integrity
        if (exception instanceof DataIntegrityException) {
            LOG.errorf(exception,
                    "action=DATA_INTEGRITY_ERROR message=%s",
                    exception.getMessage());
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error(500, "Data integrity error occurred"))
                    .build();
        }

        // JAX-RS exceptions
        if (exception instanceof jakarta.ws.rs.WebApplicationException) {
            jakarta.ws.rs.WebApplicationException wae = (jakarta.ws.rs.WebApplicationException) exception;
            int status = wae.getResponse().getStatus();
            LOG.warnf("action=WEB_EXCEPTION status=%d message=%s",
                    status,
                    exception.getMessage());
            return Response
                    .status(status)
                    .entity(ApiResponse.error(status, exception.getMessage()))
                    .build();
        }

        // Default: Internal server error (500)
        LOG.errorf(exception,
                "action=UNHANDLED_EXCEPTION message=%s",
                exception.getMessage());
        String message = "An unexpected error occurred. Please try again later.";

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiResponse.error(500, message))
                .build();
    }
}
