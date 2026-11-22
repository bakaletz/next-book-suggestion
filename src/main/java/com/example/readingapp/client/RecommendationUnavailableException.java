package com.example.readingapp.client;

public class RecommendationUnavailableException extends RuntimeException {

    public RecommendationUnavailableException(Throwable cause) {
        super("Recommendation provider is unavailable", cause);
    }
}
