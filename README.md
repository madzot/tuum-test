# tuum_test
Small core banking solution test assignment for Tuum

## How to run:

1. Clone this repository
2. Open the project in an IDE
3. Build the project
4. Open Docker Desktop
5. Run `docker-compose up` in the backend directory
6. Run `BackendApplication` in `/backend/src/main/java/com/tuum/backend/BackendApplication`
7. Now it should be running. If there is a problem building make sure gradle is running on java 21.

Spring on `localhost:8080`.
PostgreSQL database on `localhost:5432`
RabbitMQ on `localhost:5672` and management on `localhost:15672`
PostgreSQL user is `tuum` and password is `banking_solution`
RabbitMQ user is `tuum` and password is `banking_solution`

## Important choices:

- Decided to use LiquiBase for my database management, because I am familiar with it.
- Decided to use MapStruct for mapping objects from DTOs to entities and vice versa. Decided to use it because I'm familiar with it and it helps get rid of long lines of creating a dto into an entity
- Decided to make an error handler that will log internal server errors and automatically detects my own exceptions and then sends an HTTP 400 back to the client
- I was uncertain what had to be published to RabbitMQ, so I decided to just publish the DTO that would be sent back to the client.
- I've put the MyBatis classes under the `repositories` package
- I've also made 1 endpoint with the path `tuum/api/customers` that returns a list of all the customers. There is no way other than manually to insert customers in the database right now,
but I've made 3 base customers.
- All IDs start from 1 and increment by 1, I made it like this because it seemed simple and currently felt unnecessary to make it more complex
- All endpoints start with `/tuum/api/`
- I have also implemented OpenAPI documentation with Swagger UI, which should be on `localhost:8080/swagger-ui/index.html#`

## ENDPOINTS

- `/tuum/api/account` - GET request for getting details on an account.
- `/tuum/api/account` - POST request for creating account.
- `/tuum/api/transaction` - GET request for getting transactions.
- `/tuum/api/transaction` - POST request for creating transaction.
- `/tuum/api/customers` - GET request for getting all customers.

## Estimated transaction speed

I ran a test with mocking the database and then it was able to create around 20,000 transaction per second.
