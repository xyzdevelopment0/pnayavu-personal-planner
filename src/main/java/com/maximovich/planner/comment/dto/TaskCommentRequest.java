package com.maximovich.planner.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskCommentRequest(
    @NotBlank @Size(max = 500) String content,
    @NotNull Long taskId,
    @NotNull Long authorId
) {
}
