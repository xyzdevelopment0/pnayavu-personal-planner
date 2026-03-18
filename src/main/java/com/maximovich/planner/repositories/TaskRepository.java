package com.maximovich.planner.repositories;

import com.maximovich.planner.entities.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByAssigneeId(Long assigneeId);

    @EntityGraph(attributePaths = {"project", "assignee", "tags"})
    @Query("select distinct t from Task t where t.id = :id")
    Optional<Task> findByIdWithRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"project", "assignee", "tags"})
    @Query("select distinct t from Task t order by t.id")
    List<Task> findAllWithRelations();
}
