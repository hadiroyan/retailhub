package org.hadiroyan.retailhub.utils;

import java.util.regex.Pattern;

import org.hadiroyan.retailhub.exception.InvalidEmailFormatException;
import org.hadiroyan.retailhub.exception.ValidationException;
import org.hadiroyan.retailhub.exception.WeakPasswordException;

public class ValidationUtils {

    // Email validation pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Password policy constants
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    // Name validation constants
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 255;

    // Email length limit
    private static final int MAX_EMAIL_LENGTH = 255;

    private ValidationUtils() {
    }

    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }

        return email.trim().toLowerCase();
    }

    public static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailFormatException(email);
        }

        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new ValidationException(
                    String.format("Email is too long (max %d charachters", MAX_EMAIL_LENGTH));
        }
    }

    public static String normalizeAndValidateEmail(String email) {
        String normalized = normalizeEmail(email);
        validateEmail(normalized);
        return normalized;
    }

    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException(
                    String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH));
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException(
                    String.format("Password is too long (max %d characters)", MAX_PASSWORD_LENGTH));
        }

        // Optional: Add complexity requirements here
        // validatePasswordComplexity(password);
    }

    @SuppressWarnings("unused")
    private static void validatePasswordComplexity(String password) {
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        if (!hasUpperCase) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }
        if (!hasLowerCase) {
            throw new WeakPasswordException("Password must contain at least one lowercase letter");
        }
        if (!hasDigit) {
            throw new WeakPasswordException("Password must contain at least one number");
        }
        if (!hasSpecialChar) {
            throw new WeakPasswordException("Password must contain at least one special character");
        }
    }

    public static void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Full name is required");
        }

        String trimmed = fullName.trim();

        if (trimmed.length() < MIN_NAME_LENGTH) {
            throw new ValidationException(
                    String.format("Full name must be at least %d characters long", MIN_NAME_LENGTH));
        }

        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(
                    String.format("Full name is too long (max %d characters)", MAX_NAME_LENGTH));
        }
    }

    public static String normalizeFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        return fullName.trim();
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    String.format("%s is required", fieldName));
        }
    }

    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            return; // Use validateNotBlank first if null check needed
        }

        int length = value.length();

        if (length < minLength) {
            throw new ValidationException(
                    String.format("%s must be at least %d characters long", fieldName, minLength));
        }

        if (length > maxLength) {
            throw new ValidationException(
                    String.format("%s is too long (max %d characters)", fieldName, maxLength));
        }
    }

}
