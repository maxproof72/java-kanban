package ru.maxproof.demo;

import ru.maxproof.taskmanager.*;

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
        Task taskGotoPracticum = taskManager.createTask(new Task(
                "Сделать важное", "Записаться на курсы Yandex.Practicum"));      // задним числом
        printStructured();

        System.out.println("Изменение наименования задачи и обновление");
        taskGotoPracticum = new Task(taskGotoPracticum, "Важное дело", taskGotoPracticum.getDescription());
        taskManager.updateTask(taskGotoPracticum);
        printStructured();

        System.out.println("Обновление статуса задачи");
        taskGotoPracticum = new Task(taskGotoPracticum, TaskStatus.DONE);
        taskManager.updateTask(taskGotoPracticum);
        printStructured();

        System.out.println("Добавление еще одной простой задачи");
        taskManager.createTask(new Task("Сложное дело", "Завершить курсы Yandex.Practicum"));
        printStructured();

        System.out.println("Добавление Epic задачи");
        Epic epicJubilee = taskManager.createEpic(new Epic(
                "Подготовиться к юбилею", "Подготовиться к празднованию юбилея"));
        printStructured();

        System.out.println("Добавление подзадач");
        Subtask subRestaurant =
            taskManager.createSubtask(epicJubilee,
                new Subtask("Ресторан", "Выбрать ресторан"));
        taskManager.createSubtask(epicJubilee, new Subtask(
                "Гости", "Определиться с составом гостей"));
        taskManager.createSubtask(epicJubilee, new Subtask(
                "Меню", "Определиться с меню"));
        taskManager.createSubtask(epicJubilee, new Subtask(
                "Согласование", "Все согласовать с именинником"));
        Subtask subOrder =
            taskManager.createSubtask(epicJubilee, new Subtask(
                "Заказ", "Заказать выбранный ресторан"));
        printStructured();

        System.out.println("Изменение наименования последней подзадачи и обновление");
        subOrder = new Subtask(subOrder, "Бронирование", "Бронирование банкетного зала");
        taskManager.updateSubtask(subOrder);
        printStructured();

        System.out.println("Изменение статуса первой подзадачи и обновление");
        subRestaurant = new Subtask(subRestaurant, TaskStatus.DONE);
        taskManager.updateSubtask(subRestaurant);

        printStructured();

        printByStatus();

        taskManager.getTask(taskGotoPracticum.getId());
        taskManager.getEpic(epicJubilee.getId());
        taskManager.getSubtask(subRestaurant.getId());
        taskManager.getSubtask(subOrder.getId());

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
