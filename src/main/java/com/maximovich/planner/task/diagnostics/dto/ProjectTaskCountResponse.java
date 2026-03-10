package com.maximovich.planner.task.diagnostics.dto;

public record ProjectTaskCountResponse(Long projectId, String projectName, int taskCount) {
}
