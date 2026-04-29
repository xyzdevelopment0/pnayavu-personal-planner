package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.repositories.TagRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void createShouldNormalizeNameAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        when(tagRepository.existsByNameIgnoreCase("urgent")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 8L));

        var response = tagService.create(new TagRequest("  Urgent "));

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getName()).isEqualTo("urgent");
        assertThat(response.id()).isEqualTo(8L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void createShouldRejectDuplicateTagNames() {
        when(tagRepository.existsByNameIgnoreCase("urgent")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(new TagRequest("  Urgent ")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Tag urgent already exists");

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createShouldThrowWhenNameIsNull() {
        assertThatThrownBy(() -> tagService.create(new TagRequest(null)))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void getByIdShouldReturnTag() {
        when(tagRepository.findById(8L)).thenReturn(Optional.of(tag(8L)));

        var response = tagService.getById(8L);

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.name()).isEqualTo("urgent");
    }

    @Test
    void getByIdShouldThrowWhenTagDoesNotExist() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Tag with id 99 was not found");
    }

    @Test
    void findAllShouldReturnOrderedTags() {
        when(tagRepository.findAllByOrderByIdAsc()).thenReturn(List.of(tag(1L), tag(2L)));

        var responses = tagService.findAll();

        assertThat(responses).extracting(response -> response.id()).containsExactly(1L, 2L);
    }

    @Test
    void updateShouldNormalizeNameAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Tag tag = tag(8L);
        when(tagRepository.findById(8L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("backend", 8L)).thenReturn(false);

        var response = tagService.update(8L, new TagRequest("  Backend "));

        assertThat(tag.getName()).isEqualTo("backend");
        assertThat(response.name()).isEqualTo("backend");
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void updateShouldRejectDuplicateTagNames() {
        when(tagRepository.findById(8L)).thenReturn(Optional.of(tag(8L)));
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("urgent", 8L)).thenReturn(true);

        assertThatThrownBy(() -> tagService.update(8L, new TagRequest("Urgent")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("Tag urgent already exists");
    }

    @Test
    void deleteShouldDetachTagFromTasksAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Tag tag = tag(8L);
        Task task = task(4L);
        task.addTag(tag);
        when(tagRepository.findByIdWithTasks(8L)).thenReturn(Optional.of(tag));

        tagService.delete(8L);

        verify(tagRepository).delete(tag);
        assertThat(task.getTags()).isEmpty();
        assertThat(tag.getTasks()).isEmpty();
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void deleteShouldThrowWhenTagDoesNotExist() {
        when(tagRepository.findByIdWithTasks(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Tag with id 99 was not found");
    }

    private static Tag tag(Long id) {
        return withId(new Tag("urgent"), id);
    }

    private static Task task(Long id) {
        return withId(
            new Task(
                "Task",
                "Desc",
                TaskStatus.TODO,
                LocalDate.now(),
                withId(new Project("Project", "Desc", withId(new User("Owner", "owner@example.com"), 1L)), 2L),
                withId(new User("Assignee", "assignee@example.com"), 3L)
            ),
            id
        );
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private static TaskSearchIndex.CacheKey cacheKey() {
        return new TaskSearchIndex.CacheKey(TaskSearchIndex.Strategy.JPQL, "p", "u", TaskStatus.TODO, 0, 10);
    }

    private static TaskResponse taskResponse() {
        return new TaskResponse(
            1L,
            "Task",
            null,
            TaskStatus.TODO,
            null,
            1L,
            1L,
            Set.of(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
