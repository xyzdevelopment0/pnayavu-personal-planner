package com.maximovich.planner.task.diagnostics.dto;

public record EntityCountSnapshot(long users, long projects, long tasks, long comments, long tags) {
}
