package com.maximovich.planner.bootstrap;

import com.maximovich.planner.entities.TaskComment;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.entities.Project;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.entities.Tag;
import com.maximovich.planner.repositories.TagRepository;
import com.maximovich.planner.entities.Task;
import com.maximovich.planner.entities.TaskStatus;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.repositories.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DemoDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TagRepository tagRepository;

    public DemoDataInitializer(
        UserRepository userRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        TaskCommentRepository taskCommentRepository,
        TagRepository tagRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        User alice = userRepository.save(new User("Alice Novak", "alice@example.com"));
        User bob = userRepository.save(new User("Bob Stone", "bob@example.com"));

        Tag backend = tagRepository.save(new Tag("backend"));
        Tag study = tagRepository.save(new Tag("study"));
        Tag urgent = tagRepository.save(new Tag("urgent"));

        Project jpaLab = projectRepository.save(new Project(
            "JPA Laboratory",
            "Second laboratory work focused on Spring Data JPA",
            alice
        ));
        Project semester = projectRepository.save(new Project(
            "Semester Planning",
            "Tasks for study and personal planning during the semester",
            bob
        ));

        Task entityModel = new Task(
            "Create entity model",
            "Define entities and their relationships",
            TaskStatus.IN_PROGRESS,
            LocalDate.now().plusDays(2),
            jpaLab,
            alice
        );
        entityModel.addTag(backend);
        entityModel.addTag(study);

        Task nPlusOne = new Task(
            "Demonstrate N+1",
            "Prepare repository methods with EntityGraph",
            TaskStatus.TODO,
            LocalDate.now().plusDays(3),
            jpaLab,
            bob
        );
        nPlusOne.addTag(backend);
        nPlusOne.addTag(urgent);

        Task semesterReport = new Task(
            "Prepare semester report",
            "Collect completed tasks and produce summary",
            TaskStatus.TODO,
            LocalDate.now().plusDays(7),
            semester,
            alice
        );
        semesterReport.addTag(study);

        taskRepository.saveAll(List.of(entityModel, nPlusOne, semesterReport));
        taskCommentRepository.saveAll(List.of(
            new TaskComment("Start from ER diagram and then move to repositories", entityModel, bob),
            new TaskComment("Use EntityGraph for the optimized version", nPlusOne, alice),
            new TaskComment("Include transaction rollback screenshots in the report", semesterReport, bob)
        ));
    }
}
