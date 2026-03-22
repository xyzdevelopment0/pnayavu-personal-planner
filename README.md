# Личный планировщик задач

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=xyzdevelopment0_pnayavu-personal-planner&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=xyzdevelopment0_pnayavu-personal-planner&branch=main)

REST-сервис на Spring Boot 3 для работы с задачами. Требования второй лабораторной по JPA реализованы внутри самого task tracker, а не отдельным учебным модулем.

## Что реализовано

1. Подключена реляционная БД PostgreSQL.
2. Модель task tracker расширена до 5 сущностей:
   - `Task`
   - `Project`
   - `User`
   - `TaskComment`
   - `Tag`
3. Реализованы связи:
   - `OneToMany`: `Project -> Task`, `Task -> TaskComment`, `User -> Project`
   - `ManyToMany`: `Task <-> Tag`
4. Для сущностей реализованы CRUD операции через REST API.
5. Продемонстрирована проблема `N+1` и решение через `@EntityGraph`.
6. Добавлен сценарий сохранения нескольких связанных сущностей с частичным сохранением без `@Transactional` и полным rollback с `@Transactional`.
7. Добавлена ER-диаграмма с PK/FK и связями.
8. Добавлены сложные GET-запросы по `Task` с фильтрацией по вложенным сущностям через JPQL и native query.
9. Для поисковых запросов добавлены пагинация `Pageable`, in-memory индекс на `HashMap` и инвалидация кеша при изменении данных.

## Стек

- Java 17
- Spring Boot 3.3.8
- Spring Web
- Spring Data JPA
- PostgreSQL
- Testcontainers + PostgreSQL для интеграционных тестов
- Maven
- Checkstyle

## Запуск PostgreSQL

```bash
docker compose up -d
```

По умолчанию:

- host: `localhost`
- port: `5432`
- db: `planner`
- user: `planner`
- password: `planner`

Переменные окружения можно переопределить через `.env` по примеру из [`.env.example`](/Users/maximovich/personal/study/4/pnayavu/personal-planner/.env.example).

## Запуск приложения

```bash
mvn spring-boot:run
```

## Проверка

```bash
mvn test
mvn verify
```

## Доменные сущности task tracker

- `Task` — основная сущность приложения
- `Project` — группирует задачи
- `User` — владелец проекта и исполнитель задачи
- `TaskComment` — комментарии к задаче
- `Tag` — метки задач

## ER-диаграмма

```mermaid
erDiagram
    USERS ||--o{ PROJECTS : owns
    USERS ||--o{ TASKS : assigned_to
    USERS ||--o{ TASK_COMMENTS : writes
    PROJECTS ||--o{ TASKS : contains
    TASKS ||--o{ TASK_COMMENTS : has
    TASKS ||--o{ TASK_TAGS : linked_with
    TAGS ||--o{ TASK_TAGS : linked_with

    USERS {
        bigint id PK
        varchar name
        varchar email UK
        timestamp created_at
        timestamp updated_at
    }

    PROJECTS {
        bigint id PK
        varchar name
        varchar description
        bigint owner_id FK
        timestamp created_at
        timestamp updated_at
    }

    TASKS {
        bigint id PK
        varchar title
        varchar description
        enum task_status
        date due_date
        bigint project_id FK
        bigint assignee_id FK
        timestamp created_at
        timestamp updated_at
    }

    TASK_COMMENTS {
        bigint id PK
        varchar content
        bigint task_id FK
        bigint author_id FK
        timestamp created_at
        timestamp updated_at
    }

    TAGS {
        bigint id PK
        varchar name UK
        timestamp created_at
        timestamp updated_at
    }

    TASK_TAGS {
        bigint task_id FK
        bigint tag_id FK
    }
```

## Обоснование `CascadeType` и `FetchType`

### `Project -> Task`

- `cascade = CascadeType.ALL`
- `orphanRemoval = true`
- `fetch = LAZY`

Почему так:

- задачи являются частью проекта;
- при удалении проекта его задачи тоже должны удаляться;
- список задач не нужен при каждом чтении проекта.

### `Task -> TaskComment`

- `cascade = CascadeType.ALL`
- `orphanRemoval = true`
- `fetch = LAZY`

Почему так:

- комментарий живёт только вместе с задачей;
- при удалении задачи комментарии тоже должны удаляться;
- комментарии нужно подгружать только по запросу.

### `Task -> Tag`

- `fetch = LAZY`
- без каскадного удаления

Почему так:

- теги переиспользуются между задачами;
- удаление задачи не должно удалять общие теги;
- теги догружаются только в запросах, где реально нужны.

### `ManyToOne` связи

- `fetch = LAZY`
- без cascade

Почему так:

- дочерние сущности не должны управлять жизненным циклом родительских;
- eager для таких связей быстро раздувает граф загрузки и число SQL-запросов.

## API

### Tasks

- `POST /api/tasks`
- `GET /api/tasks`
- `GET /api/tasks/search/jpql`
- `GET /api/tasks/search/native`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`

Пример:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Prepare ER diagram",
    "description": "Draw PK/FK relations",
    "status": "TODO",
    "dueDate": "2026-03-20",
    "projectId": 1,
    "assigneeId": 1,
    "tagIds": [1, 2]
  }'
```

Пример поиска с фильтрацией по вложенным полям `project.name` и `project.owner.email`, пагинацией и индикатором кеша:

```bash
curl -i "http://localhost:8080/api/tasks/search/jpql?projectName=laboratory&ownerEmail=alice@example.com&status=TODO&page=0&size=1"
```

В ответе будет заголовок `X-Task-Search-Cache` со значением `MISS` или `HIT`.

### Projects

- `POST /api/projects`
- `GET /api/projects`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`

### Users

- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

### Comments

- `POST /api/comments`
- `GET /api/comments`
- `GET /api/comments/{id}`
- `PUT /api/comments/{id}`
- `DELETE /api/comments/{id}`

### Tags

- `POST /api/tags`
- `GET /api/tags`
- `GET /api/tags/{id}`
- `PUT /api/tags/{id}`
- `DELETE /api/tags/{id}`

## Диагностические endpoints для JPA

Эти endpoints нужны именно для демонстрации требований лабораторной, но остаются частью task tracker.

### `N+1`

- `GET /api/tasks/diagnostics/n-plus-one`

Сценарий:

1. Загружаются проекты обычным запросом.
2. При обращении к `project.getTasks().size()` возникает `N+1`.
3. Затем выполняется оптимизированный запрос с `@EntityGraph(attributePaths = "tasks")`.
4. В ответе возвращается число SQL-запросов до и после оптимизации.

### Транзакции

- `POST /api/tasks/diagnostics/transactions/without-transaction`
- `POST /api/tasks/diagnostics/transactions/with-transaction`

Первый endpoint показывает частичное сохранение связанных сущностей без общей транзакции. Второй показывает полный rollback той же операции внутри `@Transactional`.

## Тесты

Тест [TaskDiagnosticsIntegrationTest](/Users/maximovich/personal/study/4/pnayavu/personal-planner/src/test/java/com/maximovich/planner/task/diagnostics/TaskDiagnosticsIntegrationTest.java) проверяет:

- уменьшение количества запросов после `@EntityGraph`
- частичное сохранение без `@Transactional`
- полный rollback с `@Transactional`
