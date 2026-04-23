package com.smartcampus.repository;

/**
 *
 * @author w2024087
 */

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataSeeder {

    public static void seed() {
        DataStore store = DataStore.getInstance();

        // Rooms
        Room lib301 = new Room("LIB-301", "Library Quiet Study", 30);
        Room csci205 = new Room("CSCI-205", "Computer Science Lab", 50);
        Room aud100 = new Room("AUD-100", "Main Auditorium", 200);
        Room stor001 = new Room("STOR-001", "Storage Room", 10);

        store.getRooms().put(lib301.getId(), lib301);
        store.getRooms().put(csci205.getId(), csci205);
        store.getRooms().put(aud100.getId(), aud100);
        store.getRooms().put(stor001.getId(), stor001);

        // Sensors
        createSensor(store, "TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        createSensor(store, "TEMP-002", "Temperature", "ACTIVE", 23.0, "CSCI-205");
        createSensor(store, "CO2-001", "CO2", "ACTIVE", 420.0, "CSCI-205");
        createSensor(store, "CO2-002", "CO2", "MAINTENANCE", 0.0, "AUD-100");
        createSensor(store, "OCC-001", "Occupancy", "ACTIVE", 45.0, "AUD-100");
        createSensor(store, "LIGHT-001", "Light", "OFFLINE", 0.0, "LIB-301");

        // Readings
        seedReadings(store, "TEMP-001", new double[]{21.3, 21.5, 21.7, 21.5});
        seedReadings(store, "TEMP-002", new double[]{22.8, 23.0, 23.1, 23.0});
        seedReadings(store, "CO2-001", new double[]{410.0, 415.0, 420.0});
        seedReadings(store, "OCC-001", new double[]{40.0, 42.0, 45.0});

        System.out.println("DataStore seeded: " + store.getRooms().size() + " rooms, " + store.getSensors().size() + " sensors, " + totalReadings(store) + " readings");
    }

    private static void createSensor(DataStore store, String id, String type, String status, double currentValue, String roomId) {
        Sensor sensor = new Sensor(id, type, status, currentValue, roomId);
        store.getSensors().put(id, sensor);
        store.getRooms().get(roomId).getSensorIds().add(id);
    }

    private static void seedReadings(DataStore store, String sensorId, double[] values) {
        List<SensorReading> list = new ArrayList<>();
        long now = System.currentTimeMillis();

        // For each value in the array create a reading with the current time and a random UUID, and add it to the list
        for (double value : values) {
            list.add(new SensorReading(UUID.randomUUID().toString(), now, value));
        }

        store.getReadings().put(sensorId, list);
    }

    private static int totalReadings(DataStore store) {
        return store.getReadings().values().stream().mapToInt(List::size).sum();
    }
}