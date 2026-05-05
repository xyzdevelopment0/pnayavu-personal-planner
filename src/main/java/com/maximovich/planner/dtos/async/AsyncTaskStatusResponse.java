package com.maximovich.planner.dtos.async;

public record AsyncTaskStatusResponse(
    String taskId,
    AsyncTaskStatus status,
    Long result,
    String error
) {
}
