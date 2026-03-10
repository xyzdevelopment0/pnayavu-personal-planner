package com.maximovich.planner.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 500) String description,
    @NotNull Long ownerId
) {
}
