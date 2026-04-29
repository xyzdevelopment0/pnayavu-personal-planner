package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.dtos.UserRequest;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
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
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 3L));

        var response = userService.create(new UserRequest(" Alice ", "  Alice@Example.com "));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getName()).isEqualTo("Alice");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.id()).isEqualTo(3L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void createShouldRejectDuplicateEmails() {
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new UserRequest("Alice", "Alice@Example.com")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("User with email alice@example.com already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createShouldThrowWhenEmailIsNull() {
        assertThatThrownBy(() -> userService.create(new UserRequest("Alice", null)))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void getByIdShouldReturnUser() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));

        var response = userService.getById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.email()).isEqualTo("alice@example.com");
    }

    @Test
    void getByIdShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 99 was not found");
    }

    @Test
    void findAllShouldReturnOrderedUsers() {
        when(userRepository.findAllByOrderByIdAsc()).thenReturn(List.of(user(1L), user(2L)));

        var responses = userService.findAll();

        assertThat(responses).extracting(response -> response.id()).containsExactly(1L, 2L);
    }

    @Test
    void updateShouldNormalizeEmailAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        User user = user(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("new@example.com", 5L)).thenReturn(false);

        var response = userService.update(5L, new UserRequest(" New Name ", " New@Example.com "));

        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void updateShouldRejectDuplicateEmails() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("alice@example.com", 5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(5L, new UserRequest("Alice", "alice@example.com")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("User with email alice@example.com already exists");
    }

    @Test
    void deleteShouldRemoveUserAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        User user = user(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(projectRepository.existsByOwnerId(5L)).thenReturn(false);
        when(taskRepository.existsByAssigneeId(5L)).thenReturn(false);
        when(taskCommentRepository.existsByAuthorId(5L)).thenReturn(false);

        userService.delete(5L);

        verify(userRepository).delete(user);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void deleteShouldRejectUsersOwningProjects() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(projectRepository.existsByOwnerId(5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(5L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("User 5 owns projects and cannot be deleted");

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteShouldRejectUsersAssignedToTasks() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(projectRepository.existsByOwnerId(5L)).thenReturn(false);
        when(taskRepository.existsByAssigneeId(5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(5L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("User 5 is assigned to tasks and cannot be deleted");
    }

    @Test
    void deleteShouldRejectUsersWithComments() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(projectRepository.existsByOwnerId(5L)).thenReturn(false);
        when(taskRepository.existsByAssigneeId(5L)).thenReturn(false);
        when(taskCommentRepository.existsByAuthorId(5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(5L))
            .isInstanceOf(BusinessException.class)
            .hasMessage("User 5 has comments and cannot be deleted");
    }

    private static User user(Long id) {
        return withId(new User("Alice", "alice@example.com"), id);
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
