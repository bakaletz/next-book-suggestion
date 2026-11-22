package com.example.readingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

public record BookDto(
        @NotBlank String title,
        @NotBlank String author,
        String genre,
        @NotNull @PastOrPresent Instant finishedTimeStamp
) {
}
