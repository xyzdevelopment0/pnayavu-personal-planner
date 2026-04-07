package com.maximovich.planner.components;

import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.TaskStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskSearchIndex {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSearchIndex.class);

    private final Map<CacheKey, Page<TaskResponse>> index = new HashMap<>();

    public synchronized Optional<Page<TaskResponse>> get(CacheKey key) {
        return Optional.ofNullable(index.get(key));
    }

    public synchronized void put(CacheKey key, Page<TaskResponse> page) {
        index.put(key, page);
    }

    public synchronized void clear() {
        int size = index.size();
        index.clear();
        LOG.info("Task search cache cleared: entries={}", size);
    }

    public record CacheKey(
        Strategy strategy,
        String projectName,
        String ownerEmail,
        TaskStatus status,
        int page,
        int size
    ) {
    }

    public enum Strategy {
        JPQL,
        NATIVE
    }
}
