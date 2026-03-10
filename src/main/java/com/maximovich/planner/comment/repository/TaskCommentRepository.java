package com.maximovich.planner.comment.repository;

import com.maximovich.planner.comment.domain.TaskComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    boolean existsByAuthorId(Long authorId);

    @EntityGraph(attributePaths = {"task", "author"})
    @Query("select c from TaskComment c where c.id = :id")
    Optional<TaskComment> findByIdWithRelations(@Param("id") Long id);

    @EntityGraph(attributePaths = {"task", "author"})
    @Query("select c from TaskComment c order by c.id")
    List<TaskComment> findAllWithRelations();
}
