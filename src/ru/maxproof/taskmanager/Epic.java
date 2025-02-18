package ru.maxproof.taskmanager;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Epic extends Task {

    /**
     * Список дочерних задач
     */
    private final List<Integer> subtaskIds = new ArrayList<>();


    /**
     * Конструктор Epic задачи для внутреннего использования
     * @param id Идентификатор в реестре
     * @param name Наименование задачи
     * @param description Описание задачи
     * @param status Статус задачи
     * @param subtaskIds Перечень дочерних задач
     */
    Epic(int id, String name, String description, TaskStatus status, Collection<Integer> subtaskIds) {
        super(id, name, description, status);
        this.subtaskIds.addAll(subtaskIds);
    }

    /**
     * Конструктор копирования
     * @param epic Исходный объект
     */
    Epic(Epic epic) {
        this(epic.getId(), epic.getName(), epic.getDescription(), epic.getStatus(), epic.getSubtasks());
    }

    /**
     * Конструктор подзадачи, хранимой в менеджере задач.
     * Запускается только менеджером задач, с действительным id
     * @param id Реальный id задачи
     * @param epic Черновая Epic задача
     */
    Epic(int id, Epic epic) {
        this(id, epic.getName(), epic.getDescription(), epic.getStatus(), epic.subtaskIds);
    }


    /**
     * Конструктор Epic задачи для внешнего приложения
     * @param name Наименование Epic задачи
     * @param description Описание Epic задачи
     */
    public Epic(String name, String description) {
        super(name, description);
    }


    /**
     * Изменение наименования и/или описания
     * @param epic Исходная Epic задача
     * @param name Наименование задачи
     * @param description Описание задачи
     */
    public Epic(Epic epic, String name, String description) {
        this(epic.getId(), name, description, epic.getStatus(), epic.getSubtasks());
    }


    /**
     * Регистрация подзадачи (запускается менеджером)
     * @param id Id подзадачи
     */
    void registerSubtask(int id) {
        subtaskIds.add(id);
    }


    boolean unregisterSubtask(int id) {
        return subtaskIds.remove((Integer) id);
    }


    /**
     * Получение копии перечня дочерних задач
     * @return Список-копия перечня дочерних задач
     */
    public List<Integer> getSubtasks() {
        return List.copyOf(this.subtaskIds);
    }


    /**
     * Очистка подзадач
     */
    void clearSubtasks() {
        subtaskIds.clear();
        setStatus(TaskStatus.NEW);
    }
}
