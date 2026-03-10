package com.maximovich.planner.task.service;

import com.maximovich.planner.common.BusinessException;
import com.maximovich.planner.common.ResourceNotFoundException;
import com.maximovich.planner.project.domain.Project;
import com.maximovich.planner.project.repository.ProjectRepository;
import com.maximovich.planner.tag.domain.Tag;
import com.maximovich.planner.tag.repository.TagRepository;
import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.dto.CreateTaskRequest;
import com.maximovich.planner.task.dto.TaskResponse;
import com.maximovich.planner.task.mapper.TaskMapper;
import com.maximovich.planner.task.repository.TaskRepository;
import com.maximovich.planner.user.domain.PlannerUser;
import com.maximovich.planner.user.repository.PlannerUserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final PlannerUserRepository plannerUserRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(
        TaskRepository taskRepository,
        ProjectRepository projectRepository,
        PlannerUserRepository plannerUserRepository,
        TagRepository tagRepository,
        TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.plannerUserRepository = plannerUserRepository;
        this.tagRepository = tagRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task(
            request.title().trim(),
            trimDescription(request.description()),
            request.status(),
            request.dueDate(),
            getProject(request.projectId()),
            getAssignee(request.assigneeId())
        );
        task.replaceTags(getTags(request.tagIds()));
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse getById(Long id) {
        return taskMapper.toResponse(getEntityWithRelations(id));
    }

    @Override
    public List<TaskResponse> findAll() {
        return taskRepository.findAllWithRelations().stream().map(taskMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, CreateTaskRequest request) {
        Task task = getEntity(id);
        task.update(
            request.title().trim(),
            trimDescription(request.description()),
            request.status(),
            request.dueDate(),
            getProject(request.projectId()),
            getAssignee(request.assigneeId())
        );
        task.replaceTags(getTags(request.tagIds()));
        return taskMapper.toResponse(getEntityWithRelations(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        taskRepository.delete(getEntity(id));
    }

    private Task getEntity(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private Task getEntityWithRelations(Long id) {
        return taskRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private PlannerUser getAssignee(Long assigneeId) {
        return plannerUserRepository.findById(assigneeId)
            .orElseThrow(() -> new ResourceNotFoundException("User", assigneeId));
    }

    private Set<Tag> getTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Long> normalizedIds = new LinkedHashSet<>(tagIds);
        List<Tag> tags = tagRepository.findAllById(normalizedIds);
        if (tags.size() != normalizedIds.size()) {
            throw new BusinessException("One or more tags were not found");
        }
        return new LinkedHashSet<>(tags);
    }

    private String trimDescription(String description) {
        return description == null ? null : description.trim();
    }
}
