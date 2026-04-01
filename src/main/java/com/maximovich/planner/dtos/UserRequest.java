package com.maximovich.planner.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a user")
public record UserRequest(
    @Schema(description = "User full name", example = "Alice Novak")
    @NotBlank
    @Size(max = 80)
    String name,
    @Schema(description = "User email", example = "alice@example.com")
    @NotBlank
    @Email
    @Size(max = 255)
    String email
) {
}
