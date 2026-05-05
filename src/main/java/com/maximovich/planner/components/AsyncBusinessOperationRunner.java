package com.maximovich.planner.components;

import com.maximovich.planner.repositories.TaskRepository;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AsyncBusinessOperationRunner {

    private final TaskRepository taskRepository;

    public AsyncBusinessOperationRunner(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Long> countTasksAfterDelay(int delaySeconds) {
        try {
            Thread.sleep(delaySeconds * 1000L);
            return CompletableFuture.completedFuture(taskRepository.count());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(ex);
        }
    }
}
