package com.maximovich.planner.task.diagnostics.service;

import com.maximovich.planner.comment.repository.TaskCommentRepository;
import com.maximovich.planner.project.repository.ProjectRepository;
import com.maximovich.planner.tag.repository.TagRepository;
import com.maximovich.planner.task.diagnostics.dto.EntityCountSnapshot;
import com.maximovich.planner.task.diagnostics.dto.NPlusOneDemoResponse;
import com.maximovich.planner.task.diagnostics.dto.ProjectTaskCountResponse;
import com.maximovich.planner.task.diagnostics.dto.TransactionDemoResponse;
import com.maximovich.planner.task.repository.TaskRepository;
import com.maximovich.planner.user.repository.PlannerUserRepository;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskDiagnosticsService {

    private final ProjectRepository projectRepository;
    private final PlannerUserRepository plannerUserRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TagRepository tagRepository;
    private final TaskTransactionScenarioService taskTransactionScenarioService;
    private final EntityManagerFactory entityManagerFactory;

    public TaskDiagnosticsService(
        ProjectRepository projectRepository,
        PlannerUserRepository plannerUserRepository,
        TaskRepository taskRepository,
        TaskCommentRepository taskCommentRepository,
        TagRepository tagRepository,
        TaskTransactionScenarioService taskTransactionScenarioService,
        EntityManagerFactory entityManagerFactory
    ) {
        this.projectRepository = projectRepository;
        this.plannerUserRepository = plannerUserRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.tagRepository = tagRepository;
        this.taskTransactionScenarioService = taskTransactionScenarioService;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Transactional(readOnly = true)
    public NPlusOneDemoResponse demonstrateNPlusOne() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();
        projectRepository.findAllByOrderByIdAsc().forEach(project -> project.getTasks().size());
        long nPlusOneQueryCount = statistics.getPrepareStatementCount();

        statistics.clear();
        List<ProjectTaskCountResponse> projects = projectRepository.findAllWithTasks().stream()
            .map(project -> new ProjectTaskCountResponse(project.getId(), project.getName(), project.getTasks().size()))
            .toList();
        long optimizedQueryCount = statistics.getPrepareStatementCount();

        return new NPlusOneDemoResponse(nPlusOneQueryCount, optimizedQueryCount, projects);
    }

    public TransactionDemoResponse demonstrateWithoutTransaction() {
        EntityCountSnapshot before = snapshot();
        try {
            taskTransactionScenarioService.saveRelatedEntitiesWithoutTransaction(uniqueSuffix());
            return new TransactionDemoResponse("WITHOUT_TRANSACTION", false, null, before, snapshot());
        } catch (IllegalStateException ex) {
            return new TransactionDemoResponse("WITHOUT_TRANSACTION", false, ex.getMessage(), before, snapshot());
        }
    }

    public TransactionDemoResponse demonstrateWithTransaction() {
        EntityCountSnapshot before = snapshot();
        try {
            taskTransactionScenarioService.saveRelatedEntitiesWithTransaction(uniqueSuffix());
            return new TransactionDemoResponse("WITH_TRANSACTION", true, null, before, snapshot());
        } catch (IllegalStateException ex) {
            return new TransactionDemoResponse("WITH_TRANSACTION", true, ex.getMessage(), before, snapshot());
        }
    }

    private EntityCountSnapshot snapshot() {
        return new EntityCountSnapshot(
            plannerUserRepository.count(),
            projectRepository.count(),
            taskRepository.count(),
            taskCommentRepository.count(),
            tagRepository.count()
        );
    }

    private String uniqueSuffix() {
        return String.valueOf(System.currentTimeMillis());
    }
}
