package com.example.readingapp.service;

import com.example.readingapp.dto.RecommendationDto;
import com.example.readingapp.dto.RecommendationRequest;
import com.example.readingapp.dto.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private static final List<RecommendationDto> MOCK_RECOMMENDATIONS = List.of(
            new RecommendationDto("Hyperion", "Dan Simmons", "Science Fiction", "Mocked recommendation"),
            new RecommendationDto("The Name of the Wind", "Patrick Rothfuss", "Fantasy", "Mocked recommendation"),
            new RecommendationDto("Sapiens", "Yuval Noah Harari", "History", "Mocked recommendation"),
            new RecommendationDto("The Martian", "Andy Weir", "Science Fiction", "Mocked recommendation"),
            new RecommendationDto("Atomic Habits", "James Clear", "Self-Help", "Mocked recommendation")
    );

    public RecommendationResponse recommend(RecommendationRequest request) {
        return new RecommendationResponse(MOCK_RECOMMENDATIONS.stream()
                .limit(request.limitOrDefault())
                .toList());
    }
}
