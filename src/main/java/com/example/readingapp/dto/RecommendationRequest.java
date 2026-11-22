package com.example.readingapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RecommendationRequest(
        @NotNull @Size(min = 5, max = 10) List<@Valid @NotNull BookDto> recentBooks,
        @Min(1) @Max(10) Integer limit
) {
    public static final int DEFAULT_LIMIT = 3;

    public int limitOrDefault() {
        return limit != null ? limit : DEFAULT_LIMIT;
    }
}
