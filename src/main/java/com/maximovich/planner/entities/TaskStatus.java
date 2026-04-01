package com.maximovich.planner.entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task lifecycle status")
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
