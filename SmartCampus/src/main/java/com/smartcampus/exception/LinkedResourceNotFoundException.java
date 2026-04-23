package com.smartcampus.exception;

/**
 *
 * @author w2024087
 */

/**
 * Thrown when a request references a resource that doesn't exist in the system
 *
 * Ex.: POST /sensors with a roomId that doesn't match any room
 * The payload is a valid JSON, but it points to something
 * that isn't there (which is what 422 Unprocessable Entity is for)
 * It's more specific than 404 since 404 would suggest the requested URL itself
 * is missing
 *
 * The resourceType ("Room", "Sensor", etc.) and resourceId fields are kept
 * as separate properties so the mapper can build a structured JSON response
 * without parsing the message string.
 */

public class LinkedResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        // Call the RuntimeException constructor that accepts a message (retrieved via ex.getMessage())
        super("Resource type" + resourceType + " with id '" + resourceId + "' does not exist");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

}