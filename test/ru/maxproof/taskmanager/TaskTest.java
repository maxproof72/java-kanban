package ru.maxproof.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testEquals() {
        Task draftTask1 = new Task("abc", "def");
        Task draftTask2 = new Task("abc", "def");
        Task draftTask3 = new Task("eee", "def");
        final int id1 = manager.createTask(draftTask1);
        final int id2 = manager.createTask(draftTask2);
        final int id3 = manager.createTask(draftTask3);
        Task savedTask1 = manager.getTask(id1).orElseThrow();
        Task savedTask2 = manager.getTask(id2).orElseThrow();
        Task savedTask3 = manager.getTask(id3).orElseThrow();

        // Черновые задачи сравниваются без учета ID
        assertEquals(draftTask1, draftTask2);
        assertNotEquals(draftTask1, draftTask3);
        assertNotEquals(draftTask2, draftTask3);

        // Зарегистрированные задачи сравниваются с учетом ID
        assertEquals(draftTask1, savedTask1);
        assertEquals(draftTask2, savedTask2);
        assertEquals(draftTask3, savedTask3);
        assertNotEquals(savedTask1, savedTask2);
        assertNotEquals(savedTask1, savedTask3);
        assertNotEquals(savedTask2, savedTask3);
    }

    @Test
    void testHashCode() {

        Task draftTask1 = new Task("abc", "def");
        Task draftTask2 = new Task("abc", "def");
        Task draftTask3 = new Task("eee", "def");
        final int id1 = manager.createTask(draftTask1);
        final int id2 = manager.createTask(draftTask2);
        final int id3 = manager.createTask(draftTask3);
        Task savedTask1 = manager.getTask(id1).orElseThrow();
        Task savedTask2 = manager.getTask(id2).orElseThrow();
        Task savedTask3 = manager.getTask(id3).orElseThrow();

        // Черновые задачи сравниваются без учета ID
        assertEquals(draftTask1.hashCode(), draftTask2.hashCode());
        assertNotEquals(draftTask1.hashCode(), draftTask3.hashCode());
        assertNotEquals(draftTask2.hashCode(), draftTask3.hashCode());

        // Зарегистрированные задачи сравниваются с учетом ID
        assertNotEquals(draftTask1.hashCode(), savedTask1.hashCode());
        assertNotEquals(draftTask2.hashCode(), savedTask2.hashCode());
        assertNotEquals(draftTask3.hashCode(), savedTask3.hashCode());
        assertNotEquals(savedTask1.hashCode(), savedTask2.hashCode());
        assertNotEquals(savedTask1.hashCode(), savedTask3.hashCode());
        assertNotEquals(savedTask2.hashCode(), savedTask3.hashCode());
    }
}