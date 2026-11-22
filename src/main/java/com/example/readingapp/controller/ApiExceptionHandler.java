package com.example.readingapp.controller;

import com.example.readingapp.client.RecommendationUnavailableException;
import com.example.readingapp.dto.FieldValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationError(MethodArgumentNotValidException exception) {
        List<FieldValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldValidationError(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldValidationError::field))
                .toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(RecommendationUnavailableException.class)
    public ProblemDetail handleRecommendationUnavailable(RecommendationUnavailableException exception) {
        log.error("Recommendation provider call failed", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Recommendations are temporarily unavailable, please try again later");
    }
}
