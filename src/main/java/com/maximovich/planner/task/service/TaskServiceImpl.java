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
    public List<TaskResponse> find(TaskStatus status, String query) {
        List<Task> tasks;
        if (status != null && hasText(query)) {
            tasks = taskRepository
                .findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(status, normalize(query));
        } else if (status != null) {
            tasks = taskRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (hasText(query)) {
            tasks = taskRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(normalize(query));
        } else {
            tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        }

        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value.trim();
    }
}
