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

Ответ `201 Created` подтверждает, что аккаунт с ролью `USER` и событие создания профиля сохранены. Профиль создаётся асинхронно, а вход доступен после активации аккаунта. Попытка входа до активации возвращает `400 INVALID_CREDENTIALS`.

Пример ответа при успешном входе:

```json
{
  "refreshToken": "<refresh-token>",
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

JWT содержит `iss=editorial-auth-service`, `aud=editorial-api`, числовой ID аккаунта в строковом `sub` и роль аккаунта в `role`. Роль `USER` позволяет обращаться к своему профилю. Для записи статей требуется роль `AUTHOR`, `EDITOR` или `ADMIN`.

В текущей реализации ответ входа сообщает `expiresIn: 900` (15 минут), а поле `exp` подписанного JWT устанавливается через 1800 секунд (30 минут) после выдачи. При проверке токена сервисы используют `exp`.

## Создание профиля через Kafka

1. Auth-service сохраняет аккаунт в статусе `PENDING` и запись в `outbox_events` в одной транзакции.
2. Планировщик публикует запрос создания профиля в `PROFILE_CREATION_REQUESTS`. User-service сохраняет профиль и подтверждение в своей таблице `outbox_events` в одной транзакции.
3. Планировщик user-service публикует подтверждение в `PROFILE_CREATED`. Auth-service переводит соответствующий аккаунт из `PENDING` в `ACTIVE`.

| Топик | Отправитель → получатель | JSON |
| --- | --- | --- |
| `PROFILE_CREATION_REQUESTS` | auth-service → user-service | `eventId` (UUID), `accountId` (число), `username` (строка) |
| `PROFILE_CREATED` | user-service → auth-service | `eventId` (UUID), `accountId` (число) |

Ключ сообщения в обоих топиках — строковое представление `accountId`. Старое поле `displayName` в запросе создания профиля принимается как псевдоним для `username`, поэтому сохранённые ранее сообщения и записи outbox остаются читаемыми.

Оба планировщика удаляют запись outbox после подтверждения отправки Kafka. При ошибке отправки запись остаётся для следующей попытки. Возможна повторная доставка. User-service пропускает создание, если профиль с таким `accountId` уже существует, а auth-service меняет статус только у аккаунта в `PENDING`.

## Обновление и выход

Для обоих запросов тело одинаковое:

```json
{"refreshToken": "<refresh-token>"}
```

Refresh token действует 30 дней. При обновлении старый удаляется, клиент должен сохранить новую пару токенов. Выход удаляет переданный refresh token; уже выданный access token действует до истечения срока.

## Ошибки

| HTTP | `code` | Причина |
| --- | --- | --- |
| 400 | `INVALID_CREDENTIALS` | Неверный email или пароль либо аккаунт ещё в статусе `PENDING` |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh token не найден, истёк или отозван |
| 409 | `CONFLICT` | Username или email уже занят |

Эти ошибки возвращаются как `{"code":"...","message":"...","errors":null}`. Для ошибок валидации и некорректного JSON отдельный обработчик не задан.

