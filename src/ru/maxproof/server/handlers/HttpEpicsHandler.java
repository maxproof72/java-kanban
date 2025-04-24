package ru.maxproof.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.maxproof.exceptions.NotFoundIdException;
import ru.maxproof.exceptions.OverlappingTasksException;
import ru.maxproof.server.TaskConverter;
import ru.maxproof.taskmanager.Epic;
import ru.maxproof.taskmanager.Subtask;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpEpicsHandler extends BaseHttpHandler implements HttpHandler {

    public HttpEpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            switch (exchange.getRequestMethod()) {

                case "GET" -> {
                    var optId = getPathId(exchange);
                    if (optId.isEmpty()) {
                        sendJsonResponse(exchange, manager.getEpics(), 200);
                    } else {
                        Epic epic = manager.getEpic(optId.get());
                        if (exchange.getRequestURI().getPath().endsWith("/subtasks")) {
                            List<Subtask> subtasks = manager.getEpicSubtasks(epic);
                            sendJsonResponse(exchange, subtasks, 200);
                        } else {
                            sendJsonResponse(exchange, epic, 200);
                        }
                    }
                }

                case "POST" -> {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = TaskConverter.fromJson(body, Epic.class);
                    if (epic.getId() == TaskManager.DRAFT_TASK_ID) {
                        epic = manager.createEpic(epic);
                        sendJsonResponse(exchange, epic, 201);
                    } else {
                        manager.updateEpic(epic);
                        sendEmptyResponse(exchange, 201);
                    }
                }

                case "DELETE" -> {

                    var optId = getPathId(exchange);
                    if (optId.isPresent()) {
                        int id = optId.get();
                        manager.removeEpic(id);
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
