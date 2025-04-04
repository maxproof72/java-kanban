package ru.maxproof.taskmanager;


import java.time.LocalDateTime;
import java.util.List;

public class Epic extends Task {

    /**
     * Список дочерних задач
     */
    private final List<Integer> subtaskIds;

    private final LocalDateTime endTime;


    public Epic(TaskBuilder builder) {
        super(builder);
        subtaskIds = builder.getSubs() == null ? List.of() : builder.getSubs();
        endTime = builder.getEndTime();
    }

    /**
     * Получение копии перечня дочерних задач
     * @return Список-копия перечня дочерних задач
     */
    public List<Integer> getSubtasks() {
        return this.subtaskIds;
    }


    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
