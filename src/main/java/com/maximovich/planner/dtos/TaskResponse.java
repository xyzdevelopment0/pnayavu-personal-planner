package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Task response")
public record TaskResponse(
    @Schema(description = "Task identifier", example = "1")
    Long id,
    @Schema(description = "Task title", example = "Prepare Swagger documentation")
    String title,
    @Schema(description = "Task description", example = "Document all REST endpoints")
    String description,
    @Schema(description = "Task status", example = "TODO")
    TaskStatus status,
    @Schema(description = "Task due date", example = "2026-04-10")
    LocalDate dueDate,
    @Schema(description = "Project identifier", example = "1")
    Long projectId,
    @Schema(description = "Assignee user identifier", example = "2")
    Long assigneeId,
    @Schema(description = "Identifiers of related tags", example = "[1, 2]")
    Set<Long> tagIds,
    @Schema(description = "Creation timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime updatedAt
) {
}
