# java-kanban
Repository for homework project.

## Проект трекера задач ##

Серверная часть имеет следующие точки входа:

| Path                 | method | description                       | success code | error code | response body   |
|----------------------|:------:|-----------------------------------|:------------:|:----------:|-----------------|
| /tasks               |  GET   | Получение списка задач            |     200      |     -      | List\<Task\>    |
| /tasks/{id}          |  GET   | Получение задачи по Id            |     200      |    404     | Task            |
| /tasks               |  POST  | Создание задачи (id=0)            |     201      |    406     | Task            |
| /tasks               |  POST  | Обновление задачи (id>0)          |     201      |    406     |                 |
| /tasks/{id}          | DELETE | Удаление задачи                   |     200      |     -      |                 |
| /subtasks            |  GET   | Получение списка подзадач         |     200      |     -      | List\<Subtask\> |
| /subtasks/{id}       |  GET   | Получение подзадачи по Id         |     200      |    404     | Subtask         |
| /subtasks            |  POST  | Создание подзадачи (id=0)         |     201      |    406     | Subtask         |
| /subtasks            |  POST  | Обновление подзадачи (id>0)       |     201      |    406     |                 |
| /subtasks/{id}       | DELETE | Удаление подзадачи                |     200      |     -      |                 |
| /epics               |  GET   | Получение списка эпиков           |     200      |     -      | List\<Epic\>    |
| /epics/{id}          |  GET   | Получение эпика по Id             |     200      |    404     | Epic            |
| /epics/{id}/subtasks |  GET   | Получение подзадач эпика по Id    |     200      |    404     | List\<Subtask\> |
| /epics               |  POST  | Создание эпика (id=0)             |     201      |     -      | Epic            |
| /epics               |  POST  | Обновление эпика (id>0)           |     201      |     -      |                 |
| /epics/{id}          | DELETE | Удаление эпика                    |     200      |     -      |                 |
| /history             |  GET   | Получения истории запросов        |     200      |     -      | List\<Task\>    |
| /prioritized         |  GET   | Получение списка задач по времени |     200      |     -      | List\<Task\>    |

