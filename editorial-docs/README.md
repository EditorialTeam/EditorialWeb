# API EditorialWeb

В Docker Compose внешние запросы идут через `http://localhost:8080`. Остальные сервисы доступны внутри сети Compose.

| Сервис | API |
| --- | --- |
| [auth-service](auth-service/README.md) | Регистрация, токены и управление аккаунтами |
| [article-service](article-service/README.md) | Чтение, создание и редактирование статей |
| [user-service](user-service/README.md) | Чтение и редактирование профилей |
| [authorization-server](authorization-server/README.md) | Отдельный OAuth2-сервер для будущих интеграций |
| [gateway-service](gateway-service/README.md) | Маршрутизация внешних запросов |

Для JSON-запросов нужен `Content-Type: application/json`. Даты передаются строками ISO 8601 в UTC.

## Что изменилось

- Gateway направляет `/api/admin/accounts/**` в auth-service, а `/api/admin/users/**` в user-service. Доступ к этим методам пока настроен частично. [Аккаунты](auth-service/README.md#управление-аккаунтами) · [Профили](user-service/README.md#управление-профилями)
- Вход до создания профиля возвращает `401 ACCOUNT_STILL_PENDING`. [Регистрация и вход](auth-service/README.md#регистрация-и-вход)
- `GET /api/users/me` и список профилей возвращают `accountId`. [API профиля](user-service/README.md)
- Список статей теперь возвращает страницу с метаданными пагинации. [Параметры списка статей](article-service/README.md#список-статей)
- Регистрация создаёт аккаунт в статусе `PENDING`. Профиль создаётся асинхронно через Kafka, и подтверждение от user-service переводит аккаунт в `ACTIVE`. Оба сервиса сохраняют исходящие события в transactional outbox. [Порядок регистрации и форматы событий](auth-service/README.md#создание-профиля-через-kafka)
- Через gateway доступны `GET /api/users/me` и `PATCH /api/users/me`. User-service проверяет пользовательский JWT от auth-service. [API профиля](user-service/README.md)
- Профиль хранит `username` отдельно от `displayName` и `profileDescription`. Миграция V4 переносит старые теги в `username`, а V5 добавляет описание профиля. [Поля и миграции](user-service/README.md#поля-и-миграции)
- Внутренний REST-вызов `POST /internal/users` и OAuth2-клиент со scope `profiles:create` выведены из регистрации. [Статус authorization-server](authorization-server/README.md)
