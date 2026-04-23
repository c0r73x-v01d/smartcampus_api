package com.smartcampus.mapper;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.RoomNotEmptyException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "ROOM_NOT_EMPTY");
        body.put("message", ex.getMessage());
        body.put("roomId", ex.getRoomId());
        body.put("sensorCount", ex.getSensorCount());

        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
