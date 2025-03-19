package ru.maxproof.taskmanager;

import java.util.List;

/**
 * Класс реализации интерфейса HistoryManager для работы в памяти
 */
public class InMemoryHistoryManager implements HistoryManager {

    private final HistoryNodeList history = new HistoryNodeList();


    @Override
    public void add(Task task) {
        history.addTask(task);
    }

    @Override
    public void remove(int id) {
        history.removeTaskById(id);
    }

    @Override
    public List<Task> getHistory() {
        return history.stream().toList();
    }
}
