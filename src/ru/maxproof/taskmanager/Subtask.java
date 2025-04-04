package ru.maxproof.taskmanager;


public class Subtask extends Task {

    private final int epicId;


    public Subtask(TaskBuilder builder) {
        super(builder);
        this.epicId = builder.getEpicId();
    }

    public Subtask(TaskBuilder builder, int epicId) {
        super(builder);
        this.epicId = epicId;
    }

    /**
     * Возвращает Epic ID родительской задачи
     * @return id родительской epic задачи
     */
    public int getEpicId() {
        return epicId;
    }
}
