package ru.maxproof.taskmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс реализации интерфейса HistoryManager для работы в памяти
 */
public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> history = new ArrayList<>();

    @Override
    public void addTask(Task task) {

        history.add(task);
        while (history.size() > 10)
            history.removeFirst();
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
