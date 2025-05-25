# Foreign Exchange Application

This is a simple Spring Boot application that provides foreign exchange services, including exchange rate lookup, currency conversion, bulk file processing, and conversion history.

## Features

* **Exchange Rate Endpoint**: Get the current exchange rate between two currencies.
* **Currency Conversion Endpoint**: Convert an amount from one currency to another and record the transaction.
* **Conversion History Endpoint**: Retrieve a paginated list of past conversions, filterable by transaction ID or date.
* **External Exchange Rate Integration**: Utilizes api.exchangerate-api.com for real-time exchange rates.
* **Bulk File Processing**: Upload a CSV file for multiple currency conversion requests.
* **Error Handling**: Graceful error handling with meaningful messages and HTTP status codes.
* **RESTful API Design**: Follows REST principles.
* **Self-Contained**: Runs as a single JAR file.
* **In-Memory Database**: Uses H2 database for easy setup and demonstration.
* **API Documentation**: Integrated with OpenAPI (Swagger UI).
* **Caching**: Caches exchange rates to improve performance.
* **Docker Support**: Containerized for consistent deployment.

## Technologies Used

* **Spring Boot 3.3.12**
* **Spring Data JPA**
* **H2 Database**
* **Lombok**
* **Apache Commons CSV**
* **RestTemplate**
* **Jackson**
* **OpenAPI (Swagger UI)**
* **Redis**
* **Maven**
* **Docker**
* **JUnit 5 & Mockito**

## Setup and Running the Application

### Prerequisites

* Java 17 or higher
* Maven 3.x
* Docker (optional)
* API Key from ExchangeRate-API.com

### 1. Clone the Repository

In a real scenario, clone the Git repository. For this task, create the files manually.

### 2. Configure API Key

Update `src/main/resources/application.properties`:

```properties
forex.api.key=YOUR_API_KEY
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

#### a) As JAR

```bash
java -jar target/forex-0.0.1-SNAPSHOT.jar
```

Visit: [http://localhost:8080](http://localhost:8080)

#### b) With Docker

```bash
docker build -t forex .
docker run -p 8080:8080 forex
```

## API Documentation (Swagger UI)

* Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Endpoints

All endpoints are prefixed with `/api/v1/forex`

### 1. Get Exchange Rate

**GET** `/exchange-rate`

**Query Parameters**:

* `source`: source currency code (e.g., USD)
* `target`: target currency code (e.g., EUR)

### 2. Perform Currency Conversion

**POST** `/convert`

**Request Body**:

```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "GBP",
  "amount": 100.50
}
```

### 3. Get Conversion History

**GET** `/history`

**Query Parameters**:

* `transactionId` (optional)
* `transactionDate` (optional)
* `page`, `size` (optional pagination)

### 4. Bulk Currency Conversion

**POST** `/bulk-convert`

Upload CSV file with:

```csv
sourceCurrency,targetCurrency,amount
USD,EUR,100.00
EUR,GBP,50.00
```

Use curl:

```bash
curl -X POST -F "file=@/path/to/file.csv" http://localhost:8080/api/v1/forex/bulk-convert
```

## Error Handling

Standardized JSON responses:

```json
{
  "timestamp": "2023-01-15T12:34:56.789",
  "status": 400,
  "error": "Bad Request",
  "message": "Source currency cannot be empty",
  "path": "/api/v1/forex/convert"
}
```

### Common Error Codes

* `400 Bad Request`
* `404 Not Found`
* `503 Service Unavailable`
* `500 Internal Server Error`

## Unit Testing

Run tests with:

```bash
mvn test
```

## Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/forex-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Design Patterns and Code Structure

* Layered architecture (controller, service, repository, model)
* Dependency Injection via Spring
* DTOs to decouple API and internal models
* Strategy Pattern for external API integration
* Caching with Redis
* Asynchronous processing with CompletableFuture

## Further Improvements

* Detailed error codes
* Rate limiting
* Circuit breaker (Resilience4j)
* Persistent DB (e.g., PostgreSQL)
* Security (Auth)
* Logging and Monitoring
