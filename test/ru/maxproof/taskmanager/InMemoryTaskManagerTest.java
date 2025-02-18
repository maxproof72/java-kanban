package ru.maxproof.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class InMemoryTaskManagerTest {

    InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void createTask() {

        Task draftTask = new Task("abc", "def");
        final int taskId = manager.createTask(draftTask);
        assertTrue(taskId != TaskManager.DRAFT_TASK_ID, "Недопустимый ID задачи");

        Task task = manager.getTask(taskId).orElse(null);
        assertNotNull(task, "Задача не найдена");
        assertEquals(TaskStatus.NEW, task.getStatus(), "Неправильный статус задачи");
        assertEquals(draftTask, task, "Задачи не совпадают");

        final List<Task> tasks = manager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают");
    }

    @Test
    void createSubtask() {

        Epic draftEpic = new Epic("abc", "def");
        Subtask draftSubtask = new Subtask("sub", "bus");
        final int epicId = manager.createEpic(draftEpic);
        final int subtaskId = manager.createSubtask(epicId, draftSubtask);
        assertTrue(subtaskId != TaskManager.DRAFT_TASK_ID, "Недопустимый ID задачи");
        Subtask subtask = manager.getSubtask(subtaskId).orElse(null);

        // Проверка поиска подзадачи
        assertNotNull(subtask, "Подзадача не найдена");
        assertEquals(TaskStatus.NEW, subtask.getStatus(), "Неправильный статус задачи");
        assertEquals(draftSubtask, subtask, "Подзадачи не совпадают");

        // Проверка перечня подзадач менеджера
        final List<Subtask> subtasks = manager.getSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают");

        // Проверка перечня подзадач эпика
        Epic epic = manager.getEpic(epicId).orElseThrow();
        List<Integer> epicSubtasks = epic.getSubtasks();
        assertNotNull(epicSubtasks, "Подзадачи не возвращаются");
        assertEquals(1, epicSubtasks.size(), "Неверное количество подзадач");
        assertEquals(subtaskId, epicSubtasks.getFirst(), "Подзадачи не совпадают");

        // Проверка, что нельзя добавить подзадачу к Task и Subtask
        Subtask wrongSubtask = new Subtask("sub", "task");
        final int taskId = manager.createTask(new Task("bad", "food"));
        int wrongId = manager.createSubtask(taskId,wrongSubtask);
        assertEquals(wrongId, TaskManager.DRAFT_TASK_ID);
        wrongId = manager.createSubtask(epicSubtasks.getFirst(), wrongSubtask);
        assertEquals(wrongId, TaskManager.DRAFT_TASK_ID);
    }

    @Test
    void createEpic() {

        Epic draftEpic = new Epic("abc", "def");
        final int epicId = manager.createEpic(draftEpic);
        assertTrue(epicId != TaskManager.DRAFT_TASK_ID, "Недопустимый ID эпика");
        Epic epic = manager.getEpic(epicId).orElse(null);

        // Проверка поиска эпика
        assertNotNull(epic, "Эпик не найден");
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Неправильный статус эпика");
        assertEquals(draftEpic, epic, "Эпики не совпадают");

        // Проверка перечня эпиков менеджера
        final List<Epic> epics = manager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(1, epics.size(), "Неверное количество эпиков");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают");
    }

    @Test
    void updateTask() {

        Task draftTask = new Task("abc", "def");
        final int taskId = manager.createTask(draftTask);
        Task task = manager.getTask(taskId).orElseThrow();

        Task taskWithChangedName = new Task(task, "changed", task.getDescription());
        manager.updateTask(taskWithChangedName);
        Task updatedTask = manager.getTask(taskId).orElseThrow();
        assertEquals(taskWithChangedName, updatedTask, "Измененные задачи не совпадают");

        Task taskWithChangedStatus = new Task(updatedTask, TaskStatus.IN_PROGRESS);
        manager.updateTask(taskWithChangedStatus);
        updatedTask = manager.getTask(taskId).orElseThrow();
        assertEquals(taskWithChangedStatus, updatedTask, "Задачи с новым статусом не совпадают");
    }

    @Test
    void updateSubtask() {

        Epic draftEpic = new Epic("abc", "def");
        Subtask draftSubtask1 = new Subtask("sub1", "");
        Subtask draftSubtask2 = new Subtask("sub2", "");
        final int epicId = manager.createEpic(draftEpic);
        final int subtaskId1 = manager.createSubtask(epicId, draftSubtask1);
        final int subtaskId2 = manager.createSubtask(epicId, draftSubtask2);
        Subtask subtask1 = manager.getSubtask(subtaskId1).orElseThrow();
        Subtask subtask2 = manager.getSubtask(subtaskId2).orElseThrow();
        Epic epic = manager.getEpic(epicId).orElseThrow();
        assertEquals(TaskStatus.NEW, subtask1.getStatus());
        assertEquals(TaskStatus.NEW, subtask2.getStatus());
        assertEquals(TaskStatus.NEW, epic.getStatus());

        // Изменение наименования подзадачи
        Subtask subtaskWithChangedName = new Subtask(subtask1, "changed", subtask1.getDescription());
        manager.updateSubtask(subtaskWithChangedName);
        subtask1 = manager.getSubtask(subtaskId1).orElseThrow();
        assertEquals(subtaskWithChangedName, subtask1, "Подзадачи не совпадают");

        // Изменение статуса подзадачи 1 (IN_PROGRESS & NEW -> Epic::IN_PROGRESS)
        Subtask inProgressSubtask1 = new Subtask(subtask1, TaskStatus.IN_PROGRESS);
        manager.updateSubtask(inProgressSubtask1);
        subtask1 = manager.getSubtask(subtaskId1).orElseThrow();
        assertEquals(inProgressSubtask1, subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).orElseThrow().getStatus());

        // Изменение статуса подзадачи 2 (IN_PROGRESS & DONE -> Epic::IN_PROGRESS)
        Subtask doneSubtask2 = new Subtask(subtask2, TaskStatus.DONE);
        manager.updateSubtask(doneSubtask2);
        subtask2 = manager.getSubtask(subtaskId2).orElseThrow();
        assertEquals(subtask2, doneSubtask2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).orElseThrow().getStatus());

        // Изменение статуса подзадачи 1 (DONE & DONE -> Epic::DONE)
        Subtask doneSubtask1 = new Subtask(subtask1, TaskStatus.DONE);
        manager.updateSubtask(doneSubtask1);
        subtask1 = manager.getSubtask(subtaskId1).orElseThrow();
        assertEquals(subtask1, doneSubtask1);
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).orElseThrow().getStatus());
    }

    @Test
    void updateEpic() {

        Epic draftEpic = new Epic("abc", "def");
        final int epicId = manager.createEpic(draftEpic);
        Epic epic = manager.getEpic(epicId).orElseThrow();

        Epic epicWithChangedName = new Epic(epic, "changed", epic.getDescription());
        manager.updateEpic(epicWithChangedName);
        Epic updatedEpic = manager.getEpic(epicId).orElseThrow();
        assertEquals(epicWithChangedName, updatedEpic, "Измененные эпики не совпадают");
    }

    @Test
    void getTasks() {

        Task draftTask1 = new Task("task1", "desc1");
        Task draftTask2 = new Task("task2", "desc2");
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        List<Task> tasks = manager.getTasks();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertEquals(draftTask1, tasks.getFirst());
        assertEquals(draftTask2, tasks.getLast());
    }

    @Test
    void getSubtasks() {

        int epicId = manager.createEpic(new Epic("abc", "def"));
        Subtask draftSubtask1 = new Subtask("subtask1", "desc1");
        Subtask draftSubtask2 = new Subtask("subtask2", "desc2");
        manager.createSubtask(epicId, draftSubtask1);
        manager.createSubtask(epicId, draftSubtask2);
        List<Subtask> subtasks = manager.getSubtasks();
        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
        assertEquals(draftSubtask1, subtasks.getFirst());
        assertEquals(draftSubtask2, subtasks.getLast());
    }

    @Test
    void getEpics() {

        Epic draftEpic1 = new Epic("epic1", "desc1");
        Epic draftEpic2 = new Epic("epic2", "desc2");
        manager.createEpic(draftEpic1);
        manager.createEpic(draftEpic2);
        List<Epic> epics = manager.getEpics();
        assertNotNull(epics);
        assertEquals(2, epics.size());
        assertEquals(draftEpic1, epics.getFirst());
        assertEquals(draftEpic2, epics.getLast());
    }

    @Test
    void getTopTaskList() {

        Task draftTask1 = new Task("task1", "desc1");
        Task draftTask2 = new Task("task2", "desc2");
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        Epic draftEpic1 = new Epic("epic1", "desc1");
        Epic draftEpic2 = new Epic("epic2", "desc2");
        manager.createEpic(draftEpic1);
        manager.createEpic(draftEpic2);

        List<Task> topList = manager.getTopTaskList();
        assertNotNull(topList);
        assertEquals(4, topList.size());
        assertEquals(4, topList.stream().distinct().count(), "В списке повторяются задачи");
        assertTrue(topList.contains(draftTask1));
        assertTrue(topList.contains(draftTask2));
        assertTrue(topList.contains(draftEpic1));
        assertTrue(topList.contains(draftEpic2));
    }

    @Test
    void getEntireTaskList() {

        Task draftTask1 = new Task("task1", "desc1");
        Task draftTask2 = new Task("task2", "desc2");
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        Epic draftEpic1 = new Epic("epic1", "desc1");
        Epic draftEpic2 = new Epic("epic2", "desc2");
        final int epicId1 = manager.createEpic(draftEpic1);
        final int epicId2 = manager.createEpic(draftEpic2);
        Subtask draftSubtask1 = new Subtask("sub1", "");
        Subtask draftSubtask2 = new Subtask("sub2", "");
        manager.createSubtask(epicId1, draftSubtask1);
        manager.createSubtask(epicId2, draftSubtask2);

        List<Task> entireList = manager.getEntireTaskList();
        assertNotNull(entireList);
        assertEquals(6, entireList.size());
        assertEquals(6, entireList.stream().distinct().count(), "В списке повторяются задачи");
        assertTrue(entireList.contains(draftTask1));
        assertTrue(entireList.contains(draftTask2));
        assertTrue(entireList.contains(draftEpic1));
        assertTrue(entireList.contains(draftEpic2));
        assertTrue(entireList.contains(draftSubtask1));
        assertTrue(entireList.contains(draftSubtask2));
    }

    @Test
    void clearTasks() {

        Task draftTask1 = new Task("task1", "desc1");
        Task draftTask2 = new Task("task2", "desc2");
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        assertEquals(2, manager.getTasks().size());
        manager.clearTasks();
        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.isEmpty());
    }

    @Test
    void clearSubtasks() {

        int epicId = manager.createEpic(new Epic("abc", "def"));
        Subtask draftSubtask1 = new Subtask("subtask1", "desc1");
        Subtask draftSubtask2 = new Subtask("subtask2", "desc2");
        manager.createSubtask(epicId, draftSubtask1);
        manager.createSubtask(epicId, draftSubtask2);
        assertEquals(2, manager.getSubtasks().size());
        manager.clearSubtasks();
        assertTrue(manager.getSubtasks().isEmpty());

        // Проверка обновления статуса Epic
        Epic epic = manager.getEpic(epicId).orElseThrow();
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void clearEpics() {

        Epic draftEpic1 = new Epic("epic1", "desc1");
        Epic draftEpic2 = new Epic("epic2", "desc2");
        manager.createEpic(draftEpic1);
        manager.createEpic(draftEpic2);
        assertEquals(2, manager.getEpics().size());
        manager.clearEpics();
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.isEmpty());
    }

    @Test
    void removeTask() {

        Task draftTask1 = new Task("task1", "desc1");
        Task draftTask2 = new Task("task2", "desc2");
        final int taskId1 = manager.createTask(draftTask1);
        final int taskId2 = manager.createTask(draftTask2);
        assertEquals(2, manager.getTasks().size());
        manager.removeTask(taskId1);
        assertEquals(1, manager.getTasks().size());
        manager.removeTask(taskId1);
        assertEquals(1, manager.getTasks().size());
        manager.removeTask(taskId2);
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void removeSubtask() {

        int epicId = manager.createEpic(new Epic("abc", "def"));
        final int subtaskId1 = manager.createSubtask(epicId, new Subtask("subtask1", "desc1"));
        final int subtaskId2 = manager.createSubtask(epicId, new Subtask("subtask2", "desc2"));
        assertEquals(2, manager.getSubtasks().size());
        assertEquals(2, manager.getEpic(epicId).orElseThrow().getSubtasks().size());

        // Изменяем status подзадач на DONE
        manager.updateSubtask(new Subtask(manager.getSubtask(subtaskId1).orElseThrow(), TaskStatus.DONE));
        manager.updateSubtask(new Subtask(manager.getSubtask(subtaskId2).orElseThrow(), TaskStatus.DONE));
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).orElseThrow().getStatus());

        // Удаляем подзадачу 1
        manager.removeSubtask(subtaskId1);
        assertEquals(1, manager.getSubtasks().size());
        assertEquals(1, manager.getEpic(epicId).orElseThrow().getSubtasks().size());
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).orElseThrow().getStatus());

        // Удаляем подзадачу 2
        manager.removeSubtask(subtaskId2);
        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(manager.getEpic(epicId).orElseThrow().getSubtasks().isEmpty());
        assertEquals(TaskStatus.NEW, manager.getEpic(epicId).orElseThrow().getStatus());
    }

    @Test
    void removeEpic() {

        Epic draftEpic1 = new Epic("epic1", "desc1");
        Epic draftEpic2 = new Epic("epic2", "desc2");
        final int epicId1 = manager.createEpic(draftEpic1);
        final int epicId2 = manager.createEpic(draftEpic2);
        assertEquals(2, manager.getEpics().size());
        manager.removeEpic(epicId1);
        assertEquals(1, manager.getEpics().size());
        manager.removeEpic(epicId2);
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void getHistory() {

        final int taskId = manager.createTask(new Task("abc", "def"));
        Task task = manager.getTask(taskId).orElseThrow();
        int epicId = manager.createEpic(new Epic("EPIC", "def"));
        Subtask draftSubtask1 = new Subtask("subtask1", "desc1");
        Subtask draftSubtask2 = new Subtask("subtask2", "desc2");
        final int subtaskId1 = manager.createSubtask(epicId, draftSubtask1);
        final int subtaskId2 = manager.createSubtask(epicId, draftSubtask2);
        Epic epic = manager.getEpic(epicId).orElseThrow();
        Subtask subtask1 = manager.getSubtask(subtaskId1).orElseThrow();
        Subtask subtask2 = manager.getSubtask(subtaskId2).orElseThrow();

        // Проверка числа записей
        List<Task> history = manager.getHistory();
        assertEquals(4, history.size());
        assertEquals(List.of(task, epic, subtask1, subtask2), history);

        // Проверка переполнения
        manager.getEpic(epicId).orElseThrow();
        manager.getEpic(epicId).orElseThrow();
        manager.getEpic(epicId).orElseThrow();
        manager.getEpic(epicId).orElseThrow();
        manager.getEpic(epicId).orElseThrow();
        manager.getEpic(epicId).orElseThrow();
        manager.getEpic(epicId).orElseThrow();
        history = manager.getHistory();
        assertEquals(10, history.size());
        assertEquals(List.of(
                epic, subtask1, subtask2,
                epic, epic, epic, epic, epic, epic, epic), history);

        // Проверка сохраненного состояния
        manager.updateEpic(new Epic(epic, "EPIC2", "def"));
        Epic updatedEpic = manager.getEpic(epicId).orElseThrow();
        assertEquals("EPIC2", updatedEpic.getName());
        history = manager.getHistory();
        assertEquals("EPIC2", history.getLast().getName());
        assertEquals("EPIC", history.get(history.size() - 2).getName());
    }
}