package com.maximovich.planner.services;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.dtos.CachedTaskSearchResult;
import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.Tag;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.mappers.TaskMapper;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TagRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
    private final TaskSearchIndex taskSearchIndex;

    public TaskService(
        TaskRepository taskRepository,
        ProjectRepository projectRepository,
        UserRepository userRepository,
        TagRepository tagRepository,
        TaskMapper taskMapper,
        TaskSearchIndex taskSearchIndex
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.taskMapper = taskMapper;
        this.taskSearchIndex = taskSearchIndex;
    }

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
        TaskResponse response = taskMapper.toResponse(taskRepository.save(task));
        taskSearchIndex.clear();
        return response;
    }

    public TaskResponse getById(Long id) {
        return taskMapper.toResponse(getEntityWithRelations(id));
    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAllWithRelations().stream().map(taskMapper::toResponse).toList();
    }

    public CachedTaskSearchResult searchWithJpql(
        String projectName,
        String ownerEmail,
        TaskStatus status,
        Pageable pageable
    ) {
        return search(TaskSearchIndex.Strategy.JPQL, projectName, ownerEmail, status, pageable);
    }

    public CachedTaskSearchResult searchWithNative(
        String projectName,
        String ownerEmail,
        TaskStatus status,
        Pageable pageable
    ) {
        return search(TaskSearchIndex.Strategy.NATIVE, projectName, ownerEmail, status, pageable);
    }

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
        TaskResponse response = taskMapper.toResponse(getEntityWithRelations(id));
        taskSearchIndex.clear();
        return response;
    }

    @Transactional
    public void delete(Long id) {
        taskRepository.delete(getEntity(id));
        taskSearchIndex.clear();
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

    private User getAssignee(Long assigneeId) {
        return userRepository.findById(assigneeId)
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

    private CachedTaskSearchResult search(
        TaskSearchIndex.Strategy strategy,
        String projectName,
        String ownerEmail,
        TaskStatus status,
        Pageable pageable
    ) {
        Pageable normalizedPageable = normalizePageable(pageable);
        String normalizedProjectName = normalizeText(projectName);
        String normalizedOwnerEmail = normalizeText(ownerEmail);
        String projectPattern = normalizedProjectName == null ? "%" : "%" + normalizedProjectName + "%";
        TaskSearchIndex.CacheKey key = new TaskSearchIndex.CacheKey(
            strategy,
            normalizedProjectName,
            normalizedOwnerEmail,
            status,
            normalizedPageable.getPageNumber(),
            normalizedPageable.getPageSize()
        );
        Page<TaskResponse> cachedPage = taskSearchIndex.get(key).orElse(null);
        if (cachedPage != null) {
            return new CachedTaskSearchResult(cachedPage, true);
        }
        Page<Long> taskIdsPage = strategy == TaskSearchIndex.Strategy.JPQL
            ? searchIdsWithJpql(projectPattern, normalizedOwnerEmail, status, normalizedPageable)
            : taskRepository.searchIdsWithNative(
                projectPattern,
                normalizedOwnerEmail,
                status == null ? null : status.name(),
                normalizedPageable
            );
        List<TaskResponse> content = taskIdsPage.hasContent()
            ? taskRepository.findByIdInOrderByIdAsc(taskIdsPage.getContent())
                .stream()
                .map(taskMapper::toResponse)
                .toList()
            : List.of();
        Page<TaskResponse> page = new PageImpl<>(content, normalizedPageable, taskIdsPage.getTotalElements());
        taskSearchIndex.put(key, page);
        return new CachedTaskSearchResult(page, false);
    }

    private Pageable normalizePageable(Pageable pageable) {
        int page = pageable == null ? 0 : Math.max(pageable.getPageNumber(), 0);
        int size = pageable == null ? 10 : Math.min(Math.max(pageable.getPageSize(), 1), 50);
        return PageRequest.of(page, size);
    }

    private Page<Long> searchIdsWithJpql(
        String projectPattern,
        String ownerEmail,
        TaskStatus status,
        Pageable pageable
    ) {
        if (status == null) {
            return taskRepository.searchIdsWithJpqlWithoutStatus(projectPattern, ownerEmail, pageable);
        }
        return taskRepository.searchIdsWithJpql(projectPattern, ownerEmail, status, pageable);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
