package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.TaskComment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Task comment response")
public record TaskCommentResponse(
    @Schema(description = "Comment identifier", example = "1")
    Long id,
    @Schema(description = "Comment text", example = "Add validation examples to Swagger")
    String content,
    @Schema(description = "Task identifier", example = "1")
    Long taskId,
    @Schema(description = "Author user identifier", example = "2")
    Long authorId,
    @Schema(description = "Creation timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2026-04-01T18:42:31.123")
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
