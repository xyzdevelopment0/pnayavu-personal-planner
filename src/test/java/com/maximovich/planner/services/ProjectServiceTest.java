package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.dtos.ProjectRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.UserRepository;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;
    private TaskSearchIndex taskSearchIndex;

    @BeforeEach
    void setUp() {
        taskSearchIndex = new TaskSearchIndex();
        projectService = new ProjectService(projectRepository, userRepository, taskSearchIndex);
    }

    @Test
    void createShouldTrimFieldsAndClearSearchCache() {
        User owner = user(7L);
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        when(userRepository.findById(7L)).thenReturn(Optional.of(owner));
        when(projectRepository.save(org.mockito.ArgumentMatchers.any(Project.class)))
            .thenAnswer(invocation -> withId(invocation.getArgument(0), 11L));

        var response = projectService.create(new ProjectRequest("  Sprint  ", "  Plan work  ", 7L));

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getName()).isEqualTo("Sprint");
        assertThat(projectCaptor.getValue().getDescription()).isEqualTo("Plan work");
        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.ownerId()).isEqualTo(7L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void getByIdShouldThrowWhenProjectDoesNotExist() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Project with id 99 was not found");
    }

    private static User user(Long id) {
        return withId(new User("Owner", "owner@example.com"), id);
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
