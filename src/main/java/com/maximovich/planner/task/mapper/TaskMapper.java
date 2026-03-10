package com.maximovich.planner.task.mapper;

import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.dto.TaskResponse;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getDueDate(),
            task.getProject().getId(),
            task.getAssignee().getId(),
            task.getTags().stream()
                .map(tag -> tag.getId())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
