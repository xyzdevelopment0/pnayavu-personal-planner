# Личный планировщик задач.

Небольшой REST-сервис на Spring Boot для работы с задачами (`Task`).

## Стек

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 Database (in-memory)
- Maven
- Checkstyle

## Как запустить

Требуется: `Java 17+` и `Maven`.

```bash
mvn clean spring-boot:run
```

После старта сервис доступен на `http://localhost:8080`.

## Проверка стиля и сборки

```bash
mvn clean verify
```

Checkstyle конфиг: `checkstyle.xml`.

## API

### 1) Создание задачи

`POST /api/tasks`

Пример:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish lab work",
    "description": "Prepare REST demo",
    "status": "TODO",
    "dueDate": "2026-03-01"
  }'
```

### 2) Получение задачи по id

`GET /api/tasks/{id}`

```bash
curl http://localhost:8080/api/tasks/1
```

### 3) Получение списка задач с фильтрами

`GET /api/tasks?status=TODO&name=Finish%20lab%20work`

```bash
curl --get "http://localhost:8080/api/tasks?status=TODO&name=Finish%20lab%20work"
```

Параметры:
- `status` — `TODO`, `IN_PROGRESS`, `DONE` (необязательный)
- `name` — точный поиск по имени задачи (необязательный)

## Структура проекта

```text
src/main/java/com/maximovich/planner
  common
  task
    controller
    service
    repository
    domain
    dto
    mapper
```
## Sonar Cloud

https://sonarcloud.io/component_measures?id=xyzdevelopment0_pnayavu-personal-planner&metric=duplicated_lines_density&view=list
