package com.maximovich.planner.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_comments")
public class TaskComment extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    protected TaskComment() {
    }

    public TaskComment(String content, Task task, User author) {
        this.content = content;
        this.task = task;
        this.author = author;
    }

    public void update(String content, Task task, User author) {
        this.content = content;
        this.task = task;
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getAuthor() {
        return author;
    }
}
