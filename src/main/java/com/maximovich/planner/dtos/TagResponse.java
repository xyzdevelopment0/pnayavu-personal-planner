package com.maximovich.planner.dtos;

import com.maximovich.planner.entities.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Tag response")
public record TagResponse(
    @Schema(description = "Tag identifier", example = "1")
    Long id,
    @Schema(description = "Tag name", example = "swagger")
    String name,
    @Schema(description = "Creation timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2026-04-01T18:42:31.123")
    LocalDateTime updatedAt
) {

    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
