package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 11L));

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
    void createShouldAllowNullDescription() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 11L));

        var response = projectService.create(new ProjectRequest("Sprint", null, 7L));

        assertThat(response.description()).isNull();
    }

    @Test
    void createShouldThrowWhenOwnerDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.create(new ProjectRequest("Sprint", null, 99L)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 99 was not found");
    }

    @Test
    void getByIdShouldReturnProject() {
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project(11L, user(7L))));

        var response = projectService.getById(11L);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.ownerId()).isEqualTo(7L);
    }

    @Test
    void getByIdShouldThrowWhenProjectDoesNotExist() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Project with id 99 was not found");
    }

    @Test
    void findAllShouldReturnOrderedProjects() {
        User owner = user(7L);
        when(projectRepository.findAllByOrderByIdAsc()).thenReturn(List.of(project(1L, owner), project(2L, owner)));

        var responses = projectService.findAll();

        assertThat(responses).extracting(response -> response.id()).containsExactly(1L, 2L);
    }

    @Test
    void updateShouldTrimFieldsAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Project project = project(11L, user(7L));
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));
        when(userRepository.findById(8L)).thenReturn(Optional.of(user(8L)));

        var response = projectService.update(11L, new ProjectRequest("  Updated  ", "  New desc  ", 8L));

        assertThat(project.getName()).isEqualTo("Updated");
        assertThat(project.getDescription()).isEqualTo("New desc");
        assertThat(response.ownerId()).isEqualTo(8L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void updateShouldAllowNullDescription() {
        Project project = project(11L, user(7L));
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));
        when(userRepository.findById(8L)).thenReturn(Optional.of(user(8L)));

        var response = projectService.update(11L, new ProjectRequest("Updated", null, 8L));

        assertThat(response.description()).isNull();
    }

    @Test
    void deleteShouldRemoveProjectAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Project project = project(11L, user(7L));
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));

        projectService.delete(11L);

        verify(projectRepository).delete(project);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    private static Project project(Long id, User owner) {
        return withId(new Project("Sprint", "Plan", owner), id);
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
