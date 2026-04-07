package com.maximovich.planner.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String REQUEST_VALIDATION_FAILED_MESSAGE = "Request validation failed";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
        ResourceNotFoundException ex,
        HttpServletRequest request
    ) {
        logHandledException(HttpStatus.NOT_FOUND, request, "Resource not found: {}", ex.getMessage());
        return buildResponse(
            HttpStatus.NOT_FOUND,
            ApiErrorCode.RESOURCE_NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
        BusinessException ex,
        HttpServletRequest request
    ) {
        logHandledException(ex.getStatus(), request, "Business rule violated: {}", ex.getMessage());
        return buildResponse(
            ex.getStatus(),
            ApiErrorCode.BUSINESS_RULE_VIOLATION,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
        IllegalStateException ex,
        HttpServletRequest request
    ) {
        logHandledException(HttpStatus.BAD_REQUEST, request, "Illegal state: {}", ex.getMessage());
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.BUSINESS_RULE_VIOLATION,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ApiFieldError> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .toList();
        logHandledException(HttpStatus.BAD_REQUEST, request, "Request body validation failed: {}", details);
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.VALIDATION_ERROR,
            REQUEST_VALIDATION_FAILED_MESSAGE,
            request,
            details
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
        HandlerMethodValidationException ex,
        HttpServletRequest request
    ) {
        List<ApiFieldError> details = ex.getAllValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream()
                .map(error -> new ApiFieldError(resolveParameterName(result), error.getDefaultMessage())))
            .toList();
        logHandledException(HttpStatus.BAD_REQUEST, request, "Handler method validation failed: {}", details);
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.VALIDATION_ERROR,
            REQUEST_VALIDATION_FAILED_MESSAGE,
            request,
            details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        List<ApiFieldError> details = ex.getConstraintViolations().stream()
            .map(violation -> new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
        logHandledException(HttpStatus.BAD_REQUEST, request, "Constraint violation: {}", details);
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.VALIDATION_ERROR,
            REQUEST_VALIDATION_FAILED_MESSAGE,
            request,
            details
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        logHandledException(
            HttpStatus.BAD_REQUEST,
            request,
            "Parameter type mismatch for {}: {}",
            ex.getName(),
            ex.getValue()
        );
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.INVALID_REQUEST,
            "Invalid value for parameter %s".formatted(ex.getName()),
            request,
            List.of(new ApiFieldError(ex.getName(), "Invalid value"))
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldPath = invalidFormatException.getPath().stream()
                .map(Reference::getFieldName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
            logHandledException(
                HttpStatus.BAD_REQUEST,
                request,
                "Invalid JSON value for {}: {}",
                fieldPath,
                invalidFormatException.getValue()
            );
            return buildResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.INVALID_REQUEST,
                "Invalid value for field %s".formatted(fieldPath),
                request,
                List.of(new ApiFieldError(fieldPath, "Invalid value"))
            );
        }
        logHandledException(HttpStatus.BAD_REQUEST, request, "Malformed JSON request: {}", ex.getMessage());
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.INVALID_REQUEST,
            "Malformed JSON request",
            request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex,
        HttpServletRequest request
    ) {
        logHandledException(
            HttpStatus.BAD_REQUEST,
            request,
            "Missing request parameter: {}",
            ex.getParameterName()
        );
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ApiErrorCode.INVALID_REQUEST,
            "Missing required parameter %s".formatted(ex.getParameterName()),
            request,
            List.of(new ApiFieldError(ex.getParameterName(), "Parameter is required"))
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        HttpServletRequest request
    ) {
        logHandledException(
            HttpStatus.CONFLICT,
            request,
            "Database constraint violation: {}",
            ex.getMostSpecificCause().getMessage()
        );
        return buildResponse(
            HttpStatus.CONFLICT,
            ApiErrorCode.DATA_INTEGRITY_VIOLATION,
            "Request violates database constraints",
            request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request
    ) {
        logHandledException(HttpStatus.METHOD_NOT_ALLOWED, request, "Method not allowed: {}", ex.getMethod());
        return buildResponse(
            HttpStatus.METHOD_NOT_ALLOWED,
            ApiErrorCode.METHOD_NOT_ALLOWED,
            "HTTP method %s is not supported for this endpoint".formatted(ex.getMethod()),
            request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex,
        HttpServletRequest request
    ) {
        String contentType = ex.getContentType() == null ? "unknown" : ex.getContentType().toString();
        logHandledException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, "Unsupported media type: {}", contentType);
        return buildResponse(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            ApiErrorCode.UNSUPPORTED_MEDIA_TYPE,
            "Content type %s is not supported".formatted(contentType),
            request
        );
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(
        Exception ex,
        HttpServletRequest request
    ) {
        logHandledException(HttpStatus.NOT_FOUND, request, "Endpoint not found: {}", request.getRequestURI());
        return buildResponse(
            HttpStatus.NOT_FOUND,
            ApiErrorCode.ENDPOINT_NOT_FOUND,
            "Endpoint %s was not found".formatted(request.getRequestURI()),
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(
        Exception ex,
        HttpServletRequest request
    ) {
        LOG.error("Unhandled exception", ex);
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ApiErrorCode.INTERNAL_ERROR,
            "Unexpected server error",
            request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
        HttpStatus status,
        ApiErrorCode code,
        String message,
        HttpServletRequest request
    ) {
        return buildResponse(status, code, message, request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
        HttpStatus status,
        ApiErrorCode code,
        String message,
        HttpServletRequest request,
        List<ApiFieldError> details
    ) {
        return ResponseEntity.status(status)
            .body(ApiErrorResponse.of(status, code, message, request.getRequestURI(), details));
    }

    private void logHandledException(
        HttpStatus status,
        HttpServletRequest request,
        String message,
        Object... arguments
    ) {
        LOG.warn("[{} {}] " + message, prependArguments(status.value(), request.getRequestURI(), arguments));
    }

    private Object[] prependArguments(Object first, Object second, Object[] rest) {
        Object[] arguments = new Object[rest.length + 2];
        arguments[0] = first;
        arguments[1] = second;
        System.arraycopy(rest, 0, arguments, 2, rest.length);
        return arguments;
    }

    private ApiFieldError toFieldError(FieldError error) {
        return new ApiFieldError(error.getField(), error.getDefaultMessage());
    }

    private String resolveParameterName(ParameterValidationResult result) {
        String parameterName = result.getMethodParameter().getParameterName();
        return parameterName == null ? "parameter" : parameterName;
    }
}
