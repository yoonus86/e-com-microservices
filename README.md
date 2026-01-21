# E-commerce Microservices

Inventory and order management system using Spring Boot.

## Setup

Requires Java 17+ and Maven 3.6+

```bash
# Build
cd inventory-service && mvn clean install
cd ../order-service && mvn clean install

# Run (start inventory first, then order in separate terminal)
cd inventory-service && mvn spring-boot:run   # port 8081
cd order-service && mvn spring-boot:run       # port 8080
```

## API Endpoints

### Inventory Service (8081)

| Method | Endpoint                                      | Description           |
| ------ | --------------------------------------------- | --------------------- |
| GET    | `/inventory/{productId}`                      | Get inventory batches |
| GET    | `/inventory/check/{productId}?quantity={qty}` | Check availability    |
| POST   | `/inventory/update`                           | Reserve inventory     |

### Order Service (8080)

| Method | Endpoint                     | Description           |
| ------ | ---------------------------- | --------------------- |
| POST   | `/order`                     | Place order           |
| GET    | `/order`                     | List all orders       |
| GET    | `/order/{orderId}`           | Get order by ID       |
| GET    | `/order/product/{productId}` | Get orders by product |

## Testing

```bash
cd inventory-service && mvn test
cd order-service && mvn test
```

## URLs

| Service   | Swagger                               | H2 Console                       | JDBC URL                  |
| --------- | ------------------------------------- | -------------------------------- | ------------------------- |
| Inventory | http://localhost:8081/swagger-ui.html | http://localhost:8081/h2-console | `jdbc:h2:mem:inventorydb` |
| Order     | http://localhost:8080/swagger-ui.html | http://localhost:8080/h2-console | `jdbc:h2:mem:orderdb`     |

H2 credentials: username `sa`, no password
