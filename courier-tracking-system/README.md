# Courier Tracking System

## Project Requirements (from the case study)

The main requirements were:
1. Build a RESTful API that can handle streaming courier location updates
2. Detect and log when couriers come within 100m of Migros store locations
3. Don't count repeated store visits if they happen within a minute of each other
4. Provide a way to query total distance traveled by any courier

## Architecture Overview

I went with a microservices approach to make the system scalable and easier to maintain. The main services are:

- **Location Service**: Processes and stores courier locations
- **Distance Calculation Service**: Tracks courier movements and calculates distances
- **Store Service**: Handles store data and proximity detection
- **Discovery Service**: Eureka server for service registration
- **Config Server**: Centralized configuration management
- **API Gateway**: Entry point for external API calls

The services communicate both through REST APIs (using Feign clients) and asynchronously via Kafka events.

## Key Features

- **Real-time tracking**: Uses Kafka to stream courier location events between services
- **Store entry detection**: Efficiently calculates when couriers are within 100m of stores using geospatial formulas
- **Duplicate entry prevention**: Tracks recent store visits to prevent counting multiple entries within 1 minute
- **Distance calculation**: Uses the haversine formula to calculate distances between GPS coordinates
- **Caching with Redis**: Improves performance by caching frequently accessed data
- **Resilient design**: Implements circuit breakers to handle service failures gracefully

## Tech Stack

- Spring Cloud 2024.0.0
- Spring Boot 3.4.2
- Java 21
- JUnit 5 and Mockito for testing
- PostgreSQL for persistent storage
- Redis for caching
- Kafka for event streaming
- Resilience4j for circuit breaking
- Spring Cloud Netflix for service discovery
- Docker & Docker Compose for containerization
- Maven for building

## Design Patterns I Used

Some of my implemented design patterns to make the code more maintainable:

1. **Circuit Breaker Pattern**: Used Resilience4j to prevent cascading failures when services are down. This has saved me a lot of headaches during testing with intentionally failed services.

2. **Observer Pattern**: Implemented through Kafka to allow services to react to location events asynchronously.

## Data Flow

1. Location data is received via the REST API.
2. The Location Tracking Service processes the data.
3. The Store Entry Service checks if the courier is within 100 meters of any store.
4. If a courier enters a store's vicinity, an entry log is created (if not a reentry within 1 minute).
5. The Distance Calculation Service updates the total distance traveled by the courier.
6. Distance queries are served from the stored distance data.


## Kafka Event Streaming

The system uses Kafka for real-time event streaming between services:

1. **Location Events**: 
   - **Producer**: Location Service publishes location updates when couriers report their position
   - **Consumer**: Distance Calculation Service consumes these events to calculate travel distances
   - **Consumer**: Store Service consumes these events to detect proximity to stores

The Kafka topics are configured with appropriate partitioning (6 partitions) and replication (3 replicas) 
to ensure scalability and fault tolerance.

## Redis Caching

Redis is used for two primary purposes:

1. **Performance Optimization**:
   - The Distance Calculation Service caches total travel distances to avoid expensive recalculations
   - Cache entries expire after a configurable TTL (3600 seconds by default)
   - Keys follow the pattern `courier-total-distance:{courierId}`

2. **Store Entry Deduplication**:
   - The Store Service uses Redis to track recent store entries by couriers
   - Prevents counting multiple entries within the configured timeout period (60 seconds)
   - Keys follow the pattern `store-entry:{courierId}:{storeId}`

## PostgreSQL earthdistance Extension

This project leverages PostgreSQL's `earthdistance` extension for efficient geospatial calculations. The `earthdistance` module enables accurate great-circle distance calculations directly in the database.

I implemented this in the `StoreRepository` to efficiently find stores near a courier's location:

```java
@Query(value = "SELECT * FROM stores s " +
        "WHERE earth_box(ll_to_earth(?1, ?2), ?3) @> ll_to_earth(s.latitude, s.longitude)",
        nativeQuery = true)
List<Store> findStoresNearby(double latitude, double longitude, double radiusInMeters);
```

This query uses `earth_box` and `ll_to_earth` functions to create a bounding box for initial filtering, followed by accurate distance calculation—much more efficient than calculating distances for every store in application code.

The extension provides two approaches:
1. **Cube-based**: For higher precision (used in this project)
2. **Point-based**: Simpler longitude/latitude calculations

By pushing geospatial calculations to the database layer, we gain improved performance, better accuracy, and can leverage PostgreSQL's spatial indexing capabilities.

