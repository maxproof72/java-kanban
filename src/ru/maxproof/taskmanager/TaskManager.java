package ru.maxproof.taskmanager;

import java.util.List;
import java.util.Optional;

public interface TaskManager {

    /**
     * Идентификатор для незарегистрированной задачи (до передачи менеджеру задач)
     */
    int DRAFT_TASK_ID = 0;

    /**
     * Регистрирует черновую задачу и возвращает ее идентификатор в реестре
     * @param draftTask Черновая простая задача
     * @return Зарегистрированная задача с реальным идентификатором
     */
    int createTask(Task draftTask);

    /**
     * Регистрирует черновую подзадачу и возвращает ее идентификатор в реестре
     * @param draftSubtask Черновая подзадача
     * @return Зарегистрированная подзадача с реальным идентификатором
     */
    int createSubtask(int epicId, Subtask draftSubtask);

    /**
     * Регистрирует черновую сложную задачу и возвращает ее идентификатор в реестре
     * @param draftEpic Черновая сложная задача
     * @return Зарегистрированная Epic задача с реальным идентификатором
     */
    int createEpic(Epic draftEpic);

    /**
     * Обновляет простую задачу с указанным id.
     * @param task Новая задача с указанным id
     */
    void updateTask(Task task);

    /**
     * Обновляет подзадачу с указанным id.
     * @param subtask Новая подзадача с указанным id
     */
    void updateSubtask(Subtask subtask);

    /**
     * Обновляет Epic задачу с указанным id.
     * @param epic Новая задача с указанным id
     */
    void updateEpic(Epic epic);

    /**
     * Получение списка задач
     * @return Список задач
     */
    List<Task> getTasks();

    /**
     * Получение списка подзадач
     * @return Список подзадач
     */
    List<Subtask> getSubtasks();

    /**
     * Получение списка Epic задач
     * @return Список Epic задач
     */
    List<Epic> getEpics();

    /**
     * Получение списка задач типов Task и Epic
     * @return Список всех задач верхнего уровня
     */
    List<Task> getTopTaskList();

    /**
     * Получение списка задач всех типов
     * @return Список всех задач
     */
    List<Task> getEntireTaskList();

    /**
     * Удаление всех задач реестра
     */
    void clearTasks();

    /**
     * Удаление всех подзадач задач реестра
     */
    void clearSubtasks();

    /**
     * Удаление всех Epic задач реестра.
     * Чтобы подзадачи не зависли, удаляем их тоже
     */
    void clearEpics();

    /**
     * Поиск задачи по идентификатору
     * @param id Идентификатор задачи
     * @return Опционально задача с заданным идентификатором
     */
    Optional<Task> getTask(int id);

    /**
     * Поиск подзадачи по идентификатору
     * @param id Идентификатор подзадачи
     * @return Опционально подзадача с заданным идентификатором
     */
    Optional<Subtask> getSubtask(int id);

    /**
     * Поиск Epic задачи по идентификатору
     * @param id Идентификатор Epic задачи
     * @return Опционально Epic задача с заданным идентификатором
     */
    Optional<Epic> getEpic(int id);

    /**
     * Удаление задачи с заданным идентификатором
     *
     * @param id Идентификатор задачи
     */
    void removeTask(int id);

    /**
     * Удаление подзадачи с заданным идентификатором
     *
     * @param id Идентификатор подзадачи
     */
    void removeSubtask(int id);

    /**
     * Удаление Epic задачи с заданным идентификатором
     * @param id Идентификатор Epic задачи
     */
    void removeEpic(int id);

    /**
     * Возвращает список подзадач указанной Epic задачи
     * @param epic Задача типа Epic
     * @return Список подзадач
     */
    List<Subtask> getEpicSubtasks(Epic epic);

    /**
     * Проверяет, пуст ли менеджер задач
     * @return true, если никаких задач нет
     */
    boolean isEmpty();

    /**
     * Возвращает список из не более чем 10 задач, к которым обращались последними
     * @return Список задач
     */
    List<Task> getHistory();

    /**
     * Возвращает перечень задач, отсортированных по времени выполнения
     * @return Перечень задач
     */
    List<Task> getPrioritizedTasks();
}
