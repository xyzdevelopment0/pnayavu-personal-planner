package com.maximovich.planner.task.service;

import com.maximovich.planner.task.domain.TaskStatus;
import com.maximovich.planner.task.dto.CreateTaskRequest;
import com.maximovich.planner.task.dto.TaskResponse;
import java.util.List;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    TaskResponse getById(Long id);

    List<TaskResponse> find(TaskStatus status, String query);
}
