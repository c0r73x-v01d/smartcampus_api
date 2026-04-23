package com.smartcampus;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 *
 * @author w2024087
 */

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Jersey auto-discovers resource classes via package scanning in Main.java
}
