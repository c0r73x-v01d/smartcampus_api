package com.smartcampus;

/**
 *
 * @author w2024087
 */

import com.smartcampus.repository.DataSeeder;

import jakarta.ws.rs.ApplicationPath;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {
    
    public static void main(String[] args) throws Exception {
        // Call seed() from the DataSeeder class to populate DataStore with test data (rooms, sensors, readings)
        DataSeeder.seed();

        // Scans com.smartcampus package and find all classes with JAX-RS annotations
        ResourceConfig config = new ResourceConfig().packages("com.smartcampus");

        String appPath = SmartCampusApplication.class.getAnnotation(ApplicationPath.class).value();
        URI baseUri = URI.create("http://localhost:8080" + appPath + "/");

        // Start the Grizzly HTTP server on port 8080
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
        
        System.out.println("Smart Campus API started: http://localhost:8080");
        System.out.println("Press Enter to stop the server...");
        System.in.read();
        server.shutdownNow();
    }
}
