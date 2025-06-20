package com.awesome.pizza.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a structured error response.
 */
public record ApiError(
        int status,
        String message,
        LocalDateTime timestamp,
        List<String> details
) {}