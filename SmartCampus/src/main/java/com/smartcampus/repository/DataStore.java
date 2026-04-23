package com.smartcampus.repository;

/**
 *
 * @author w2024087
 */

import java.util.List;
import java.util.Map;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    // Singleton pattern
    // Creates an object which will exist throughout the entire lifetime of the application
    private static final DataStore instance = new DataStore();

    private DataStore() {}

    public static DataStore getInstance() {
        return instance;
    }

    // Data storage

    // Rooms
    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Map<String, Room> getRooms() {
        return rooms;
    }

    // Sensors
    private Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    // Sensor data
    private Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    public Map<String, List<SensorReading>> getReadings() {
        return readings;
    }
}
