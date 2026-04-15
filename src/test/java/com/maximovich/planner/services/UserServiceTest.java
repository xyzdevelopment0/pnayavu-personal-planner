package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.dtos.UserRequest;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.repositories.TaskRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    private UserService userService;
    private TaskSearchIndex taskSearchIndex;

    @BeforeEach
    void setUp() {
        taskSearchIndex = new TaskSearchIndex();
        userService = new UserService(
            userRepository,
            projectRepository,
            taskRepository,
            taskCommentRepository,
            taskSearchIndex
        );
    }

    @Test
    void createShouldNormalizeEmailAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
            .thenAnswer(invocation -> withId(invocation.getArgument(0), 3L));

        var response = userService.create(new UserRequest(" Alice ", "  Alice@Example.com "));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.id()).isEqualTo(3L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void deleteShouldRejectUsersOwningProjects() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(withId(new User("Alice", "alice@example.com"), 5L)));
        when(projectRepository.existsByOwnerId(5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(5L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("User 5 owns projects and cannot be deleted");

        verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any(User.class));
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
