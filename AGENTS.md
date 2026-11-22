# AGENTS.md

Spring Boot web app for reading, with AI integration planned.

## Stack

- Java 21, Spring Boot 4.2 (Spring MVC), Maven via wrapper (`./mvnw`, Windows: `.\mvnw.cmd`)
- Base package: `com.example.readingapp`
- Config: `src/main/resources/application.properties`

## Commands

- Build and test: `./mvnw verify`
- Run: `./mvnw spring-boot:run` (port 8080)

## Rules

- Keep dependencies minimal; add one only when a feature needs it.
- Never commit secrets. Read API keys from environment variables.
