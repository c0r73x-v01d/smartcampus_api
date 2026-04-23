package com.smartcampus.exception;

/**
 *
 * @author w2024087
 */

/**
 * Thrown when a POST tries to create a resource whose ID is already taken
 * Mapped to 409 Conflict
 */

public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;

    public DuplicateResourceException(String resourceType, String resourceId) {
        super(resourceType + " with id '" + resourceId + "' already exists");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
}