package ru.maxproof.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.maxproof.taskmanager.TaskManager;

import java.io.IOException;

public class HttpPrioritizedHandler extends BaseHttpHandler implements HttpHandler {

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
