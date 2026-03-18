package com.maximovich.planner.services;

import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import java.util.List;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse getById(Long id);

    List<TaskResponse> findAll();

    TaskResponse update(Long id, CreateTaskRequest request);

    void delete(Long id);
}
