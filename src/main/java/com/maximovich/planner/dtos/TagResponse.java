package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.Tag;
import java.time.LocalDateTime;

public record TagResponse(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
