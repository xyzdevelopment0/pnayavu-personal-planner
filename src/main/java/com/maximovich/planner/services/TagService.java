package com.maximovich.planner.services;

import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.entities.Tag;
import com.maximovich.planner.dtos.TagRequest;
import com.maximovich.planner.dtos.TagResponse;
import com.maximovich.planner.repositories.TagRepository;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.services.tasksearch.TaskSearchIndex;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final TaskSearchIndex taskSearchIndex;

    public TagService(TagRepository tagRepository, TaskSearchIndex taskSearchIndex) {
        this.tagRepository = tagRepository;
        this.taskSearchIndex = taskSearchIndex;
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = normalizeName(request.name());
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Tag %s already exists".formatted(name));
        }
        TagResponse response = TagResponse.fromEntity(tagRepository.save(new Tag(name)));
        taskSearchIndex.clear();
        return response;
    }

    public TagResponse getById(Long id) {
        return TagResponse.fromEntity(getEntity(id));
    }

    public List<TagResponse> findAll() {
        return tagRepository.findAllByOrderByIdAsc().stream().map(TagResponse::fromEntity).toList();
    }

    @Transactional
    public TagResponse update(Long id, TagRequest request) {
        Tag tag = getEntity(id);
        String name = normalizeName(request.name());
        if (tagRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessException("Tag %s already exists".formatted(name));
        }
        tag.update(name);
        TagResponse response = TagResponse.fromEntity(tag);
        taskSearchIndex.clear();
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findByIdWithTasks(id).orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        for (Task task : new LinkedHashSet<>(tag.getTasks())) {
            task.removeTag(tag);
        }
        tagRepository.delete(tag);
        taskSearchIndex.clear();
    }

    private Tag getEntity(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase();
    }
}
