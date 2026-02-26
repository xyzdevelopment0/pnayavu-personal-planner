package com.maximovich.planner.task.repository;

import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.domain.TaskStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(
        """
        select t
        from Task t
        where (:status is null or t.status = :status)
          and (:name is null or lower(t.title) = lower(:name))
        order by t.createdAt desc
        """
    )
    List<Task> findAllByFilters(@Param("status") TaskStatus status, @Param("name") String name);
}
