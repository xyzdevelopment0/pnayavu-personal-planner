package com.maximovich.planner.services;

import com.maximovich.planner.dtos.TransactionDiagnosticsResponse;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TaskTransactionDiagnosticsService {

    private static final String WITHOUT_TRANSACTION = "WITHOUT_TRANSACTION";
    private static final String WITH_TRANSACTION = "WITH_TRANSACTION";

    private final TaskTransactionScenarioService taskTransactionScenarioService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    public TaskTransactionDiagnosticsService(
        TaskTransactionScenarioService taskTransactionScenarioService,
        UserRepository userRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        TaskCommentRepository taskCommentRepository
    ) {
        this.taskTransactionScenarioService = taskTransactionScenarioService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    public TransactionDiagnosticsResponse demonstrateWithoutTransaction() {
        return runScenario(WITHOUT_TRANSACTION, taskTransactionScenarioService::runWithoutTransaction);
    }

    public TransactionDiagnosticsResponse demonstrateWithTransaction() {
        return runScenario(WITH_TRANSACTION, taskTransactionScenarioService::runWithTransaction);
    }

    private TransactionDiagnosticsResponse runScenario(String scenario, TransactionScenarioRunner runner) {
        String marker = "tx-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String errorMessage = null;

        try {
            runner.run(marker);
        } catch (RuntimeException exception) {
            errorMessage = exception.getMessage();
        }

        long persistedUsers = userRepository.countByEmailContainingIgnoreCase(marker);
        long persistedProjects = projectRepository.countByNameContainingIgnoreCase(marker);
        long persistedTasks = taskRepository.countByTitleContainingIgnoreCase(marker);
        long persistedComments = taskCommentRepository.countByContentContainingIgnoreCase(marker);
        boolean rollbackApplied = persistedUsers == 0
            && persistedProjects == 0
            && persistedTasks == 0
            && persistedComments == 0;

        return new TransactionDiagnosticsResponse(
            scenario,
            marker,
            errorMessage != null,
            rollbackApplied,
            errorMessage,
            persistedUsers,
            persistedProjects,
            persistedTasks,
            persistedComments
        );
    }

    @FunctionalInterface
    private interface TransactionScenarioRunner {

        void run(String marker);
    }
}
