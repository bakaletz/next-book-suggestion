package com.example.readingapp.service;

import com.example.readingapp.client.OpenAiRecommendationClient;
import com.example.readingapp.dto.BookDto;
import com.example.readingapp.dto.RecommendationRequest;
import com.example.readingapp.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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
        return openAiRecommendationClient.recommend(mostRecentBooks, request.limitOrDefault());
    }
}
