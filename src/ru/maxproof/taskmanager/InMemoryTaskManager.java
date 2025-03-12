package ru.maxproof.taskmanager;

import java.util.*;

/**
 * Класс менеджера задач
 */
public class InMemoryTaskManager implements TaskManager {

    private static int TaskId = 0;
    private final Map<Integer, Task> taskRegistry = new HashMap<>();
    private final Map<Integer, Epic> epicRegistry = new HashMap<>();
    private final Map<Integer, Subtask> subtaskRegistry = new HashMap<>();
    private final HistoryManager historyManager;


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int createTask(Task draftTask) {

        Task registeredTask = new Task(++TaskId, draftTask);
        taskRegistry.put(registeredTask.getId(), registeredTask);
        return registeredTask.getId();
    }


    @Override
    public int createSubtask(int epicId, Subtask draftSubtask) {

        Epic epic = epicRegistry.get(epicId);
        if (epic == null) {
            return TaskManager.DRAFT_TASK_ID;
        }
        Subtask registeredSubtask = new Subtask(epicId, ++TaskId, draftSubtask);
        subtaskRegistry.put(registeredSubtask.getId(), registeredSubtask);
        epic.registerSubtask(registeredSubtask.getId());
        epic.setStatus(estimateEpicStatus(epic));
        return registeredSubtask.getId();
    }


    @Override
    public int createEpic(Epic draftEpic) {

        Epic registeredEpic = new Epic(++TaskId, draftEpic);
        epicRegistry.put(registeredEpic.getId(), registeredEpic);
        return registeredEpic.getId();
    }


    @Override
    public void updateTask(Task task) {
        if (task.getId() != TaskManager.DRAFT_TASK_ID && taskRegistry.containsKey(task.getId())) {
            taskRegistry.put(task.getId(), task);
            // 2025-03-12 maxproof new
            // Обновление менеджера истории.
            // Иначе, в истории будет храниться неактуальная версия задачи.
            historyManager.add(task);
        }
    }


    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask.getId() != TaskManager.DRAFT_TASK_ID && subtaskRegistry.containsKey(subtask.getId())) {
            subtaskRegistry.put(subtask.getId(), subtask);
            // 2025-03-12 maxproof new
            // Обновление менеджера истории.
            // Иначе, в истории будет храниться неактуальная версия задачи.
            historyManager.add(subtask);

            // Обновление статуса родительского epic
            Epic epic = epicRegistry.get(subtask.getEpicId());
            epic.setStatus(estimateEpicStatus(epic));
        }
    }


    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() != TaskManager.DRAFT_TASK_ID && epicRegistry.containsKey(epic.getId())) {
            epicRegistry.put(epic.getId(), epic);
            // 2025-03-12 maxproof new
            // Обновление менеджера истории.
            // Иначе, в истории будет храниться неактуальная версия задачи.
            historyManager.add(epic);
        }
    }


    @Override
    public List<Task> getTasks() {
        return List.copyOf(taskRegistry.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return List.copyOf(subtaskRegistry.values());
    }

    @Override
    public List<Epic> getEpics() {
        return List.copyOf(epicRegistry.values());
    }

    @Override
    public List<Task> getTopTaskList() {
        var list = new ArrayList<Task>();
        list.addAll(taskRegistry.values());
        list.addAll(epicRegistry.values());
        return list;
    }


    @Override
    public List<Task> getEntireTaskList() {
        var list = getTopTaskList();
        list.addAll(subtaskRegistry.values());
        return list;
    }


    @Override
    public void clearTasks() {
        taskRegistry.clear();
    }


    @Override
    public void clearSubtasks() {
        epicRegistry.values().forEach(Epic::clearSubtasks);
        subtaskRegistry.clear();
    }


    @Override
    public void clearEpics() {
        clearSubtasks();
        epicRegistry.clear();
    }


    @Override
    public Optional<Task> getTask(int id) {
        Task task = taskRegistry.get(id);
        if (task != null) {
            // 2025-03-12 maxproof fix
            // Вследствие того, что в истории будет храниться только одна последняя
            // версия задачи, нет смысла делать копию её состояния
//            historyManager.add(new Task(task));
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }


    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtaskRegistry.get(id);
        if (subtask != null) {
            // 2025-03-12 maxproof fix
            // Вследствие того, что в истории будет храниться только одна последняя
            // версия задачи, нет смысла делать копию её состояния
            // historyManager.add(new Subtask(subtask));
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }


    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epicRegistry.get(id);
        if (epic != null) {
            // 2025-03-12 maxproof fix
            // Вследствие того, что в истории будет храниться только одна последняя
            // версия задачи, нет смысла делать копию её состояния
            // historyManager.add(new Epic(epic));
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
    }


    @Override
    public void removeTask(int id) {
        taskRegistry.remove(id);
        historyManager.remove(id);
    }


    @Override
    public void removeSubtask(int id) {
        subtaskRegistry.remove(id);
        historyManager.remove(id);
        epicRegistry.values().forEach(epic -> {
            if (epic.unregisterSubtask(id))
                epic.setStatus(estimateEpicStatus(epic));
        });
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epicRegistry.get(id);
        if (epic != null) {
            // 2025-03-12 maxproof new
            // Согласно разъяснению ТЗ, при удалении эпика,
            // удаляются все связанные с ним подзадачи
            epic.getSubtasks().forEach(subtask -> {
                subtaskRegistry.remove(subtask);
                historyManager.remove(subtask);
            });
            epicRegistry.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtasks().stream().map(subtaskRegistry::get).toList();
    }

    /**
     * Процедура обновления статуса Epic задания на основе текущих статусов дочерних заданий
     */
    private TaskStatus estimateEpicStatus(Epic epic) {

        var subtasks = getEpicSubtasks(epic);
        return subtasks.stream().allMatch(Task::isNew) ? TaskStatus.NEW :
                subtasks.stream().allMatch(Task::isDone) ? TaskStatus.DONE :
                        TaskStatus.IN_PROGRESS;
    }

    @Override
    public boolean isEmpty() {
        return taskRegistry.isEmpty() && epicRegistry.isEmpty() && subtaskRegistry.isEmpty();
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
