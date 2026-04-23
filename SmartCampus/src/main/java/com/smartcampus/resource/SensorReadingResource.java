package com.smartcampus.resource;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource for managing the historical readings of a specific sensor
 *
 * Instances in this class are created by SensorResource via a sub-resource locator method
 * at /sensors/{sensorId}/readings
 * The parent resource passes the sensorId to the constructor, so this class always operates in the context of one sensor
 *
 * Keeps the parent class focused on sensor
 * CRUD while delegating reading-specific logic here
 */

public class SensorReadingResource {

    // The ID of the sensor this sub-resource is bound to
    private String sensorId;
    // Reference to the shared data store (singleton)
    private DataStore dataStore;

    // Invoked by SensorResource.getReadings() when a request matches
    // --> /sensors/{sensorId}/readings
    // The sensorId is extracted from the URL by the parent's @PathParam and passed here
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
        this.dataStore = DataStore.getInstance();
    }

    // Returns the full reading history for this sensor
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getReadings() {
        // Validate that the parent sensor exists before returning anything.
        if (!dataStore.getSensors().containsKey(sensorId)) {
            // Returns 404 if the sensor itself does not exist,
            // so clients can distinguish "sensor doesn't exist" (404)
            // from "sensor exists but has no readings yet" (200 with empty array).
            throw new NotFoundException();
        }
        // getOrDefault returns an empty list if no readings have been
        // recorded yet for this sensor
        return dataStore.getReadings().getOrDefault(sensorId, new ArrayList<>());
    }

    /**
     * Appends a new reading to this sensor's history and updates the sensor's
     * currentValue as a side effect (Part 4.2)
     *
     * The client only supplies the "value" field. The server generates the
     * reading's id (UUID) and timestamp (current ms)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensors().get(sensorId);

        // 404 if the sensor doesn't exist
        if (sensor == null) {
            throw new NotFoundException();
        }

        // 403 if the sensor cannot physically produce readings right now
        // MAINTENANCE is required by the spec; OFFLINE is added here because
        // a disconnected sensor clearly cannot report data either
        String status = sensor.getStatus();
        if ("MAINTENANCE".equals(status) || "OFFLINE".equals(status)) {
            throw new SensorUnavailableException(sensorId, status);
        }

        // Server-generated fields (unique IDs + accurate timestamps)
        // regardless of what the client sent in these fields
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // computeIfAbsent creates a new empty list for this sensor on the first
        // reading, then returns it on subsequent calls.
        // Ex.: if we write List<SensorReading> readings = dataStore.getReadings().get(sensorId);
        // and then try readings.add(reading);
        // get(sensorId) will return null because there is no value for this key,
        // and .add(...) throws NPE because you cannot call any method on a null reference
        // So if the key is missing, it creates a new empty ArrayList, stores it in the map under sensorId, and returns it,
        // so .add(...) always has a real list to work with
        List<SensorReading> readings = dataStore.getReadings().computeIfAbsent(sensorId, k -> new ArrayList<>());
        readings.add(reading);

        // Keep the parent sensor's currentValue in sync with
        // the most recent reading so clients hitting GET /sensors/{id}
        // always see the latest measurement without fetching the full history
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

}
