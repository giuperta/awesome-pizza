package com.awesome.pizza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handling across the whole application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationError(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of("Illegal argument provided"));
    }

    @ExceptionHandler(InvalidPizzaException.class)
    public ResponseEntity<ApiError> handleInvalidPizza(InvalidPizzaException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of("Invalid pizza selection"));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFound(OrderNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), List.of("Order not found"));
    }

    @ExceptionHandler(NoPendingOrdersException.class)
    public ResponseEntity<ApiError> handleNoPendingOrders(NoPendingOrdersException ex) {
        return buildError(HttpStatus.NO_CONTENT, ex.getMessage(), List.of("No RECEIVED orders to prepare"));
    }

    @ExceptionHandler(OrderCompletionException.class)
    public ResponseEntity<ApiError> handleOrderCompletionFailure(OrderCompletionException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of("Order could not be completed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", List.of(ex.getMessage()));
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, List<String> details) {
        ApiError error = new ApiError(
                status.value(),
                message,
                LocalDateTime.now(),
                details
        );
        return new ResponseEntity<>(error, status);
    }
}
