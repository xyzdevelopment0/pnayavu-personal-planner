package com.maximovich.planner.user.controller;

import com.maximovich.planner.user.dto.PlannerUserRequest;
import com.maximovich.planner.user.dto.PlannerUserResponse;
import com.maximovich.planner.user.service.PlannerUserService;
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
@RequestMapping("/api/users")
public class PlannerUserController {

    private final PlannerUserService plannerUserService;

    public PlannerUserController(PlannerUserService plannerUserService) {
        this.plannerUserService = plannerUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlannerUserResponse create(@Valid @RequestBody PlannerUserRequest request) {
        return plannerUserService.create(request);
    }

    @GetMapping("/{id}")
    public PlannerUserResponse getById(@PathVariable Long id) {
        return plannerUserService.getById(id);
    }

    @GetMapping
    public List<PlannerUserResponse> findAll() {
        return plannerUserService.findAll();
    }

    @PutMapping("/{id}")
    public PlannerUserResponse update(@PathVariable Long id, @Valid @RequestBody PlannerUserRequest request) {
        return plannerUserService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        plannerUserService.delete(id);
    }
}
