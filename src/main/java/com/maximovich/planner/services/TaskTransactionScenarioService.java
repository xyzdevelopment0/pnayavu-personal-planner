package com.maximovich.planner.services;

import com.maximovich.planner.entities.Project;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.repositories.UserRepository;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskTransactionScenarioService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public TaskTransactionScenarioService(
        UserRepository userRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public void runWithoutTransaction(String marker) {
        persistAndFail(marker);
    }

    @Transactional
    public void runWithTransaction(String marker) {
        persistAndFail(marker);
    }

    private void persistAndFail(String marker) {
        User user = userRepository.saveAndFlush(new User(
            "Transaction Demo " + marker,
            marker + "@planner.demo"
        ));
        Project project = projectRepository.saveAndFlush(new Project(
            "Transaction Project " + marker,
            "Transaction demonstration " + marker,
            user
        ));
        taskRepository.saveAndFlush(new Task(
            "Transaction Task " + marker,
            "This row exists only when no shared transaction is used",
            TaskStatus.TODO,
            LocalDate.now().plusDays(1),
            project,
            user
        ));
        throw new BusinessException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Transaction diagnostics failure for marker " + marker
        );
    }
}
