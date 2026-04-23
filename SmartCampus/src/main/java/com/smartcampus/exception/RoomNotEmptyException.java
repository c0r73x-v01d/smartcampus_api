package com.smartcampus.exception;

/**
 *
 * @author w2024087
 */

/**
 * Thrown when a client tries to delete a room that still has sensors assigned
 */

public class RoomNotEmptyException extends RuntimeException {
    private final String roomId;
    private final int sensorCount;

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room " + roomId + " cannot be deleted: " + sensorCount + " sensor(s) still assigned");
        this.roomId = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getSensorCount() {
        return sensorCount;
    }
}
