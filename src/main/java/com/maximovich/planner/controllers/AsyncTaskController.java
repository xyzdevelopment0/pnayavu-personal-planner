package com.maximovich.planner.controllers;

import com.maximovich.planner.components.concurrency.CounterRaceDemo;
import com.maximovich.planner.dtos.async.AsyncTaskStartResponse;
import com.maximovich.planner.dtos.async.AsyncTaskStatusResponse;
import com.maximovich.planner.dtos.async.CounterRaceResponse;
import com.maximovich.planner.services.AsyncBusinessOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/async")
public class AsyncTaskController {

    private final AsyncBusinessOperationService asyncBusinessOperationService;
    private final CounterRaceDemo counterRaceDemo;

    public AsyncTaskController(
        AsyncBusinessOperationService asyncBusinessOperationService,
        CounterRaceDemo counterRaceDemo
    ) {
        this.asyncBusinessOperationService = asyncBusinessOperationService;
        this.counterRaceDemo = counterRaceDemo;
    }

    @Operation(summary = "Start asynchronous task count operation")
    @PostMapping("/tasks/count")
    public AsyncTaskStartResponse startTaskCount(
        @Parameter(description = "Artificial delay in seconds", example = "20")
        @RequestParam(defaultValue = "20")
        @Min(0)
        @Max(60)
        int delaySeconds
    ) {
        return new AsyncTaskStartResponse(asyncBusinessOperationService.startTaskCountOperation(delaySeconds));
    }

    @Operation(summary = "Get asynchronous operation status")
    @GetMapping("/tasks/{taskId}")
    public AsyncTaskStatusResponse getStatus(@PathVariable String taskId) {
        return asyncBusinessOperationService.getStatus(taskId);
    }

    @Operation(summary = "Get completed asynchronous operations count")
    @GetMapping("/tasks/completed-count")
    public long getCompletedTasks() {
        return asyncBusinessOperationService.getCompletedTasks();
    }

    @Operation(summary = "Demonstrate race condition and thread-safe counters")
    @GetMapping("/race-condition")
    public CounterRaceResponse raceCondition(
        @RequestParam(defaultValue = "64")
        @Min(50)
        @Max(256)
        int threads,
        @RequestParam(defaultValue = "100000")
        @Min(1)
        @Max(500000)
        int incrementsPerThread
    ) {
        return counterRaceDemo.run(threads, incrementsPerThread);
    }
}
