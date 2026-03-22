package com.maximovich.planner.services.tasksearch;

import com.maximovich.planner.dtos.TaskResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskSearchIndex {

    private final Map<TaskSearchCacheKey, Page<TaskResponse>> index = new HashMap<>();

    public synchronized Optional<Page<TaskResponse>> get(TaskSearchCacheKey key) {
        return Optional.ofNullable(index.get(key));
    }

    public synchronized void put(TaskSearchCacheKey key, Page<TaskResponse> page) {
        index.put(key, page);
    }

    public synchronized void clear() {
        index.clear();
    }
}
