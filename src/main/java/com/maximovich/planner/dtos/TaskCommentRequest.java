package com.maximovich.planner.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a task comment")
public record TaskCommentRequest(
    @Schema(description = "Comment text", example = "Add validation examples to Swagger")
    @NotBlank
    @Size(max = 500)
    String content,
    @Schema(description = "Task identifier", example = "1")
    @NotNull
    @Positive
    Long taskId,
    @Schema(description = "Author user identifier", example = "2")
    @NotNull
    @Positive
    Long authorId
) {
}
