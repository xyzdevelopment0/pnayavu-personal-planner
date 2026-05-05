package com.maximovich.planner.services;

import com.maximovich.planner.components.AsyncBusinessOperationRunner;
import com.maximovich.planner.dtos.async.AsyncTaskStatus;
import com.maximovich.planner.dtos.async.AsyncTaskStatusResponse;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class AsyncBusinessOperationService {

    private final AsyncBusinessOperationRunner runner;
    private final Map<String, AsyncTaskStatusResponse> tasks = new ConcurrentHashMap<>();
    private final AtomicLong completedTasks = new AtomicLong();

    public AsyncBusinessOperationService(AsyncBusinessOperationRunner runner) {
        this.runner = runner;
    }

    public String startTaskCountOperation(int delaySeconds) {
        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, new AsyncTaskStatusResponse(taskId, AsyncTaskStatus.RUNNING, null, null));
        runner.countTasksAfterDelay(delaySeconds).whenComplete((result, error) -> {
            if (error == null) {
                completedTasks.incrementAndGet();
                tasks.put(taskId, new AsyncTaskStatusResponse(taskId, AsyncTaskStatus.COMPLETED, result, null));
            } else {
                tasks.put(taskId, new AsyncTaskStatusResponse(
                    taskId,
                    AsyncTaskStatus.FAILED,
                    null,
                    error.getClass().getSimpleName()
                ));
            }
        });
        return taskId;
    }

    public AsyncTaskStatusResponse getStatus(String taskId) {
        AsyncTaskStatusResponse response = tasks.get(taskId);
        if (response == null) {
            throw new ResourceNotFoundException("Async task", taskId);
        }
        return response;
    }

    public long getCompletedTasks() {
        return completedTasks.get();
    }
}