For more information, see the [PostgreSQL earthdistance documentation](https://www.postgresql.org/docs/current/earthdistance.html).

## Getting Started

This is second version of the project. This project contains the following changes:
- Added Simple Spring Security to Api Gateway
- Docker image size reduction from around 580 Mb to 120 Mb 



### Prerequisites

You'll need:
- Java 21+
- Docker and Docker Desktop
- Maven
- Some basic knowledge of Spring Boot and Kafka

### Running the App

# Clone the repo
git clone https://github.com/hasandg/courier-tracking-system2.git


#### Option 1: The Easy Way (Docker)

The easiest way to run everything is with Docker Compose:

```bash
##### First Build Shared Objects
cd courier-tracking-system/shared-objects
mvn clean install

##### Build All Services(Skip tests)
cd ..
mvn clean install -DskipTests

cd courier_tracking_system_infra
```

```bash
docker-compose up -d
```
if any changes made in the code and want to rebuild the image then run below command  
(run maven clean install before running below command)
```bash
docker compose up -d --build
```

## Stopping the Application

```bash
docker compose down
```
if you want to remove the images as well then run below command
```bash
docker compose down --rmi all
```
if you want to remove the volumes as well then run below command
```bash
docker compose down -v
```

#### Option 2: Manual Setup

If you want more control or to run without Docker:

1. Make sure you have PostgreSQL, Redis, and Kafka running locally or update configs to point to your instances

2Build and start each service (start them in this order):
```bash
mvn clean package

# Start in this order:
java -jar discovery-service/target/discovery-service.jar
java -jar config-server/target/config-server.jar
java -jar location-service/target/location-service.jar
java -jar distance-calculation-service/target/distance-calculation-service.jar
java -jar store-service/target/store-service.jar
java -jar api-gateway/target/api-gateway.jar
```

#### Option 3: Hybrid Setup (Advised if logs are wanted to be seen on console)

```bash
##### First Build Shared Objects
cd courier-tracking-system/shared-objects
mvn clean install

##### Build All Services(Skip tests)
cd ..
mvn clean install -DskipTests

cd courier_tracking_system_infra
```

```bash
docker-compose up -d
```
if any changes made in the code and want to rebuild the image then run below command  
(run maven clean install before running below command)
```bash
docker compose up -d --build
```

## Stop Distance Calculation, Store and Location Services
Using Docker Desktop, you can stop the services by clicking on the stop button on the container.

## Start Distance Calculation, Store and Location Services on IDE ( Eclipse, IDEA ...)
1. Open the project in IDE
2. Go to the respective service and run the main class


```bash

## Stopping the Application

```bash
docker compose down
```
if you want to remove the images as well then run below command
```bash
docker compose down --rmi all
```
if you want to remove the volumes as well then run below command
```bash
docker compose down -v
```

## API Examples

Once everything is running, you can access the Swagger UI to explore the APIs:

### Distance Calculation Service
```
http://localhost:8083/swagger-ui.html
```

### Location Service
```
http://localhost:8084/swagger-ui.html
```

### Store Service
```
http://localhost:8082/swagger-ui.html
```


Here are some key endpoints to try:

### 1. Track a courier location

```http
http://localhost:8084/swagger-ui/index.html#/location-controller/recordLocation
{
  "courierId": "courier-123",
  "latitude": 40.9925123,
  "longitude": 29.1248765
}
```

This will be published to Kafka and processed by the system.

### 2. Get a courier's total travel distance

```http
http://localhost:8083/swagger-ui/index.html#/distance-controller/getTotalTravelDistance

courierId: courier-123
```

Response:
```json
{
  "courierId": "courier-123",
  "totalDistance": 15243.87,
  "unit": "meters"
}
```

### 3. View a courier's location history

```http
http://localhost:8084/swagger-ui/index.html#/location-controller/getLocationHistory

courierId: courier-123

```

### 4. Check store entries for a courier

```http
http://localhost:8082/swagger-ui/index.html#/store-entry-controller/getEntriesForCourier
courierId: courier-123

```

## A Note on Resilience

One of the most interesting parts of this project was implementing the circuit breaker pattern with Resilience4j. It helps the system degrade gracefully when services are unavailable.

Here's a snippet from the config showing how I configured it:

```properties
# Location service circuit breaker settings
resilience4j.circuitbreaker.instances.locationService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.locationService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.locationService.waitDurationInOpenState=5s
```

The circuit breaker kicks in when 50% of the last 10 calls fail, and then stays open for 5 seconds before trying again.

I also implemented fallback methods in the services that allow the system to return cached data or reasonable defaults when a service is down.

## Monitoring

You can monitor the system's health and circuit breakers using Actuator endpoints:

```
http://localhost:8083/actuator/health
http://localhost:8083/actuator/circuitbreakers
```

## Testing

There are unit tests and integration tests for various components:

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Future Improvements

If I had more time, I'd add:
- Kubernetes deployment configs
- Better monitoring with Prometheus and Grafana
- A simple visualization UI to show courier movements on a map
- Performance testing for high throughput scenarios
- Distributed Tracing: Micrometer and Zipkin
- Aggrogating Log files with ELK Stack
- Spring Security for API Security
- CQRS pattern for better separation of concerns

Some examples of those can be seen in my following repositories:
https://github.com/hasandg/Spring_Boot_Cloud_Security  
https://github.com/hasandg/Bank-Account-Project-with-CQRS-and-Event-Sourcing-Kafka

