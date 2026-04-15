package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.Tag;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.mappers.TaskMapper;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TagRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    private TaskService taskService;
    private TaskSearchIndex taskSearchIndex;

    @BeforeEach
    void setUp() {
        taskSearchIndex = new TaskSearchIndex();
        taskService = new TaskService(
            taskRepository,
            projectRepository,
            userRepository,
            tagRepository,
            new TaskMapper(),
            taskSearchIndex
        );
    }

    @Test
    void createBulkShouldSaveEveryTaskAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new org.springframework.data.domain.PageImpl<>(List.of(taskResponse())));
        Project project = withId(new Project("Planner", "Main", withId(new User("Owner", "owner@example.com"), 1L)), 10L);
        User assignee = withId(new User("Alice", "alice@example.com"), 20L);
        Tag tag = withId(new Tag("backend"), 30L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(Set.of(30L))).thenReturn(List.of(tag));
        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class)))
            .thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                Long nextId = task.getTitle().equals("First") ? 101L : 102L;
                return withId(task, nextId);
            });

        var responses = taskService.createBulk(List.of(
            new CreateTaskRequest(" First ", " Desc ", TaskStatus.TODO, LocalDate.now().plusDays(1), 10L, 20L, Set.of(30L)),
            new CreateTaskRequest(" Second ", null, TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(2), 10L, 20L, Set.of())
        ));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, org.mockito.Mockito.times(2)).save(taskCaptor.capture());
        assertThat(taskCaptor.getAllValues()).extracting(Task::getTitle).containsExactly("First", "Second");
        assertThat(taskCaptor.getAllValues().get(0).getDescription()).isEqualTo("Desc");
        assertThat(taskCaptor.getAllValues().get(1).getDescription()).isNull();
        assertThat(responses).extracting(response -> response.id()).containsExactly(101L, 102L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void createBulkShouldFailFastWhenOneOfTagsDoesNotExist() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new org.springframework.data.domain.PageImpl<>(List.of(taskResponse())));
        Project project = withId(new Project("Planner", "Main", withId(new User("Owner", "owner@example.com"), 1L)), 10L);
        User assignee = withId(new User("Alice", "alice@example.com"), 20L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class)))
            .thenAnswer(invocation -> withId(invocation.getArgument(0), 101L));
        when(tagRepository.findAllById(Set.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> taskService.createBulk(List.of(
            new CreateTaskRequest(" First ", null, TaskStatus.TODO, LocalDate.now().plusDays(1), 10L, 20L, Set.of()),
            new CreateTaskRequest(" Broken ", null, TaskStatus.TODO, LocalDate.now().plusDays(2), 10L, 20L, Set.of(999L))
        )))
            .isInstanceOf(BusinessException.class)
            .hasMessage("One or more tags were not found");

        verify(taskRepository).save(org.mockito.ArgumentMatchers.any(Task.class));
        assertThat(taskSearchIndex.get(key)).isPresent();
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
