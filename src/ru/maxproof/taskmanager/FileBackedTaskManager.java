package ru.maxproof.taskmanager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
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
        LocalDateTime startTime = task.getStartTime();
        joiner.add(startTime == null ? "" : startTime.toString());
        Duration duration = task.getDuration();
        joiner.add(duration == null ? "" : duration.toString());
        return joiner.toString();
    }

    private Task loadTaskFromString(String taskString) {

        enum PosData {
            ID, CLASS, NAME, STATUS, DESCRIPTION, EPIC_ID, START_TIME, DURATION
        }

        Task task = null;
        // !! Здесь указание limit (=8) важно для того, чтобы не были выкинуты trailing empty strings !!
        String[] parts = taskString.split(",", PosData.values().length);
        TaskBuilder builder = new TaskBuilder();
        builder.setId(Integer.parseInt(parts[PosData.ID.ordinal()]));
        builder.setName(parts[PosData.NAME.ordinal()]);
        builder.setStatus(TaskStatus.valueOf(parts[PosData.STATUS.ordinal()]));
        builder.setDescription(parts[PosData.DESCRIPTION.ordinal()]);
        builder.setStartTime(parts[6].isEmpty() ? null : LocalDateTime.parse(parts[PosData.START_TIME.ordinal()]));
        builder.setDuration(parts[7].isEmpty() ? null : Duration.parse(parts[PosData.DURATION.ordinal()]));
        String taskClass = parts[1];
        switch (taskClass) {
            case "TASK" -> task = new Task(builder);
            case "EPIC" -> task = new Epic(builder);
            case "SUBTASK" -> {
                int epicId = Integer.parseInt(parts[PosData.EPIC_ID.ordinal()]);
                task = new Subtask(builder, epicId);
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
                    case Subtask subtask -> manager.subtaskRegistry.put(subtask.getId(), subtask);
                    case Epic epic -> manager.epicRegistry.put(epic.getId(), epic);
                    case Task task -> manager.taskRegistry.put(task.getId(), task);
                }
                globalId = Math.max(globalId, loadedTask.getId());
            }
            // Регистрация subtask в эпиках
            manager.epicRegistry.keySet()
                    .forEach(epicId -> manager.epicRegistry.computeIfPresent(epicId,
                            (k, oldEpic) -> manager.updateEpicImplicitly(oldEpic,
                                    list -> list.addAll(manager.subtaskRegistry.values().stream()
                                            .filter(subtask -> subtask.getEpicId() == oldEpic.getId())
                                            .map(Subtask::getId)
                                            .toList()))));
            manager.taskId = globalId;
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    @Override
    public Task createTask(Task draftTask) {
        Task task = super.createTask(draftTask);
        save();
        return task;
    }

    @Override
    public Subtask createSubtask(Subtask draftSubtask) {
        Subtask subtask = super.createSubtask(draftSubtask);
        save();
        return subtask;
    }

    @Override
    public Epic createEpic(Epic draftEpic) {
        Epic epic = super.createEpic(draftEpic);
        save();
        return epic;
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
}
