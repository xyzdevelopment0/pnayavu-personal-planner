package com.maximovich.planner.services;

import com.maximovich.planner.components.TaskSearchIndex;
import com.maximovich.planner.repositories.TaskCommentRepository;
import com.maximovich.planner.exceptions.BusinessException;
import com.maximovich.planner.exceptions.ResourceNotFoundException;
import com.maximovich.planner.repositories.ProjectRepository;
import com.maximovich.planner.repositories.TaskRepository;
import com.maximovich.planner.entities.User;
import com.maximovich.planner.dtos.UserRequest;
import com.maximovich.planner.dtos.UserResponse;
import com.maximovich.planner.repositories.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskSearchIndex taskSearchIndex;

    public UserService(
        UserRepository userRepository,
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        TaskCommentRepository taskCommentRepository,
        TaskSearchIndex taskSearchIndex
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskSearchIndex = taskSearchIndex;
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "User with email %s already exists".formatted(email));
        }
        User user = new User(request.name().trim(), email);
        UserResponse response = UserResponse.fromEntity(userRepository.save(user));
        taskSearchIndex.clear();
        return response;
    }

    public UserResponse getById(Long id) {
        return UserResponse.fromEntity(getEntity(id));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserResponse::fromEntity).toList();
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = getEntity(id);
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "User with email %s already exists".formatted(email));
        }
        user.update(request.name().trim(), email);
        UserResponse response = UserResponse.fromEntity(user);
        taskSearchIndex.clear();
        return response;
    }

    @Transactional
    public void delete(Long id) {
        User user = getEntity(id);
        if (projectRepository.existsByOwnerId(id)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "User %d owns projects and cannot be deleted".formatted(id)
            );
        }
        if (taskRepository.existsByAssigneeId(id)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "User %d is assigned to tasks and cannot be deleted".formatted(id)
            );
        }
        if (taskCommentRepository.existsByAuthorId(id)) {
            throw new BusinessException(
                HttpStatus.CONFLICT,
                "User %d has comments and cannot be deleted".formatted(id)
            );
        }
        userRepository.delete(user);
        taskSearchIndex.clear();
    }

    private User getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
