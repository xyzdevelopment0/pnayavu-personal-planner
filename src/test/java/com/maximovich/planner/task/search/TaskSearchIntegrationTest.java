package com.maximovich.planner.task.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.maximovich.planner.dtos.CreateTaskRequest;
import com.maximovich.planner.dtos.TaskResponse;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.services.TaskService;
import com.maximovich.planner.services.tasksearch.TaskSearchIndex;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class TaskSearchIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSearchIndex taskSearchIndex;

    @BeforeEach
    void setUp() {
        taskSearchIndex.clear();
    }

    @Test
    void shouldFilterTasksWithJpqlAndReuseCache() {
        var first = taskService.searchWithJpql(
            "laboratory",
            "alice@example.com",
            null,
            PageRequest.of(0, 1)
        );

        assertThat(first.cached()).isFalse();
        assertThat(first.page().getTotalElements()).isEqualTo(2);
        assertThat(first.page().getContent()).hasSize(1);

        var second = taskService.searchWithJpql(
            "laboratory",
            "alice@example.com",
            null,
            PageRequest.of(0, 1)
        );

        assertThat(second.cached()).isTrue();
        assertThat(second.page().getContent())
            .extracting(TaskResponse::id)
            .containsExactlyElementsOf(first.page().getContent().stream().map(TaskResponse::id).toList());
    }

    @Test
    void shouldReturnSameResultsForJpqlAndNativeQueries() {
        var jpql = taskService.searchWithJpql(
            "laboratory",
            "alice@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 10)
        );
        var nativeQuery = taskService.searchWithNative(
            "laboratory",
            "alice@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 10)
        );

        assertThat(jpql.page().getTotalElements()).isEqualTo(nativeQuery.page().getTotalElements());
        assertThat(nativeQuery.page().getContent())
            .extracting(TaskResponse::id)
            .containsExactlyElementsOf(jpql.page().getContent().stream().map(TaskResponse::id).toList());
    }

    @Test
    void shouldInvalidateCacheAfterTaskUpdate() {
        var initial = taskService.searchWithJpql(
            "laboratory",
            "alice@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 10)
        );

        assertThat(initial.cached()).isFalse();
        assertThat(initial.page().getTotalElements()).isEqualTo(1);

        var cached = taskService.searchWithJpql(
            "laboratory",
            "alice@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 10)
        );

        assertThat(cached.cached()).isTrue();

        Long taskId = initial.page().getContent().get(0).id();
        var task = taskRepository.findByIdWithRelations(taskId).orElseThrow();

        taskService.update(
            taskId,
            new CreateTaskRequest(
                task.getTitle(),
                task.getDescription(),
                TaskStatus.DONE,
                task.getDueDate(),
                task.getProject().getId(),
                task.getAssignee().getId(),
                task.getTags().stream()
                    .map(tag -> tag.getId())
                    .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll)
            )
        );

        var afterUpdate = taskService.searchWithJpql(
            "laboratory",
            "alice@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 10)
        );

        assertThat(afterUpdate.cached()).isFalse();
        assertThat(afterUpdate.page().getTotalElements()).isZero();
    }
}
