# article-service API

Через gateway запросы идут на `http://localhost:8080`. Внутренний адрес сервиса в Compose — `http://article-service:8080`. Все маршруты ниже требуют `Authorization: Bearer <accessToken>` от auth-service. Сервис берёт ID аккаунта из `sub`, роль — из `role`.

| Метод | Путь | Доступ по текущему коду | Успешный ответ |
| --- | --- | --- | --- |
| GET | `/api/articles` | Любая роль с токеном. Фильтры действуют независимо от роли | `200`, страница статей |
| GET | `/api/articles/my` | Любая роль с токеном. Только статьи своего аккаунта | `200`, страница статей |
| GET | `/api/articles/{id}` | `PUBLISHED` доступен всем с токеном. `ADMIN` видит любую статью, `AUTHOR` — свою в любом статусе, `EDITOR` — также `PENDING_REVIEW` | `200`, статья |
| POST | `/api/articles` | `AUTHOR`, `EDITOR`, `ADMIN`. Создаёт статью со своим ID аккаунта | `201`, статья в `DRAFT` |
| PUT, PATCH | `/api/articles/{id}` | `AUTHOR` для своих `DRAFT` и `REJECTED`. `EDITOR` и `ADMIN` для любых `DRAFT`, `REJECTED` и `PENDING_REVIEW` | `200`, обновлённая статья |
| DELETE | `/api/articles/{id}` | `AUTHOR` для своих статей вне `PENDING_REVIEW`. `EDITOR` и `ADMIN` для любых статей | `204` |
| POST | `/api/articles/{id}/submit` | `AUTHOR`, `EDITOR` или `ADMIN`. Аккаунт должен владеть статьёй | `204` |
| POST | `/api/articles/{id}/withdraw-from-review` | `AUTHOR`, `EDITOR` или `ADMIN`. Проверка в сервисе отклоняет `AUTHOR` для собственной статьи в `PENDING_REVIEW` | `204` |
| POST | `/api/articles/{id}/archive` | `AUTHOR` для своей статьи, `EDITOR` и `ADMIN` для любой | `204` |
| GET | `/api/editor/articles` | `EDITOR` и `ADMIN` | `200`, страница статей в `PENDING_REVIEW` |
| POST | `/api/editor/articles/{id}/moderate` | `EDITOR` и `ADMIN` | `204` |

## Список статей

`GET /api/articles` сортирует статьи по возрастанию `id`.

