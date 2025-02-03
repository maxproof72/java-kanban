package ru.maxproof.taskmanager;

import java.util.Objects;

public class Task {

    /**
     * Идентификатор для незарегистрированной задачи (до передачи менеджеру задач)
     */
    public static final int DRAFT_TASK_ID = 0;


    // region Fields

    private final int id;
    private final String name;
    private final String description;
    private TaskStatus status;

    // endregion


    // region Constructors

    /**
     * Общий конструктор инициализации полей
     * @param id Идентификатор задачи
     * @param name Наименование задачи
     * @param description Описание задачи
     * @param status Статус задачи
     */
    Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    /**
     * Конструктор создания черновой задачи со статусом NEW
     * @param name Наименование задачи
     * @param description Краткое описание задачи
     */
    public Task(String name, String description) {
        this(DRAFT_TASK_ID, name, description, TaskStatus.NEW);
    }

    /**
     * Изменение наименования и/или описания задачи
     * @param task Исходная задача
     * @param name Новое имя
     * @param description Новое описание
     */
    public Task(Task task, String name, String description) {
        this(task.id, name, description, task.status);
    }

    /**
     * Изменение статуса задачи
     * @param task Исходная задача
     * @param status Новый статус
     */
    public Task(Task task, TaskStatus status) {
        this(task.id, task.name, task.description, status);
    }

    /**
     * Конструктор создания задачи на основе черновой
     * @param id Легальный идентификатор, полученный от менеджера задач
     * @param draftTask Исходная черновая задача
     */
    Task(int id, Task draftTask) {
        this(id, draftTask.name, draftTask.description, draftTask.status);
    }

    // endregion


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
        return id == task.id;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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
}
