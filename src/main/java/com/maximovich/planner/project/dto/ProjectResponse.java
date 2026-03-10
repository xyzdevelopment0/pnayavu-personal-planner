package com.maximovich.planner.project.dto;

import com.maximovich.planner.project.domain.Project;
import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    Long ownerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ProjectResponse fromEntity(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getOwner().getId(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}
