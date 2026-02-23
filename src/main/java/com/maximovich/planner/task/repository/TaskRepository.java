package com.maximovich.planner.task.repository;

import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.domain.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    List<Task> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    List<Task> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(TaskStatus status, String title);
}
