package com.example.readingapp.service;

import com.example.readingapp.client.OpenAiRecommendationClient;
import com.example.readingapp.dto.BookDto;
import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationRequest;
import com.example.readingapp.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final BookDto BOOK_JAN = book("January Book", "2026-01-01T00:00:00Z");
    private static final BookDto BOOK_FEB = book("February Book", "2026-02-01T00:00:00Z");
    private static final BookDto BOOK_MAR = book("March Book", "2026-03-01T00:00:00Z");
    private static final BookDto BOOK_APR = book("April Book", "2026-04-01T00:00:00Z");
    private static final BookDto BOOK_MAY = book("May Book", "2026-05-01T00:00:00Z");
    private static final BookDto BOOK_JUN = book("June Book", "2026-06-01T00:00:00Z");
    private static final BookDto BOOK_JUL = book("July Book", "2026-07-01T00:00:00Z");

    private static final RecommendationResponse CLIENT_RESPONSE = new RecommendationResponse(List.of(
            new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Reason")));

    @Mock
    private OpenAiRecommendationClient openAiRecommendationClient;

    @InjectMocks
    private RecommendationService service;

    @Test
    void returnsRecommendationsFromClient() {
        List<BookDto> books = List.of(BOOK_JAN, BOOK_FEB, BOOK_MAR, BOOK_APR, BOOK_MAY);
        when(openAiRecommendationClient.recommend(List.of(BOOK_MAY, BOOK_APR, BOOK_MAR, BOOK_FEB, BOOK_JAN), 4))
                .thenReturn(CLIENT_RESPONSE);

        RecommendationResponse response = service.recommend(new RecommendationRequest(books, 4));

        assertThat(response).isEqualTo(CLIENT_RESPONSE);
    }

    @Test
    void sendsFiveMostRecentBooksNewestFirst() {
        List<BookDto> books = List.of(BOOK_MAR, BOOK_JUL, BOOK_JAN, BOOK_MAY, BOOK_FEB, BOOK_JUN, BOOK_APR);

        service.recommend(new RecommendationRequest(books, 3));

        verify(openAiRecommendationClient).recommend(List.of(BOOK_JUL, BOOK_JUN, BOOK_MAY, BOOK_APR, BOOK_MAR), 3);
    }

    @Test
    void sendsDefaultLimitWhenLimitIsNotSet() {
        List<BookDto> books = List.of(BOOK_JAN, BOOK_FEB, BOOK_MAR, BOOK_APR, BOOK_MAY);

        service.recommend(new RecommendationRequest(books, null));

        verify(openAiRecommendationClient).recommend(List.of(BOOK_MAY, BOOK_APR, BOOK_MAR, BOOK_FEB, BOOK_JAN), RecommendationRequest.DEFAULT_LIMIT);
    }

    private static BookDto book(String title, String finishedTimeStamp) {
        return new BookDto(title, "Author", "Genre", Instant.parse(finishedTimeStamp));
    }
}
