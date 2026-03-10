package com.maximovich.planner.tag.controller;

import com.maximovich.planner.tag.dto.TagRequest;
import com.maximovich.planner.tag.dto.TagResponse;
import com.maximovich.planner.tag.service.TagService;
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
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody TagRequest request) {
        return tagService.create(request);
    }

    @GetMapping("/{id}")
    public TagResponse getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @GetMapping
    public List<TagResponse> findAll() {
        return tagService.findAll();
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return tagService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }
}
