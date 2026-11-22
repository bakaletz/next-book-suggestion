package com.example.readingapp.service;

import com.example.readingapp.client.OpenAiRecommendationClient;
import com.example.readingapp.dto.BookDto;
import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationRequest;
import com.example.readingapp.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    static final int MAX_RECENT_BOOKS = 5;

    private final OpenAiRecommendationClient openAiRecommendationClient;

    public RecommendationResponse recommend(RecommendationRequest request) {
        List<BookDto> mostRecentBooks = request.recentBooks().stream()
                .sorted(Comparator.comparing(BookDto::finishedTimeStamp).reversed())
                .limit(MAX_RECENT_BOOKS)
                .toList();
        int limit = request.limitOrDefault();
        RecommendationResponse modelResponse = openAiRecommendationClient.recommend(mostRecentBooks, limit);
        return new RecommendationResponse(keepUnreadUniqueBooks(modelResponse, request.recentBooks(), limit));
    }

    private static List<RecommendationDto> keepUnreadUniqueBooks(
            RecommendationResponse modelResponse, List<BookDto> readBooks, int limit) {
        if (modelResponse == null || modelResponse.recommendations() == null) {
            return List.of();
        }
        Set<String> knownBooks = readBooks.stream()
                .map(book -> bookKey(book.title(), book.author()))
                .collect(Collectors.toCollection(HashSet::new));
        return modelResponse.recommendations().stream()
                .filter(Objects::nonNull)
                .filter(recommendation -> recommendation.title() != null && !recommendation.title().isBlank())
                .filter(recommendation -> knownBooks.add(bookKey(recommendation.title(), recommendation.author())))
                .limit(limit)
                .toList();
    }

    private static String bookKey(String title, String author) {
        return normalize(title) + "|" + normalize(author);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
