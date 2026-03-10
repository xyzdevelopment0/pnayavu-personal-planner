package com.maximovich.planner.tag.service;

import com.maximovich.planner.common.BusinessException;
import com.maximovich.planner.common.ResourceNotFoundException;
import com.maximovich.planner.tag.domain.Tag;
import com.maximovich.planner.tag.dto.TagRequest;
import com.maximovich.planner.tag.dto.TagResponse;
import com.maximovich.planner.tag.repository.TagRepository;
import com.maximovich.planner.task.domain.Task;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = normalizeName(request.name());
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Tag %s already exists".formatted(name));
        }
        return TagResponse.fromEntity(tagRepository.save(new Tag(name)));
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
        return TagResponse.fromEntity(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findByIdWithTasks(id).orElseThrow(() -> new ResourceNotFoundException("Tag", id));
        for (Task task : new LinkedHashSet<>(tag.getTasks())) {
            task.removeTag(tag);
        }
        tagRepository.delete(tag);
    }

    private Tag getEntity(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase();
    }
}
