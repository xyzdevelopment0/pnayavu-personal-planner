package com.maximovich.planner.task.dto;

import com.maximovich.planner.task.domain.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    LocalDate dueDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
