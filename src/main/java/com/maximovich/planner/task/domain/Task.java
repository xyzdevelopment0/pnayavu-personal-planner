package com.maximovich.planner.task.domain;

import com.maximovich.planner.comment.domain.TaskComment;
import com.maximovich.planner.common.domain.BaseEntity;
import com.maximovich.planner.project.domain.Project;
import com.maximovich.planner.tag.domain.Tag;
import com.maximovich.planner.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TaskComment> comments = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    protected Task() {
    }

    public Task(
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Project project,
        User assignee
    ) {
        this.title = title;
        this.description = description;
        this.status = status == null ? TaskStatus.TODO : status;
        this.dueDate = dueDate;
        this.project = project;
        this.assignee = assignee;
    }

    public void update(
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Project project,
        User assignee
    ) {
        this.title = title;
        this.description = description;
        this.status = status == null ? TaskStatus.TODO : status;
        this.dueDate = dueDate;
        this.project = project;
        this.assignee = assignee;
    }

    public void replaceTags(Set<Tag> newTags) {
        Set<Tag> existingTags = new LinkedHashSet<>(tags);
        existingTags.forEach(this::removeTag);
        newTags.forEach(this::addTag);
    }

    public void addTag(Tag tag) {
        if (tags.add(tag)) {
            tag.getTasks().add(this);
        }
    }

    public void removeTag(Tag tag) {
        if (tags.remove(tag)) {
            tag.getTasks().remove(this);
        }
    }

    public void addComment(TaskComment comment) {
        comments.add(comment);
        comment.setTask(this);
    }

    public void removeComment(TaskComment comment) {
        comments.remove(comment);
        comment.setTask(null);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignee() {
        return assignee;
    }

    public List<TaskComment> getComments() {
        return comments;
    }

    public Set<Tag> getTags() {
        return tags;
    }
}
