package com.maximovich.planner.tag.domain;

import com.maximovich.planner.common.domain.BaseEntity;
import com.maximovich.planner.task.domain.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Task> tasks = new LinkedHashSet<>();

    protected Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<Task> getTasks() {
        return tasks;
    }
}
