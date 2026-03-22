package com.maximovich.planner.services;

import com.maximovich.planner.dtos.CachedTaskSearchResult;
import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.TaskStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse getById(Long id);

    List<TaskResponse> findAll();

    CachedTaskSearchResult searchWithJpql(String projectName, String ownerEmail, TaskStatus status, Pageable pageable);

    CachedTaskSearchResult searchWithNative(
        String projectName,
        String ownerEmail,
        TaskStatus status,
        Pageable pageable
    );

    TaskResponse update(Long id, CreateTaskRequest request);

    void delete(Long id);
}
