package com.smartcampus.mapper;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.SensorUnavailableException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "SENSOR_UNAVAILABLE");
        body.put("message", ex.getMessage());
        body.put("sensorId", ex.getSensorId());
        body.put("status", ex.getStatus());

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}