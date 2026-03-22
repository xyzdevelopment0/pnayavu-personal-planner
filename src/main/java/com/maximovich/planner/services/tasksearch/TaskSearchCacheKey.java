package com.maximovich.planner.services.tasksearch;

import com.maximovich.planner.entities.TaskStatus;
import java.util.Objects;

public final class TaskSearchCacheKey {

    private final TaskSearchStrategy strategy;
    private final String projectName;
    private final String ownerEmail;
    private final TaskStatus status;
    private final int page;
    private final int size;

    public TaskSearchCacheKey(
        TaskSearchStrategy strategy,
        String projectName,
        String ownerEmail,
        TaskStatus status,
        int page,
        int size
    ) {
        this.strategy = strategy;
        this.projectName = projectName;
        this.ownerEmail = ownerEmail;
        this.status = status;
        this.page = page;
        this.size = size;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TaskSearchCacheKey other)) {
            return false;
        }
        return page == other.page
            && size == other.size
            && strategy == other.strategy
            && Objects.equals(projectName, other.projectName)
            && Objects.equals(ownerEmail, other.ownerEmail)
            && status == other.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategy, projectName, ownerEmail, status, page, size);
    }
}
