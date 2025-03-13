package ru.maxproof.taskmanager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Список истории просмотров с быстрым доступом (O(1)) к элементам
 */
public class HistoryNodeList implements Iterable<Task> {

    private static class Node {
        final Task task;
        Node prev;
        Node next;

        public Node(Task task) {
            this.task = task;
        }
    }

    private final HashMap<Integer, Node> nodes = new HashMap<>();   // таблица элементов списка
    private Node first = null;      // указатель на первый элемент связного списка
    private Node last = null;       // указатель на последний элемент связного списка
    private int size = 0;           // размер списка


    /**
     * Добавляет элемент в конец списка
     * @param node Добавляемый элемент списка
     */
    private void addNode(Node node) {

        node.prev = last;
        if (last != null)
            last.next = node;
        last = node;
        if (first == null)
            first = node;
        ++size;
    }

    /**
     * Удаляет элемент списка
     * @param node Удаляемый элемент списка
     */
    private void removeNode(Node node) {

        if (node.prev != null)
            node.prev.next = node.next;
        if (node.next != null)
            node.next.prev = node.prev;
        if (first == node)
            first = node.next;
        if (last == node)
            last = node.prev;
        --size;
    }

    @Override
    public Iterator<Task> iterator() {

        return new Iterator<>() {
            private Node current = first;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Task next() {

                if (current == null)
                    return null;
                Task task = current.task;
                current = current.next;
                return task;
            }
        };
    }

    /**
     * Добавление задачи в конец списка с удалением предыдущей версии (если таковая имеется)
     * @param task Новая задача
     */
    public void addTask(Task task) {

        // Удаляем (опционально) предыдущую версию задачи
        removeTaskById(task.getId());

        // Добавляем задачу в конец двусвязного списка
        Node node = new Node(task);
        addNode(node);
        nodes.put(task.getId(), node);
    }

    /**
     * Удаление задачи из списка
     * @param id ID удаляемой задачи
     */
    public void removeTaskById(int id) {

        final Node node = nodes.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    /**
     * Возвращает поток списка истории задач
     * @return Поток списка истории задач
     */
    public Stream<Task> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Возвращает размер списка истории
     * @return Размер списка
     */
    public int size() {
        return size;
    }
}
