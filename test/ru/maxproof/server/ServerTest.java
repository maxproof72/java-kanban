package ru.maxproof.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.maxproof.taskmanager.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    HttpTaskServer server;
    HttpClient client;


    @BeforeEach
    void setUp() throws IOException {
        server = new HttpTaskServer();
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.stop();
    }

    record RequestResult<T>(int code, T object) {}

    private HttpResponse<String> sendRequest(String method, String path, Object data)
            throws IOException, InterruptedException {

        URI uri = URI.create("http://localhost:8080" + path);
        var requestBuilder = HttpRequest.newBuilder(uri);
        requestBuilder.version(HttpClient.Version.HTTP_1_1);
        requestBuilder.setHeader("Content-Type", "application/json");
        requestBuilder.setHeader("Accept", "application/json");
        if (data != null) {
            String json = TaskConverter.toJson(data);
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        HttpRequest request = requestBuilder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private <T> T extractObject(HttpResponse<String> response, Class<T> clazz) {

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            var body = response.body();
            if (body != null)
                return TaskConverter.fromJson(body, clazz);
        }
        return null;
    }

    private <T> List<T> extractList(HttpResponse<String> response, Class<T> clazz) {

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            var body = response.body();
            if (body != null)
                return TaskConverter.listFromJson(body, clazz);
        }
        return null;
    }

    private <T> RequestResult<T> makeObjectRequest(String method, String path, Object data, Class<T> clazz)
            throws IOException, InterruptedException {

        HttpResponse<String> response = sendRequest(method, path, data);
        return new RequestResult<>(response.statusCode(), extractObject(response, clazz));
    }

    private <T> RequestResult<List<T>> makeListRequest(String path, Class<T> clazz)
            throws IOException, InterruptedException {

        HttpResponse<String> response = sendRequest("GET", path, null);
        return new RequestResult<>(response.statusCode(), extractList(response, clazz));
    }



    @Test
    void taskHandlerTest() throws IOException, InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        RequestResult<Task> rr;

        // Создание задачи 1
        Task task1 = new TaskBuilder()
                .setName("T1")
                .setDescription("task 1")
                .setStartTime(now)
                .setDuration(Duration.ofHours(1))
                .buildTask();
        rr = makeObjectRequest("POST", "/tasks", task1, Task.class);
        assertEquals(201, rr.code());
        task1 = rr.object();
        assertNotEquals(TaskManager.DRAFT_TASK_ID,task1.getId());

        // Запрос созданной задачи по ее ID
        rr = makeObjectRequest("GET", "/tasks/" + task1.getId(), null, Task.class);
        assertEquals(200, rr.code());
        assertEquals(task1, rr.object());
        assertEquals(TaskStatus.NEW, task1.getStatus());

        // Запрос задачи по неправильному ID
        rr = makeObjectRequest("GET", "/tasks/999", null, Task.class);
        assertEquals(404, rr.code());

        // Проверка задач с пересекающимся временем выполнения
        Task task2 = new TaskBuilder()
                .setName("T2")
                .setDescription("task 2")
                .setStartTime(LocalDateTime.now().plusMinutes(30))
                .setDuration(Duration.ofHours(1))
                .buildTask();
        rr = makeObjectRequest("POST", "/tasks", task2, Task.class);
        assertEquals(406, rr.code());

        // Проверка задач с непересекающимся временем выполнения
        task2 = new TaskBuilder(task2)
                .setStartTime(LocalDateTime.now().minusDays(2))
                .buildTask();
        rr = makeObjectRequest("POST", "/tasks", task2, Task.class);
        assertEquals(201, rr.code());
        task2 = rr.object();

        // Проверка задачи без указания времени выполнения
        Task task3 = new TaskBuilder()
                .setName("T3")
                .setDescription("task 3")
                .buildTask();
        rr = makeObjectRequest("POST", "/tasks", task3, Task.class);
        assertEquals(201, rr.code());
        task3 = rr.object();

        // Проверка обновления задачи
        task3 = new TaskBuilder(task3)
                .setStatus(TaskStatus.IN_PROGRESS)
                .buildTask();
        rr = makeObjectRequest("POST", "/tasks", task3, Task.class);
        assertEquals(201, rr.code());
        rr = makeObjectRequest("GET", "/tasks/" + task3.getId(), null, Task.class);
        assertEquals( TaskStatus.IN_PROGRESS, rr.object().getStatus());

        // Проверка перечня задач
        RequestResult<List<Task>> rl = makeListRequest("/tasks", Task.class);
        assertEquals(200, rl.code());
        assertEquals(3, rl.object().size());

        // Проверка перечня задач на временной шкале
        rl = makeListRequest("/prioritized", Task.class);
        assertEquals(200, rl.code());
        assertEquals(2, rl.object().size());
        assertEquals(List.of(task2.getId(), task1.getId()),
                rl.object().stream().map(Task::getId).toList());

        // Удаление задачи 1 и проверка перечня задач
        rr = makeObjectRequest("DELETE", "/tasks/" + task1.getId(), null, Task.class);
        assertEquals(200, rr.code());
        rl = makeListRequest("/tasks", Task.class);
        assertEquals(200, rl.code());
        assertEquals(2, rl.object().size());
    }

    @Test
    void subtaskHandlerTest() throws IOException, InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        RequestResult<Subtask> rr;

        // Создание эпика
        Epic epic = new TaskBuilder()
                .setName("epic1")
                .buildEpic();
        RequestResult<Epic> re = makeObjectRequest("POST", "/epics", epic, Epic.class);
        assertEquals(201, re.code());
        int epicId = re.object().getId();
        assertNotEquals(TaskManager.DRAFT_TASK_ID, epicId);

        // Создание подзадачи 1
        Subtask subtask1 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("abc")
                .setDescription("def")
                .setStartTime(now)
                .setDuration(Duration.ofHours(1))
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask1, Subtask.class);
        assertEquals(201, rr.code());
        subtask1 = rr.object();
        assertNotEquals(TaskManager.DRAFT_TASK_ID, subtask1.getId());
        assertEquals(TaskStatus.NEW, subtask1.getStatus());

        // Запрос созданной подзадачи по ее ID
        rr = makeObjectRequest("GET", "/subtasks/" + subtask1.getId(), null, Subtask.class);
        assertEquals(200, rr.code());
        assertEquals(subtask1, rr.object());
        assertEquals(TaskStatus.NEW, subtask1.getStatus());

        // Проверка задач с пересекающимся временем выполнения
        Subtask subtask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("T2")
                .setDescription("task 2")
                .setStartTime(LocalDateTime.now().plusMinutes(30))
                .setDuration(Duration.ofHours(1))
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask2, Subtask.class);
        assertEquals(406, rr.code());

        // Проверка задач с непересекающимся временем выполнения
        subtask2 = new TaskBuilder(subtask2)
                .setStartTime(LocalDateTime.now().minusDays(2))
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask2, Subtask.class);
        assertEquals(201, rr.code());
        subtask2 = rr.object();

        // Проверка задачи без указания времени выполнения
        Subtask subtask3 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("T3")
                .setDescription("task 3")
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask3, Subtask.class);
        assertEquals(201, rr.code());
        subtask3 = rr.object();

        // Проверка обновления задачи
        subtask3 = new TaskBuilder(subtask3)
                .setStatus(TaskStatus.IN_PROGRESS)
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask3, Subtask.class);
        assertEquals(201, rr.code());
        rr = makeObjectRequest("GET", "/subtasks/" + subtask3.getId(), null, Subtask.class);
        assertEquals( TaskStatus.IN_PROGRESS, rr.object().getStatus());

        // Проверка невозможности создания подзадачи с неправильным Id эпика
        int code404 = makeObjectRequest(
                "POST",
                "/subtasks",
                new TaskBuilder().setEpicId(999).setName("WrongSubtask").buildSubtask(),
                Subtask.class
                ).code();
        assertEquals(404, code404);

        // Проверка перечня подзадач
        RequestResult<List<Subtask>> r1 = makeListRequest("/subtasks", Subtask.class);
        assertEquals(200, r1.code());
        assertEquals(3, r1.object().size());

        // Проверка перечня подзадач на временной шкале
        RequestResult<List<Task>> r2 = makeListRequest("/prioritized", Task.class);
        assertEquals(200, r2.code());
        assertEquals(2, r2.object().size());
        assertEquals(List.of(subtask2.getId(), subtask1.getId()),
                r2.object().stream().map(Task::getId).toList());

        // Удаление подзадачи 1 и проверка перечня подзадач
        rr = makeObjectRequest("DELETE", "/subtasks/" + subtask1.getId(), null, Subtask.class);
        assertEquals(200, rr.code());
        r1 = makeListRequest("/subtasks", Subtask.class);
        assertEquals(200, r1.code());
        assertEquals(2, r1.object().size());
    }

    @Test
    void epicHandlerTest() throws IOException, InterruptedException {

        LocalDateTime now = LocalDateTime.now();
        RequestResult<Subtask> rr;
        RequestResult<Epic> re;

        // Создание эпика
        Epic epic = new TaskBuilder()
                .setName("epic1")
                .buildEpic();
        re = makeObjectRequest("POST", "/epics", epic, Epic.class);
        assertEquals(201, re.code());
        int epicId = re.object().getId();
        assertNotEquals(TaskManager.DRAFT_TASK_ID, epicId);

        // Создание подзадачи 1
        Subtask subtask1 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("sub1")
                .setDescription("subtask 1")
                .setStartTime(now)
                .setDuration(Duration.ofHours(1))
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask1, Subtask.class);
        assertEquals(201, rr.code());
        subtask1 = rr.object();
        assertNotEquals(TaskManager.DRAFT_TASK_ID, subtask1.getId());
        assertEquals(TaskStatus.NEW, subtask1.getStatus());

        // Создание подзадачи 2
        Subtask subtask2 = new TaskBuilder()
                .setEpicId(epicId)
                .setName("sub2")
                .setDescription("subtask 2")
                .setStartTime(now.plusDays(1))
                .setDuration(Duration.ofHours(1))
                .buildSubtask();
        rr = makeObjectRequest("POST", "/subtasks", subtask2, Subtask.class);
        assertEquals(201, rr.code());
        subtask2 = rr.object();
        assertNotEquals(TaskManager.DRAFT_TASK_ID, subtask2.getId());
        assertEquals(TaskStatus.NEW, subtask2.getStatus());

        // Запрос и проверка эпика
        re = makeObjectRequest("GET", "/epics/" + epicId, null, Epic.class);
        assertEquals(200, re.code());
        epic = re.object();
        assertEquals(List.of(subtask1.getId(), subtask2.getId()), epic.getSubtasks());

        // Запрос и проверка подзадач эпика
        var epicSubtasks = makeListRequest("/epics/" + epicId + "/subtasks", Subtask.class);
        assertEquals(200, epicSubtasks.code());
        assertEquals(2, epicSubtasks.object().size());
        assertEquals(subtask1, epicSubtasks.object().getFirst());
        assertEquals(subtask2, epicSubtasks.object().getLast());

        // Добавление еще одного эпика и проверка списка эпиков
        Epic epic2 = new TaskBuilder().setName("epic2").buildEpic();
        re = makeObjectRequest("POST", "/epics", epic2, Epic.class);
        assertEquals(201, re.code());
        var epics = makeListRequest("/epics", Epic.class);
        assertEquals(200, epics.code());
        assertEquals(2, epics.object().size());

        // Удаление эпика 1 и проверка перечня эпиков и подзадач
        re = makeObjectRequest("DELETE", "/epics/" + epic.getId(), null, Epic.class);
        assertEquals(200, re.code());
        var r1 = makeListRequest("/subtasks", Subtask.class);
        assertEquals(200, r1.code());
        assertTrue(r1.object().isEmpty());
        var r2 = makeListRequest("/epics", Epic.class);
        assertEquals(200, r2.code());
        assertEquals(1, r2.object().size());
    }

    @Test
    void historyHandlerTest() throws IOException, InterruptedException {

        // Проверка пустой истории
        var rh = makeListRequest("/history", Task.class);
        assertEquals(200, rh.code());
        assertTrue(rh.object().isEmpty());

        // Создание задачи 1
        Task task1 = new TaskBuilder().setName("T1").buildTask();
        task1 = makeObjectRequest("POST", "/tasks", task1, Task.class).object();

        // Создание задачи 2
        Task task2 = new TaskBuilder().setName("T2").buildTask();
        task2 = makeObjectRequest("POST", "/tasks", task2, Task.class).object();

        // Создание задачи 3
        Task task3 = new TaskBuilder().setName("T3").buildTask();
        task3 = makeObjectRequest("POST", "/tasks", task3, Task.class).object();

        // Запрос задач в порядке 3 - 1 - 2
        makeObjectRequest("GET", "/tasks/" + task3.getId(), null, Task.class);
        makeObjectRequest("GET", "/tasks/" + task1.getId(), null, Task.class);
        makeObjectRequest("GET", "/tasks/" + task2.getId(), null, Task.class);

        // Проверка истории
        rh = makeListRequest("/history", Task.class);
        assertEquals(200, rh.code());
        assertEquals(3, rh.object().size());
        assertEquals(List.of(task3.getId(), task1.getId(), task2.getId()),
                rh.object().stream().map(Task::getId).toList());
    }

    @Test
    void prioritizedHandlerTest() throws IOException, InterruptedException {

        // Создание задачи 1
        Task task1 = new TaskBuilder()
                .setName("T1")
                .setStartTime(LocalDateTime.now().plusHours(1))
                .setDuration(Duration.ofHours(1))
                .buildTask();
        task1 = makeObjectRequest("POST", "/tasks", task1, Task.class).object();

        // Создание задачи 2
        Task task2 = new TaskBuilder()
                .setName("T2")
                .setStartTime(LocalDateTime.now().plusHours(3))
                .setDuration(Duration.ofHours(2))
                .buildTask();
        task2 = makeObjectRequest("POST", "/tasks", task2, Task.class).object();

        // Создание задачи 3
        Task task3 = new TaskBuilder()
                .setName("T3")
                .setStartTime(LocalDateTime.now())
                .setDuration(Duration.ofMinutes(5))
                .buildTask();
        task3 = makeObjectRequest("POST", "/tasks", task3, Task.class).object();

        // Задачи выполняются в порядке 3 - 1 - 2
        var rh = makeListRequest("/prioritized", Task.class);
        assertEquals(200, rh.code());
        assertEquals(3, rh.object().size());
        assertEquals(List.of(task3.getId(), task1.getId(), task2.getId()),
                rh.object().stream().map(Task::getId).toList());
    }
}
