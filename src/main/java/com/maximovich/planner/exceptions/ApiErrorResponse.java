package com.maximovich.planner.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;

@Schema(description = "Unified error response")
public record ApiErrorResponse(
    @Schema(description = "Error creation timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime timestamp,
    @Schema(description = "HTTP status code", example = "400")
    int status,
    @Schema(description = "HTTP status text", example = "Bad Request")
    String error,
    @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
    ApiErrorCode code,
    @Schema(description = "Human-readable error message", example = "Request validation failed")
    String message,
    @Schema(description = "Request path", example = "/api/users")
    String path,
    @Schema(description = "Additional validation details")
    List<ApiFieldError> details
) {

    public ApiErrorResponse {
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ApiErrorResponse of(
        HttpStatus status,
        ApiErrorCode code,
        String message,
        String path
    ) {
        return of(status, code, message, path, List.of());
    }

    public static ApiErrorResponse of(
        HttpStatus status,
        ApiErrorCode code,
        String message,
        String path,
        List<ApiFieldError> details
    ) {
        return new ApiErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            code,
            message,
            path,
            details
        );
    }
}
