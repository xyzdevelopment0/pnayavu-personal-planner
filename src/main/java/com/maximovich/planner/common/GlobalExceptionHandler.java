package com.maximovich.planner.common;

import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ApiErrorResponse(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({BusinessException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBusiness(RuntimeException ex) {
        return new ApiErrorResponse(ex.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return new ApiErrorResponse("Request violates database constraints", LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
            .orElse("Validation failed");
        return new ApiErrorResponse(message, LocalDateTime.now());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ApiErrorResponse("Invalid value for parameter %s".formatted(ex.getName()), LocalDateTime.now());
    }
}
