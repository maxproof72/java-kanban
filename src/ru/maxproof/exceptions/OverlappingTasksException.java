package ru.maxproof.exceptions;

/**
 * Класс исключения для возбуждения в случае создания или обновления
 * задачи, пересекающейся во времени с другими задачами.
 */
public class OverlappingTasksException extends RuntimeException {
    public OverlappingTasksException(String message) {
        super(message);
    }
}
