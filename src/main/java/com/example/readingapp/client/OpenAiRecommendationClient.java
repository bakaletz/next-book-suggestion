package com.example.readingapp.client;

import com.example.readingapp.dto.BookDto;
import com.example.readingapp.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiRecommendationClient {

    private static final String SYSTEM_PROMPT = """
            You are a book recommendation assistant.
            Recommend books the user has not read yet, based on the books they finished recently.
            Never recommend a book from the user's list.
            For each recommendation give the title, author, genre and a short reason.
            """;

    private static final String USER_PROMPT = """
            Recently finished books, newest first:
            {books}

            Recommend {limit} books.
            """;

    private final ChatClient chatClient;

    public RecommendationResponse recommend(List<BookDto> recentBooks, int limit) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(user -> user.text(USER_PROMPT)
                        .param("books", formatBooks(recentBooks))
                        .param("limit", limit))
                .call()
                .entity(RecommendationResponse.class);
    }

    private static String formatBooks(List<BookDto> books) {
        return books.stream()
                .map(book -> "- \"%s\" by %s, genre: %s, finished: %s".formatted(
                        book.title(),
                        book.author(),
                        book.genre() != null ? book.genre() : "unknown",
                        book.finishedTimeStamp()))
                .collect(Collectors.joining("\n"));
    }
}
