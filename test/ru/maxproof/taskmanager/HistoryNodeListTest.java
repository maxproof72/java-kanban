package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Дополнительные тесты HistoryNodeList для методов,
 * не отраженных в явном виде в тестах InMemoryHistoryManager
 */

public class HistoryNodeListTest {


    @Test
    public void iterate() {

        final HistoryNodeList list = new HistoryNodeList();
        Assertions.assertEquals(0, list.size());

        // Проверка итерации пустого списка
        var iterator = list.iterator();
        Assertions.assertNotNull(iterator);
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertNull(iterator.next());
    }
}
