package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    T manager;

    @Test
    void createTask() {

        LocalDateTime now = LocalDateTime.now();
        Task draftTask = new TaskBuilder()
                .setName("abc")
                .setDescription("def")
                .setStartTime(now)
                .setDuration(Duration.ofHours(1))
                .buildTask();
        final int t1 = manager.createTask(draftTask).getId();
        assertTrue(t1 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID задачи");

        Task task = manager.getTask(t1);
        assertNotNull(task, "Задача не найдена");
        assertEquals(TaskStatus.NEW, task.getStatus(), "Неправильный статус задачи");
        assertEquals(draftTask, task, "Задачи не совпадают");

        final List<Task> tasks = manager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают");

        // Проверка задач с одинаковым временем старта (граничное условие)
        Task draftTask2 = new TaskBuilder()
                .setName("T2")
                .setStartTime(now)
                .setDuration(Duration.ofHours(2))
                .buildTask();
        assertThrows(TaskManager.NotAcceptableTaskException.class, () -> manager.createTask(draftTask2));

        // Проверка задач с пересекающимся временем выполнения
        Task draftTask3 = new TaskBuilder(draftTask2)
                .setName("T3")
                .setStartTime(LocalDateTime.now().plusMinutes(30))
                .buildTask();
        assertThrows(TaskManager.NotAcceptableTaskException.class, () -> manager.createTask(draftTask3));

        // Проверка задач с примыкающим временем выполнения
        Task draftTask4 = new TaskBuilder(draftTask2)
                .setName("T4")
                .setStartTime(now.plusHours(1))
                .buildTask();
        int t4 = manager.createTask(draftTask4).getId();
        assertTrue(t4 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");

        // Проверка задач с непересекающимся временем выполнения
        Task draftTask5 = new TaskBuilder(draftTask2)
                .setName("T5")
                .setStartTime(LocalDateTime.now().minusDays(2))
                .buildTask();
        int t5 = manager.createTask(draftTask5).getId();
        assertTrue(t5 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");

        // Проверка задачи без указания времени выполнения
        Task draftTask6 = new TaskBuilder().setName("T6").buildTask();
        int t6 = manager.createTask(draftTask6).getId();
        assertTrue(t6 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");

        // Проверка перечня задач на временной шкале
        assertEquals(List.of(t5, t1, t4),
                manager.getPrioritizedTasks().stream().map(Task::getId).toList());
    }

    @Test
    void createSubtask() {

        LocalDateTime now = LocalDateTime.now();
        Epic draftEpic = new TaskBuilder()
                .setName("abc")
                .setDescription("def")
                .buildEpic();
        final int epicId = manager.createEpic(draftEpic).getId();
        Subtask draftSubtask = new TaskBuilder()
                .setEpicId(epicId)
                .setName("sub")
                .setDescription("bus")
                .setStartTime(now)
                .setDuration(Duration.ofHours(1))
                .buildSubtask();
        final int sub1 = manager.createSubtask(draftSubtask).getId();
        assertTrue(sub1 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID задачи");
        Subtask subtask = manager.getSubtask(sub1);

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
        Epic epic = manager.getEpic(epicId);
        List<Integer> epicSubtasks = epic.getSubtasks();
        assertNotNull(epicSubtasks, "Подзадачи не возвращаются");
        assertEquals(1, epicSubtasks.size(), "Неверное количество подзадач");
        assertEquals(sub1, epicSubtasks.getFirst(), "Подзадачи не совпадают");

        // Проверка, что нельзя добавить подзадачу к Task и Subtask
        final int taskId = manager.createTask(new TaskBuilder()
                .setName("bad").setDescription("food").buildTask()).getId();
        final Subtask wrongSubtask1 = new TaskBuilder()
                .setEpicId(taskId)                  // <-- wrong id
                .setName("sub")
                .setDescription("task")
                .buildSubtask();
        assertThrows(TaskManager.InvalidTaskIdException.class,
                () -> manager.createSubtask(wrongSubtask1));
        final Subtask wrongSubtask2 = new TaskBuilder(wrongSubtask1)
                .setEpicId(epicSubtasks.getFirst())     // <-- wrong id
                .buildSubtask();
        assertThrows(TaskManager.InvalidTaskIdException.class,
                () -> manager.createSubtask(wrongSubtask2));

        // Проверка задач с одинаковым временем старта (граничное условие)
        Subtask draftSubtask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("T2")
                .setStartTime(now)
                .setDuration(Duration.ofHours(2))
                .buildSubtask();
        assertThrows(TaskManager.NotAcceptableTaskException.class, () -> manager.createSubtask(draftSubtask2));

        // Проверка задач с пересекающимся временем выполнения
        Subtask draftSubtask3 = new TaskBuilder(draftSubtask2)
                .setEpicId(epicId)
                .setName("T3")
                .setStartTime(LocalDateTime.now().plusMinutes(30))
                .buildSubtask();
        assertThrows(TaskManager.NotAcceptableTaskException.class, () -> manager.createSubtask(draftSubtask3));

        // Проверка задач с примыкающим временем выполнения
        Subtask draftSubtask4 = new TaskBuilder(draftSubtask2)
                .setEpicId(epicId)
                .setName("T4")
                .setStartTime(now.plusHours(1))
                .buildSubtask();
        int sub4 = manager.createSubtask(draftSubtask4).getId();
        assertTrue(sub4 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");

        // Проверка задач с непересекающимся временем выполнения
        Subtask draftSubtask5 = new TaskBuilder(draftSubtask2)
                .setEpicId(epicId)
                .setName("T5")
                .setStartTime(LocalDateTime.now().minusDays(2))
                .buildSubtask();
        int sub5 = manager.createSubtask(draftSubtask5).getId();
        assertTrue(sub5 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");

        // Проверка задачи без указания времени выполнения
        Subtask draftSubtask6 = new TaskBuilder().setEpicId(epicId).setName("T6").buildSubtask();
        int sub6 = manager.createSubtask(draftSubtask6).getId();
        assertTrue(sub6 != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");

        // Проверка перечня задач на временной шкале
        assertEquals(List.of(sub5, sub1, sub4),
                manager.getPrioritizedTasks().stream().map(Task::getId).toList());

        // Проверка времени выполнения эпика
        LocalDateTime epicStart = manager.getSubtask(sub5).getStartTime();
        LocalDateTime epicEnd = manager.getSubtask(sub4).getEndTime();
        Duration epicDuration = manager.getPrioritizedTasks().stream()
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        epic = manager.getEpic(epicId);
        assertEquals(epicStart, epic.getStartTime());
        assertEquals(epicEnd, epic.getEndTime());
        assertEquals(epicDuration, epic.getDuration());
    }

    @Test
    void createEpic() {

        Epic draftEpic = new TaskBuilder().setName("abc").setDescription("def").buildEpic();
        final int epicId = manager.createEpic(draftEpic).getId();
        assertTrue(epicId != TaskManager.DRAFT_TASK_ID, "Недопустимый ID эпика");
        Epic epic = manager.getEpic(epicId);

        // Проверка поиска эпика
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

        Task draftTask = new TaskBuilder().setName("abc").setDescription("def").buildTask();
        final int taskId = manager.createTask(draftTask).getId();
        Task task = manager.getTask(taskId);

        Task taskWithChangedName = new TaskBuilder(task).setName("changed").buildTask();
        manager.updateTask(taskWithChangedName);
        Task updatedTask = manager.getTask(taskId);
        assertEquals(taskWithChangedName, updatedTask, "Измененные задачи не совпадают");

        Task taskWithChangedStatus = new TaskBuilder(updatedTask).setStatus(TaskStatus.IN_PROGRESS).buildTask();
        manager.updateTask(taskWithChangedStatus);
        updatedTask = manager.getTask(taskId);
        assertEquals(taskWithChangedStatus, updatedTask, "Задачи с новым статусом не совпадают");

        // Проверка на пересечение во времени
        LocalDateTime now = LocalDateTime.now();
        int otherId = manager.createTask(new TaskBuilder()
                .setName("Other").setStartTime(now).setDuration(Duration.ofHours(1))
                .buildTask()).getId();
        assertTrue(otherId != TaskManager.DRAFT_TASK_ID, "Недопустимый ID");
        assertThrows(InMemoryTaskManager.NotAcceptableTaskException.class,
                () -> manager.updateTask(
                        new TaskBuilder(manager.getTask(taskId))
                                .setStartTime(now)
                                .setDuration(Duration.ofHours(1))
                                .buildTask()), "Недопустимы задачи с одновременным началом");
        assertThrows(InMemoryTaskManager.NotAcceptableTaskException.class,
                () -> manager.updateTask(
                        new TaskBuilder(manager.getTask(taskId))
                                .setStartTime(LocalDateTime.now().plusMinutes(30))
                                .setDuration(Duration.ofHours(1))
                                .buildTask()), "Недопустимы задачи с пересечением во времени");

        // Проверка изменения перечня задач во временной шкале
        manager.updateTask(new TaskBuilder(manager.getTask(taskId))
                .setStartTime(LocalDateTime.now().minusDays(1))
                .setDuration(Duration.ofHours(1))
                .buildTask());
        assertEquals(List.of(taskId, otherId),
                manager.getPrioritizedTasks().stream().map(Task::getId).toList());
        manager.updateTask(new TaskBuilder(manager.getTask(taskId))
                .setStartTime(LocalDateTime.now().plusDays(2))
                .buildTask());
        assertEquals(List.of(otherId, taskId),
                manager.getPrioritizedTasks().stream().map(Task::getId).toList());
    }

    @Test
    void updateSubtask() {

        Epic draftEpic = new TaskBuilder().setName("abc").setDescription("def").buildEpic();
        final int epicId = manager.createEpic(draftEpic).getId();
        Subtask draftSubtask1 = new TaskBuilder().setEpicId(epicId).setName("sub1").buildSubtask();
        Subtask draftSubtask2 = new TaskBuilder().setEpicId(epicId).setName("sub2").buildSubtask();
        final int subtaskId1 = manager.createSubtask(draftSubtask1).getId();
        final int subtaskId2 = manager.createSubtask(draftSubtask2).getId();
        Subtask subtask1 = manager.getSubtask(subtaskId1);
        Subtask subtask2 = manager.getSubtask(subtaskId2);
        Epic epic = manager.getEpic(epicId);
        assertEquals(TaskStatus.NEW, subtask1.getStatus());
        assertEquals(TaskStatus.NEW, subtask2.getStatus());
        assertEquals(TaskStatus.NEW, epic.getStatus());

        // Изменение наименования подзадачи
        Subtask subtaskWithChangedName = new TaskBuilder(subtask1).setName("changed").buildSubtask();
        manager.updateSubtask(subtaskWithChangedName);
        subtask1 = manager.getSubtask(subtaskId1);
        assertEquals(subtaskWithChangedName, subtask1, "Подзадачи не совпадают");

        // Изменение статуса подзадачи 1 (IN_PROGRESS & NEW -> Epic::IN_PROGRESS)
        Subtask inProgressSubtask1 = new TaskBuilder(subtask1).setStatus(TaskStatus.IN_PROGRESS).buildSubtask();
        manager.updateSubtask(inProgressSubtask1);
        subtask1 = manager.getSubtask(subtaskId1);
        assertEquals(inProgressSubtask1, subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus());

        // Изменение статуса подзадачи 2 (IN_PROGRESS & DONE -> Epic::IN_PROGRESS)
        Subtask doneSubtask2 = new TaskBuilder(subtask2).setStatus(TaskStatus.DONE).buildSubtask();
        manager.updateSubtask(doneSubtask2);
        subtask2 = manager.getSubtask(subtaskId2);
        assertEquals(subtask2, doneSubtask2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus());

        // Изменение статуса подзадачи 1 (DONE & DONE -> Epic::DONE)
        Subtask doneSubtask1 = new TaskBuilder(subtask1).setStatus(TaskStatus.DONE).buildSubtask();
        manager.updateSubtask(doneSubtask1);
        subtask1 = manager.getSubtask(subtaskId1);
        assertEquals(subtask1, doneSubtask1);
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).getStatus());

        // Изменение времени подзадачи должно приводить к корректировке эпика
        LocalDateTime now = LocalDateTime.now();
        manager.updateSubtask(new TaskBuilder(manager.getSubtask(subtaskId1))
                .setStartTime(now)
                .setDuration(Duration.ofHours(2))
                .buildSubtask());
        epic = manager.getEpic(epicId);
        assertEquals(now, epic.getStartTime());
        assertEquals(now.plusHours(2), epic.getEndTime());
        assertEquals(Duration.ofHours(2), epic.getDuration());
        assertThrows(TaskManager.NotAcceptableTaskException.class,
                () -> manager.updateSubtask(new TaskBuilder(manager.getSubtask(subtaskId2))
                        .setStartTime(now.plusHours(1))
                        .setDuration(Duration.ofHours(4))
                        .buildSubtask()));
        manager.updateSubtask(new TaskBuilder(manager.getSubtask(subtaskId2))
                .setStartTime(now.plusDays(5))
                .setDuration(Duration.ofHours(4))
                .buildSubtask());
        epic = manager.getEpic(epicId);
        assertEquals(now, epic.getStartTime());
        assertEquals(now.plusDays(5).plusHours(4), epic.getEndTime());
        assertEquals(Duration.ofHours(6), epic.getDuration());
    }

    @Test
    void updateEpic() {

        Epic draftEpic = new TaskBuilder().setName("abc").setDescription("def").buildEpic();
        final int epicId = manager.createEpic(draftEpic).getId();
        Epic epic = manager.getEpic(epicId);

        Epic epicWithChangedName = new TaskBuilder(epic).setName("changed").buildEpic();
        manager.updateEpic(epicWithChangedName);
        Epic updatedEpic = manager.getEpic(epicId);
        assertEquals(epicWithChangedName, updatedEpic, "Измененные эпики не совпадают");
    }

    @Test
    void getTasks() {

        Task draftTask1 = new TaskBuilder().setName("task1").setDescription("desc1").buildTask();
        Task draftTask2 = new TaskBuilder().setName("task2").setDescription("desc2").buildTask();
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

        int epicId = manager.createEpic(new TaskBuilder()
                .setName("abc")
                .setDescription("def")
                .buildEpic()).getId();
        Subtask draftSubtask1 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("subtask1")
                .setDescription("desc1")
                .buildSubtask();
        Subtask draftSubtask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("subtask2")
                .setDescription("desc2")
                .buildSubtask();
        manager.createSubtask(draftSubtask1);
        manager.createSubtask(draftSubtask2);
        List<Subtask> subtasks = manager.getSubtasks();
        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
        assertEquals(draftSubtask1, subtasks.getFirst());
        assertEquals(draftSubtask2, subtasks.getLast());
    }

    @Test
    void getEpics() {

        Epic draftEpic1 = new TaskBuilder().setName("epic1").setDescription("desc1").buildEpic();
        Epic draftEpic2 = new TaskBuilder().setName("epic2").setDescription("desc2").buildEpic();
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

        Task draftTask1 = new TaskBuilder().setName("task1").setDescription("desc1").buildTask();
        Task draftTask2 = new TaskBuilder().setName("task2").setDescription("desc2").buildTask();
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        Epic draftEpic1 = new TaskBuilder().setName("epic1").setDescription("desc1").buildEpic();
        Epic draftEpic2 = new TaskBuilder().setName("epic2").setDescription("desc2").buildEpic();
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

        Task draftTask1 = new TaskBuilder().setName("task1").setDescription("desc1").buildTask();
        Task draftTask2 = new TaskBuilder().setName("task2").setDescription("desc2").buildTask();
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        Epic draftEpic1 = new TaskBuilder().setName("epic1").setDescription("desc1").buildEpic();
        Epic draftEpic2 = new TaskBuilder().setName("epic2").setDescription("desc2").buildEpic();
        final int epicId1 = manager.createEpic(draftEpic1).getId();
        final int epicId2 = manager.createEpic(draftEpic2).getId();
        Subtask draftSubtask1 = new TaskBuilder().setEpicId(epicId1).setName("sub1").buildSubtask();
        Subtask draftSubtask2 = new TaskBuilder().setEpicId(epicId2).setName("sub2").buildSubtask();
        manager.createSubtask(draftSubtask1);
        manager.createSubtask(draftSubtask2);

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

        Task draftTask1 = new TaskBuilder().setName("task1").setDescription("desc1").buildTask();
        Task draftTask2 = new TaskBuilder().setName("task2").setDescription("desc2").buildTask();
        manager.createTask(draftTask1);
        manager.createTask(draftTask2);
        assertEquals(2, manager.getTasks().size());
        manager.clearTasks();
        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.isEmpty());
    }

    @Test
    void clearSubtasks() {

        int epicId = manager.createEpic(new TaskBuilder().setName("abc").setDescription("def").buildEpic()).getId();
        Subtask draftSubtask1 = new TaskBuilder().setEpicId(epicId).setName("subtask1").setDescription("desc1").buildSubtask();
        Subtask draftSubtask2 = new TaskBuilder().setEpicId(epicId).setName("subtask2").setDescription("desc2").buildSubtask();
        manager.createSubtask(draftSubtask1);
        manager.createSubtask(draftSubtask2);
        assertEquals(2, manager.getSubtasks().size());
        manager.clearSubtasks();
        assertTrue(manager.getSubtasks().isEmpty());

        // Проверка обновления статуса Epic
        Epic epic = manager.getEpic(epicId);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void clearEpics() {

        Epic draftEpic1 = new TaskBuilder().setName("epic1").setDescription("desc1").buildEpic();
        Epic draftEpic2 = new TaskBuilder().setName("epic2").setDescription("desc2").buildEpic();
        manager.createEpic(draftEpic1);
        manager.createEpic(draftEpic2);
        assertEquals(2, manager.getEpics().size());
        manager.clearEpics();
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.isEmpty());
    }

    @Test
    void removeTask() {

        Task draftTask1 = new TaskBuilder().setName("task1").setDescription("desc1").buildTask();
        Task draftTask2 = new TaskBuilder().setName("task2").setDescription("desc2").buildTask();
        final int taskId1 = manager.createTask(draftTask1).getId();
        final int taskId2 = manager.createTask(draftTask2).getId();
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

        int epicId = manager.createEpic(new TaskBuilder()
                .setName("abc")
                .setDescription("def")
                .buildEpic()).getId();
        Subtask subtask1 = manager.createSubtask(new TaskBuilder()
                .setEpicId(epicId)
                .setName("subtask1")
                .setDescription("desc1")
                .buildSubtask());
        Subtask subtask2 = manager.createSubtask(new TaskBuilder()
                .setEpicId(epicId)
                .setName("subtask2")
                .setDescription("desc2")
                .buildSubtask());
        assertEquals(2, manager.getSubtasks().size());
        assertEquals(2, manager.getEpic(epicId).getSubtasks().size());

        // Изменяем status всех подзадач на DONE
        manager.updateSubtask(new TaskBuilder(subtask1)
                .setStatus(TaskStatus.DONE)
                .buildSubtask());
        manager.updateSubtask(new TaskBuilder(subtask2)
                .setStatus(TaskStatus.DONE)
                .buildSubtask());
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).getStatus());

        // Удаляем подзадачу 1
        manager.removeSubtask(subtask1.getId());
        assertEquals(1, manager.getSubtasks().size());
        assertEquals(1, manager.getEpic(epicId).getSubtasks().size());
        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).getStatus());

        // Удаляем подзадачу 2
        manager.removeSubtask(subtask2.getId());
        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(manager.getEpic(epicId).getSubtasks().isEmpty());
        assertEquals(TaskStatus.NEW, manager.getEpic(epicId).getStatus());
    }

    @Test
    void removeEpic() {

        Epic draftEpic1 = new TaskBuilder().setName("epic1").setDescription("desc1").buildEpic();
        Epic draftEpic2 = new TaskBuilder().setName("epic2").setDescription("desc2").buildEpic();
        Epic epic1 = manager.createEpic(draftEpic1);
        Epic epic2 = manager.createEpic(draftEpic2);
        assertEquals(2, manager.getEpics().size());
        manager.removeEpic(epic1.getId());
        assertEquals(1, manager.getEpics().size());
        manager.removeEpic(epic2.getId());
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void getHistory() {

        final int taskId = manager.createTask(new TaskBuilder()
                .setName("abc").setDescription("def").buildTask()).getId();
        Task task = manager.getTask(taskId);
        int epicId = manager.createEpic(new TaskBuilder()
                .setName("EPIC")
                .setDescription("def")
                .buildEpic()).getId();
        Subtask draftSubtask1 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("subtask1")
                .setDescription("desc1")
                .buildSubtask();
        Subtask draftSubtask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("subtask2")
                .setDescription("desc2")
                .buildSubtask();
        final int subtaskId1 = manager.createSubtask(draftSubtask1).getId();
        final int subtaskId2 = manager.createSubtask(draftSubtask2).getId();
        Epic epic = manager.getEpic(epicId);
        Subtask subtask1 = manager.getSubtask(subtaskId1);
        Subtask subtask2 = manager.getSubtask(subtaskId2);

        // Проверка числа записей
        List<Task> history = manager.getHistory();
        assertEquals(4, history.size());
        assertEquals(List.of(task, epic, subtask1, subtask2), history);

        // Проверка переполнения
        manager.getEpic(epicId);
        history = manager.getHistory();
        assertEquals(4, history.size());
        assertEquals(List.of( task, subtask1, subtask2, epic), history);

        // Проверка актуального состояния
        manager.updateEpic(new TaskBuilder(epic)
                .setName("EPIC2")
                .setDescription("def")
                .buildEpic());
        history = manager.getHistory();
        assertEquals("EPIC2", manager.getEpic(history.getLast().getId()).getName());
    }

}
