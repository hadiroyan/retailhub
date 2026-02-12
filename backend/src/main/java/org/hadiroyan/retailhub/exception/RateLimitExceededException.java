package org.hadiroyan.retailhub.exception;

public class RateLimitExceededException extends AppException {
    private final int retryAfterSeconds;

    public RateLimitExceededException(int retryAfterSeconds) {
        super(String.format("Rate limit exceeded. Please try again in %d seconds.", retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
