package ru.maxproof.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс менеджера задач
 */
public class InMemoryTaskManager implements TaskManager {

    protected int taskId = 0;
    protected final Map<Integer, Task> taskRegistry = new HashMap<>();
    protected final Map<Integer, Epic> epicRegistry = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskRegistry = new HashMap<>();
    private final HistoryManager historyManager;


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int createTask(Task draftTask) {

        Task registeredTask = new TaskBuilder(draftTask).setId(++taskId).buildTask();
        taskRegistry.put(registeredTask.getId(), registeredTask);
        return registeredTask.getId();
    }


    @Override
    public int createSubtask(int epicId, Subtask draftSubtask) {

        Epic epic = epicRegistry.get(epicId);
        if (epic == null) {
            return TaskManager.DRAFT_TASK_ID;
        }
        Subtask registeredSubtask = new TaskBuilder(draftSubtask).setId(++taskId).buildSubtask(epicId);
        subtaskRegistry.put(registeredSubtask.getId(), registeredSubtask);
        epic.registerSubtask(registeredSubtask.getId());
        updateEpicStatus(epic);
        return registeredSubtask.getId();
    }


    @Override
    public int createEpic(Epic draftEpic) {

        Epic registeredEpic = new TaskBuilder(draftEpic).setId(++taskId).buildEpic();
        epicRegistry.put(registeredEpic.getId(), registeredEpic);
        return registeredEpic.getId();
    }


    @Override
    public void updateTask(Task task) {

        if (task.getId() != TaskManager.DRAFT_TASK_ID && taskRegistry.containsKey(task.getId())) {
            taskRegistry.put(task.getId(), task);
            historyManager.add(task);
        }
    }


    @Override
    public void updateSubtask(Subtask subtask) {

        if (subtask.getId() != TaskManager.DRAFT_TASK_ID && subtaskRegistry.containsKey(subtask.getId())) {
            subtaskRegistry.put(subtask.getId(), subtask);
            historyManager.add(subtask);

            // Обновление статуса родительского epic
            Epic epic = epicRegistry.get(subtask.getEpicId());
            updateEpicStatus(epic);
        }
    }


    @Override
    public void updateEpic(Epic epic) {

        if (epic.getId() != TaskManager.DRAFT_TASK_ID && epicRegistry.containsKey(epic.getId())) {
            epicRegistry.put(epic.getId(), epic);
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
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }


    @Override
    public Optional<Subtask> getSubtask(int id) {

        Subtask subtask = subtaskRegistry.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }


    @Override
    public Optional<Epic> getEpic(int id) {

        Epic epic = epicRegistry.get(id);
        if (epic != null) {
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

        Subtask subtask = subtaskRegistry.get(id);
        subtaskRegistry.remove(id);
        historyManager.remove(id);
        Epic epic = epicRegistry.get(subtask.getEpicId());
        epic.unregisterSubtask (id);
        updateEpicStatus(epic);
    }

    @Override
    public void removeEpic(int id) {

        Epic epic = epicRegistry.get(id);
        if (epic != null) {
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
    private void updateEpicStatus(Epic epic) {
        var subtasks = getEpicSubtasks(epic);

        // Обновление статуса выполнения эпика
        var status = subtasks.stream().allMatch(Task::isNew) ? TaskStatus.NEW :
                subtasks.stream().allMatch(Task::isDone) ? TaskStatus.DONE :
                        TaskStatus.IN_PROGRESS;
        epic.setStatus(status);

        // Обновление времени начала и завершения эпика
        Comparator<LocalDateTime> timeComparator = (t1, t2) -> t1.isBefore(t2)? -1: t2.isBefore(t1)? +1: 0;
        epic.startTime = subtasks.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(timeComparator)
                .orElse(null);
        epic.duration = subtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        epic.endTime = subtasks.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(timeComparator)
                .orElse(null);
    }

    @Override
    public boolean isEmpty() {
        return taskRegistry.isEmpty() && epicRegistry.isEmpty() && subtaskRegistry.isEmpty();
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryTaskManager that = (InMemoryTaskManager) o;
        return Objects.equals(taskRegistry, that.taskRegistry) &&
               Objects.equals(epicRegistry, that.epicRegistry) &&
               Objects.equals(subtaskRegistry, that.subtaskRegistry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskRegistry, epicRegistry, subtaskRegistry);
    }
}
