package com.maximovich.planner.services;

import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.dtos.ProjectRequest;
import com.maximovich.planner.dtos.ProjectResponse;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.repositories.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

    private User getOwner(Long ownerId) {
        return userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));
    }

    private String trimDescription(String description) {
        return description == null ? null : description.trim();
    }
}
