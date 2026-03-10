package com.maximovich.planner.task.dto;

import com.maximovich.planner.task.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record CreateTaskRequest(
    @NotBlank @Size(max = 120) String title,
    @Size(max = 500) String description,
    TaskStatus status,
    LocalDate dueDate,
    @NotNull Long projectId,
    @NotNull Long assigneeId,
    Set<Long> tagIds
) {
}
