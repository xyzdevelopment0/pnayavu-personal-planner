package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maximovich.planner.dtos.TaskCommentRequest;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskComment;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private TaskCommentService taskCommentService;

    @Test
    void createShouldTrimContent() {
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
        User author = withId(new User("Author", "author@example.com"), 5L);
        when(taskRepository.findById(4L)).thenReturn(Optional.of(task));
        when(userRepository.findById(5L)).thenReturn(Optional.of(author));
        when(taskCommentRepository.save(org.mockito.ArgumentMatchers.any(TaskComment.class)))
            .thenAnswer(invocation -> withId(invocation.getArgument(0), 6L));

        var response = taskCommentService.create(new TaskCommentRequest("  Looks good  ", 4L, 5L));

        ArgumentCaptor<TaskComment> commentCaptor = ArgumentCaptor.forClass(TaskComment.class);
        verify(taskCommentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getContent()).isEqualTo("Looks good");
        assertThat(response.id()).isEqualTo(6L);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
