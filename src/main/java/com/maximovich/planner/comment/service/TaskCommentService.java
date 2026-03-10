package com.maximovich.planner.comment.service;

import com.maximovich.planner.comment.domain.TaskComment;
import com.maximovich.planner.comment.dto.TaskCommentRequest;
import com.maximovich.planner.comment.dto.TaskCommentResponse;
import com.maximovich.planner.comment.repository.TaskCommentRepository;
import com.maximovich.planner.common.ResourceNotFoundException;
import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.repository.TaskRepository;
import com.maximovich.planner.user.domain.PlannerUser;
import com.maximovich.planner.user.repository.PlannerUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final PlannerUserRepository plannerUserRepository;

    public TaskCommentService(
        TaskCommentRepository taskCommentRepository,
        TaskRepository taskRepository,
        PlannerUserRepository plannerUserRepository
    ) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.plannerUserRepository = plannerUserRepository;
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

    private PlannerUser getAuthor(Long authorId) {
        return plannerUserRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", authorId));
    }
}
