package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.dtos.TaskCommentRequest;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskComment;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private TaskCommentService taskCommentService;

    @BeforeEach
    void setUp() {
        taskCommentService = new TaskCommentService(taskCommentRepository, taskRepository, userRepository);
    }

    @Test
    void createShouldTrimContent() {
        Task task = task(4L);
        User author = user(5L);
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));
        when(userRepository.findById(5L)).thenReturn(Optional.of(author));
        when(taskCommentRepository.save(any(TaskComment.class)))
            .thenAnswer(invocation -> withId(invocation.getArgument(0), 6L));

        var response = taskCommentService.create(new TaskCommentRequest("  Looks good  ", 4L, 5L));

        ArgumentCaptor<TaskComment> commentCaptor = ArgumentCaptor.forClass(TaskComment.class);
        verify(taskCommentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getContent()).isEqualTo("Looks good");
        assertThat(response.id()).isEqualTo(6L);
        assertThat(response.taskId()).isEqualTo(4L);
        assertThat(response.authorId()).isEqualTo(5L);
    }

    @Test
    void createShouldThrowWhenContentIsNull() {
        assertThatThrownBy(() -> taskCommentService.create(new TaskCommentRequest(null, 4L, 5L)))
            .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void createShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.create(new TaskCommentRequest("Text", 4L, 5L)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Task with id 4 was not found");
    }

    @Test
    void createShouldThrowWhenAuthorDoesNotExist() {
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task(4L)));
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.create(new TaskCommentRequest("Text", 4L, 5L)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 5 was not found");
    }

    @Test
    void getByIdShouldReturnCommentWithRelations() {
        when(taskCommentRepository.findByIdWithRelations(6L)).thenReturn(Optional.of(comment(6L, task(4L), user(5L))));

        var response = taskCommentService.getById(6L);

        assertThat(response.id()).isEqualTo(6L);
        assertThat(response.taskId()).isEqualTo(4L);
        assertThat(response.authorId()).isEqualTo(5L);
    }

    @Test
    void getByIdShouldThrowWhenCommentDoesNotExist() {
        when(taskCommentRepository.findByIdWithRelations(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Comment with id 99 was not found");
    }

    @Test
    void findAllShouldReturnCommentsWithRelations() {
        when(taskCommentRepository.findAllWithRelations()).thenReturn(List.of(
            comment(6L, task(4L), user(5L)),
            comment(7L, task(4L), user(5L))
        ));

        var responses = taskCommentService.findAll();

        assertThat(responses).extracting(response -> response.id()).containsExactly(6L, 7L);
    }

    @Test
    void updateShouldTrimContentAndReloadRelations() {
        TaskComment comment = comment(6L, task(4L), user(5L));
        Task newTask = task(8L);
        User newAuthor = user(9L);
        when(taskCommentRepository.findById(6L)).thenReturn(Optional.of(comment));
        when(taskRepository.findById(8L)).thenReturn(Optional.of(newTask));
        when(userRepository.findById(9L)).thenReturn(Optional.of(newAuthor));
        when(taskCommentRepository.findByIdWithRelations(6L)).thenReturn(Optional.of(comment));

        var response = taskCommentService.update(6L, new TaskCommentRequest("  Updated  ", 8L, 9L));

        assertThat(comment.getContent()).isEqualTo("Updated");
        assertThat(comment.getTask()).isSameAs(newTask);
        assertThat(comment.getAuthor()).isSameAs(newAuthor);
        assertThat(response.taskId()).isEqualTo(8L);
        assertThat(response.authorId()).isEqualTo(9L);
    }

    @Test
    void updateShouldThrowWhenCommentDoesNotExist() {
        when(taskCommentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.update(99L, new TaskCommentRequest("Text", 4L, 5L)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Comment with id 99 was not found");
    }

    @Test
    void deleteShouldRemoveComment() {
        TaskComment comment = comment(6L, task(4L), user(5L));
        when(taskCommentRepository.findById(6L)).thenReturn(Optional.of(comment));

        taskCommentService.delete(6L);

        verify(taskCommentRepository).delete(comment);
    }

    private static TaskComment comment(Long id, Task task, User author) {
        return withId(new TaskComment("Looks good", task, author), id);
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

    private static User user(Long id) {
        return withId(new User("Author", "author@example.com"), id);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
