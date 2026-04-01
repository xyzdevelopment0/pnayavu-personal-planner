package com.maximovich.planner.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction diagnostics response")
public record TransactionDiagnosticsResponse(
    @Schema(description = "Scenario identifier", example = "WITHOUT_TRANSACTION")
    String scenario,
    @Schema(description = "Unique marker to find demo rows in the database", example = "tx-4f3a8c2d")
    String marker,
    @Schema(description = "Whether the simulated operation failed", example = "true")
    boolean failed,
    @Schema(description = "Whether the database state was fully rolled back", example = "false")
    boolean rollbackApplied,
    @Schema(
        description = "Failure message from the simulated operation",
        example = "Transaction diagnostics failure for marker tx-4f3a8c2d"
    )
    String errorMessage,
    @Schema(description = "Number of persisted users matching the marker", example = "1")
    long persistedUsers,
    @Schema(description = "Number of persisted projects matching the marker", example = "1")
    long persistedProjects,
    @Schema(description = "Number of persisted tasks matching the marker", example = "1")
    long persistedTasks,
    @Schema(description = "Number of persisted comments matching the marker", example = "0")
    long persistedComments
) {
}
