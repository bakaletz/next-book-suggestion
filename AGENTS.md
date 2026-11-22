# AGENTS.md

Spring Boot web app for reading, with AI integration planned.

## Stack

- Java 21, Spring Boot 4.2 (Spring MVC), Spring AI with OpenAI (`OPENAI_API_KEY` from git-ignored `.env` or environment variable)
- Base package: `com.example.readingapp`
- Config: `src/main/resources/application.properties`

## Commands

- Build and test: `./mvnw verify`
- Run: `./mvnw spring-boot:run` (port 8080)

## Rules

- Keep dependencies minimal; add one only when a feature needs it.
- Never commit secrets. Read API keys from environment variables.
- Controllers only handle HTTP (mapping, validation, delegating). All business logic belongs in services.
- No comments in code (including Javadoc). Use clear names instead.
- Use constructor injection with `private final` fields and Lombok `@RequiredArgsConstructor`.
