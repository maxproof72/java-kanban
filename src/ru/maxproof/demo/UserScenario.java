package ru.maxproof.demo;

import ru.maxproof.taskmanager.*;

import java.util.stream.Stream;

public class UserScenario {

    final TaskManager manager = Managers.getDefault();

    private void checkNoDuplicatesInHistory() {

        var history = manager.getHistory();
        if (history.size() != history.stream().distinct().count())
            throw new RuntimeException("В списке есть дубликаты");
        System.out.println("Проверка - размер истории: " + history.size() + ", дубликатов нет.");
    }

    private boolean historyContainsId(int id) {
        return manager.getHistory().stream()
                .anyMatch(task -> task.getId() == id);
    }

    private void printHistory() {
        manager.getHistory().forEach(System.out::println);
        System.out.println();
    }

    public void run() {

        // 1. Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач
        int id1 = manager.createTask(new TaskBuilder().setName("task1").buildTask());
        int id2 = manager.createTask(new TaskBuilder().setName("task2").buildTask());
        int id3 = manager.createEpic(new TaskBuilder().setName("epic1").buildEpic());
        int id31 = manager.createSubtask(id3, new TaskBuilder().setName("sub1").buildSubtask());
        int id32 = manager.createSubtask(id3, new TaskBuilder().setName("sub2").buildSubtask());
        int id33 = manager.createSubtask(id3, new TaskBuilder().setName("sub3").buildSubtask());
        int id4 = manager.createEpic(new TaskBuilder().setName("epic2").buildEpic());

        // 2. Запросите созданные задачи несколько раз в разном порядке
        // 3. После каждого запроса выведите историю и убедитесь, что в ней нет повторов
        manager.getTask(id2);
        manager.getSubtask(id33);
        manager.getSubtask(id32);
        manager.getSubtask(id31);
        checkNoDuplicatesInHistory();
        printHistory();
        manager.getTask(id1);
        manager.getTask(id2);
        checkNoDuplicatesInHistory();
        printHistory();
        manager.getEpic(id3);
        manager.getSubtask(id31);
        manager.getSubtask(id32);
        manager.getEpic(id4);
        checkNoDuplicatesInHistory();
        printHistory();

        // 4. Удалите задачу, которая есть в истории, и проверьте,
        //    что при печати она не будет выводиться
        if (!historyContainsId(id2))
            throw new RuntimeException("!! id2 потерялся :(");
        manager.removeTask(id2);
        if (historyContainsId(id2))
            throw new RuntimeException("!! id2 не убрался из истории 8|");
        System.out.println("Удаление id2 привело к исключению задачи из истории");
        printHistory();

        // 5. Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился
        //    как сам эпик, так и все его подзадачи
        manager.removeEpic(id3);
        boolean cutAllItems = Stream.of(id3, id31, id32, id33)
                .noneMatch(this::historyContainsId);
        if (!cutAllItems)
            throw new RuntimeException("Удаление эпика не привело к исключению всех элементов из истории");
        System.out.println("Удаление эпика привело к исключению всех элементов из истории");
        printHistory();
    }
}
