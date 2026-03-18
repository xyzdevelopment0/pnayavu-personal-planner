package com.maximovich.planner.services;

import com.maximovich.planner.entities.TaskComment;
import com.maximovich.planner.dtos.TaskCommentRequest;
import com.maximovich.planner.dtos.TaskCommentResponse;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.repositories.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskCommentService(
        TaskCommentRepository taskCommentRepository,
        TaskRepository taskRepository,
        UserRepository userRepository
    ) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskCommentResponse create(TaskCommentRequest request) {
        TaskComment comment = new TaskComment(
            request.content().trim(),
            getTask(request.taskId()),
            getAuthor(request.authorId())
        );
        return TaskCommentResponse.fromEntity(taskCommentRepository.save(comment));
    }

    public TaskCommentResponse getById(Long id) {
        return TaskCommentResponse.fromEntity(getEntityWithRelations(id));
    }

    public List<TaskCommentResponse> findAll() {
        return taskCommentRepository.findAllWithRelations().stream().map(TaskCommentResponse::fromEntity).toList();
    }

    @Transactional
    public TaskCommentResponse update(Long id, TaskCommentRequest request) {
        TaskComment comment = getEntity(id);
        comment.update(request.content().trim(), getTask(request.taskId()), getAuthor(request.authorId()));
        return TaskCommentResponse.fromEntity(getEntityWithRelations(id));
    }

    @Transactional
    public void delete(Long id) {
        taskCommentRepository.delete(getEntity(id));
    }

    private TaskComment getEntity(Long id) {
        return taskCommentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    private TaskComment getEntityWithRelations(Long id) {
        return taskCommentRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private User getAuthor(Long authorId) {
        return userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", authorId));
    }
}
