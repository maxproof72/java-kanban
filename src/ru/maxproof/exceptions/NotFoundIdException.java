package ru.maxproof.exceptions;

/**
 * Класс исключения для следующих ситуаций:
 * <li> В случае недопустимого значения Id запрашиваемой задачи; </li>
 * <li> В случае привязки подзадачи к несуществующему эпику; </li>
 */
public class NotFoundIdException extends RuntimeException {
    public NotFoundIdException(String message) {
        super(message);
    }
}
