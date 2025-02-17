package ru.maxproof.taskmanager;

import java.util.List;

/**
 * Интерфейс управления историей просмотра
 */
public interface HistoryManager {

    /**
     * Помечает задачу как просмотренную
     * @param task Просмотренная задача
     */
    void addTask(Task task);

    /**
     * Возвращает список просмотренных задач
     * @return Список просмотренных задач
     */
    List<Task> getHistory();
}
