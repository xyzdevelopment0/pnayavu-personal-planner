package com.maximovich.planner.task.controller;

import com.maximovich.planner.task.domain.TaskStatus;
import com.maximovich.planner.task.dto.CreateTaskRequest;
import com.maximovich.planner.task.dto.TaskResponse;
import com.maximovich.planner.task.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @GetMapping
    public List<TaskResponse> find(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) String name
    ) {
        return taskService.find(status, name);
    }
}
