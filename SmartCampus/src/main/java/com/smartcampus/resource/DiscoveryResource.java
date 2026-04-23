package com.smartcampus.resource;

/**
 *
 * @author w2024087
 */

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns API metadata including version, admin contact, and a map of
 * primary resource collections.
 * The @Path("/") maps this resource to the application's base URI.
 * Combined with @ApplicationPath("/api/v1") on SmartCampusApplication,
 * the full URL: GET /api/v1
 */

@Path("/")
public class DiscoveryResource {

    // Handle GET requests to /api/v1 and returns API metadata as JSON
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getApiInfo() {
        // Top level object containing all API metadata
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0");
        info.put("admin", "admin@smartcampus.ac.uk");

        // Nested map listing the primary resource collections and their paths
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        info.put("resources", resources);

        return info;
    }
}