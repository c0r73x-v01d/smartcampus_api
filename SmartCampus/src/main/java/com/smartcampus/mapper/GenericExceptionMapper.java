package com.smartcampus.mapper;

/**
 *
 * @author w2024087
 */

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" for any exception that no other mapper catches
 *
 * Without it an uncaught exception (e.g. a NullPointerException from
 * a bug in the code) would leak a raw Java stack trace to the client. That's
 * a security risk since stack traces reveal internal class names, file paths,
 * library versions, and structural details that attackers can use to find
 * vulnerabilities
 *
 * Instead, we catch everything, log the full details server-side for
 * debugging, and return a generic 500 response to the client
 */

// Used to record full exception details (including stack trace) server-side so we can
// investigate bugs without exposing internals to the client
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // WebApplicationException covers all deliberate HTTP
        // responses from JAX-RS including NotFoundException (404),
        // BadRequestException (400), and the ones our own mappers produce
        // Because this mapper is registered for Throwable (the root of all
        // exceptions), it would otherwise catch these too and incorrectly
        // convert them all into 500 Internal Server Error. That would
        // break every 404 and 400 response
        // So if the exception already carries a fully formed Response,
        // just pass it through unchanged
        //
        // Pattern taken from here:
        // https://stackoverflow.com/questions/13716793/jersey-how-to-register-a-exceptionmapper-that-omits-some-subclasses
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        // If we get here it's a genuinely unexpected exception, i.e. the kind
        // a programmer didn't anticipate (NPE, IndexOutOfBoundsException,
        // ArithmeticException, etc.).
        // Log the full details server-side
        // (Level.SEVERE + the exception itself -> full stack trace in logs)
        logger.log(Level.SEVERE, "Unhandled exception", ex);

        // Return a deliberately vague body to the client.
        // Do NOT include ex.getMessage() because  it might contain file paths
        // or other internals
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", "An unexpected error occurred");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}