package ru.maxproof.taskmanager;

import java.util.List;

public interface TaskManager {

    /**
     * Идентификатор для незарегистрированной задачи (до передачи менеджеру задач)
     */
    int DRAFT_TASK_ID = 0;

    /**
     * Класс исключения для следующих ситуаций:
     * <li> В случае недопустимого значения Id запрашиваемой задачи; </li>
     * <li> В случае привязки подзадачи к несуществующему эпику; </li>
     */
    class NotFoundIdException extends RuntimeException {
        public NotFoundIdException(String message) {
            super(message);
        }
    }

    /**
     * Класс исключения для возбуждения в случае создания или обновления
     * задачи, пересекающейся во времени с другими задачами.
     */
    class OverlappingTasksException extends RuntimeException {
        public OverlappingTasksException(String message) {
            super(message);
        }
    }


    /**
     * Регистрирует черновую задачу и возвращает ее идентификатор в реестре
     * @param draftTask Черновая простая задача с нулевым Id
     * @return Зарегистрированная задача с реальным идентификатором
     * @throws NotFoundIdException если задача имеет ненулевой Id.
     * @throws OverlappingTasksException если задача пересекается во времени с другой задачей.
     */
    Task createTask(Task draftTask);

    /**
     * Регистрирует черновую подзадачу и возвращает ее идентификатор в реестре
     * @param draftSubtask Черновая подзадача
     * @return Зарегистрированная подзадача с реальным идентификатором
     * @throws NotFoundIdException если задача имеет ненулевой Id.
     * @throws OverlappingTasksException если задача пересекается во времени с другой задачей.
     */
    Subtask createSubtask(Subtask draftSubtask);

    /**
     * Регистрирует черновую сложную задачу и возвращает ее идентификатор в реестре
     * @param draftEpic Черновая сложная задача
     * @return Зарегистрированная Epic задача с реальным идентификатором
     * @throws NotFoundIdException если задача имеет ненулевой Id.
     */
    Epic createEpic(Epic draftEpic);

    /**
     * Обновляет простую задачу с указанным id.
     * @param task Новая задача с указанным id
     * @throws NotFoundIdException если задача имеет ненулевой Id.
     * @throws OverlappingTasksException если задача пересекается во времени с другой задачей.
     */
    void updateTask(Task task);

    /**
     * Обновляет подзадачу с указанным id.
     * @param subtask Новая подзадача с указанным id
     * @throws NotFoundIdException если задача имеет ненулевой Id.
     * @throws OverlappingTasksException если задача пересекается во времени с другой задачей.
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
     * @throws NotFoundIdException если задача с указанным Id не найдена.
     */
    Task getTask(int id);

    /**
     * Поиск подзадачи по идентификатору
     * @param id Идентификатор подзадачи
     * @return Опционально подзадача с заданным идентификатором
     * @throws NotFoundIdException если подзадача с указанным Id не найдена.
     */
    Subtask getSubtask(int id);

    /**
     * Поиск Epic задачи по идентификатору
     * @param id Идентификатор Epic задачи
     * @return Опционально Epic задача с заданным идентификатором
     * @throws NotFoundIdException если эпик с указанным Id не найден.
     */
    Epic getEpic(int id);

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
