package ru.maxproof.taskmanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;


/**
 * Класс менеджера задач
 */
public class InMemoryTaskManager implements TaskManager {

    protected int taskId = 0;
    protected final Map<Integer, Task> taskRegistry = new HashMap<>();
    protected final Map<Integer, Epic> epicRegistry = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskRegistry = new HashMap<>();
    private final HistoryManager historyManager;

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime));


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int createTask(Task draftTask) {

        checkTaskInTime(draftTask);
        Task registeredTask = new TaskBuilder(draftTask).setId(++taskId).buildTask();
        taskRegistry.put(registeredTask.getId(), registeredTask);
        if (registeredTask.isValidTime())
            prioritizedTasks.add(registeredTask);
        return registeredTask.getId();
    }


    @Override
    public int createSubtask(int epicId, Subtask draftSubtask) {

        checkTaskInTime(draftSubtask);
        Epic epic = epicRegistry.get(epicId);
        if (epic == null) {
            return TaskManager.DRAFT_TASK_ID;
        }
        Subtask registeredSubtask = new TaskBuilder(draftSubtask)
                .setId(++taskId)
                .setEpicId(epicId)
                .buildSubtask();
        subtaskRegistry.put(registeredSubtask.getId(), registeredSubtask);
        epicRegistry.put(epicId, updateEpicImplicitly(epic,
                list -> list.add(registeredSubtask.getId())));
        if (registeredSubtask.isValidTime())
            prioritizedTasks.add(registeredSubtask);
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

        checkTaskInTime(task);
        if (task.getId() != TaskManager.DRAFT_TASK_ID && taskRegistry.containsKey(task.getId())) {
            Task oldTask = taskRegistry.get(task.getId());
            taskRegistry.put(task.getId(), task);
            if (oldTask.isValidTime())
                prioritizedTasks.remove(oldTask);
            if (task.isValidTime()) {
                prioritizedTasks.add(task);
            }
        }
    }


    @Override
    public void updateSubtask(Subtask subtask) {

        checkTaskInTime(subtask);
        if (subtask.getId() != TaskManager.DRAFT_TASK_ID && subtaskRegistry.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtaskRegistry.get(subtask.getId());
            subtaskRegistry.put(subtask.getId(), subtask);
            // Обновление соответствующего эпика (если, конечно, подзадача привязана к эпику)
            epicRegistry.computeIfPresent(subtask.getEpicId(),
                    (k, epic) -> updateEpicImplicitly(epic, null));
            if (oldSubtask.isValidTime())
                prioritizedTasks.remove(oldSubtask);
            if (subtask.isValidTime()) {
                prioritizedTasks.add(subtask);
            }
        }
    }


    @Override
    public void updateEpic(Epic epic) {
        epicRegistry.computeIfPresent(epic.getId(), (k, oldEpic) -> epic);
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
        prioritizedTasks.removeIf(task -> task.getClass() == Subtask.class);
    }


    @Override
    public void clearSubtasks() {

        epicRegistry.keySet().forEach(
                k -> epicRegistry.computeIfPresent(k,
                        (k1, oldEpic) -> updateEpicImplicitly(oldEpic, ArrayList::clear)
        ));
        subtaskRegistry.clear();
        prioritizedTasks.removeIf(task -> task.getClass() == Subtask.class);
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
        prioritizedTasks.removeIf(task -> task.getId() == id);
    }


    @Override
    public void removeSubtask(int id) {

        Subtask subtask = subtaskRegistry.get(id);
        subtaskRegistry.remove(id);
        historyManager.remove(id);
        // Обновление соответствующего эпика (если, конечно, подзадача привязана к эпику)
        epicRegistry.computeIfPresent(subtask.getEpicId(),
                (k, epic) -> updateEpicImplicitly(epic, list -> list.remove((Integer)id)));
        prioritizedTasks.removeIf(task -> task.getId() == id);
    }


    @Override
    public void removeEpic(int id) {

        Epic epic = epicRegistry.get(id);
        if (epic != null) {
            epic.getSubtasks().forEach(subtaskId -> {
                subtaskRegistry.remove(subtaskId);
                historyManager.remove(subtaskId);
                prioritizedTasks.removeIf(task -> task.getId() == id);
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
     * @param epic Обновляемый эпик
     * @param subOperation Операция с подзадачами
     */
    protected Epic updateEpicImplicitly(Epic epic, Consumer<ArrayList<Integer>> subOperation) {

        TaskBuilder builder = new TaskBuilder(epic);

        // Обновление списка подзадач
        if (subOperation != null) {
            subOperation.accept(builder.getSubs());
        }

        // Обновление статуса выполнения эпика
        List<Subtask> subtasks = builder.getSubs().stream()
                .map(subtaskRegistry::get)
                .toList();
        TaskStatus newStatus = subtasks.stream().allMatch(Task::isNew) ? TaskStatus.NEW :
                    subtasks.stream().allMatch(Task::isDone) ? TaskStatus.DONE :
                        TaskStatus.IN_PROGRESS;
        builder.setStatus(newStatus);

        // Обновление времени начала
        LocalDateTime epicStartTime = subtasks.stream()
                .filter(Task::isValidTime)
                .map(Task::getStartTime)
                .min(Comparator.naturalOrder())
                .orElse(null);
        builder.setStartTime(epicStartTime);

        // Обновление длительности эпика
        Duration epicDuration = subtasks.stream()
                .filter(Task::isValidTime)
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        builder.setDuration(epicDuration);

        // Обновление времени окончания
        LocalDateTime epicEndTime = subtasks.stream()
                .filter(Task::isValidTime)
                .map(Task::getEndTime)
                .max(Comparator.naturalOrder())
                .orElse(null);
        builder.setEndTime(epicEndTime);

        return builder.buildEpic();
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

    @Override
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    /**
     * Проверка, что две задачи не пересекаются во времени
     * @param task1 Задача 1
     * @param task2 Задача 2
     * @return true, если задачи не пересекаются во времени
     */
    private boolean checkTasksDisjointInTime(Task task1, Task task2) {

        // Проверка имеет смысл, если время задано
        if (!task1.isValidTime() || !task2.isValidTime())
            return true;

        // Для проверки удобно, чтобы первой задачей была task1, для определенности
        if (task2.getStartTime().isBefore(task1.getStartTime())) {
            var t = task1;
            task1 = task2;
            task2 = t;
        }

        // Если начало задачи 2 настает раньше, чем оканчивается задача 1,
        // либо задачи запускаются одновременно -
        // задачи пересекаются во времени
        return !task2.getStartTime().isBefore(task1.getEndTime()) &&
                task2.getStartTime() != task1.getStartTime();
    }


    /**
     * Проверка задачи пересечение с другими задачами на временной шкале
     * @param task Проверяемая задача
     * @throws RuntimeException Если задача пересекается с имеющейся задачей
     */
    private void checkTaskInTime(Task task) {

        if (task.isValidTime()) {
            if (prioritizedTasks.stream()
                    .filter(task1 -> task1.getId() != task.getId())
                    .anyMatch(task1 -> !checkTasksDisjointInTime(task1, task)))
                throw new IllegalArgumentException("Недопустимо пересечение задач во времени");
        }
    }
}
