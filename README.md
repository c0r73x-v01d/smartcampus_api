# Smart Campus API

A RESTful API for managing rooms and sensors across a university campus, built with JAX-RS (Jersey) on an embedded Grizzly HTTP server.

**Author:** Stanislau Mikhailouski
**Student ID:** w2024087
**Module:** 5COSC022W — Client-Server Architectures
**Video demonstration:** _uploaded directly to the BlackBoard submission link_

---
## 1. Overview

The Smart Campus API  facilities managers and automated building systems interact with campus infrastructure through a uniform REST interface. The system consists of three core entities:

- **Room** - a physical space with an ID, name, capacity, and the list of sensors installed in it.
- **Sensor** - a device (temperature, CO2, occupancy, light, etc.) installed in a specific room, with a status of `ACTIVE`, `MAINTENANCE`, or `OFFLINE`.
- **SensorReading** - a timestamped measurement recorded by a sensor.

Data is stored in memory using `ConcurrentHashMap` structures — no database is used, in line with the coursework constraints.

---

## 2. Architecture

### Technology stack

- **Java 17** 
- **JAX-RS (Jakarta REST)** via **Jersey**
- **Grizzly - embedded HTTP server
- **Jackson**
- **Maven** 

### Project structure

```
src/main/java/com/smartcampus/
├── Main.java                       // Entry point; starts Grizzly
├── SmartCampusApplication.java     // JAX-RS Application (@ApplicationPath)
├── model/                          // Main models
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── repository/                     // In-memory storage
│   ├── DataStore.java              // Singleton with ConcurrentHashMap fields
│   └── DataSeeder.java             // Populates the store on startup
├── resource/                       // JAX-RS resource classes
│   ├── DiscoveryResource.java      // GET /api/v1
│   ├── SensorRoom.java             // /rooms
│   ├── SensorResource.java         // /sensors
│   └── SensorReadingResource.java  // /sensors/{id}/readings (sub-resource)
├── exception/                      // Custom exceptions
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   ├── SensorUnavailableException.java
│   ├── DuplicateResourceException.java
│   └── ValidationException.java
└── mapper/                         // ExceptionMappers + filter
    ├── RoomNotEmptyExceptionMapper.java
    ├── LinkedResourceNotFoundExceptionMapper.java
    ├── SensorUnavailableExceptionMapper.java
    ├── DuplicateResourceExceptionMapper.java
    ├── ValidationExceptionMapper.java
    ├── GenericExceptionMapper.java
    └── LoggingFilter.java
```

### Endpoint map

| Method | Path                            | Description                                 |
| ------ | ------------------------------- | ------------------------------------------- |
| GET    | `/api/v1`                       | Discovery - API metadata and resource links |
| GET    | `/api/v1/rooms`                 | List all rooms                              |
| POST   | `/api/v1/rooms`                 | Create a new room                           |
| GET    | `/api/v1/rooms/{id}`            | Get a specific room                         |
| DELETE | `/api/v1/rooms/{id}`            | Delete a room (blocked if contains sensors) |
| GET    | `/api/v1/sensors`               | List all sensors (optional ?type= filter)   |
| POST   | `/api/v1/sensors`               | Create a new sensor                         |
| GET    | `/api/v1/sensors/{id}`          | Get a specific sensor                       |
| DELETE | `/api/v1/sensors/{id}`          | Delete a sensor (cascade readings deletion) |
| GET    | `/api/v1/sensors/{id}/readings` | Get reading history for a sensor            |
| POST   | `/api/v1/sensors/{id}/readings` | Add a new reading (updates `currentValue`)  |

---

## 3. Build & Run

### Prerequisites

- Java 17 or later
- Maven 3.6 or later

### Build

```bash
mvn clean package
```

### Run

```bash
mvn exec:java
```

or, after packaging:

```bash
java -cp target/smart-campus-1.0-SNAPSHOT.jar com.smartcampus.Main
```

On startup you should see:

```
DataStore seeded: 4 rooms, 6 sensors, 14 readings
Smart Campus API started: http://localhost:8080/api/v1
Press Enter to stop the server...
```

The server listens on `http://localhost:8080/api/v1`. Press Enter in the terminal to shut it down.

---

