## Project Overview

This is a Spring Boot application for report automation. The backend is built with Java 17 and utilizes Spring Boot, Spring Data JPA, Spring Security, and JWT for authentication. The project is configured to use both H2 and MySQL databases. The frontend is not yet developed.

## Building and Running

The project is built using Maven. To build and run the project, you can use the following commands:

```bash
# Build the project
./mvnw clean install

# Run the project
./mvnw spring-boot:run
```

## Development Conventions

The code follows standard Java and Spring Boot conventions. It uses Lombok to reduce boilerplate code. The project is structured with a clear separation of concerns, with a dedicated `auth` package for authentication and authorization logic. The implementation of the authentication and authorization logic is not yet complete.
