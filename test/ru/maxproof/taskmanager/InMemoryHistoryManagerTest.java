package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void addAndRemove() {

        Task task1 = new TaskBuilder()
                .setId(1).setName("task1").setDescription("desc1").setStatus(TaskStatus.NEW).buildTask();
        Task task2 = new TaskBuilder()
                .setId(2).setName("task2").setDescription("desc2").setStatus(TaskStatus.NEW).buildTask();
        Task task3 = new TaskBuilder()
                .setId(3).setName("task3").setDescription("desc3").setStatus(TaskStatus.NEW).buildTask();
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