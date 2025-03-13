package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void addAndRemove() {

        Task task1 = new Task(1, "task1", "desc1", TaskStatus.NEW);
        Task task2 = new Task(2, "task2", "desc2", TaskStatus.NEW);
        Task task3 = new Task(3, "task3", "desc3", TaskStatus.NEW);
        List<Task> history;

        // Добавляем три задачи и проверяем размер и верхнюю запись
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task3, history.getLast());

        // Обновляем просмотр задачи 1
        historyManager.add(task1);
        history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.getLast());

        // Удаление задачи 2
        historyManager.remove(2);
        history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task3, history.getFirst());
        assertEquals(task1, history.getLast());
    }
}