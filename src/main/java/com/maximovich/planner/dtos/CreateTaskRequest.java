package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Request for creating or updating a task")
public record CreateTaskRequest(
    @Schema(description = "Task title", example = "Demo bulk success 1")
    @NotBlank
    @Size(max = 120)
    String title,
    @Schema(description = "Task description", example = "First valid task")
    @Size(max = 500)
    String description,
    @Schema(description = "Task status. Defaults to TODO when omitted", example = "TODO")
    TaskStatus status,
    @Schema(description = "Task due date", example = "2026-05-14")
    LocalDate dueDate,
    @Schema(description = "Project identifier", example = "1")
    @NotNull
    @Positive
    Long projectId,
    @Schema(description = "Assignee user identifier", example = "1")
    @NotNull
    @Positive
    Long assigneeId,
    @Schema(description = "Identifiers of related tags", example = "[1, 2]")
    Set<@Positive Long> tagIds
) {
}
