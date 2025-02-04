package ru.maxproof.taskmanager;

import java.util.*;

/**
 * Класс менеджера задач
 */
public class TaskManager {


    private final Map<Integer, Task> taskRegistry = new HashMap<>();
    private final Map<Integer, Epic> epicRegistry = new HashMap<>();
    private final Map<Integer, Subtask> subtaskRegistry = new HashMap<>();
    private static int TaskId = 0;

    /**
     * Регистрирует черновую задачу и возвращает ее идентификатор в реестре
     * @param draftTask Черновая простая задача
     * @return Зарегистрированная задача с реальным идентификатором
     */
    public Task createTask(Task draftTask) {

        Task registeredTask = new Task(++TaskId, draftTask);
        taskRegistry.put(registeredTask.getId(), registeredTask);
        return registeredTask;
    }


    /**
     * Регистрирует черновую подзадачу и возвращает ее идентификатор в реестре
     * @param draftSubtask Черновая подзадача
     * @return Зарегистрированная подзадача с реальным идентификатором
     */
    public Subtask createSubtask(Epic epic, Subtask draftSubtask) {

        Subtask registeredSubtask = new Subtask(epic.getId(), ++TaskId, draftSubtask);
        subtaskRegistry.put(registeredSubtask.getId(), registeredSubtask);
        epic.registerSubtask(registeredSubtask.getId());
        epic.setStatus(estimateEpicStatus(epic));
        return registeredSubtask;
    }


    /**
     * Регистрирует черновую сложную задачу и возвращает ее идентификатор в реестре
     * @param draftEpic Черновая сложная задача
     * @return Зарегистрированная Epic задача с реальным идентификатором
     */
    public Epic createEpic(Epic draftEpic) {

        Epic registeredEpic = new Epic(++TaskId, draftEpic);
        epicRegistry.put(registeredEpic.getId(), registeredEpic);
        return registeredEpic;
    }


    /**
     * Обновляет простую задачу с указанным id.
     * @param task Новая задача с указанным id
     */
    public void updateTask(Task task) {
        if (task.getId() != Task.DRAFT_TASK_ID && taskRegistry.containsKey(task.getId())) {
            taskRegistry.put(task.getId(), task);
        }
    }


    /**
     * Обновляет подзадачу с указанным id.
     * @param subtask Новая подзадача с указанным id
     */
    public void updateSubtask(Subtask subtask) {
        if (subtask.getId() != Task.DRAFT_TASK_ID && subtaskRegistry.containsKey(subtask.getId())) {
            subtaskRegistry.put(subtask.getId(), subtask);

            // Обновление статуса родительского epic
            Epic epic = epicRegistry.get(subtask.getEpicId());
            epic.setStatus(estimateEpicStatus(epic));
        }
    }


    /**
     * Обновляет Epic задачу с указанным id.
     * @param epic Новая задача с указанным id
     */
    public void updateEpic(Epic epic) {
        if (epic.getId() != Task.DRAFT_TASK_ID && epicRegistry.containsKey(epic.getId())) {
            epicRegistry.put(epic.getId(), epic);
        }
    }


    /**
     * Получение списка задач
     * @return Список задач
     */
    public List<Task> getTaskList() {
        return List.copyOf(taskRegistry.values());
    }

    /**
     * Получение списка подзадач
     * @return Список подзадач
     */
    public List<Subtask> getSutaskList() {
        return List.copyOf(subtaskRegistry.values());
    }

    /**
     * Получение списка Epic задач
     * @return Список Epic задач
     */
    public List<Epic> getEpicList() {
        return List.copyOf(epicRegistry.values());
    }

    /**
     * Получение списка задач типов Task и Epic
     * @return Список всех задач верхнего уровня
     */
    public List<Task> getTopTaskList() {
        var list = new ArrayList<Task>();
        list.addAll(taskRegistry.values());
        list.addAll(epicRegistry.values());
        return list;
    }


    /**
     * Получение списка задач всех типов
     * @return Список всех задач
     */
    public List<Task> getEntireTaskList() {
        var list = getTopTaskList();
        list.addAll(subtaskRegistry.values());
        return list;
    }


    /**
     * Удаление всех задач реестра
     */
    public void clearTasks() {
        taskRegistry.clear();
    }


    /**
     * Удаление всех задач реестра
     */
    public void clearSubtasks() {
        epicRegistry.values().forEach(Epic::clearSubtasks);
        subtaskRegistry.clear();
    }


    /**
     * Удаление всех Epic задач реестра.
     * Чтобы подзадачи не зависли, удаляем их тоже
     */
    public void clearEpics() {
        clearSubtasks();
        epicRegistry.clear();
    }


    /**
     * Поиск задачи по идентификатору
     * @param id Идентификатор задачи
     * @return Опционально задача с заданным идентификатором
     */
    public Optional<Task> getTask(int id) {
        return Optional.ofNullable(taskRegistry.get(id));
    }


    /**
     * Поиск подзадачи по идентификатору
     * @param id Идентификатор подзадачи
     * @return Опционально подзадача с заданным идентификатором
     */
    public Optional<Subtask> getSubtask(int id) {
        return Optional.ofNullable(subtaskRegistry.get(id));
    }


    /**
     * Поиск Epic задачи по идентификатору
     * @param id Идентификатор Epic задачи
     * @return Опционально Epic задача с заданным идентификатором
     */
    public Optional<Epic> getEpic(int id) {
        return Optional.ofNullable(epicRegistry.get(id));
    }


    /**
     * Удаление задачи с заданным идентификатором
     *
     * @param id Идентификатор задачи
     */
    public void removeTask(int id) {
        taskRegistry.remove(id);
    }


    /**
     * Удаление подзадачи с заданным идентификатором
     *
     * @param id Идентификатор подзадачи
     */
    public void removeSubtask(int id) {
        subtaskRegistry.remove(id);
        epicRegistry.values().forEach(epic -> {
            if (epic.unregisterSubtask(id))
                epic.setStatus(estimateEpicStatus(epic));
        });
    }


    /**
     * Удаление Epic задачи с заданным идентификатором
     * @param id Идентификатор Epic задачи
     */
    public void removeEpic(int id) {
        epicRegistry.remove(id);
    }


    /**
     * Возвращает список подзадач указанной Epic задачи
     * @param epic Задача типа Epic
     * @return Список подзадач
     */
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtasks().stream().map(subtaskRegistry::get).toList();
    }


    /**
     * Процедура обновления статуса Epic задания на основе текущих статусов дочерних заданий
     */
    private TaskStatus estimateEpicStatus(Epic epic) {

        var subtasks = getEpicSubtasks(epic);
        return subtasks.stream().allMatch(Task::isNew)? TaskStatus.NEW:
                subtasks.stream().allMatch(Task::isDone)? TaskStatus.DONE:
                        TaskStatus.IN_PROGRESS;
    }


    /**
     * Проверяет, пуст ли менеджер задач
     * @return true, если никаких задач нет
     */
    public boolean isEmpty() {
        return taskRegistry.isEmpty() && epicRegistry.isEmpty() && subtaskRegistry.isEmpty();
    }
}
