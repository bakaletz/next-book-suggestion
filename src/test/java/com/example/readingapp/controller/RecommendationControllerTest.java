package com.example.readingapp.controller;

import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationRequest;
import com.example.readingapp.dto.RecommendationResponse;
import com.example.readingapp.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    private static final String URL = "/api/recommendations";
    private static final String VALID_TIMESTAMP = "2026-01-01T00:00:00Z";
    private static final RecommendationResponse EMPTY_RESPONSE = new RecommendationResponse(List.of());

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    void returnsRecommendationsFromService() {
        RecommendationResponse expected = new RecommendationResponse(List.of(
                new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Reason")));
        when(recommendationService.recommend(any(RecommendationRequest.class))).thenReturn(expected);

        assertThat(performPost(requestBody(validBooks(5), null)))
                .hasStatusOk()
                .bodyJson()
                .convertTo(RecommendationResponse.class)
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10})
    void acceptsAllowedNumberOfBooks(int count) {
        when(recommendationService.recommend(any(RecommendationRequest.class))).thenReturn(EMPTY_RESPONSE);

        assertThat(performPost(requestBody(validBooks(count), 3))).hasStatusOk();
    }

    @Test
    void acceptsBookWithoutGenre() {
        when(recommendationService.recommend(any(RecommendationRequest.class))).thenReturn(EMPTY_RESPONSE);
        List<String> books = validBooks(4);
        books.add(book("\"title\":\"Dune\",\"author\":\"Frank Herbert\",\"finishedTimeStamp\":\"" + VALID_TIMESTAMP + "\""));

        assertThat(performPost(requestBody(books, null))).hasStatusOk();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 11})
    void rejectsNotAllowedNumberOfBooks(int count) {
        assertBadRequest(requestBody(validBooks(count), null));
    }

    @Test
    void rejectsMissingBooks() {
        assertBadRequest("{\"limit\":3}");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "\"author\":\"Frank Herbert\",\"finishedTimeStamp\":\"" + VALID_TIMESTAMP + "\"",
            "\"title\":\" \",\"author\":\"Frank Herbert\",\"finishedTimeStamp\":\"" + VALID_TIMESTAMP + "\"",
            "\"title\":\"Dune\",\"finishedTimeStamp\":\"" + VALID_TIMESTAMP + "\"",
            "\"title\":\"Dune\",\"author\":\"\",\"finishedTimeStamp\":\"" + VALID_TIMESTAMP + "\"",
            "\"title\":\"Dune\",\"author\":\"Frank Herbert\"",
            "\"title\":\"Dune\",\"author\":\"Frank Herbert\",\"finishedTimeStamp\":\"2999-01-01T00:00:00Z\""
    })
    void rejectsInvalidBook(String invalidBookFields) {
        List<String> books = validBooks(4);
        books.add(book(invalidBookFields));

        assertBadRequest(requestBody(books, null));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 11})
    void rejectsLimitOutOfRange(int limit) {
        assertBadRequest(requestBody(validBooks(5), limit));
    }

    private void assertBadRequest(String body) {
        assertThat(performPost(body)).hasStatus(HttpStatus.BAD_REQUEST);
        verify(recommendationService, never()).recommend(any());
    }

    private MvcTestResult performPost(String body) {
        return mockMvc.post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }

    private static List<String> validBooks(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> book("\"title\":\"Book " + i + "\",\"author\":\"Author " + i
                        + "\",\"genre\":\"Genre\",\"finishedTimeStamp\":\"" + VALID_TIMESTAMP + "\""))
                .collect(Collectors.toList());
    }

    private static String book(String fields) {
        return "{" + fields + "}";
    }

    private static String requestBody(List<String> books, Integer limit) {
        String limitField = limit != null ? ",\"limit\":" + limit : "";
        return "{\"recentBooks\":[" + String.join(",", books) + "]" + limitField + "}";
    }
}
