# AGENTS.md

Spring Boot web app that recommends books with OpenAI, based on the books a user finished recently.

## Stack

- Java 21, Spring Boot 4.2 (Spring MVC), Spring AI with OpenAI
- `OPENAI_API_KEY` comes from the git-ignored `.env` file or an environment variable
- Base package: `com.example.readingapp`
- Config: `src/main/resources/application.properties`

## Structure

- `controller` - HTTP endpoints (`POST /api/recommendations`)
- `service` - business logic (picks the 5 most recently finished books)
- `client` - OpenAI calls through Spring AI `ChatClient`
- `dto` - request and response records with validation
- `config` - Spring beans

## Commands

- Build and test: `./mvnw verify`
- Run: `./mvnw spring-boot:run` (port 8080)

## Rules

- Keep dependencies minimal; add one only when a feature needs it.
- Never commit secrets. Read API keys from `.env` or environment variables.
- Controllers only handle HTTP (mapping, validation, delegating). All business logic belongs in services.
- No comments in code (including Javadoc). Use clear names instead.
- Use constructor injection with `private final` fields and Lombok `@RequiredArgsConstructor`.

## Tests

- Tests never call the real OpenAI API; mock `ChatModel` or the client.
- Use AssertJ and compare real values (whole objects or exact lists), not only sizes.
- Unit tests sit next to the class they test; full-app tests use `@SpringBootTest` (`RecommendationIntegrationTest`).
