package com.proactiva.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @Column(name = "history_id") // CORREÇÃO: Mapeando para history_id do banco
    private Long id;

    @NotNull(message = "Task ID é obrigatório")
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @NotBlank(message = "Action é obrigatória")
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // Construtores
    public TaskHistory() {
    }

    public TaskHistory(Long taskId, String action, String oldStatus, String newStatus, String description) {
        this.taskId = taskId;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.description = description;
    }

    // Métodos de ciclo de vida
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskHistory that = (TaskHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString
    @Override
    public String toString() {
        return "TaskHistory{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", action='" + action + '\'' +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", changedAt=" + changedAt +
                '}';
    }
}