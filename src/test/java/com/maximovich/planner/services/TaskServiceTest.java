package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.maximovich.planner.exceptions.ResourceNotFoundException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    void createShouldTrimFieldsUseDefaultStatusAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Project project = project(10L);
        User assignee = user(20L);
        Tag tag = tag(30L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(Set.of(30L))).thenReturn(List.of(tag));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 101L));

        var response = taskService.create(request("  First  ", "  Desc  ", null, Set.of(30L)));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getTitle()).isEqualTo("First");
        assertThat(taskCaptor.getValue().getDescription()).isEqualTo("Desc");
        assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskCaptor.getValue().getTags()).containsExactly(tag);
        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.tagIds()).containsExactly(30L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void createShouldAllowNullDescriptionAndNullTags() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L)));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 101L));

        var response = taskService.create(request("First", null, TaskStatus.IN_PROGRESS, null));

        assertThat(response.description()).isNull();
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.tagIds()).isEmpty();
    }

    @Test
    void createShouldThrowWhenProjectDoesNotExist() {
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request("First", null, TaskStatus.TODO, Set.of())))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Project with id 10 was not found");
    }

    @Test
    void createShouldThrowWhenAssigneeDoesNotExist() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L)));
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request("First", null, TaskStatus.TODO, Set.of())))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("User with id 20 was not found");
    }

    @Test
    void createShouldThrowWhenOneOfTagsDoesNotExist() {
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L)));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L)));
        when(tagRepository.findAllById(Set.of(30L, 31L))).thenReturn(List.of(tag(30L)));

        assertThatThrownBy(() -> taskService.create(request("First", null, TaskStatus.TODO, Set.of(30L, 31L))))
            .isInstanceOf(BusinessException.class)
            .hasMessage("One or more tags were not found");
    }

    @Test
    void createBulkShouldSaveEveryTaskAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Project project = project(10L);
        User assignee = user(20L);
        Tag tag = tag(30L);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(Set.of(30L))).thenReturn(List.of(tag));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            return withId(task, task.getTitle().equals("First") ? 101L : 102L);
        });

        var responses = taskService.createBulk(List.of(
            request(" First ", " Desc ", TaskStatus.TODO, Set.of(30L)),
            request(" Second ", null, TaskStatus.IN_PROGRESS, Set.of())
        ));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, org.mockito.Mockito.times(2)).save(taskCaptor.capture());
        assertThat(taskCaptor.getAllValues()).extracting(Task::getTitle).containsExactly("First", "Second");
        assertThat(responses).extracting(response -> response.id()).containsExactly(101L, 102L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void createBulkShouldFailFastWhenOneOfTagsDoesNotExist() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project(10L)));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user(20L)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 101L));
        when(tagRepository.findAllById(Set.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> taskService.createBulk(List.of(
            request("First", null, TaskStatus.TODO, Set.of()),
            request("Broken", null, TaskStatus.TODO, Set.of(999L))
        )))
            .isInstanceOf(BusinessException.class)
            .hasMessage("One or more tags were not found");

        verify(taskRepository).save(any(Task.class));
        assertThat(taskSearchIndex.get(key)).isPresent();
    }

    @Test
    void getByIdShouldReturnTaskWithRelations() {
        when(taskRepository.findByIdWithRelations(101L)).thenReturn(Optional.of(task(101L)));

        var response = taskService.getById(101L);

        assertThat(response.id()).isEqualTo(101L);
    }

    @Test
    void getByIdShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findByIdWithRelations(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Task with id 99 was not found");
    }

    @Test
    void findAllShouldReturnTasksWithRelations() {
        when(taskRepository.findAllWithRelations()).thenReturn(List.of(task(101L), task(102L)));

        var responses = taskService.findAll();

        assertThat(responses).extracting(response -> response.id()).containsExactly(101L, 102L);
    }

    @Test
    void updateShouldApplyRequestAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Task task = task(101L);
        Project project = project(11L);
        User assignee = user(21L);
        Tag tag = tag(31L);
        when(taskRepository.findById(101L)).thenReturn(Optional.of(task));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(20L)).thenReturn(Optional.of(assignee));
        when(tagRepository.findAllById(Set.of(31L))).thenReturn(List.of(tag));
        when(taskRepository.findByIdWithRelations(101L)).thenReturn(Optional.of(task));

        var response = taskService.update(101L, request("  Updated  ", "  New  ", TaskStatus.DONE, Set.of(31L)));

        assertThat(task.getTitle()).isEqualTo("Updated");
        assertThat(task.getDescription()).isEqualTo("New");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(response.tagIds()).containsExactly(31L);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void updateShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(99L, request("First", null, TaskStatus.TODO, Set.of())))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Task with id 99 was not found");
    }

    @Test
    void deleteShouldRemoveTaskAndClearSearchCache() {
        TaskSearchIndex.CacheKey key = cacheKey();
        taskSearchIndex.put(key, new PageImpl<>(List.of(taskResponse())));
        Task task = task(101L);
        when(taskRepository.findById(101L)).thenReturn(Optional.of(task));

        taskService.delete(101L);

        verify(taskRepository).delete(task);
        assertThat(taskSearchIndex.get(key)).isEmpty();
    }

    @Test
    void deleteShouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Task with id 99 was not found");

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void searchWithJpqlShouldNormalizeInputAndCacheMissThenHit() {
        LocalDate dueDate = LocalDate.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        Object[] row = {101L, "Task", "Desc", TaskStatus.TODO, dueDate, 10L, 20L, "3,,1", createdAt, updatedAt};
        when(taskRepository.searchWithJpql("%planner%", "owner@example.com", TaskStatus.TODO, PageRequest.of(0, 50)))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), PageRequest.of(0, 50), 1));

        var first = taskService.searchWithJpql(
            "  Planner  ",
            "  OWNER@example.com  ",
            TaskStatus.TODO,
            PageRequest.of(0, 100)
        );
        var second = taskService.searchWithJpql(
            "planner",
            "owner@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 50)
        );

        assertThat(first.cached()).isFalse();
        assertThat(second.cached()).isTrue();
        assertThat(first.page().getContent().get(0).tagIds()).containsExactly(1L, 3L);
    }

    @Test
    void searchWithJpqlShouldHandleNullFiltersNullPageableAndNullTagIds() {
        Object[] row = {101L, "Task", null, TaskStatus.TODO, null, 10L, 20L, null, null, null};
        when(taskRepository.searchWithJpqlWithoutStatus("%", null, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), PageRequest.of(0, 10), 1));

        var result = taskService.searchWithJpql(null, null, null, null);

        assertThat(result.cached()).isFalse();
        assertThat(result.page().getContent().get(0).tagIds()).isEmpty();
    }

    @Test
    void searchWithJpqlShouldNormalizeBlankFiltersAndNegativePage() {
        Object[] row = {101L, "Task", null, TaskStatus.TODO, null, 10L, 20L, " ", null, null};
        when(taskRepository.searchWithJpqlWithoutStatus("%", null, PageRequest.of(0, 1)))
            .thenReturn(new PageImpl<>(List.<Object[]>of(row), PageRequest.of(0, 1), 1));

        var result = taskService.searchWithJpql("  ", "  ", null, invalidPageable());

        assertThat(result.page().getContent().get(0).tagIds()).isEmpty();
    }

    @Test
    void searchWithNativeShouldLoadTasksWhenIdsPageHasContent() {
        when(taskRepository.searchIdsWithNative("%planner%", "owner@example.com", "TODO", PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(101L), PageRequest.of(0, 10), 1));
        when(taskRepository.findByIdInOrderByIdAsc(List.of(101L))).thenReturn(List.of(task(101L)));

        var result = taskService.searchWithNative(
            "Planner",
            "owner@example.com",
            TaskStatus.TODO,
            PageRequest.of(0, 10)
        );

        assertThat(result.cached()).isFalse();
        assertThat(result.page().getContent()).extracting(TaskResponse::id).containsExactly(101L);
    }

    @Test
    void searchWithNativeShouldReturnEmptyPageWhenIdsPageHasNoContent() {
        when(taskRepository.searchIdsWithNative("%", null, null, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        var result = taskService.searchWithNative(null, null, null, PageRequest.of(0, 10));

        assertThat(result.page().getContent()).isEmpty();
    }

    private static CreateTaskRequest request(String title, String description, TaskStatus status, Set<Long> tagIds) {
        return new CreateTaskRequest(title, description, status, LocalDate.now().plusDays(1), 10L, 20L, tagIds);
    }

    private static Task task(Long id) {
        Task task = withId(
            new Task("Task", "Desc", TaskStatus.TODO, LocalDate.now(), project(10L), user(20L)),
            id
        );
        task.addTag(tag(30L));
        return task;
    }

    private static Project project(Long id) {
        return withId(new Project("Project", "Desc", user(1L)), id);
    }

    private static User user(Long id) {
        return withId(new User("User", "user@example.com"), id);
    }

    private static Tag tag(Long id) {
        return withId(new Tag("backend"), id);
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

    private static Pageable invalidPageable() {
        return new Pageable() {
            @Override
            public int getPageNumber() {
                return -1;
            }

            @Override
            public int getPageSize() {
                return 0;
            }

            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Sort getSort() {
                return Sort.unsorted();
            }

            @Override
            public Pageable next() {
                return this;
            }

            @Override
            public Pageable previousOrFirst() {
                return this;
            }

            @Override
            public Pageable first() {
                return this;
            }

            @Override
            public Pageable withPage(int pageNumber) {
                return this;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
    }
}
