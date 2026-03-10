package com.maximovich.planner.user.service;

import com.maximovich.planner.comment.repository.TaskCommentRepository;
import com.maximovich.planner.common.BusinessException;
import com.maximovich.planner.common.ResourceNotFoundException;
import com.maximovich.planner.project.repository.ProjectRepository;
import com.maximovich.planner.task.repository.TaskRepository;
import com.maximovich.planner.user.domain.PlannerUser;
import com.maximovich.planner.user.dto.PlannerUserRequest;
import com.maximovich.planner.user.dto.PlannerUserResponse;
import com.maximovich.planner.user.repository.PlannerUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlannerUserService {

    private final PlannerUserRepository plannerUserRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    public PlannerUserService(
        PlannerUserRepository plannerUserRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        TaskCommentRepository taskCommentRepository
    ) {
        this.plannerUserRepository = plannerUserRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    @Transactional
    public PlannerUserResponse create(PlannerUserRequest request) {
        String email = normalizeEmail(request.email());
        if (plannerUserRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("User with email %s already exists".formatted(email));
        }
        PlannerUser user = new PlannerUser(request.name().trim(), email);
        return PlannerUserResponse.fromEntity(plannerUserRepository.save(user));
    }

    public PlannerUserResponse getById(Long id) {
        return PlannerUserResponse.fromEntity(getEntity(id));
    }

    public List<PlannerUserResponse> findAll() {
        return plannerUserRepository.findAllByOrderByIdAsc().stream().map(PlannerUserResponse::fromEntity).toList();
    }

    @Transactional
    public PlannerUserResponse update(Long id, PlannerUserRequest request) {
        PlannerUser user = getEntity(id);
        String email = normalizeEmail(request.email());
        if (plannerUserRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("User with email %s already exists".formatted(email));
        }
        user.update(request.name().trim(), email);
        return PlannerUserResponse.fromEntity(user);
    }

    @Transactional
    public void delete(Long id) {
        PlannerUser user = getEntity(id);
        if (projectRepository.existsByOwnerId(id)) {
            throw new BusinessException("User %d owns projects and cannot be deleted".formatted(id));
        }
        if (taskRepository.existsByAssigneeId(id)) {
            throw new BusinessException("User %d is assigned to tasks and cannot be deleted".formatted(id));
        }
        if (taskCommentRepository.existsByAuthorId(id)) {
            throw new BusinessException("User %d has comments and cannot be deleted".formatted(id));
        }
        plannerUserRepository.delete(user);
    }

    private PlannerUser getEntity(Long id) {
        return plannerUserRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
