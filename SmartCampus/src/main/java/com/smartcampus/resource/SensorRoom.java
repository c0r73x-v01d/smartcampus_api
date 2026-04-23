package com.smartcampus.resource;

/**
 *
 * @author w2024087
 */

import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.ValidationException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * JAX-RS resource for managing rooms at /api/v1/rooms
 * Supports listing, fetching, creating, and deleting rooms
 */

@Path("/rooms")
public class SensorRoom {
    private DataStore dataStore = DataStore.getInstance();

    /**
     * Returns all rooms in the system
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.getRooms().values());
    }

    /**
     * Returns one room by its ID, or 404 if it doesn't exist
     */
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            throw new NotFoundException();
        }

        return room;
    }

    /**
     * Creates a new room
     * Validates that id, name and capacity are sensible,
     * otherwise returns 400 Bad Request via ValidationExceptionMapper
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            throw new ValidationException("id", "Room id is required");
        }

        if (room.getName() == null || room.getName().isBlank()) {
            throw new ValidationException("name", "Room name is required");
        }

        // A room with zero or negative capacity makes no sense
        if (room.getCapacity() <= 0) {
            throw new ValidationException("capacity", "Room capacity must be more than 0");
        }

        // Check for duplicate ID
        if (dataStore.getRooms().containsKey(room.getId())) {
            throw new DuplicateResourceException("Room", room.getId());
        }

        dataStore.getRooms().put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    /**
     * Deletes a room. Blocks the deletion if the room still has sensors
     * assigned which prevents creating orphaned sensor references.
     *
     * Returns 404 if the room doesn't exist,
     * or 409 Conflict via RoomNotEmptyExceptionMapper if the room is not empty
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);

        // If the room is already gone, treat as success
        if (room == null) {
            return Response.noContent().build();
        }

        // Block deletion of rooms that still contain sensors
        // The client must delete or reassign those sensors first
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        dataStore.getRooms().remove(roomId);

        return Response.noContent().build();

    }

}