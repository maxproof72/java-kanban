package ru.maxproof.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    TaskManager manager;
    Epic epic;
    int idSub1;
    int idSub2;


    @BeforeEach
    void initTaskManager() {
        manager = new InMemoryTaskManager();
        int epicId = manager.createEpic(new Epic("abc", "def"));
        idSub1 = manager.createSubtask(epicId, new Subtask("sub1", "desc1"));
        idSub2 = manager.createSubtask(epicId, new Subtask("sub2", "desc2"));
        epic = manager.getEpic(epicId).orElseThrow();
    }

    @Test
    void registerAndUnregisterSubtask() {
        assertEquals(epic.getSubtasks().size(), 2, "Неправильное число подзадач");
        assertTrue(epic.getSubtasks().contains(idSub1), "Не найдено привязанной подзадачи 1");
        assertTrue(epic.getSubtasks().contains(idSub2), "Не найдено привязанной подзадачи 2");
        epic.unregisterSubtask(idSub1);
        assertFalse(epic.getSubtasks().contains(idSub1), "Не работает отвязка подзадачи от эпика");
    }

    @Test
    void getSubtasks() {
        assertEquals(epic.getSubtasks().size(), 2, "Неправильное число подзадач");
        assertTrue(epic.getSubtasks().contains(idSub1), "Не найдено привязанной подзадачи 1");
        assertTrue(epic.getSubtasks().contains(idSub2), "Не найдено привязанной подзадачи 2");
    }

    @Test
    void clearSubtasks() {
        epic.clearSubtasks();
        assertTrue(epic.getSubtasks().isEmpty(), "Некорректная работа очистки подзадач");
    }

    @Test
    void testEquals() {
        Epic draftTask1 = new Epic("abc", "def");
        Epic draftTask2 = new Epic("abc", "def");
        Epic draftTask3 = new Epic("eee", "def");
        final int id1 = manager.createEpic(draftTask1);
        final int id2 = manager.createEpic(draftTask2);
        final int id3 = manager.createEpic(draftTask3);
        Epic savedTask1 = manager.getEpic(id1).orElseThrow();
        Epic savedTask2 = manager.getEpic(id2).orElseThrow();
        Epic savedTask3 = manager.getEpic(id3).orElseThrow();

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

        Epic draftTask1 = new Epic("abc", "def");
        Epic draftTask2 = new Epic("abc", "def");
        Epic draftTask3 = new Epic("eee", "def");
        final int id1 = manager.createEpic(draftTask1);
        final int id2 = manager.createEpic(draftTask2);
        final int id3 = manager.createEpic(draftTask3);
        Epic savedTask1 = manager.getEpic(id1).orElseThrow();
        Epic savedTask2 = manager.getEpic(id2).orElseThrow();
        Epic savedTask3 = manager.getEpic(id3).orElseThrow();

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