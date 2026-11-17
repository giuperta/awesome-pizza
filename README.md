# 🍕 Awesome Pizza

![CI](https://github.com/giuperta/awesome-pizza/actions/workflows/ci.yml/badge.svg)

REST API for a pizzeria: customers order without registering and track their order; the pizzaiolo works through a **first-in, first-out queue** of orders.

Java 21 · Spring Boot 3.5 · Spring Data JPA · H2 · Bean Validation · OpenAPI · JUnit 5 / Mockito / MockMvc

## Features

- Order pizzas without authentication, with input validation (customer name, 1–20 pizzas from the menu).
- Order tracking by ID: `RECEIVED → IN_PREPARATION → READY`.
- Kitchen queue **paginated and filtered in the database**, oldest orders first.
- Safe for more than one pizzaiolo: optimistic locking (`@Version`) stops two people taking the same order; the loser gets `409 Conflict` and can retry.
- Consistent JSON errors with the right HTTP status (400, 404, 409), and no internal details leaked on 500.
- Interactive API docs with Swagger UI.

## API

Base path: `/api/v1`

| Method | Path | Who | Description |
|---|---|---|---|
| `POST` | `/orders` | Customer | Place an order → `201 Created` + `Location` header |
| `GET` | `/orders/{id}` | Customer | Track an order |
| `GET` | `/orders/menu` | Customer | Available pizzas |
| `GET` | `/kitchen/orders` | Pizzaiolo | Queue of orders not yet ready (**paginated**) |
| `POST` | `/kitchen/orders/next` | Pizzaiolo | Take the oldest `RECEIVED` order → `IN_PREPARATION` (`204` if the queue is empty) |
| `POST` | `/kitchen/orders/{id}/complete` | Pizzaiolo | `IN_PREPARATION` → `READY` (`409` if the order is in another state) |

### Pagination

`GET /api/v1/kitchen/orders?status=RECEIVED&page=0&size=20`

| Parameter | Default | Notes |
|---|---|---|
| `page` | `0` | zero-based |
| `size` | `20` | 1–100 |
| `status` | both | `RECEIVED` or `IN_PREPARATION` |

```json
{
  "content": [
    { "id": "6f1c…", "customerName": "Anna", "pizzas": ["MARGHERITA"], "status": "RECEIVED" }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

The response uses an explicit `PageResponse` record rather than serializing Spring's `Page` directly, so the JSON contract stays stable across Spring versions.

### Example

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Mario","pizzas":["MARGHERITA","DIAVOLA"]}'
```

Error format:

```json
{ "status": 400, "message": "Validation failed", "timestamp": "2026-11-17T12:30:00", "details": ["size: must be less than or equal to 100"] }
```

## Run

```bash
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI spec: http://localhost:8080/v3/api-docs
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:pizza-db`, user `sa`, empty password)

### Docker

```bash
docker build -t awesome-pizza .
docker run -p 8080:8080 awesome-pizza
```

## Tests

```bash
./mvnw verify      # tests + JaCoCo coverage report in target/site/jacoco
```

- Unit tests for the service and the error handling (Mockito).
- Web layer tests with MockMvc (`@WebMvcTest`): status codes, validation, pagination parameters.
- Repository tests (`@DataJpaTest`) for the paginated, filtered query.
- An end-to-end test of the whole order lifecycle on the real application context.

CI runs the full build on every push (GitHub Actions).

## Project structure

```
controller/   REST endpoints
service/      business rules and transactions
repository/   Spring Data JPA queries
model/        JPA entity and enums
dto/          request/response records, PageResponse
exception/    domain exceptions and global error handler
```
