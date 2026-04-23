package com.smartcampus.model;

/**
 *
 * @author w2024087
 */

public class SensorReading {
    private String id;
    private long timestamp;
    private double value;
    
    // Empty constructor so that Jackson could create SensorReading object
    public SensorReading() { }
    
    //constructor
    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }
    
    // setters and getters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
}
