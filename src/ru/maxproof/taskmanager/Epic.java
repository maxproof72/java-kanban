package ru.maxproof.taskmanager;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    /**
     * Список дочерних задач
     */
    private final List<Integer> subtaskIds = new ArrayList<>();

    private final LocalDateTime endTime;


    public Epic(TaskBuilder builder) {
        super(builder);
        if (builder.getSubs() != null)
            subtaskIds.addAll(builder.getSubs());
        endTime = builder.getEndTime();
    }

    /**
     * Регистрация подзадачи (запускается менеджером)
     * @param id Id подзадачи
     */
    void registerSubtask(int id) {
        subtaskIds.add(id);
    }


    Epic unregisterSubtask(int id) {
        subtaskIds.remove((Integer) id);
        return this;
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
    Epic clearSubtasks() {
        subtaskIds.clear();
        return this;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
