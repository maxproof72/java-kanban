package ru.maxproof.taskmanager;

/**
 * Статический класс создания менеджеров задач и истории
 */
public class Managers {

    /**
     * Создает менеджер задач класса по умолчанию
     * @return Менеджер задач
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    /**
     * Создает менеджер истории класса по умолчанию
     * @return Менеджер истории задач
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
