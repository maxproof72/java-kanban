package ru.maxproof.server;

import com.sun.net.httpserver.HttpServer;
import ru.maxproof.server.handlers.*;
import ru.maxproof.taskmanager.Managers;
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
        httpServer.createContext("/tasks", new HttpTaskHandler(taskManager));
        httpServer.createContext("/subtasks", new HttpSubtaskHandler(taskManager));
        httpServer.createContext("/epics", new HttpEpicsHandler(taskManager));
        httpServer.createContext("/history", new HttpHistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new HttpPrioritizedHandler(taskManager));
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
