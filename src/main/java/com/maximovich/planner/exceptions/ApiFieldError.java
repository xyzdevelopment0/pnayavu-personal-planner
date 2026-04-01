package com.maximovich.planner.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Validation error details")
public record ApiFieldError(
    @Schema(description = "Field or parameter name", example = "email")
    String field,
    @Schema(description = "Validation error message", example = "must be a well-formed email address")
    String message
) {
}
