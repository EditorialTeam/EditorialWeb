# user-service API

Профиль текущего пользователя доступен через gateway по адресу `http://localhost:8080/api/users/me`. В сети Compose сервис доступен по адресу `http://user-service:8080`.

Методы `/api/users/me` требуют `Authorization: Bearer <accessToken>` от auth-service. User-service определяет аккаунт по `sub` токена.

| Метод | Путь | Успешный ответ |
| --- | --- | --- |
| GET | `/api/users/me` | `200` с тегом, отображаемым именем, датой создания и ID аккаунта |
| PATCH | `/api/users/me` | `200` с текущими значениями отображаемого имени и описания |

## Чтение профиля

Пример ответа на `GET /api/users/me` сразу после активации аккаунта.

```json
{
  "username": "ivan",
  "displayName": null,
  "createdAt": "2026-11-05T12:00:00Z",
  "accountId": 7
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

Ответ содержит `displayName` и `profileDescription` после обновления. Описание профиля доступно в ответе PATCH. Переданная строка сохраняется, в том числе пустая. Отсутствующее поле и явный `null` означают пропуск обновления этого поля.

## Управление профилями

| Метод | Путь | Фактическое требование доступа | Ответ `200` |
| --- | --- | --- | --- |
| GET | `/api/admin/users` | JWT с ролью `ADMIN` | Страница профилей |
| PATCH | `/api/admin/users/{accountId}` | `permitAll()` | Текущие `displayName` и `profileDescription` указанного аккаунта |

**Текущее ограничение доступа.** Правило `ADMIN` привязано к точному пути `/api/admin/users`. Путь редактирования содержит ID аккаунта и попадает под `permitAll()`.

Список принимает `page` (по умолчанию 0) и `size` (по умолчанию 20). Номер страницы отсчитывается от 0, размер должен быть положительным. Профили сортируются по возрастанию внутреннего ID. Ответ представляет собой Spring Data `Page`, элементы в `content` имеют тот же формат, что ответ `GET /api/users/me`.

PATCH ищет профиль по ID аккаунта, переданному в пути. Тело и результат совпадают с `PATCH /api/users/me`.

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
| 401 | Для `/api/users/me` JWT отсутствует или не прошёл проверку |
| 404 | Профиль аккаунта не найден. Код ошибки `USER_NOT_FOUND`. |

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User profile couldn't be found",
  "errors": null
}
```
