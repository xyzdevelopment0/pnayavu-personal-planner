package com.maximovich.planner.user.dto;

import com.maximovich.planner.user.domain.PlannerUser;
import java.time.LocalDateTime;

public record PlannerUserResponse(
    Long id,
    String name,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static PlannerUserResponse fromEntity(PlannerUser user) {
        return new PlannerUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
