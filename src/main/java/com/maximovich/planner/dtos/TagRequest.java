package com.maximovich.planner.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(@NotBlank @Size(max = 50) String name) {
}
