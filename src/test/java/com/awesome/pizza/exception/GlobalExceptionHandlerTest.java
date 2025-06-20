package com.awesome.pizza.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleInvalidPizzaException() {
        InvalidPizzaException ex = new InvalidPizzaException("Pizza not available");
        ResponseEntity<ApiError> response = handler.handleInvalidPizza(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Pizza not available");
    }

    @Test
    void shouldHandleOrderNotFoundException() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found");
        ResponseEntity<ApiError> response = handler.handleOrderNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Order not found");
    }

    @Test
    void shouldHandleNoPendingOrdersException() {
        NoPendingOrdersException ex = new NoPendingOrdersException("Nothing to prepare");
        ResponseEntity<ApiError> response = handler.handleNoPendingOrders(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Nothing to prepare");
    }

    @Test
    void shouldHandleOrderCompletionException() {
        OrderCompletionException ex = new OrderCompletionException("Cannot complete");
        ResponseEntity<ApiError> response = handler.handleOrderCompletionFailure(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Cannot complete");
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid input");
    }

    @Test
    void shouldHandleGenericException() {
        RuntimeException ex = new RuntimeException("Unexpected error");
        ResponseEntity<ApiError> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
    }

    @Test
    void shouldHandleValidationError() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ApiError> response = handler.handleValidationError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().details()).contains("field: must not be null");
    }
}