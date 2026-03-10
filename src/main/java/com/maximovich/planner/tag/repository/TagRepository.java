package com.maximovich.planner.tag.repository;

import com.maximovich.planner.tag.domain.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByIdAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @EntityGraph(attributePaths = "tasks")
    @Query("select t from Tag t where t.id = :id")
    Optional<Tag> findByIdWithTasks(@Param("id") Long id);
}
