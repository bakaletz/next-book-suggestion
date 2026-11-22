package com.example.readingapp.service;

import com.example.readingapp.dto.BookDto;
import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationRequest;
import com.example.readingapp.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationServiceTest {

    private static final RecommendationDto HYPERION =
            new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Mocked recommendation");
    private static final RecommendationDto NAME_OF_THE_WIND =
            new RecommendationDto("The Name of the Wind", "Patrick Rothfuss", "Fantasy", "Mocked recommendation");
    private static final RecommendationDto SAPIENS =
            new RecommendationDto("Sapiens", "Yuval Noah Harari", "History", "Mocked recommendation");
    private static final RecommendationDto THE_MARTIAN =
            new RecommendationDto("The Martian", "Andy Weir", "Science Fiction", "Mocked recommendation");
    private static final RecommendationDto ATOMIC_HABITS =
            new RecommendationDto("Atomic Habits", "James Clear", "Self-Help", "Mocked recommendation");

    private final RecommendationService service = new RecommendationService();

    @Test
    void returnsDefaultNumberOfRecommendationsWhenLimitIsNotSet() {
        RecommendationResponse response = service.recommend(requestWithLimit(null));

        assertThat(response.recommendations()).containsExactly(HYPERION, NAME_OF_THE_WIND, SAPIENS);
    }

    @Test
    void returnsRequestedNumberOfRecommendations() {
        RecommendationResponse response = service.recommend(requestWithLimit(2));

        assertThat(response.recommendations()).containsExactly(HYPERION, NAME_OF_THE_WIND);
    }

    @Test
    void returnsAllAvailableRecommendationsWhenLimitExceedsThem() {
        RecommendationResponse response = service.recommend(requestWithLimit(10));

        assertThat(response.recommendations())
                .containsExactly(HYPERION, NAME_OF_THE_WIND, SAPIENS, THE_MARTIAN, ATOMIC_HABITS);
    }

    private static RecommendationRequest requestWithLimit(Integer limit) {
        List<BookDto> books = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> new BookDto("Book " + i, "Author " + i, "Genre", Instant.parse("2026-01-0" + i + "T00:00:00Z")))
                .toList();
        return new RecommendationRequest(books, limit);
    }
}
