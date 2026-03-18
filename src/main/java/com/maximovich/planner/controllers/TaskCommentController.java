package com.maximovich.planner.controllers;

import com.maximovich.planner.dtos.TaskCommentRequest;
import com.maximovich.planner.dtos.TaskCommentResponse;
import com.maximovich.planner.services.TaskCommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCommentResponse create(@Valid @RequestBody TaskCommentRequest request) {
        return taskCommentService.create(request);
    }

    @GetMapping("/{id}")
    public TaskCommentResponse getById(@PathVariable Long id) {
        return taskCommentService.getById(id);
    }

    @GetMapping
    public List<TaskCommentResponse> findAll() {
        return taskCommentService.findAll();
    }

    @PutMapping("/{id}")
    public TaskCommentResponse update(@PathVariable Long id, @Valid @RequestBody TaskCommentRequest request) {
        return taskCommentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskCommentService.delete(id);
    }
}
