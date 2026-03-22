package com.maximovich.planner.repositories;

import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.Task;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @EntityGraph(attributePaths = {"project", "assignee", "tags"})
    List<Task> findByIdInOrderByIdAsc(Collection<Long> ids);

    @Query(
        value = """
            select t.id
            from Task t
            join t.project p
            join p.owner o
            where (:projectName is null or lower(p.name) like lower(concat('%', :projectName, '%')))
              and (:ownerEmail is null or lower(o.email) = lower(:ownerEmail))
              and (:status is null or t.status = :status)
            order by t.id
            """,
        countQuery = """
            select count(t.id)
            from Task t
            join t.project p
            join p.owner o
            where (:projectName is null or lower(p.name) like lower(concat('%', :projectName, '%')))
              and (:ownerEmail is null or lower(o.email) = lower(:ownerEmail))
              and (:status is null or t.status = :status)
            """
    )
    Page<Long> searchIdsWithJpql(
        @Param("projectName") String projectName,
        @Param("ownerEmail") String ownerEmail,
        @Param("status") TaskStatus status,
        Pageable pageable
    );

    @Query(
        value = """
            select t.id
            from tasks t
            join projects p on p.id = t.project_id
            join users u on u.id = p.owner_id
            where (:projectName is null or p.name ilike concat('%', :projectName, '%'))
              and (:ownerEmail is null or lower(u.email) = lower(:ownerEmail))
              and (:status is null or cast(t.status as varchar) = :status)
            order by t.id
            """,
        countQuery = """
            select count(t.id)
            from tasks t
            join projects p on p.id = t.project_id
            join users u on u.id = p.owner_id
            where (:projectName is null or p.name ilike concat('%', :projectName, '%'))
              and (:ownerEmail is null or lower(u.email) = lower(:ownerEmail))
              and (:status is null or cast(t.status as varchar) = :status)
            """,
        nativeQuery = true
    )
    Page<Long> searchIdsWithNative(
        @Param("projectName") String projectName,
        @Param("ownerEmail") String ownerEmail,
        @Param("status") String status,
        Pageable pageable
    );
}
