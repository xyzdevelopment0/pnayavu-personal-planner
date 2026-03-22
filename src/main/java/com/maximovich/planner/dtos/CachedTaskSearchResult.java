package com.maximovich.planner.dtos;

import org.springframework.data.domain.Page;

public record CachedTaskSearchResult(Page<TaskResponse> page, boolean cached) {
}
