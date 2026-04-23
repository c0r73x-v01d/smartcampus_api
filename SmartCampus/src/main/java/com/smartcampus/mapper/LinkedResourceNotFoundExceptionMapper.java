package com.smartcampus.mapper;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.LinkedResourceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "LINKED_RESOURCE_NOT_FOUND");
        body.put("message", ex.getMessage());
        body.put("resourceType", ex.getResourceType());
        body.put("resourceId", ex.getResourceId());

        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}