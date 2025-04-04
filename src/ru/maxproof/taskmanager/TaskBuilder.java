package ru.maxproof.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskBuilder {

    private int id;
    private String name;
    private String description;
    private TaskStatus status;
    private LocalDateTime startTime;
    private Duration duration;
    private int epicId;
    private List<Integer> subs;


    public TaskBuilder() {
        clear();
    }

    public TaskBuilder(Task task) {
        id = task.getId();
        name = task.getName();
        description = task.getDescription();
        status = task.getStatus();
        startTime = task.getStartTime();
        duration = task.getDuration();
    }

    public TaskBuilder(Subtask subtask) {
        this((Task)subtask);
        epicId = subtask.getEpicId();
    }

    public TaskBuilder(Epic epic) {
        this((Task)epic);
        this.subs = epic.getSubtasks();
    }

    void clear() {
        id = TaskManager.DRAFT_TASK_ID;
        name = "";
        description = "";
        status = TaskStatus.NEW;
        startTime = null;
        duration = null;
        epicId = TaskManager.DRAFT_TASK_ID;
        subs = null;
    }

    TaskBuilder setId(int id) {
        this.id = id;
        return this;
    }

    public TaskBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TaskBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskBuilder setStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public TaskBuilder setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public TaskBuilder setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public Task buildTask() {
        return new Task(this);
    }

    public Subtask buildSubtask() {
        return new Subtask(this);
    }

    public Subtask buildSubtask(int epicId) {
        return new Subtask(this, epicId);
    }

    public Epic buildEpic() {
        return new Epic(this);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public List<Integer> getSubs() {
        return subs;
    }

    public void setSubs(List<Integer> subs) {
        this.subs = subs;
    }
}
