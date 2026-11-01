# user-service API

Статус: WIP. Сервис в разработке; ниже описан текущий API, контракт может меняться.

Хранит профили пользователей. Адрес в сети Compose: `http://user-service:8080`. Через gateway сервис не опубликован; публичных методов пока нет.

## Создание профиля

`POST /internal/users`

Заголовки:

```http
Authorization: Bearer <service-access-token>
Content-Type: application/json
```

Нужен OAuth2-токен от authorization-server со scope `profiles:create`, `iss=http://authorization-server:8080` и `aud=editorial-user-service`. Пользовательский токен из auth-service не подходит.

```json
{
  "accountId": 7,
  "displayName": "ivan"
}
```

`accountId` — числовой ID аккаунта, уникальный для профиля. `displayName` — строка до 100 символов по модели. Проверка тела через Bean Validation не подключена.

При регистрации auth-service вызывает этот метод сам, передавая username как displayName.

## Ответы

| HTTP | Результат |
| --- | --- |
| 201 | Создан профиль; контроллер возвращает объект `User` |
| 401 | Нет подходящего токена или токен не прошёл проверку |
| 403 | Нет scope `profiles:create` |
| 409 | Профиль с таким accountId уже существует |

Тело ошибки `409`:

```json
{
  "code": "USER_ALREADY_EXISTS",
  "message": "User with this account ID already exists",
  "errors": null
}
```

Формат тела успешного ответа пока не зафиксирован.
