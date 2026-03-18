package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.User;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    LocalDateTime createdAt,
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
