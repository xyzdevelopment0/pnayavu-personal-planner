package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    LocalDate dueDate,
    Long projectId,
    Long assigneeId,
    Set<Long> tagIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
