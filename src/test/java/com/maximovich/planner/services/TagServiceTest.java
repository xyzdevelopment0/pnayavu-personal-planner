package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.dtos.TagRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.Tag;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.repositories.TagRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private TagService tagService;
    private TaskSearchIndex taskSearchIndex;

    @BeforeEach
    void setUp() {
        taskSearchIndex = new TaskSearchIndex();
        tagService = new TagService(tagRepository, taskSearchIndex);
    }

    @Test
    void createShouldRejectDuplicateTagNames() {
        when(tagRepository.existsByNameIgnoreCase("urgent")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(new TagRequest("  Urgent ")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Tag urgent already exists");
    }

    @Test
    void deleteShouldDetachTagFromTasksAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Tag tag = withId(new Tag("backend"), 8L);
        Task task = withId(
            new Task(
                "Task",
                "Desc",
                TaskStatus.TODO,
                LocalDate.now(),
                withId(new Project("Project", "Desc", withId(new User("Owner", "owner@example.com"), 1L)), 2L),
                withId(new User("Assignee", "assignee@example.com"), 3L)
            ),
            4L
        );
        task.addTag(tag);
        when(tagRepository.findByIdWithTasks(8L)).thenReturn(Optional.of(tag));

        tagService.delete(8L);

        verify(tagRepository).delete(tag);
        assertThat(task.getTags()).isEmpty();
        assertThat(tag.getTasks()).isEmpty();
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private static TaskSearchIndex.CacheKey cacheKey() {
        return new TaskSearchIndex.CacheKey(TaskSearchIndex.Strategy.JPQL, "p", "u", TaskStatus.TODO, 0, 10);
    }

    private static TaskResponse taskResponse() {
        return new TaskResponse(1L, "Task", null, TaskStatus.TODO, null, 1L, 1L, Set.of(), LocalDateTime.now(), LocalDateTime.now());
    }
}
