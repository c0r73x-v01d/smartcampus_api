package com.smartcampus.mapper;

import com.smartcampus.exception.DuplicateResourceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class DuplicateResourceExceptionMapper implements ExceptionMapper<DuplicateResourceException> {
    @Override
    public Response toResponse(DuplicateResourceException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "DUPLICATE_RESOURCE");
        body.put("message", ex.getMessage());
        body.put("resourceType", ex.getResourceType());
        body.put("resourceId", ex.getResourceId());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}