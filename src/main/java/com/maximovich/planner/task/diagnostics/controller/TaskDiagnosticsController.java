package com.maximovich.planner.task.diagnostics.controller;

import com.maximovich.planner.task.diagnostics.dto.NPlusOneDemoResponse;
import com.maximovich.planner.task.diagnostics.dto.TransactionDemoResponse;
import com.maximovich.planner.task.diagnostics.service.TaskDiagnosticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/diagnostics")
public class TaskDiagnosticsController {

    private final TaskDiagnosticsService taskDiagnosticsService;

    public TaskDiagnosticsController(TaskDiagnosticsService taskDiagnosticsService) {
        this.taskDiagnosticsService = taskDiagnosticsService;
    }

    @GetMapping("/n-plus-one")
    public NPlusOneDemoResponse demonstrateNPlusOne() {
        return taskDiagnosticsService.demonstrateNPlusOne();
    }

    @PostMapping("/transactions/without-transaction")
    public TransactionDemoResponse demonstrateWithoutTransaction() {
        return taskDiagnosticsService.demonstrateWithoutTransaction();
    }

    @PostMapping("/transactions/with-transaction")
    public TransactionDemoResponse demonstrateWithTransaction() {
        return taskDiagnosticsService.demonstrateWithTransaction();
    }
}
