package com.maximovich.planner.task.service;

import com.maximovich.planner.common.TaskNotFoundException;
import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.domain.TaskStatus;
import com.maximovich.planner.task.dto.CreateTaskRequest;
import com.maximovich.planner.task.dto.TaskResponse;
import com.maximovich.planner.task.mapper.TaskMapper;
import com.maximovich.planner.task.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse getById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponse> find(TaskStatus status, String name) {
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        return taskRepository.findAllByFilters(status, normalizedName).stream().map(taskMapper::toResponse).toList();
    }
}
