package org.manu.jobhunt.application.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.manu.jobhunt.application.common.exception.InvalidFieldValueException;
import org.manu.jobhunt.application.common.exception.RequiredFieldMissingException;

/**
 * Generic field checks shared by every feature. Each one reports the offending field
 * by name, so the client gets a message like "company is required".
 * <p>
 * Every free-text field has a maximum length, so no request can store unbounded data.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validations {

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldMissingException(fieldName);
        }
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new RequiredFieldMissingException(fieldName);
        }
    }

    /** A missing value passes: whether it's required is checked separately. */
    public static void requireMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidFieldValueException(fieldName, "must be at most %d characters".formatted(maxLength));
        }
    }

    /** A missing value passes: whether it's required is checked separately. */
    public static void requireBetween(Short value, int min, int max, String fieldName) {
        if (value != null && (value < min || value > max)) {
            throw new InvalidFieldValueException(fieldName, "must be between %d and %d".formatted(min, max));
        }
    }

    /** Refuses anything but http(s), such as javascript: links. A missing value passes. */
    public static void requireWebAddress(String value, String fieldName) {
        if (value != null && !value.matches("(?i)^https?://.+")) {
            throw new InvalidFieldValueException(fieldName, "must start with http:// or https://");
        }
    }
}
