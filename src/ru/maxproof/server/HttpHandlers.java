package ru.maxproof.server;

//import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.maxproof.taskmanager.Epic;
import ru.maxproof.taskmanager.Subtask;
import ru.maxproof.taskmanager.Task;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class HttpHandlers {

    static class BaseHttpHandler {

        private static final Pattern pathPattern = Pattern.compile("/(\\d+)");
        protected final TaskManager manager;

        public BaseHttpHandler(TaskManager manager) {
            this.manager = manager;
        }

        void sendNotFound(HttpExchange exchange) throws IOException {
            sendEmptyResponse(exchange, 404);
        }

        void sendHasInteractions(HttpExchange exchange) throws IOException {
            sendEmptyResponse(exchange, 406);
        }

        void sendJsonResponse(HttpExchange exchange, Object responseObject, int responseCode) throws IOException {

            String responseString = TaskConverter.toJson(responseObject);
            byte[] resp = responseString.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(responseCode, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        }

        void sendEmptyResponse(HttpExchange exchange, int responseCode) throws IOException {
            exchange.sendResponseHeaders(responseCode, 0);
            exchange.close();
        }

        Optional<Integer> getPathId(HttpExchange exchange) {

            String path = exchange.getRequestURI().getPath();
            var match = pathPattern.matcher(path);
            if (match.find())
                return Optional.of(Integer.parseInt(match.group(1)));
            return Optional.empty();
        }
    }

    static class HttpTaskHandler extends BaseHttpHandler implements HttpHandler {


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
            } catch (TaskManager.NotFoundIdException e) {
                sendNotFound(exchange);
            } catch (TaskManager.OverlappingTasksException e) {
                sendHasInteractions(exchange);
            }
        }
    }

    static class HttpSubtaskHandler extends BaseHttpHandler implements HttpHandler {

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
            } catch (TaskManager.NotFoundIdException e) {
                sendNotFound(exchange);
            } catch (TaskManager.OverlappingTasksException e) {
                sendHasInteractions(exchange);
            }
        }
    }

    static class HttpEpicsHandler extends BaseHttpHandler implements HttpHandler {

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
            } catch (TaskManager.NotFoundIdException e) {
                sendNotFound(exchange);
            } catch (TaskManager.OverlappingTasksException e) {
                sendHasInteractions(exchange);
            }
        }
    }

    public static class HttpHistoryHandler extends BaseHttpHandler implements HttpHandler {

        public HttpHistoryHandler(TaskManager manager) {
            super(manager);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                var history = manager.getHistory();
                sendJsonResponse(exchange, history, 200);
            }
        }
    }

    public static class HttpPrioritizedHandler extends BaseHttpHandler implements HttpHandler {

        public HttpPrioritizedHandler(TaskManager manager) {
            super(manager);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("GET")) {
                var tasks = manager.getPrioritizedTasks();
                sendJsonResponse(exchange, tasks, 200);
            }
        }
    }


    private HttpHandlers() {}
}
