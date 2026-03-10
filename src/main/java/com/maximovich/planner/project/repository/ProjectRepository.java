package com.maximovich.planner.project.repository;

import com.maximovich.planner.project.domain.Project;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByIdAsc();

    boolean existsByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = "tasks")
    @Query("select distinct p from Project p order by p.id")
    List<Project> findAllWithTasks();
}
