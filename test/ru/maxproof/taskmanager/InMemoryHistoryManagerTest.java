package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void addTask() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.addTask(new Task("task1", "desc1"));
        historyManager.addTask(new Task("task2", "desc2"));
        assertEquals(2, historyManager.getHistory().size());
    }
}