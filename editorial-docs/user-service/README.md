# user-service API

Профиль текущего пользователя доступен через gateway по адресу `http://localhost:8080/api/users/me`. В сети Compose сервис доступен по адресу `http://user-service:8080`.

Оба метода требуют `Authorization: Bearer <accessToken>` от auth-service. User-service определяет аккаунт по `sub` токена.

| Метод | Путь | Успешный ответ |
| --- | --- | --- |
| GET | `/api/users/me` | `200` с тегом, отображаемым именем и датой создания |
| PATCH | `/api/users/me` | `200` с текущими значениями отображаемого имени и описания |

## Чтение профиля

Пример ответа на `GET /api/users/me` сразу после активации аккаунта.

```json
{
  "username": "ivan",
  "displayName": null,
  "createdAt": "2026-11-05T12:00:00Z"
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
| 404 | Профиль аккаунта не найден. Код ошибки `USER_NOT_FOUND`. |

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User profile couldn't be found",
  "errors": null
}
```
