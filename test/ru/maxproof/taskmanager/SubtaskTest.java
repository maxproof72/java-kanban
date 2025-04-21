package ru.maxproof.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testEquals() {

        final int epicId = manager.createEpic(new TaskBuilder().setName("epic").setDescription("description").buildEpic());
        Subtask draftTask1 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("abc")
                .setDescription("def")
                .buildSubtask();
        Subtask draftTask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("abc")
                .setDescription("def")
                .buildSubtask();
        Subtask draftTask3 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("sub3")
                .setDescription("desc3")
                .buildSubtask();
        final int id1 = manager.createSubtask(draftTask1);
        final int id2 = manager.createSubtask(draftTask2);
        final int id3 = manager.createSubtask(draftTask3);
        Subtask savedTask1 = manager.getSubtask(id1).orElseThrow();
        Subtask savedTask2 = manager.getSubtask(id2).orElseThrow();
        Subtask savedTask3 = manager.getSubtask(id3).orElseThrow();

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

        final int epicId = manager.createEpic(new TaskBuilder()
                .setName("epic")
                .setDescription("description")
                .buildEpic());
        Subtask draftTask1 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("abc")
                .setDescription("def")
                .buildSubtask();
        Subtask draftTask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("abc")
                .setDescription("def")
                .buildSubtask();
        Subtask draftTask3 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("sub3")
                .setDescription("desc3")
                .buildSubtask();
        final int id1 = manager.createSubtask(draftTask1);
        final int id2 = manager.createSubtask(draftTask2);
        final int id3 = manager.createSubtask(draftTask3);
        Subtask savedTask1 = manager.getSubtask(id1).orElseThrow();
        Subtask savedTask2 = manager.getSubtask(id2).orElseThrow();
        Subtask savedTask3 = manager.getSubtask(id3).orElseThrow();

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