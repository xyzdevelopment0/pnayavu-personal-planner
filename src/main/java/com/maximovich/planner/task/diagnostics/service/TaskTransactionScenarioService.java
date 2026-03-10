package com.maximovich.planner.task.diagnostics.service;

import com.maximovich.planner.comment.domain.TaskComment;
import com.maximovich.planner.comment.repository.TaskCommentRepository;
import com.maximovich.planner.project.domain.Project;
import com.maximovich.planner.project.repository.ProjectRepository;
import com.maximovich.planner.task.domain.Task;
import com.maximovich.planner.task.domain.TaskStatus;
import com.maximovich.planner.task.repository.TaskRepository;
import com.maximovich.planner.user.domain.PlannerUser;
import com.maximovich.planner.user.repository.PlannerUserRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskTransactionScenarioService {

    private final PlannerUserRepository plannerUserRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    public TaskTransactionScenarioService(
        PlannerUserRepository plannerUserRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        TaskCommentRepository taskCommentRepository
    ) {
        this.plannerUserRepository = plannerUserRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    public void saveRelatedEntitiesWithoutTransaction(String suffix) {
        persistAndFail(suffix);
    }

    @Transactional
    public void saveRelatedEntitiesWithTransaction(String suffix) {
        persistAndFail(suffix);
    }

    private void persistAndFail(String suffix) {
        PlannerUser user = plannerUserRepository.save(new PlannerUser(
            "Rollback Demo %s".formatted(suffix),
            "rollback-%s@example.com".formatted(suffix)
        ));
        Project project = projectRepository.save(new Project(
            "Transaction Project %s".formatted(suffix),
            "Persistence scenario for transaction laboratory work",
            user
        ));
        Task task = new Task(
            "Failing task %s".formatted(suffix),
            "This task is created to demonstrate transaction boundaries",
            TaskStatus.IN_PROGRESS,
            LocalDate.now().plusDays(3),
            project,
            user
        );
        Task savedTask = taskRepository.save(task);
        taskCommentRepository.save(new TaskComment(
            "The next line throws an exception on purpose",
            savedTask,
            user
        ));
        throw new IllegalStateException("Simulated failure after saving related entities");
    }
}
