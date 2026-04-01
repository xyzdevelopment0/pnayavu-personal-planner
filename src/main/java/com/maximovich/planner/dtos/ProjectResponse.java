package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Project response")
public record ProjectResponse(
    @Schema(description = "Project identifier", example = "1")
    Long id,
    @Schema(description = "Project name", example = "JPA Laboratory")
    String name,
    @Schema(description = "Project description", example = "Fourth laboratory work")
    String description,
    @Schema(description = "Owner user identifier", example = "1")
    Long ownerId,
    @Schema(description = "Creation timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2026-04-01T18:42:31.123")
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
