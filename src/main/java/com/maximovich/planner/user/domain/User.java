package com.maximovich.planner.user.domain;

import com.maximovich.planner.comment.domain.TaskComment;
import com.maximovich.planner.common.domain.BaseEntity;
import com.maximovich.planner.project.domain.Project;
import com.maximovich.planner.task.domain.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Project> ownedProjects = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<TaskComment> comments = new ArrayList<>();

    protected User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void update(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Project> getOwnedProjects() {
        return ownedProjects;
    }

    public List<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public List<TaskComment> getComments() {
        return comments;
    }
}
