package com.maximovich.planner.task.mapper;

import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.dto.CreateTaskRequest;
import com.maximovich.planner.task.dto.TaskResponse;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public Task toEntity(CreateTaskRequest request) {
        return new Task(
            request.getTitle().trim(),
            request.getDescription(),
            request.getStatus(),
            request.getDueDate()
        );
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
