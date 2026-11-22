# reading-app

Recommends books with OpenAI, based on the books a user finished recently. Built with Spring Boot and Spring AI.

## Requirements

- Java 21 (Maven is not required, the project includes the Maven wrapper)
- An OpenAI API key with credits

## Run

Copy `.env.example` to `.env` and set your key (`.env` is git-ignored):

```
OPENAI_API_KEY=sk-...
```

```bash
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The app starts on http://localhost:8080.

## API

`POST /api/recommendations`

- `recentBooks`: 5 to 10 books, in any order. `title`, `author` and `finishedTimeStamp` (ISO-8601, not in the future) are required; `genre` is optional. Only the 5 most recently finished books are sent to OpenAI.
- `limit`: number of recommendations, 1 to 10, default 3.

```bash
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "recentBooks": [
      { "title": "Dune", "author": "Frank Herbert", "genre": "Science Fiction", "finishedTimeStamp": "2026-11-01T18:30:00Z" },
      { "title": "Project Hail Mary", "author": "Andy Weir", "genre": "Science Fiction", "finishedTimeStamp": "2026-10-15T09:00:00Z" },
      { "title": "The Hobbit", "author": "J.R.R. Tolkien", "genre": "Fantasy", "finishedTimeStamp": "2026-09-20T12:00:00Z" },
      { "title": "Sapiens", "author": "Yuval Noah Harari", "genre": "History", "finishedTimeStamp": "2026-08-05T20:00:00Z" },
      { "title": "1984", "author": "George Orwell", "genre": "Dystopian", "finishedTimeStamp": "2026-07-10T10:00:00Z" }
    ],
    "limit": 3
  }'
```

Response:

```json
{
  "recommendations": [
    { "title": "Hyperion", "author": "Dan Simmons", "genre": "Science Fiction", "reason": "..." }
  ]
}
```

Recommendations are popular books the user has not read: books from `recentBooks` and duplicates are removed from the model answer, and at most `limit` items are returned.

Errors use the `application/problem+json` format:

- `400` - invalid request, with an `errors` list of `{field, message}`
- `502` - OpenAI is unavailable or failed

## Configuration

The OpenAI model can be changed in `application.properties` (default is Spring AI's `gpt-5-mini`):

```properties
spring.ai.openai.chat.model=gpt-5-mini
```

## Test

```bash
./mvnw test
```

Tests do not call OpenAI and do not need an API key.

## IntelliJ IDEA

If IntelliJ fails to import the project (e.g. `release version 5 not supported`), set
Settings → Build Tools → Maven → Maven home path to "Bundled (Maven 3)" and reload the Maven project.
Enable annotation processing for Lombok.
