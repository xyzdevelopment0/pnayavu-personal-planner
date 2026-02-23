package com.maximovich.planner.common;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task with id=%d not found".formatted(id));
    }
}
