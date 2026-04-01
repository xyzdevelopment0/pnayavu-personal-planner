package com.maximovich.planner.controllers;

import com.maximovich.planner.dtos.TagRequest;
import com.maximovich.planner.dtos.TagResponse;
import com.maximovich.planner.exceptions.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.maximovich.planner.services.TagService;
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
@Tag(name = "Tags", description = "Operations with task tags")
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "Create tag", description = "Creates a new tag")
    @ApiResponse(responseCode = "201", description = "Tag created")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody TagRequest request) {
        return tagService.create(request);
    }

    @Operation(summary = "Get tag by id", description = "Returns a tag by identifier")
    @ApiResponse(
        responseCode = "404",
        description = "Tag not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public TagResponse getById(
        @Parameter(description = "Tag identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        return tagService.getById(id);
    }

    @Operation(summary = "Get all tags", description = "Returns all tags ordered by id")
    @GetMapping
    public List<TagResponse> findAll() {
        return tagService.findAll();
    }

    @Operation(summary = "Update tag", description = "Updates an existing tag")
    @ApiResponse(
        responseCode = "404",
        description = "Tag not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PutMapping("/{id}")
    public TagResponse update(
        @Parameter(description = "Tag identifier", example = "1")
        @PathVariable
        @Positive
        Long id,
        @Valid @RequestBody TagRequest request
    ) {
        return tagService.update(id, request);
    }

    @Operation(summary = "Delete tag", description = "Deletes a tag by identifier")
    @ApiResponse(responseCode = "204", description = "Tag deleted")
    @ApiResponse(
        responseCode = "404",
        description = "Tag not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "Tag identifier", example = "1")
        @PathVariable
        @Positive
        Long id
    ) {
        tagService.delete(id);
    }
}
