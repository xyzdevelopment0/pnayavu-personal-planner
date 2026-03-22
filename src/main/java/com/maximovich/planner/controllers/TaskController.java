package com.maximovich.planner.controllers;

import com.maximovich.planner.dtos.CachedTaskSearchResult;
import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.services.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/search/jpql")
    public ResponseEntity<Page<TaskResponse>> searchWithJpql(
        @RequestParam(required = false) String projectName,
        @RequestParam(required = false) String ownerEmail,
        @RequestParam(required = false) TaskStatus status,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return toResponse(taskService.searchWithJpql(projectName, ownerEmail, status, pageable));
    }

    @GetMapping("/search/native")
    public ResponseEntity<Page<TaskResponse>> searchWithNative(
        @RequestParam(required = false) String projectName,
        @RequestParam(required = false) String ownerEmail,
        @RequestParam(required = false) TaskStatus status,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return toResponse(taskService.searchWithNative(projectName, ownerEmail, status, pageable));
    }

    @GetMapping
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody CreateTaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    private ResponseEntity<Page<TaskResponse>> toResponse(CachedTaskSearchResult result) {
        return ResponseEntity.ok()
            .header("X-Task-Search-Cache", result.cached() ? "HIT" : "MISS")
            .body(result.page());
    }
}
