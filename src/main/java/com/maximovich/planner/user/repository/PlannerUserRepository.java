package com.maximovich.planner.user.repository;

import com.maximovich.planner.user.domain.PlannerUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlannerUserRepository extends JpaRepository<PlannerUser, Long> {

    List<PlannerUser> findAllByOrderByIdAsc();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
