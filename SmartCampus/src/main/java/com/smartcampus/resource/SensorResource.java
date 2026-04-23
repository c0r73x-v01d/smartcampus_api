package com.smartcampus.resource;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ValidationException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles sensor CRUD operations and delegates reading-related requests
 * (/{sensorId}/readings) to SensorReadingResource via a sub-resource locator
 */

@Path("/sensors")
public class SensorResource {

    // Shared singleton store
    private DataStore dataStore = DataStore.getInstance();

    /**
     * Returns all sensors, optionally filtered by type
     *
     * The ?type= query parameter is optional: if absent, all sensors are
     * returned; if present (e.g. ?type=CO2), only matching sensors are returned
     * Filtering via @QueryParam is preferred over path segments
     * like /sensors/type/CO2 because a sensor isn't "of type CO2" in the URL hierarchy sense,
     * CO2 is just one criterion by which we filter the collection
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        // Snapshot of all sensors currently in the store
        List<Sensor> allSensors = new ArrayList<>(dataStore.getSensors().values());

        // If no filter requested, return the full list
        if (type == null) {
            return allSensors;
        }

        // Filter in-memory
        List<Sensor> filtered = new ArrayList<>();
        for (Sensor sensor : allSensors) {
            if (sensor.getType().equals(type)) {
                filtered.add(sensor);
            }
        }

        return filtered;

    }

    /**
     * Registers a new sensor in the system
     *
     * Validation happens in two stages:
     * 1. Field-level checks (id, type, status, roomId non-empty and valid)
     *    which return 400 Bad Request via ValidationExceptionMapper
     * 2. Linked-resource check ("does the referenced room actually exist?")
     *    which returns 422 Unprocessable Entity via LinkedResourceNotFoundExceptionMapper
     *
     * We only check if the room exists after we've confirmed roomId is present in the payload
     * since asking "does null exist in the rooms map?" is meaningless
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {

        // Stage 1: field validation (400 Bad Request on failure)
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            throw new ValidationException("id", "Sensor id is required");
        }

        if (sensor.getType() == null || sensor.getType().isBlank()) {
            throw new ValidationException("type", "Sensor type is required");
        }

        // Status is a closed set of three values
        // Any other string (including null) is a client error
        String status = sensor.getStatus();
        if (!"ACTIVE".equals(status) && !"MAINTENANCE".equals(status) && !"OFFLINE".equals(status)) {
            throw new ValidationException("status", "Sensor status must be one of: ACTIVE, MAINTENANCE, OFFLINE");
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new ValidationException("roomId", "Sensor roomId is required");
        }

        // Check for duplicate ID
        if (dataStore.getSensors().containsKey(sensor.getId())) {
            throw new DuplicateResourceException("Sensor", sensor.getId());
        }

        // Stage 2: referential integrity (422 on failure)

        // The sensor must belong to a room that actually exists,
        // otherwise we'd create an orphan sensor
        if (!dataStore.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Update the room's sensorIds list and the sensors map
        // Keeping them in sync is essential so that DELETE /rooms/{id} can correctly detect non-empty rooms
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        dataStore.getSensors().put(sensor.getId(), sensor);

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    /**
     * Returns a single sensor by its ID, or 404 if it does not exist
     */
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);

        // Explicit 404 instead of letting the serializer emit JSON "null"
        // the client needs to distinguish "sensor exists" from "sensor missing"
        if (sensor == null) {
            throw new NotFoundException();
        }

        return sensor;
    }

    /**
     * Delegates /sensors/{sensorId}/readings to SensorReadingResource
     * The sensorId from the URL is passed into the sub-resource so it
     * knows which sensor it's operating on
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadings(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    /**
     * Decommissions a sensor (with cascading cleanup)
     *
     * Not required by the coursework spec, but logically stems from it
     * Needed to make DELETE /rooms/{id} usable in practice
     * since a room cannot be deleted while it has sensors,
     * so we need a way to remove those sensors first
     *
     * The cleanup is performed in a specific order to avoid leaving
     * orphaned data if something fails mid-operation:
     * 1. Remove the sensor's ID from its parent room's sensorIds list
     * 2. Delete all historical readings for this sensor
     * 3. Finally, remove the sensor itself from the sensors map
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);

        // If the sensor is already gone, treat as success
        if (sensor == null) {
            return Response.noContent().build();
        }

        // Remove sensor's reference from its parent room
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        // Break the link from the parent room
        room.getSensorIds().remove(sensorId);

        // Delete all historical readings for this sensor
        dataStore.getReadings().remove(sensorId);

        // Finally remove the sensor itself
        dataStore.getSensors().remove(sensorId);

        return Response.noContent().build();   // 204
    }
}
