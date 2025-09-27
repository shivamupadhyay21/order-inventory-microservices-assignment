# 📦 Order & Inventory Microservices Assignment

This repository contains two Spring Boot microservices built with **Java 17** and **Gradle**:

- **Order Service** – Handles order placement after checking available stock.
- **Inventory Service** – Manages product inventory, supporting multiple batches per product sorted by expiry date.

Both services use **Spring Boot 3**, **Spring Data JPA**, **H2 in-memory database**, and **springdoc-openapi** for API documentation.


Unit test cases are written in JUnit 5 and Mockito.

Component/integration tests using @SpringBootTest and H2 database.


---

## 🚀 Project Setup

### Prerequisites
- Java 17+
- Gradle 8+
- IntelliJ IDEA (recommended)
- Compatible Browser(for opening H2 console and Swagger UI)

### Project Structure
```
order-inventory-microservices-assignment/
├── inventory-service/ # Inventory microservice
│ ├── build/ # Gradle build outputs
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/
│ │ │ │ └── org.koerber.inventory/
│ │ │ │ ├── config/
│ │ │ │ ├── controller/
│ │ │ │ ├── dto/
│ │ │ │ ├── model/
│ │ │ │ ├── repository/
│ │ │ │ ├── service/
│ │ │ │ └── status/
│ │ │ └── resources/
│ │ └── test/            # Unit and Component Test Cases
│ └── build.gradle
│
├── order-service/ # Order microservice
│ ├── build/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/
│ │ │ │ └── org.koerber.order/
│ │ │ │ ├── client/
│ │ │ │ ├── configuration/
│ │ │ │ ├── controller/
│ │ │ │ ├── dto/
│ │ │ │ ├── model/
│ │ │ │ ├── repository/
│ │ │ │ └── service/
│ │ │ └── resources/
│ │ └── test/             # Unit and Component Test Cases
│ └── build.gradle
│
├── build.gradle          # Root Gradle file
├── settings.gradle       # Gradle multi-module config
├── gradlew / gradlew.bat # Gradle wrapper scripts
├── .gitignore
└── README.md
```
### H2 in-memory database console

After running respective Spring Boot application on local.

- http://localhost:8081/h2-console (for inventory-service)
- http://localhost:8080/h2-console (for order-service)

### Swagger UI

After running respective Spring Boot application on local.

- http://localhost:8081/swagger-ui/index.html#/ (for inventory-service)
- http://localhost:8080/swagger-ui/index.html#/ (for order-service)

### Curl for inventory-service
**POST /inventory/update – Updates inventory after an order is placed.**
```
curl --location --request POST 'http://localhost:8081/inventory/update' \
--header 'Content-Type: application/json' \
--data '{
  "productId": 123,
  "quantity": 2
}'
```

**GET /inventory/{productId} – Returns list of inventory batches sorted by expiry date.** 
```
curl --location --request GET 'http://localhost:8081/inventory/5' \
--header 'Content-Type: application/json' \
--data ''
```

### Curl for order-service
**POST /order – Places an order and updates inventory accordingly.**

```
curl --location 'http://localhost:8080/order' \
--header 'Content-Type: application/json' \
--data '{
    "productId":5,
    "quantity":10
}'
```

### Clone Repository
```bash
git clone https://github.com/<your-username>/order-inventory-microservices-assignment.git
cd order-inventory-microservices-assignment
```