## 4. curl Examples

### Discovery

```bash
curl -s "http://localhost:8080/api/v1" | jq
```

### List all rooms

```bash
curl -s "http://localhost:8080/api/v1/rooms" | jq
```

### Create a new room

```bash
curl -s -X POST "http://localhost:8080/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"TEST-101","name":"Test Room","capacity":20}'
```

### Filter sensors by type

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=Temperature" | jq
```

### Add a reading (server generates `id` and `timestamp`)

```bash
curl -s -X POST "http://localhost:8080/api/v1/sensors/TEMP-001/readings" \
  -H "Content-Type: application/json" \
  -d '{"value":22.7}'
```

### Trigger 409 - delete a room with active sensors

```bash
curl -i -X DELETE "http://localhost:8080/api/v1/rooms/LIB-301"
```

### Trigger 422 - create a sensor referencing a non-existent room

```bash
curl -i -X POST "http://localhost:8080/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"NOPE-999"}'
```

### Trigger 403 - reading on a MAINTENANCE sensor

```bash
curl -i -X POST "http://localhost:8080/api/v1/sensors/CO2-002/readings" \
  -H "Content-Type: application/json" \
  -d '{"value":500.0}'
```

### Trigger 400 — validation failure (negative capacity)

```bash
curl -i -X POST "http://localhost:8080/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"BAD","name":"Test","capacity":-5}'
```

---

## 5. Questions & Answers

### Part 1.1 - Default lifecycle of a JAX-RS resource class

By default, JAX-RS instantiates a new resource class per request. The runtime creates a fresh instance of `SensorRoom`, `SensorResource`, etc. for every incoming HTTP call and discards it once the response is sent. Each request therefore has its own isolated instance, and any fields declared on the class are re-initialised each time.

Because individual resource instances are temporary, they cannot hold shared state directly, so any data that must survive across requests has to live in a shared store accessible from every instance. That is why `DataStore` is implemented as a singleton: every resource instance fetches the same `ConcurrentHashMap` references via `DataStore.getInstance()`, so mutations made by one request are visible to the next.

Because multiple requests may be processed concurrently by different threads, the underlying collections must also be thread-safe. So using `ConcurrentHashMap` instead of a plain `HashMap` protects against race conditions on individual `PUT`/`GET` operations. Composite operations that span multiple calls (e.g. "check then modify") still require external care, which is why operations such as "delete sensor and remove its ID from the room's `sensorIds` list" are kept as short and localised as possible.

### Part 1.2 - Why HATEOAS is a hallmark of advanced REST

HATEOAS (Hypermedia as the Engine of Application State) means that responses include links describing what actions a client can take next rather than requiring the client to know every endpoint in advance. It is considered a hallmark of mature REST because it makes the API self-descriptive, i.e. a client that starts at the entry point can discover the rest of the system by following links.

The benefit over static documentation is **decoupling**. If the server changes a URL pattern, adds a new optional operation, or deprecates an endpoint, clients that read links dynamically from responses can adapt automatically. Clients that hardcode URLs from documentation will break. Hypermedia also lets the server communicate state-dependent affordances, for example, a `Room` response could include a `delete` link only when the room has no sensors attached, telling the client whether deletion is currently permitted without forcing the client to reimplement the business rule.

The discovery endpoint in this API (`GET /api/v1`) exposes a simplified form of this idea because it returns a map of resource collections to their URLs, thus giving clients a single known starting point from which the rest of the API can be navigated.

### Part 2.1 - Returning IDs vs full objects in list responses

Returning full objects in a list response is heavier because the payload grows linearly with the number of rooms, and each object carries fields that may be unnecessary when the client only wants to pick one room out of many. For a collection browser that just shows a list of room names, nested sensor data and capacity values are wasted bytes on the wire and CPU cycles client-side.

Returning only IDs or minimal summaries keeps list responses small, but forces the client into the N+1 problem: to render anything useful about each room, it has to issue a follow-up GET request per ID. On a campus with thousands of rooms, that amounts to thousands of round trips.

The practical middle ground (the one used in this API) is to return the full top-level object in list responses while including only ID references for nested collections (for example, `sensorIds: ["TEMP-001", "CO2-001"]` rather than embedding full `Sensor` objects). This keeps the list compact but also gives clients enough information to decide whether to fetch individual sensors.

### Part 2.2 - Is DELETE idempotent in this implementation?

In this implementation, DELETE is idempotent. A request to `DELETE /rooms/{id}` returns `204 No Content` both the first time and on every subsequent call for the same ID, regardless of whether the room ever existed. The end state is the same after one call or after ten, so the response reflects that.

An alternative design would be to return 404 on the second and later calls, on the grounds that there is no longer anything to delete. That approach is defensible but less idempotent since the response would then depend on whether the caller happened to hit the system before or after a prior deletion, even though the caller's intent (ensure the room is gone) is satisfied in either case. I chose the idempotent path because it matches the REST convention that DELETE describes a desired end state.

The only case where DELETE returns a non-2xx response is `409 Conflict`, when the room still has sensors assigned, because the precondition for deletion is not met, and further attempts with unchanged state will consistently fail with the same code.

### Part 3.1 - Consequences of sending the wrong Content-Type

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the method will only accept request bodies with `Content-Type: application/json`. If a client sends `text/plain` or `application/xml` to a JSON-only endpoint, JAX-RS rejects the request before the method body is ever entered. The runtime performs content negotiation against the declared media types and in case of finding no match returns `415 Unsupported Media Type` automatically.

The resource method itself does not need to defensively check the `Content-Type` header because the framework enforces it. The same mechanism protects `@Produces` in the other direction: if a client's `Accept` header does not match what the method produces, JAX-RS returns `406 Not Acceptable`.

The practical consequence is clean separation of concerns. Each resource method can focus on its business case, assuming the payload has already been parsed into the expected Java object by the framework. If Jackson fails to deserialise a syntactically valid but structurally incorrect JSON into the target POJO, that is handled separately as `400 Bad Request`, usually by Jackson's own error path.

### Part 3.2 - Why `?type=X` is superior to a path segment

In REST, URL paths identify resources, while query parameters describe modifiers on how those resources are retrieved. Filtering by type is a modifier because there is no "type" resource in the system, only sensors that happen to have a type attribute. Structuring the URL as `/sensors/type/CO2` misrepresents the data model by suggesting that "type" is a level in the resource hierarchy, when it is simply one attribute among several that a sensor has.

Query parameters also scale better when multiple filters are involved. For example, adding a status filter becomes `?type=CO2&status=ACTIVE` which make the parameters independent, order-free, and optional. 

Finally, public APIs such as GitHub's, Stripe's, and Google's use query parameters for all collection filtering. Following the convention makes the API predictable for any developer who has worked with REST before, and reduces the time for learning new APIs.

### Part 4.1 - Benefits of the Sub-Resource Locator pattern

The Sub-Resource Locator pattern delegates nested path handling from a parent resource class to a dedicated sub-resource class. In this API, `SensorResource` handles sensor CRUD, and when a request matches `/sensors/{id}/readings`, `SensorResource` returns a new `SensorReadingResource` instance that takes over request handling from that point in the URL.

The architectural benefit is separation of concerns. Without this pattern, `SensorResource` would need to define every endpoint under `/sensors/{id}/readings/...` directly, which mixes sensor logic with reading history logic in one growing class. As more nested paths are added (`/alerts`, `/schedule`, `/calibration` etc.) the parent class would become an unmaintainable monolith, and each new feature would increase its coupling to every existing one.

With the sub-resource approach each nested class stays focused on one responsibility, and parent-specific context (here, the sensor ID) is passed into the sub-resource's constructor once. The parent only needs to know how to route to the right sub-resource.

### Part 5.2 - Why 422 is more semantically accurate than 404

`HTTP 404 Not Found` means the requested URL does not correspond to any resource the server can return. It is the correct code for `GET /rooms/NONE-999` since  the URL points to a room that does not exist, so the server has nothing to give back.

However, when a client issues `POST /sensors` with `roomId: "NONE-999"`, the situation is different. The URL (`/sensors`) is valid because it is the sensors collection. The payload is valid JSON with all the expected fields. The problem is that one field inside that payload references a resource that does not exist. The target of the request (creation of a new sensor) was found correctly, but the server simply cannot process the payload because of an integrity error inside it.

The semantic hierarchy is: `400` is for malformed or syntactically invalid requests, `404` is for missing URL targets, and `422` is for well-formed requests with semantic errors in the payload. Using `404` in the POST-sensor case would conflate two different failure modes: "the endpoint is wrong" and "the endpoint is right but the payload is inconsistent". Therefore, returning 422 gives clients enough specificity to retry with a corrected payload without second-guessing the endpoint itself.

### Part 5.4 - Security risks of exposing Java stack traces

Exposing raw Java stack traces to external API consumers is a well-known security anti-pattern. A stack trace typically leaks several classes of internal information at once.

First, it reveals the **technology stack**: framework names and versions (Jersey, Grizzly, Jackson), the JVM version, and library choices. This tells an attacker which published CVEs are worth attempting. Second, it reveals the **file system and package layout**: stack frames contain class paths, package hierarchies, and sometimes literal source file paths on the server. Third, when an exception wraps a lower-level error (e.g. `SQLException`), the message often contains table names, connection strings, etc., which effectively map out the data layer.

Beyond direct leakage, stack traces help attackers fingerprint fuzzing attempts so that an attacker can probe for logic flaws without ever authenticating by observing which inputs trigger which exceptions.

The `GenericExceptionMapper` in this project mitigates all of the above. Unhandled exceptions are logged server-side with full detail for developer debugging, while the client receives a generic `{"error": "INTERNAL_SERVER_ERROR", "message": "An unexpected error occurred"}` response.

### Part 5.5 - Why filters are superior to inline `Logger.info` calls

Placing `Logger.info(...)` calls inside every resource method has three drawbacks.

First, **it is easy to forget**. When a new endpoint is added, adding the log line is a separate conscious step, and it is exactly the kind of thing developers might skip under deadline pressure or whatnot, resulting in some endpoints being logged and others not.

Second, **it is repetitive**. Every method would have nearly identical lines at entry and exit, duplicated across a dozen methods. Any change to the log format would require editing every method individually.

Third, **it mixes concerns**. Logging is a cross-cutting concern because it applies uniformly across all endpoints, regardless of their business logic. Embedding it inside each method couples business code to infrastructure code unnecessarily.

A filter solves all of them. `LoggingFilter` implements `ContainerRequestFilter` and `ContainerResponseFilter`, which JAX-RS invokes automatically before and after every resource method. New endpoints are logged automatically, the format lives in one place, and resource methods stay focused on their domain logic.

---

## 6. Design Decisions Beyond the Specification

The following design choices extend the specification.

### `DELETE /sensors/{id}` with cascading cleanup

The specification does not seem to require a sensor-level DELETE endpoint, but without one `DELETE /rooms/{id}` (which requires the room to be empty) becomes practically unusable. Once sensors are assigned, they can never be removed. I added `DELETE /sensors/{id}` which (a) removes the sensor's ID from its parent room's `sensorIds` list, (b) deletes the sensor's reading history, and (c) removes the sensor itself. This order keeps referential integrity consistent even if one step were to fail.

### 403 for OFFLINE sensors as well as MAINTENANCE

The specification requires `403 Forbidden` when posting a reading to a `MAINTENANCE` sensor. I extended this rule to `OFFLINE` as well. An offline sensor is physically disconnected and cannot produce readings.

### 409 Conflict on duplicate POST

`Map.put()` silently overwrites existing keys. Without an explicit check, `POST /rooms` with an already-used ID would return 201 while silently destroying the existing room. I added a `DuplicateResourceException` that returns `409 Conflict` with a structured JSON body, to preserve existing data and signal to the client that the ID is already taken.

### Field-level validation with 400 Bad Request

The specification does not cover payload validation (missing required fields, invalid enum values, non-positive capacity). A `ValidationException` and its mapper return `400 Bad Request` with the specific offending field name, so clients know exactly what to correct.

### Server-generated UUIDs and timestamps for readings

For `POST /sensors/{id}/readings`, the client supplies only the `value` field. The server generates the reading's `id` (as a UUID) and `timestamp` (current epoch milliseconds) to ensure global uniqueness without coordination with clients and keep capture time (the server decides when the reading was recorded, not the client).
