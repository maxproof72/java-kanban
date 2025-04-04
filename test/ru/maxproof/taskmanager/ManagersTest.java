package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault() {

        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
        assertNotEquals(TaskManager.DRAFT_TASK_ID,
                manager.createTask(new TaskBuilder()
                        .setName("abc")
                        .setDescription("def")
                        .buildTask()));
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    void getDefaultHistory() {

        HistoryManager manager = Managers.getDefaultHistory();
        assertNotNull(manager);
    }
}