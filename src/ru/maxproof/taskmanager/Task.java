package ru.maxproof.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    // region Fields

    private final int id;
    private final String name;
    private final String description;
    private TaskStatus status;
    protected LocalDateTime startTime;
    protected Duration duration;

    // endregion


    /**
     * Конструктор задачи при помощи настроенного строителя
     * @param builder Строитель задачи
     */
    public Task(TaskBuilder builder) {
        this.id = builder.getId();
        this.name = builder.getName();
        this.description = builder.getDescription();
        this.status = builder.getStatus();
        this.startTime = builder.getStartTime();
        this.duration = builder.getDuration();
    }


    // region Getters & setters

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public boolean isNew() {
        return getStatus() == TaskStatus.NEW;
    }

    public boolean isDone() {
        return getStatus() == TaskStatus.DONE;
    }

    // endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return (id == TaskManager.DRAFT_TASK_ID || task.id == TaskManager.DRAFT_TASK_ID || id == task.id) &&
               Objects.equals(name, task.name) &&
               Objects.equals(description, task.description) &&
               status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    LocalDateTime getStartTime() {
        return startTime;
    }

    Duration getDuration() {
        return duration;
    }

    LocalDateTime getEndTime() {
        return (startTime != null && duration != null)? startTime.plus(duration) : null;
    }
}
