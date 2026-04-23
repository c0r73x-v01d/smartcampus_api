package com.smartcampus.mapper;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.ValidationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_FAILED");
        body.put("field", ex.getField());
        body.put("message", ex.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}