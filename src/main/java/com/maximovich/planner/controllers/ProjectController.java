package com.maximovich.planner.controllers;

import com.maximovich.planner.dtos.ProjectRequest;
import com.maximovich.planner.dtos.ProjectResponse;
import com.maximovich.planner.exceptions.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.maximovich.planner.services.ProjectService;
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
@Tag(name = "Projects", description = "Operations with planner projects")
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Create project", description = "Creates a new project")
    @ApiResponse(responseCode = "201", description = "Project created")
    @ApiResponse(
        responseCode = "404",
        description = "Owner user not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @Operation(summary = "Get project by id", description = "Returns a project by identifier")
    @ApiResponse(
        responseCode = "404",
        description = "Project not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public ProjectResponse getById(
        @Parameter(description = "Project identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        return projectService.getById(id);
    }

    @Operation(summary = "Get all projects", description = "Returns all projects ordered by id")
    @GetMapping
    public List<ProjectResponse> findAll() {
        return projectService.findAll();
    }

    @Operation(summary = "Update project", description = "Updates an existing project")
    @ApiResponse(
        responseCode = "404",
        description = "Project or owner user not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PutMapping("/{id}")
    public ProjectResponse update(
        @Parameter(description = "Project identifier", example = "1")
        @PathVariable
        @Positive
        Long id,
        @Valid @RequestBody ProjectRequest request
    ) {
        return projectService.update(id, request);
    }

    @Operation(summary = "Delete project", description = "Deletes a project by identifier")
    @ApiResponse(responseCode = "204", description = "Project deleted")
    @ApiResponse(
        responseCode = "404",
        description = "Project not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "Project identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        projectService.delete(id);
    }
}
