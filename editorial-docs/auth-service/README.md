# auth-service API

Внешний адрес: `http://localhost:8080/api/auth`. В сети Compose: `http://auth-service:8080`.

Методы в таблице доступны без `Authorization`. Доступ к управлению аккаунтами описан отдельно.

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

`username` обязателен и содержит до 100 символов. `email` обязателен и должен быть корректным адресом длиной до 254 символов. `password` содержит от 6 до 100 символов. Email при регистрации и входе приводится к нижнему регистру, пробелы по краям удаляются. Email и username уникальны.

Ответ `201 Created` подтверждает, что аккаунт с ролью `USER` и событие создания профиля сохранены. Профиль создаётся асинхронно, а вход доступен после активации аккаунта. Для аккаунта в статусе `PENDING` вход возвращает `401 ACCOUNT_STILL_PENDING`. Сервис проверяет этот статус до проверки пароля.

Успешный вход возвращает следующую пару токенов.

```json
{
  "refreshToken": "<refresh-token>",
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

JWT содержит `iss=editorial-auth-service`, `aud=editorial-api`, числовой ID аккаунта в строковом `sub` и роль аккаунта в `role`. С ролью `USER` доступны свой профиль и чтение статей в пределах правил видимости. Создание и редактирование статей требуют `AUTHOR`, `EDITOR` или `ADMIN`. [Права на операции со статьями](../article-service/README.md)

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

Обновление и выход используют одинаковое тело запроса.

```json
{"refreshToken": "<refresh-token>"}
```

Refresh token действует 30 дней. При обновлении старый токен удаляется. Клиент должен сохранить новую пару токенов. Выход удаляет переданный refresh token. Уже выданный access token действует до истечения срока.

## Управление аккаунтами

Через gateway методы доступны под `/api/admin/accounts`. Все операции требуют `Authorization: Bearer <accessToken>` с ролью `ADMIN`.

| Метод | Путь | Доступ | Ответ `200` |
| --- | --- | --- | --- |
| GET | `/api/admin/accounts` | JWT с ролью `ADMIN` | Страница аккаунтов |
| GET | `/api/admin/accounts/{accountId}` | JWT с ролью `ADMIN` | Один аккаунт |
| PATCH | `/api/admin/accounts/{accountId}` | JWT с ролью `ADMIN` | Текущие `email`, `username` и `role` |

Список отсортирован по возрастанию ID аккаунта. Контроллер принимает `page` от 0 (по умолчанию 0) и `size` от 1 до 100 (по умолчанию 20). Ответ представляет собой Spring Data `Page` с элементами в `content` и метаданными пагинации. Поля элемента и ответа GET по ID совпадают.

```json
{
  "accountId": 7,
  "email": "ivan@example.com",
  "username": "ivan",
  "createdAt": "2026-11-05T12:00:00Z",
  "role": "USER"
}
```

PATCH принимает любое сочетание `email`, `username` и `role`. Отсутствующее поле и `null` пропускают обновление. Для роли допустимы точные значения `USER`, `AUTHOR`, `EDITOR`, `ADMIN`. Обновление сохраняется в auth-service. Тег в базе профилей меняется независимо, поскольку PATCH аккаунта не публикует событие синхронизации. Новая роль появится в следующем выданном access token.

## Ошибки

| HTTP | `code` | Причина |
| --- | --- | --- |
| 400 | `INVALID_CREDENTIALS` | Неверный email или пароль |
| 401 | `ACCOUNT_STILL_PENDING` | Аккаунт ждёт подтверждения создания профиля |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh token не найден, истёк или отозван |
| 409 | `CONFLICT` | Username или email уже занят |
| 404 | `ACCOUNT_NOT_FOUND` | Аккаунт для GET или PATCH по ID не найден |
| 401 | — | Для административного запроса нет действительного JWT |
| 403 | — | Для административного запроса требуется `ADMIN` |

Ошибки с кодом возвращаются как `{"code":"...","message":"...","errors":null}`. Для ошибок валидации и некорректного JSON отдельный обработчик не задан.
