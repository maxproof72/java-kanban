package ru.maxproof.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.maxproof.exceptions.NotFoundIdException;
import ru.maxproof.exceptions.OverlappingTasksException;
import ru.maxproof.server.TaskConverter;
import ru.maxproof.taskmanager.Task;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpTaskHandler extends BaseHttpHandler implements HttpHandler {


    public HttpTaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            switch (exchange.getRequestMethod()) {

                case "GET" -> {
                    var optId = getPathId(exchange);
                    if (optId.isEmpty()) {
                        sendJsonResponse(exchange, manager.getTasks(), 200);
                    } else {
                        Task task = manager.getTask(optId.get());
                        sendJsonResponse(exchange, task, 200);
                    }
                }

                case "POST" -> {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Task task = TaskConverter.fromJson(body, Task.class);
                    if (task.getId() == TaskManager.DRAFT_TASK_ID) {
                        task = manager.createTask(task);
                        sendJsonResponse(exchange, task, 201);
                    } else {
                        manager.updateTask(task);
                        sendEmptyResponse(exchange, 201);
                    }
                }

                case "DELETE" -> {

                    var optId = getPathId(exchange);
                    if (optId.isPresent()) {
                        int id = optId.get();
                        manager.removeTask(id);
                        sendEmptyResponse(exchange, 200);
                    }
                }
            }
        } catch (NotFoundIdException e) {
            sendNotFound(exchange);
        } catch (OverlappingTasksException e) {
            sendHasInteractions(exchange);
        }
    }
}
