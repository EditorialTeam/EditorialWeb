# auth-service API

Внешний адрес: `http://localhost:8080/api/auth`. В сети Compose: `http://auth-service:8080`.

Все перечисленные методы доступны без `Authorization`.

| Метод | Путь | Тело запроса | Успешный ответ |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | `username`, `email`, `password` | `201`: `username`, `email`, `createdAt` |
| POST | `/api/auth/login` | `email`, `password` | `200`: `refreshToken`, `accessToken`, `tokenType`, `expiresIn` |
| POST | `/api/auth/refresh` | `refreshToken` | `200`: `accessToken`, `refreshToken` |
| POST | `/api/auth/logout` | `refreshToken` | `204`, без тела |
| GET | `/.well-known/jwks.json` | — | `200`: публичные ключи в формате JWKS (`keys`) |

Все поля запросов — строки. JWKS доступен напрямую внутри сети, маршрут через gateway для него не задан.

## Регистрация и вход

```json
{
  "username": "ivan",
  "email": "ivan@example.com",
  "password": "example-password"
}
```

`username` обязателен, до 100 символов; `email` обязателен, должен быть корректным адресом, до 254 символов; `password` — от 6 до 100 символов. Email при регистрации и входе приводится к нижнему регистру, пробелы по краям удаляются. Email и username уникальны.

Регистрация создаёт аккаунт с ролью `USER` и профиль в user-service. Токены возвращаются при входе:

```json
{
  "refreshToken": "<refresh-token>",
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Access token действует 15 минут: `iss=editorial-auth-service`, `aud=editorial-api`, `sub` — ID аккаунта, `role` — его роль. Роль `USER` не даёт права изменять статьи; API смены роли нет.

## Обновление и выход

Для обоих запросов тело одинаковое:

```json
{"refreshToken": "<refresh-token>"}
```

Refresh token действует 30 дней. При обновлении старый удаляется, клиент должен сохранить новую пару токенов. Выход удаляет переданный refresh token; уже выданный access token действует до истечения срока.

## Ошибки

| HTTP | `code` | Причина |
| --- | --- | --- |
| 400 | `INVALID_CREDENTIALS` | Неверный email или пароль |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh token не найден, истёк или отозван |
| 409 | `CONFLICT` | Username или email уже занят |

Эти ошибки возвращаются как `{"code":"...","message":"...","errors":null}`. Для ошибок валидации и некорректного JSON отдельный обработчик не задан.

