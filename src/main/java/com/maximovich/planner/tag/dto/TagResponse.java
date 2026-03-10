package com.maximovich.planner.tag.dto;

import com.maximovich.planner.tag.domain.Tag;
import java.time.LocalDateTime;

public record TagResponse(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
