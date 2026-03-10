package com.maximovich.planner.task.diagnostics.dto;

public record TransactionDemoResponse(
    String mode,
    boolean rolledBack,
    String errorMessage,
    EntityCountSnapshot before,
    EntityCountSnapshot after
) {
}
