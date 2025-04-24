package ru.maxproof.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.maxproof.server.TaskConverter;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

public class BaseHttpHandler {

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
