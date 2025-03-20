package ru.maxproof.taskmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManagerTest {

    @Test
    public void saveTest() throws Exception {

        // Проверка записи пустого менеджера
        Path storageFile = Files.createTempFile("kanban_s7", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(storageFile);
        manager.save();

        // Проверка файла
        List<String> csv = Files.readAllLines(storageFile);
        Assertions.assertEquals(csv.size(), 1);
        Assertions.assertTrue(csv.getFirst().startsWith("id"));
        Assertions.assertEquals(6, csv.getFirst().split(",", 6).length);

        // Проверка чтения пустого файла
        FileBackedTaskManager manager1 = new FileBackedTaskManager(storageFile);
        Assertions.assertTrue(manager1.isEmpty());

        // Создание нескольких задач
        int epicId = manager.createEpic(new Epic("epic1", ""));
        manager.createSubtask(epicId, new Subtask("sub1", ""));
        manager.createTask(new Task("task1", ""));

        // Проверка файла с несколькими задачами
        csv = Files.readAllLines(storageFile);
        Assertions.assertEquals(csv.size(), 4);
        Assertions.assertTrue(csv.get(1).startsWith("3,"));
        Assertions.assertTrue(csv.get(2).startsWith("1,"));
        Assertions.assertTrue(csv.get(3).startsWith("2,"));
        Assertions.assertEquals(6, csv.get(1).split(",", 6).length);
        Assertions.assertEquals(6, csv.get(2).split(",", 6).length);
        Assertions.assertEquals(6, csv.get(3).split(",", 6).length);

        // Проверка чтения из файла с задачами
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadTaskManager(storageFile);
        Assertions.assertEquals(manager, manager2);
    }
}
