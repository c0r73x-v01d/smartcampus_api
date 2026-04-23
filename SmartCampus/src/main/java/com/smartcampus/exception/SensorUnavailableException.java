package com.smartcampus.exception;

/**
 *
 * @author w2024087
 */

/**
 * Thrown when a client tries to submit a reading for a sensor that is not
 * currently accepting data (i.e. its status is MAINTENANCE or OFFLINE)
 */

public class SensorUnavailableException extends RuntimeException {
    private final String sensorId;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor " + sensorId + " is unavailable (status: " + status + ")");
        this.sensorId = sensorId;
        this.status = status;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getStatus() {
        return status;
    }
}