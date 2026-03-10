package com.maximovich.planner.task.diagnostics.dto;

import java.util.List;

public record NPlusOneDemoResponse(
    long nPlusOneQueryCount,
    long optimizedQueryCount,
    List<ProjectTaskCountResponse> projects
) {
}
