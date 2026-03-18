package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.TaskComment;
import java.time.LocalDateTime;

public record TaskCommentResponse(
    Long id,
    String content,
    Long taskId,
    Long authorId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static TaskCommentResponse fromEntity(TaskComment comment) {
        return new TaskCommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getTask().getId(),
            comment.getAuthor().getId(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
