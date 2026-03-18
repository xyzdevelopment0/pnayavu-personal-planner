package com.maximovich.planner.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Size(max = 80) String name,
    @NotBlank @Email String email
) {
}
