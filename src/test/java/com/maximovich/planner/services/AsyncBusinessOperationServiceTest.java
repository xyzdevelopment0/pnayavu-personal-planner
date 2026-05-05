package com.maximovich.planner.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.maximovich.planner.components.AsyncBusinessOperationRunner;
import com.maximovich.planner.dtos.async.AsyncTaskStatus;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class AsyncBusinessOperationServiceTest {

    @Test
    void startTaskCountOperationShouldReturnRunningTaskAndCompleteIt() {
        CompletableFuture<Long> future = new CompletableFuture<>();
        FakeAsyncBusinessOperationRunner runner = new FakeAsyncBusinessOperationRunner(future);
        AsyncBusinessOperationService service = new AsyncBusinessOperationService(runner);

        String taskId = service.startTaskCountOperation(20);

        assertThat(service.getStatus(taskId).status()).isEqualTo(AsyncTaskStatus.RUNNING);
        assertThat(service.getCompletedTasks()).isZero();

        future.complete(15L);

        var status = service.getStatus(taskId);
        assertThat(status.status()).isEqualTo(AsyncTaskStatus.COMPLETED);
        assertThat(status.result()).isEqualTo(15L);
        assertThat(status.error()).isNull();
        assertThat(service.getCompletedTasks()).isEqualTo(1L);
    }

    @Test
    void startTaskCountOperationShouldStoreFailure() {
        CompletableFuture<Long> future = new CompletableFuture<>();
        FakeAsyncBusinessOperationRunner runner = new FakeAsyncBusinessOperationRunner(future);
        AsyncBusinessOperationService service = new AsyncBusinessOperationService(runner);

        String taskId = service.startTaskCountOperation(1);
        future.completeExceptionally(new IllegalStateException("failed"));

        var status = service.getStatus(taskId);
        assertThat(status.status()).isEqualTo(AsyncTaskStatus.FAILED);
        assertThat(status.result()).isNull();
        assertThat(status.error()).isEqualTo("IllegalStateException");
        assertThat(service.getCompletedTasks()).isZero();
    }

    @Test
    void getStatusShouldThrowWhenTaskDoesNotExist() {
        AsyncBusinessOperationService service = new AsyncBusinessOperationService(
            new FakeAsyncBusinessOperationRunner(new CompletableFuture<>())
        );

        assertThatThrownBy(() -> service.getStatus("missing"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Async task with id missing was not found");
    }

    private static final class FakeAsyncBusinessOperationRunner extends AsyncBusinessOperationRunner {

        private final CompletableFuture<Long> future;

        private FakeAsyncBusinessOperationRunner(CompletableFuture<Long> future) {
            super(null);
            this.future = future;
        }

        @Override
        public CompletableFuture<Long> countTasksAfterDelay(int delaySeconds) {
            return future;
        }
    }
}
