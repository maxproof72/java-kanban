package ru.maxproof.server;

import com.sun.net.httpserver.HttpServer;
import ru.maxproof.taskmanager.Managers;
import ru.maxproof.taskmanager.Task;
import ru.maxproof.taskmanager.TaskBuilder;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {

    private static final int PORT = 8080;

    private final HttpServer httpServer;

    HttpTaskServer() throws IOException {

        // Создание сервера
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Создание менеджера задач
        TaskManager taskManager = Managers.getDefault();

        // Настройка сервера
        httpServer.createContext("/tasks", new HttpHandlers.HttpTaskHandler(taskManager));
        httpServer.createContext("/subtasks", new HttpHandlers.HttpSubtaskHandler(taskManager));
        httpServer.createContext("/epics", new HttpHandlers.HttpEpicsHandler(taskManager));
        httpServer.createContext("/history", new HttpHandlers.HttpHistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new HttpHandlers.HttpPrioritizedHandler(taskManager));
    }

    /**
     * Запуск сервера
     */
    void start() {

        // Запуск сервера
        httpServer.start();
        System.out.println("Server started at port: " + PORT);
    }

    /**
     * Останов сервера
     */
    void stop() {
        httpServer.stop(0);
        System.out.println("Server stopped");
    }


    public static void main(String[] agrs) throws IOException {

        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}
