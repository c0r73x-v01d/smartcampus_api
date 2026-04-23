package com.smartcampus.exception;

/**
 *
 * @author w2024087
 */

/**
 * Thrown when a POST request body fails field-level validation
 * E.g. a required field is missing, blank, or has an invalid value
 *
 * Maps to 400 Bad Request, which is the standard response for a
 * syntactically valid but semantically broken payload
 *
 * This exception is about the fields themselves being wrong (400). Validation happens
 * first since there's no point checking if a room exists when the roomId
 * field is null
 */
public class ValidationException extends RuntimeException {
    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}