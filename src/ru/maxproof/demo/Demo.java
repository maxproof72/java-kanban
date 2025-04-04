package ru.maxproof.demo;

import ru.maxproof.taskmanager.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * Демонстрационный режим приложения
 */
public class Demo {

    private final TaskManager taskManager;

    /**
     * Конструктор демо-модуля
     * @param taskManager Интерфейс менеджера задач
     */
    public Demo(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Печать общего перечня задач в структурном виде
     */
    private void printStructured() {

        var allTasks = taskManager.getTopTaskList();
        System.out.println("Перечень задач (структурно)");
        System.out.println("------------------------------------------------");
        if (taskManager.isEmpty())
            System.out.println("Перечень задач пуст");
        else {
            for (var task : allTasks) {
                System.out.println(" * " + task);
                if (task instanceof Epic epic) {
                    for (Subtask subtask : taskManager.getEpicSubtasks(epic)) {
                        System.out.println("    * " + subtask);
                    }
                }
            }
        }
        System.out.println();
    }


    /**
     * Печать реестра задач с группировкой по статусу
     */
    void printByStatus() {

        System.out.println("Перечень задач (группировка по статусу)");
        System.out.println("------------------------------------------------");
        for (var status: TaskStatus.values()) {
            System.out.println(status);
            taskManager.getEntireTaskList().stream()
                    .filter(task -> task.getStatus() == status)
                    .forEach(task -> System.out.println(" * " + task));
        }
        System.out.println();
    }

    /**
     * Печать задач по типу
     */
    public void printByType() {

        System.out.println();
        System.out.println("Задачи по типу");
        System.out.println("------------------------------------------------");
        System.out.println("Задачи:");
        taskManager.getTasks().forEach(task -> System.out.println(" * " + task));
        System.out.println("Эпики:");
        taskManager.getEpics().forEach(epic -> {
            System.out.println(epic);
            taskManager.getEpicSubtasks(epic).forEach(sub -> System.out.println(" --> " + sub));
        });
        System.out.println("Подзадачи:");
        taskManager.getSubtasks().forEach(sub -> System.out.println(" * " + sub));
        System.out.println("История:");
        taskManager.getHistory().forEach(item -> System.out.println(" * " + item));
        System.out.println();
    }


    /**
     * Демо код
     */
    public void run() {

        System.out.println("Running Demo");

        System.out.println("Добавление простой задачи");
        final int idGotoPracticum = taskManager.createTask(new TaskBuilder()
                .setName("Сделать важное")
                .setDescription("Записаться на курсы Yandex.Practicum")
                .buildTask());
        printStructured();

        System.out.println("Изменение наименования задачи и обновление");
        Task taskGotoPracticum = taskManager.getTask(idGotoPracticum).orElseThrow();
        taskGotoPracticum = new TaskBuilder(taskGotoPracticum).setName("Важное дело").buildTask();
        taskManager.updateTask(taskGotoPracticum);
        printStructured();

        System.out.println("Обновление статуса задачи");
        taskGotoPracticum = new TaskBuilder(taskGotoPracticum).setStatus(TaskStatus.DONE).buildTask();
        taskManager.updateTask(taskGotoPracticum);
        printStructured();

        System.out.println("Добавление еще одной простой задачи");
        taskManager.createTask(new TaskBuilder()
                .setName("Сложное дело")
                .setDescription("Завершить курсы Yandex.Practicum")
                .buildTask());
        printStructured();

        System.out.println("Добавление Epic задачи");
        final LocalDateTime jubileeDate = LocalDateTime.of(2025, Month.JULY, 15, 0, 0);
        final int idJubilee = taskManager.createEpic(new TaskBuilder()
                .setName("Подготовиться к юбилею")
                .setDescription("Подготовиться к празднованию юбилея")
                .buildEpic());
        printStructured();

        System.out.println("Добавление подзадач");
        final int idRestaurant =
            taskManager.createSubtask(idJubilee, new TaskBuilder()
                    .setName("Ресторан")
                    .setDescription("Выбрать ресторан")
                    .setStartTime(jubileeDate.minusDays(30))
                    .setDuration(Duration.ofDays(1))
                    .buildSubtask());
            taskManager.createSubtask(idJubilee, new TaskBuilder()
                    .setName("Гости")
                    .setDescription("Определиться с составом гостей")
                    .setStartTime(jubileeDate.minusDays(40))
                    .setDuration(Duration.ofDays(7))
                    .buildSubtask());
            taskManager.createSubtask(idJubilee, new TaskBuilder()
                    .setName("Меню")
                    .setDescription("Определиться с меню")
                    .setStartTime(jubileeDate.minusDays(14))
                    .setDuration(Duration.ofDays(3))
                    .buildSubtask());
        final int idOrder =
            taskManager.createSubtask(idJubilee, new TaskBuilder()
                    .setName("Заказ")
                    .setDescription("Заказать выбранный ресторан")
                    .setStartTime(jubileeDate.minusDays(25))
                    .buildSubtask());
        printStructured();

        System.out.println("Изменение наименования последней подзадачи и обновление");
        Subtask subOrder = taskManager.getSubtask(idOrder).orElseThrow();
        subOrder = new TaskBuilder(subOrder)
                .setName("Бронирование")
                .setDescription("Бронирование банкетного зала")
                .buildSubtask();
        taskManager.updateSubtask(subOrder);
        printStructured();

        System.out.println("Изменение статуса первой подзадачи и обновление");
        Subtask subRestaurant = taskManager.getSubtask(idRestaurant).orElseThrow();
        subRestaurant = new TaskBuilder(subRestaurant).setStatus(TaskStatus.DONE).buildSubtask();
        taskManager.updateSubtask(subRestaurant);

        printStructured();

        printByStatus();

        printByType();

        System.out.println("Удаление задачи " + taskGotoPracticum);
        taskManager.removeTask(taskGotoPracticum.getId());
        printStructured();

        System.out.println("Удаление подзадачи " + subRestaurant);
        taskManager.removeSubtask(subRestaurant.getId());
        printStructured();

        System.out.println("Очистка перечня задач");
        taskManager.clearTasks();
        printStructured();

        System.out.println("Очистка перечня Epic задач");
        taskManager.clearEpics();
        printStructured();
    }
}
