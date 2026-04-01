package com.maximovich.planner.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a project")
public record ProjectRequest(
    @Schema(description = "Project name", example = "JPA Laboratory")
    @NotBlank
    @Size(max = 120)
    String name,
    @Schema(description = "Project description", example = "Fourth laboratory work")
    @Size(max = 500)
    String description,
    @Schema(description = "Owner user identifier", example = "1")
    @NotNull
    @Positive
    Long ownerId
) {
}
