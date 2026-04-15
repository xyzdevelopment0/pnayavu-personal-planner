package com.maximovich.planner.services;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.dtos.ProjectRequest;
import com.maximovich.planner.dtos.ProjectResponse;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskSearchIndex taskSearchIndex;

    public ProjectService(
        ProjectRepository projectRepository,
        UserRepository userRepository,
        TaskSearchIndex taskSearchIndex
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskSearchIndex = taskSearchIndex;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project project = new Project(
            request.name().trim(),
            trimDescription(request.description()),
            getOwner(request.ownerId())
        );
        ProjectResponse response = ProjectResponse.fromEntity(projectRepository.save(project));
        taskSearchIndex.clear();
        return response;
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
        ProjectResponse response = ProjectResponse.fromEntity(project);
        taskSearchIndex.clear();
        return response;
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.delete(getEntity(id));
        taskSearchIndex.clear();
    }

    private Project getEntity(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private User getOwner(Long ownerId) {
        return userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));
    }

    private String trimDescription(String description) {
        return Optional.ofNullable(description).map(String::trim).orElse(null);
    }
}
