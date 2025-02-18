package ru.maxproof.taskmanager;


public class Subtask extends Task {

    private final int epicId;


    /**
     * Конструктор подзадачи, хранимой в менеджере задач.
     * Запускается только менеджером задач, с действительным id
     * @param epicId Id родительской Epic задача
     * @param id Реальный id подзадачи
     * @param name Наименование подзадачи
     * @param description Описание подзадачи
     * @param status Статус подзадачи
     */
    Subtask(int epicId, int id, String name, String description, TaskStatus status) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    /**
     * Конструктор копирования
     * @param subtask Исходный объект
     */
    Subtask(Subtask subtask) {
        this(subtask.epicId, subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus());
    }

    /**
     * Конструктор подзадачи, хранимой в менеджере задач.
     * Запускается только менеджером задач, с действительным id
     * @param epicId Id родительской Epic задача
     * @param id Реальный id подзадачи
     * @param subtask Черновая подзадача
     */
    Subtask(int epicId, int id, Subtask subtask) {
        this(epicId, id, subtask.getName(), subtask.getDescription(), subtask.getStatus());
    }


    /**
     * Конструктор подзадачи для внешнего приложения.
     * Может быть использован только при создании Epic задач
     * @param name Наименование подзадачи
     * @param description Описание подзадачи
     */
    public Subtask(String name, String description) {
        this(TaskManager.DRAFT_TASK_ID, TaskManager.DRAFT_TASK_ID, name, description, TaskStatus.NEW);
    }


    /**
     * Изменение наименования и/или описания подзадачи
     * @param subtask Исходная подзадача
     * @param name Наименование подзадачи
     * @param description Описание подзадачи
     */
    public Subtask(Subtask subtask, String name, String description) {
        this(subtask.getEpicId(),subtask.getId(), name, description, subtask.getStatus());
    }


    /**
     * Изменение статуса подзадачи
     * @param subtask Исходная подзадача
     * @param status Новый статус подзадачи
     */
    public Subtask(Subtask subtask, TaskStatus status) {
        this(subtask.getEpicId(),subtask.getId(), subtask.getName(), subtask.getDescription(), status);
    }


    /**
     * Возвращает Epic ID родительской задачи
     * @return id родительской epic задачи
     */
    public int getEpicId() {
        return epicId;
    }
}
