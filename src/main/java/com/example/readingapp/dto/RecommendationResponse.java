package com.example.readingapp.dto;

import java.util.List;

public record RecommendationResponse(List<RecommendationDto> recommendations) {
}
