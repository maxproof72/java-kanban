package ru.maxproof.taskmanager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public static class ManagerSaveException extends RuntimeException {

    }

    private final Path storageFile;

    public FileBackedTaskManager(Path storageFile) {
        super();
        this.storageFile = storageFile;
    }

    private String saveTaskToString(Task task) {

        StringJoiner joiner = new StringJoiner(",");
        joiner.add(String.valueOf(task.getId()));
        joiner.add(task.getClass().getSimpleName().toUpperCase());
        joiner.add(task.getName());
        joiner.add(task.getStatus().name());
        joiner.add(task.getDescription());
        if (task instanceof Subtask)
            joiner.add(String.valueOf(((Subtask) task).getEpicId()));
        else
            joiner.add("");
        return joiner.toString();
    }

    private Task loadTaskFromString(String taskString) {

        Task task = null;
        // !! Здесь указание limit (=6) важно для того, чтобы не были выкинуты trailing empty strings !!
        String[] parts = taskString.split(",", 6);
        int id = Integer.parseInt(parts[0]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        String taskClass = parts[1];
        switch (taskClass) {
            case "TASK" -> task = new Task(id, name, description, status);
            case "EPIC" -> task = new Epic(id, name, description, status, List.of());
            case "SUBTASK" -> {
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(epicId, id, name, description, status);
            }
        }
        return task;
    }

    /**
     * Сохраняет текущее состояние менеджера задач в файл-хранилище
     */
    void save() {

        StringJoiner joiner = new StringJoiner("\n");
        joiner.add("id,type,name,status,description,epic");
        taskRegistry.values().forEach(task -> joiner.add(saveTaskToString(task)));
        epicRegistry.values().forEach(task -> joiner.add(saveTaskToString(task)));
        subtaskRegistry.values().forEach(task -> joiner.add(saveTaskToString(task)));
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(storageFile.toFile(), StandardCharsets.UTF_8))) {
            bw.write(joiner.toString());
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    static FileBackedTaskManager loadTaskManager(Path storageFile) {

        var manager = new FileBackedTaskManager(storageFile);
        try (BufferedReader br = new BufferedReader(
                new FileReader(storageFile.toFile(), StandardCharsets.UTF_8))) {
            int globalId = 0;
            boolean first = true;
            while (br.ready()) {
                String line = br.readLine();
                if (first) {
                    first = false;
                    continue;
                }
                Task loadedTask = manager.loadTaskFromString(line);
                switch (loadedTask) {
                    case Subtask subtask -> {
                        manager.subtaskRegistry.put(subtask.getId(), subtask);
                        manager.epicRegistry.get(subtask.getEpicId()).registerSubtask(subtask.getId());
                    }
                    case Epic epic -> manager.epicRegistry.put(epic.getId(), epic);
                    case Task task -> manager.taskRegistry.put(task.getId(), task);
                }
                globalId = Math.max(globalId, loadedTask.getId());
            }
            manager.taskId = globalId;
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    @Override
    public int createTask(Task draftTask) {
        int id = super.createTask(draftTask);
        save();
        return id;
    }

    @Override
    public int createSubtask(int epicId, Subtask draftSubtask) {
        int id = super.createSubtask(epicId, draftSubtask);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic draftEpic) {
        int id = super.createEpic(draftEpic);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }


    public static void main(String[] args) throws IOException {

        File tempStorage = File.createTempFile("practicum", ".csv");

        // 1. Заведите несколько разных задач, эпиков и подзадач.
        FileBackedTaskManager manager = new FileBackedTaskManager(tempStorage.toPath());
        manager.createTask(new Task("task1", ""));
        manager.createTask(new Task("task2", ""));
        int id3 = manager.createEpic(new Epic("epic1", ""));
        manager.createSubtask(id3, new Subtask("sub1", ""));
        manager.createSubtask(id3, new Subtask("sub2", ""));
        manager.createSubtask(id3, new Subtask("sub3", ""));
        manager.createEpic(new Epic("epic2", ""));

        // 2. Создайте новый FileBackedTaskManager-менеджер из этого же файла.
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadTaskManager(tempStorage.toPath());

        // 3. Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом
        if (Objects.equals(manager, manager2))
            System.out.println("Менеджеры идентичны !! :)");
        else
            System.out.println("Менеджеры не равны !! :(");
    }
}
