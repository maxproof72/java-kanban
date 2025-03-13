package ru.maxproof.taskmanager;

import java.util.List;

/**
 * Интерфейс управления историей просмотра
 */
public interface HistoryManager {

    /**
     * Добавляет задачу в историю просмотра
     * @param task Просмотренная задача
     */
    void add(Task task);

    /**
     * Удаляет задачу из истории просмотра
     * @param id Идентификатор задачи
     */
    void remove(int id);

    /**
     * Возвращает список просмотренных задач
     * @return Список просмотренных задач
     */
    List<Task> getHistory();

//    /**
//     * Возвращает число записей истории просмотра
//     * @return Число записей истории просмотра
//     */
//    int size();
}
