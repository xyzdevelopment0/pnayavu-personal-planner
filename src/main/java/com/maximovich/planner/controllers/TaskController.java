package com.maximovich.planner.controllers;

import com.maximovich.planner.dtos.CachedTaskSearchResult;
import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.dtos.TransactionDiagnosticsResponse;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.exceptions.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.maximovich.planner.services.TaskService;
import com.maximovich.planner.services.TaskTransactionDiagnosticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Tasks", description = "Operations with planner tasks")
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskTransactionDiagnosticsService taskTransactionDiagnosticsService;

    public TaskController(
        TaskService taskService,
        TaskTransactionDiagnosticsService taskTransactionDiagnosticsService
    ) {
        this.taskService = taskService;
        this.taskTransactionDiagnosticsService = taskTransactionDiagnosticsService;
    }

    @Operation(summary = "Create task", description = "Creates a new task")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(
        responseCode = "404",
        description = "Project or assignee not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }

    @Operation(summary = "Get task by id", description = "Returns a task by identifier")
    @ApiResponse(
        responseCode = "404",
        description = "Task not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public TaskResponse getById(
        @Parameter(description = "Task identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        return taskService.getById(id);
    }

    @Operation(
        summary = "Search tasks with JPQL",
        description = "Filters tasks by project name, owner email and status using JPQL query"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Paginated task list returned. Header X-Task-Search-Cache contains HIT or MISS."
    )
    @GetMapping("/search/jpql")
    public ResponseEntity<Page<TaskResponse>> searchWithJpql(
        @Parameter(description = "Project name fragment", example = "laboratory")
        @RequestParam(required = false)
        @Size(max = 120)
        String projectName,
        @Parameter(description = "Project owner email", example = "alice@example.com")
        @RequestParam(required = false)
        @Email
        @Size(max = 255)
        String ownerEmail,
        @Parameter(description = "Task status", example = "TODO")
        @RequestParam(required = false)
        TaskStatus status,
        @ParameterObject
        @PageableDefault(size = 10)
        Pageable pageable
    ) {
        return toResponse(taskService.searchWithJpql(projectName, ownerEmail, status, pageable));
    }

    @Operation(
        summary = "Search tasks with native SQL",
        description = "Filters tasks by project name, owner email and status using native SQL query"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Paginated task list returned. Header X-Task-Search-Cache contains HIT or MISS."
    )
    @GetMapping("/search/native")
    public ResponseEntity<Page<TaskResponse>> searchWithNative(
        @Parameter(description = "Project name fragment", example = "laboratory")
        @RequestParam(required = false)
        @Size(max = 120)
        String projectName,
        @Parameter(description = "Project owner email", example = "alice@example.com")
        @RequestParam(required = false)
        @Email
        @Size(max = 255)
        String ownerEmail,
        @Parameter(description = "Task status", example = "TODO")
        @RequestParam(required = false)
        TaskStatus status,
        @ParameterObject
        @PageableDefault(size = 10)
        Pageable pageable
    ) {
        return toResponse(taskService.searchWithNative(projectName, ownerEmail, status, pageable));
    }

    @Operation(summary = "Get all tasks", description = "Returns all tasks ordered by id")
    @GetMapping
    public List<TaskResponse> findAll() {
        return taskService.findAll();
    }

    @Operation(summary = "Update task", description = "Updates an existing task")
    @ApiResponse(
        responseCode = "404",
        description = "Task, project or assignee not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PutMapping("/{id}")
    public TaskResponse update(
        @Parameter(description = "Task identifier", example = "1")
        @PathVariable
        @Positive
        Long id,
        @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.update(id, request);
    }

    @Operation(summary = "Delete task", description = "Deletes a task by identifier")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(
        responseCode = "404",
        description = "Task not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "Task identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        taskService.delete(id);
    }

    @Operation(
        summary = "Demonstrate partial persistence without shared transaction",
        description = """
            Runs a failing multi-step write without method-level transaction
            and returns what remained in the database
            """
    )
    @PostMapping("/diagnostics/transactions/without-transaction")
    public TransactionDiagnosticsResponse demonstrateWithoutTransaction() {
        return taskTransactionDiagnosticsService.demonstrateWithoutTransaction();
    }

    @Operation(
        summary = "Demonstrate rollback with shared transaction",
        description = """
            Runs the same failing multi-step write inside @Transactional
            and returns the database state after rollback
            """
    )
    @PostMapping("/diagnostics/transactions/with-transaction")
    public TransactionDiagnosticsResponse demonstrateWithTransaction() {
        return taskTransactionDiagnosticsService.demonstrateWithTransaction();
    }

    private ResponseEntity<Page<TaskResponse>> toResponse(CachedTaskSearchResult result) {
        return ResponseEntity.ok()
            .header("X-Task-Search-Cache", result.cached() ? "HIT" : "MISS")
            .body(result.page());
    }
}
