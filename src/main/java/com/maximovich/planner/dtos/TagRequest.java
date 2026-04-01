package com.maximovich.planner.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a tag")
public record TagRequest(
    @Schema(description = "Tag name", example = "swagger")
    @NotBlank
    @Size(max = 50)
    String name
) {
}
