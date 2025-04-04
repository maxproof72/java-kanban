package ru.maxproof.taskmanager;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Epic extends Task {

    /**
     * Список дочерних задач
     */
    private final List<Integer> subtaskIds = new ArrayList<>();

    LocalDateTime endTime;


    public Epic(TaskBuilder builder) {
        super(builder);
        if (builder.getSubs() != null)
            subtaskIds.addAll(builder.getSubs());
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
