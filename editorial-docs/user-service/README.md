# user-service API

Профиль текущего пользователя доступен через gateway по адресу `http://localhost:8080/api/users/me`. В сети Compose сервис доступен по адресу `http://user-service:8080`.

Методы `/api/users/me` требуют `Authorization: Bearer <accessToken>` от auth-service. User-service определяет аккаунт по `sub` токена.

| Метод | Путь | Успешный ответ |
| --- | --- | --- |
| GET | `/api/users/me` | `200` с тегом, отображаемым именем, датой создания, ID аккаунта и описанием |
| PATCH | `/api/users/me` | `200` с текущими значениями отображаемого имени и описания |

## Чтение профиля

Пример ответа на `GET /api/users/me` сразу после активации аккаунта.

```json
{
  "username": "ivan",
  "displayName": null,
  "createdAt": "2026-11-05T12:00:00Z",
  "accountId": 7,
  "profileDescription": null
}
```

## Редактирование профиля

Для `PATCH /api/users/me` укажите `Content-Type: application/json` и передайте одно или оба поля.

```json
{
  "displayName": "Иван Иванов",
  "profileDescription": "Пишу о разработке."
}
```

Ответ содержит `displayName` и `profileDescription` после обновления. Оба поля также входят в ответ чтения профиля и в список профилей. Переданная строка сохраняется, в том числе пустая. Отсутствующее поле и явный `null` означают пропуск обновления этого поля.

## Управление профилями

| Метод | Путь | Доступ | Ответ `200` |
| --- | --- | --- | --- |
| GET | `/api/admin/users` | JWT с ролью `ADMIN` | Страница профилей |
| PATCH | `/api/admin/users/{accountId}` | JWT с ролью `ADMIN` | Текущие `displayName` и `profileDescription` указанного аккаунта |

Список принимает `page` (по умолчанию 0) и `size` (по умолчанию 20). Номер страницы отсчитывается от 0, размер должен быть положительным. Профили сортируются по возрастанию внутреннего ID. Ответ представляет собой Spring Data `Page`, элементы в `content` имеют тот же формат, что ответ `GET /api/users/me`.

PATCH ищет профиль по ID аккаунта, переданному в пути. Тело и результат совпадают с `PATCH /api/users/me`.

## Внутренний API профилей

Article-service получает отображаемые имена авторов статей и комментариев через `POST http://user-service:8080/api/internal/users/batch`. В текущей конфигурации user-service этот маршрут доступен с `permitAll()`. Gateway маршрутизирует внешние запросы к профилям по префиксам `/api/users/**` и `/api/admin/users/**`, внутренний API вызывается напрямую в сети Compose.

Тело содержит набор ID аккаунтов.

```json
{
  "accountIds": [7, 8]
}
```

Успешный ответ имеет статус `200` и содержит массив найденных профилей.

```json
[
  {"accountId": 7, "displayName": "Иван Иванов"},
  {"accountId": 8, "displayName": null}
]
```

Профиль с незаполненным именем возвращается с `displayName: null`. Отсутствующие профили пропускаются. Порядок элементов задаётся результатом выборки, сопоставлять их с запросом нужно по `accountId`.

## Поля и миграции

| Поле | Назначение |
| --- | --- |
| `username` | Обязательный тег, полученный от auth-service при регистрации. В базе профилей хранится как `TEXT` без ограничения длины. |
| `displayName` | Отображаемое имя, например ФИО. При создании профиля имеет значение `null`. |
| `profileDescription` | Описание профиля. При создании профиля имеет значение `null`. |

Flyway применяет [V4](../../user-service/src/main/resources/db/migration/V4__split_username_and_display_name.sql), которая переносит прежние значения `display_name` в `username` и добавляет новую колонку для отображаемого имени. [V5](../../user-service/src/main/resources/db/migration/V5__add_profile_description.sql) добавляет nullable-колонку `profile_description` типа `TEXT`.

Профиль создаётся по [событию Kafka](../auth-service/README.md#создание-профиля-через-kafka). После записи профиля user-service отправляет подтверждение для активации аккаунта.

## Ошибки

| HTTP | Причина |
| --- | --- |
| 401 | JWT отсутствует или не прошёл проверку |
| 403 | JWT прошёл проверку, но для административного маршрута требуется `ADMIN` |
| 404 | Профиль аккаунта не найден. Код ошибки `USER_NOT_FOUND`. |

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User profile couldn't be found",
  "errors": null
}
```
