package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "User response")
public record UserResponse(
    @Schema(description = "User identifier", example = "1")
    Long id,
    @Schema(description = "User full name", example = "Alice Novak")
    String name,
    @Schema(description = "User email", example = "alice@example.com")
    String email,
    @Schema(description = "Creation timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime updatedAt
) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
