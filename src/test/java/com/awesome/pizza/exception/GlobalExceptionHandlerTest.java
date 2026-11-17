package com.awesome.pizza.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    void shouldHandleOrderNotFoundException() {
        ResponseEntity<ApiError> response = handler.handleOrderNotFound(new OrderNotFoundException("Order not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Order not found");
    }

    @Test
    void shouldHandleOrderCompletionExceptionAsConflict() {
        ResponseEntity<ApiError> response = handler.handleOrderCompletionFailure(new OrderCompletionException("Cannot complete"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Cannot complete");
    }

    @Test
    void shouldHandleConcurrentUpdateAsConflict() {
        ResponseEntity<ApiError> response = handler.handleConcurrentUpdate(
                new ObjectOptimisticLockingFailureException("PizzaOrder", "id"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        ResponseEntity<ApiError> response = handler.handleIllegalArgument(new IllegalArgumentException("Invalid input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid input");
    }

    @Test
    void shouldNotLeakInternalDetailsOnUnexpectedErrors() {
        ResponseEntity<ApiError> response = handler.handleGeneric(new RuntimeException("db password is 1234"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
        assertThat(response.getBody().details()).isEmpty();
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
