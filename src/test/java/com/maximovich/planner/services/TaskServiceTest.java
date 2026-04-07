package com.maximovich.planner.services;

import com.maximovich.planner.components.TaskSearchIndex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.maximovich.planner.mappers.TaskMapper;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TagRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
            taskRepository,
            projectRepository,
            userRepository,
            tagRepository,
            new TaskMapper(),
            new TaskSearchIndex()
        );
    }

    @Test
    void shouldNormalizeSearchParamsAndReuseCacheAcrossCaseVariations() {
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.searchWithJpqlWithoutStatus(
            eq("%laboratory%"),
            eq("alice@example.com"),
            argThat(value -> value != null && value.getPageNumber() == 0 && value.getPageSize() == 10)
        )).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var first = taskService.searchWithJpql("  Laboratory  ", "  ALICE@example.com  ", null, pageable);
        var second = taskService.searchWithJpql("laboratory", "alice@example.com", null, pageable);

        assertThat(first.cached()).isFalse();
        assertThat(second.cached()).isTrue();

        verify(taskRepository, times(1)).searchWithJpqlWithoutStatus(
            eq("%laboratory%"),
            eq("alice@example.com"),
            argThat(value -> value != null && value.getPageNumber() == 0 && value.getPageSize() == 10)
        );
        verifyNoMoreInteractions(taskRepository);
    }
}
