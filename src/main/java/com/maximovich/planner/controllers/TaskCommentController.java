package com.maximovich.planner.controllers;

import com.maximovich.planner.dtos.TaskCommentRequest;
import com.maximovich.planner.dtos.TaskCommentResponse;
import com.maximovich.planner.exceptions.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.maximovich.planner.services.TaskCommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Comments", description = "Operations with task comments")
@RequestMapping("/api/comments")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    public TaskCommentController(TaskCommentService taskCommentService) {
        this.taskCommentService = taskCommentService;
    }

    @Operation(summary = "Create comment", description = "Creates a new task comment")
    @ApiResponse(responseCode = "201", description = "Comment created")
    @ApiResponse(
        responseCode = "404",
        description = "Task or author not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCommentResponse create(@Valid @RequestBody TaskCommentRequest request) {
        return taskCommentService.create(request);
    }

    @Operation(summary = "Get comment by id", description = "Returns a comment by identifier")
    @ApiResponse(
        responseCode = "404",
        description = "Comment not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public TaskCommentResponse getById(
        @Parameter(description = "Comment identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        return taskCommentService.getById(id);
    }

    @Operation(summary = "Get all comments", description = "Returns all comments ordered by id")
    @GetMapping
    public List<TaskCommentResponse> findAll() {
        return taskCommentService.findAll();
    }

    @Operation(summary = "Update comment", description = "Updates an existing task comment")
    @ApiResponse(
        responseCode = "404",
        description = "Comment, task or author not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PutMapping("/{id}")
    public TaskCommentResponse update(
        @Parameter(description = "Comment identifier", example = "1")
        @PathVariable
        @Positive
        Long id,
        @Valid @RequestBody TaskCommentRequest request
    ) {
        return taskCommentService.update(id, request);
    }

    @Operation(summary = "Delete comment", description = "Deletes a task comment by identifier")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(
        responseCode = "404",
        description = "Comment not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "Comment identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        taskCommentService.delete(id);
    }
}
