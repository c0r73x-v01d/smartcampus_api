package com.smartcampus.mapper;

/**
 *
 * @author w2024087
 */

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

/**
 * Logs every incoming HTTP request and every outgoing response.
 *
 * Runs automatically on every endpoint without any changes to the resource
 * classes themselves (i.e. adding logger.info(...) inside each resource method
 * Implements both interfaces so one class handles both directions:
 * - ContainerRequestFilter -> triggered before the resource method runs,
 *   logs method + URI of the incoming request
 * - ContainerResponseFilter -> triggered after the resource method finishes,
 *   logs the final HTTP status code paired with the request info
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Standard Java logger scoped to this class. Output goes to the server
    // console where the app was started from.
    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Request filter fires before the resource method runs
     * Logs an arrow pointing into the server with the HTTP method and full URI
     * Ex.: --> GET http://localhost:8080/api/v1/rooms/LIB-301
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        logger.info(String.format("--> %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    /**
     * Response filter fires after the resource method (or any exception
     * mapper) has produced a response, but before it's sent to the client.
     * Logs an arrow pointing out of the server with the final status code.
     * Ex.: <-- 409 DELETE http://localhost:8080/api/v1/rooms/LIB-301
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        logger.info(String.format("<-- %d %s %s",
                responseContext.getStatus(),
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }
}