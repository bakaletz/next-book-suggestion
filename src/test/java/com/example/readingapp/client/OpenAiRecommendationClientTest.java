package com.example.readingapp.client;

import com.example.readingapp.dto.BookDto;
import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiRecommendationClientTest {

    private static final String MODEL_ANSWER = """
            {"recommendations":[
              {"title":"Hyperion","author":"Dan Simmons","genre":"Science Fiction","reason":"Epic space opera like Dune"},
              {"title":"The Martian","author":"Andy Weir","genre":"Science Fiction","reason":"Same author as Project Hail Mary"}
            ]}
            """;

    private static final List<BookDto> RECENT_BOOKS = List.of(
            new BookDto("Dune", "Frank Herbert", "Science Fiction", Instant.parse("2026-11-01T18:30:00Z")),
            new BookDto("Project Hail Mary", "Andy Weir", null, Instant.parse("2026-10-15T09:00:00Z"))
    );

    private final AtomicReference<Prompt> sentPrompt = new AtomicReference<>();

    private final ChatModel chatModel = prompt -> {
        sentPrompt.set(prompt);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(MODEL_ANSWER))));
    };

    private final OpenAiRecommendationClient client = new OpenAiRecommendationClient(ChatClient.create(chatModel));

    @Test
    void returnsRecommendationsFromModelAnswer() {
        RecommendationResponse response = client.recommend(RECENT_BOOKS, 2);

        assertThat(response).isEqualTo(new RecommendationResponse(List.of(
                new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Epic space opera like Dune"),
                new RecommendationDto("The Martian", "Andy Weir", "Science Fiction", "Same author as Project Hail Mary"))));
    }

    @Test
    void sendsSystemPrompt() {
        client.recommend(RECENT_BOOKS, 2);

        assertThat(sentPrompt.get().getSystemMessage().getText()).isEqualTo("""
                You are a book recommendation assistant.
                Recommend books the user has not read yet, based on the books they finished recently.
                Never recommend a book from the user's list.
                For each recommendation give the title, author, genre and a short reason.
                """);
    }

    @Test
    void sendsRecentBooksAndLimitInUserPrompt() {
        client.recommend(RECENT_BOOKS, 2);

        String userPrompt = sentPrompt.get().getUserMessage().getText().replace("\r\n", "\n");

        assertThat(userPrompt).startsWith("""
                Recently finished books, newest first:
                - "Dune" by Frank Herbert, genre: Science Fiction, finished: 2026-11-01T18:30:00Z
                - "Project Hail Mary" by Andy Weir, genre: unknown, finished: 2026-10-15T09:00:00Z

                Recommend 2 books.
                """);
    }
}