| Параметр | По умолчанию | Назначение |
| --- | --- | --- |
| `page` | `0` | Номер страницы от 0 |
| `size` | `20` | Размер страницы от 1 до 100 |
| `status` | Любой статус | Точное значение `DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, `REJECTED` или `ARCHIVED` |
| `authorAccountId` | Любой автор | Числовой ID аккаунта автора |

Для ленты опубликованных статей передавайте `status=PUBLISHED`. Если оба фильтра заданы, сервис применяет их одновременно. Общий список использует фильтры запроса, а чтение по ID применяет отдельные правила видимости из таблицы доступа.

`GET /api/articles/my` фильтрует по `sub` и сортирует по времени создания от старых статей к новым. `GET /api/editor/articles` выбирает только `PENDING_REVIEW` и сортирует по времени создания, затем по `id`. Для обоих списков `page` и `size` по умолчанию равны `0` и `20`. Ограничение `size` до 100 задано на общем и персональном списках.

Списки возвращают Spring Data `Page`. Массив `content` содержит статьи, а поля `number`, `size`, `totalElements` и `totalPages` описывают пагинацию.

## Тело запроса

Для создания через `POST` и полного обновления через `PUT` передайте оба поля.

```json
{
  "title": "Заголовок",
  "content": "Текст статьи"
}
```

`title` должен содержать до 200 символов. Оба поля обязательны и должны содержать хотя бы один непробельный символ. Сервис присваивает автора при создании статьи.

Для `PATCH` можно передать одно или оба поля. Отсутствующее поле и явный `null` сохраняют предыдущее значение. Для переданного `title` требуется от 1 до 200 символов, для `content` — хотя бы один символ. Строки из пробелов проходят проверку `PATCH`. Пустой объект `{}` допустим и обновляет внутреннее поле `updatedAt` без изменения текста.

## Редакционный цикл

| Операция | Исходный статус | Результат |
| --- | --- | --- |
| Создание | — | `DRAFT` |
| `submit` | `DRAFT`, `REJECTED` или `ARCHIVED` | `PENDING_REVIEW`. Прежняя причина отклонения сбрасывается |
| `moderate?decision=APPROVED` | `PENDING_REVIEW` | `PUBLISHED`. Сервис записывает время публикации и сбрасывает причину отклонения |
| `moderate?decision=REJECTED` | `PENDING_REVIEW` | `REJECTED`. Сервис сохраняет переданную причину |
| `archive` | `PUBLISHED` | `ARCHIVED` |
| `withdraw-from-review` | В текущем коде исходный статус отдельно не проверяется | `DRAFT`, если проверка доступа из таблицы пройдена |

`submit`, `archive` и `withdraw-from-review` не требуют тела запроса. Для модерации передайте `decision` в строке запроса. При отклонении можно передать тело с причиной, например `{"rejectionReason":"Добавьте ссылки на источники"}`. Тело и причина необязательны.

## Ответ со статьёй

`GET /api/articles` и `GET /api/articles/{id}` возвращают `GetArticlesResponse`. В общем списке объекты находятся в `content`.

```json
{
  "articleId": 1,
  "authorId": 7,
  "title": "Заголовок",
  "content": "Текст статьи",
  "authorDisplayName": "Иван Иванов",
  "status": "PUBLISHED",
  "createdAt": "2026-11-12T10:00:00Z",
  "publishedAt": "2026-11-12T11:00:00Z"
}
```

`authorId` содержит ID аккаунта автора. Фильтр общего списка по-прежнему называется `authorAccountId`. Поле `publishedAt` может содержать `null`.

Имена авторов article-service получает у user-service через `POST /api/internal/users/batch`. Для страницы списка собирается набор уникальных ID авторов и выполняется один пакетный запрос. При получении статьи по ID передаётся ID аккаунта её автора. Запрос идёт напрямую на `http://user-service:8080`.

Если профиль отсутствует, отображаемое имя равно `null` или запрос завершается ошибкой `RestClientException`, статья возвращается с `authorDisplayName: null`. При пустом наборе ID запрос к user-service пропускается. [Внутренний API профилей](../user-service/README.md#внутренний-api-профилей)

`GET /api/articles/my`, `GET /api/editor/articles`, создание и обновление возвращают `Article` со следующими полями.

```json
{
  "id": 1,
  "title": "Заголовок",
  "content": "Текст статьи",
  "authorAccountId": 7,
  "status": "DRAFT",
  "createdAt": "2026-11-12T10:00:00Z",
  "updatedAt": null,
  "publishedAt": null
}
```

При создании сервис записывает `createdAt`, при `PUT` и `PATCH` — `updatedAt`, при одобрении — `publishedAt`. Причина отклонения хранится в базе, текущие ответы со статьёй её не содержат.

## Ошибки

| HTTP | `code` | Причина |
| --- | --- | --- |
| 400 | `Validation error` | Тело запроса не прошло проверку полей |
| 400 | `INVALID_JSON` | Некорректное тело JSON |
| 401 | — | Токен отсутствует или не прошёл проверку |
| 403 | — | Роль отклонена на уровне Spring Security |
| 403 | `NOT_ENOUGH_RIGHTS` | Операция отклонена проверкой в article-service |
| 404 | `ARTICLE_NOT_FOUND` | Статья не найдена |
| 409 | `INCORRECT_ARTICLE_STATUS` | Операция недоступна для текущего статуса |

Обработанные сервисом ошибки содержат поля `code`, `message` и `errors`. Для ошибок полей `errors` содержит сообщения по каждому полю, в остальных случаях возвращается `{}` или `null`.
