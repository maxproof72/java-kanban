package ru.maxproof.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.maxproof.exceptions.NotFoundIdException;
import ru.maxproof.exceptions.OverlappingTasksException;
import ru.maxproof.server.TaskConverter;
import ru.maxproof.taskmanager.Subtask;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpSubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public HttpSubtaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            switch (exchange.getRequestMethod()) {

                case "GET" -> {
                    var optId = getPathId(exchange);
                    if (optId.isEmpty()) {
                        sendJsonResponse(exchange, manager.getSubtasks(), 200);
                    } else {
                        Subtask subtask = manager.getSubtask(optId.get());
                        sendJsonResponse(exchange, subtask, 200);
                    }
                }

                case "POST" -> {

                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = TaskConverter.fromJson(body, Subtask.class);
                    if (subtask.getId() == TaskManager.DRAFT_TASK_ID) {
                        subtask = manager.createSubtask(subtask);
                        sendJsonResponse(exchange, subtask, 201);
                    } else {
                        manager.updateSubtask(subtask);
                        sendEmptyResponse(exchange, 201);
                    }
                }

                case "DELETE" -> {

                    var optId = getPathId(exchange);
                    if (optId.isPresent()) {
                        int id = optId.get();
                        manager.removeSubtask(id);
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
