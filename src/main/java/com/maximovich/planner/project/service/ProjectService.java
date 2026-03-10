package com.maximovich.planner.project.service;

import com.maximovich.planner.common.ResourceNotFoundException;
import com.maximovich.planner.project.domain.Project;
import com.maximovich.planner.project.dto.ProjectRequest;
import com.maximovich.planner.project.dto.ProjectResponse;
import com.maximovich.planner.project.repository.ProjectRepository;
import com.maximovich.planner.user.domain.PlannerUser;
import com.maximovich.planner.user.repository.PlannerUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PlannerUserRepository plannerUserRepository;

    public ProjectService(ProjectRepository projectRepository, PlannerUserRepository plannerUserRepository) {
        this.projectRepository = projectRepository;
        this.plannerUserRepository = plannerUserRepository;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project project = new Project(
            request.name().trim(),
            trimDescription(request.description()),
            getOwner(request.ownerId())
        );
        return ProjectResponse.fromEntity(projectRepository.save(project));
    }

    public ProjectResponse getById(Long id) {
        return ProjectResponse.fromEntity(getEntity(id));
    }

    public List<ProjectResponse> findAll() {
        return projectRepository.findAllByOrderByIdAsc().stream().map(ProjectResponse::fromEntity).toList();
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getEntity(id);
        project.update(
            request.name().trim(),
            trimDescription(request.description()),
            getOwner(request.ownerId())
        );
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.delete(getEntity(id));
    }

    private Project getEntity(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private PlannerUser getOwner(Long ownerId) {
        return plannerUserRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));
    }

    private String trimDescription(String description) {
        return description == null ? null : description.trim();
    }
}
