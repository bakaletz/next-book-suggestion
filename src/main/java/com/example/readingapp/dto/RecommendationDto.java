package com.example.readingapp.dto;

public record RecommendationDto(
        String title,
        String author,
        String genre,
        String reason
) {
}
