# reading-app

Reading application with AI integration, built with Spring Boot.

## Requirements

- Java 21 (Maven is not required, the project includes the Maven wrapper)
- An OpenAI API key

## Run

Copy `.env.example` to `.env` and set your key (`.env` is git-ignored):

```
OPENAI_API_KEY=sk-...
```

```bash
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The app starts on http://localhost:8080.

## Test

```bash
./mvnw test
```
