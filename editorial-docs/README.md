# API EditorialWeb

В Docker Compose внешние запросы к API идут через `http://localhost:8080`. Gateway сохраняет исходный путь и направляет запрос нужному сервису.

| Сервис | Назначение |
| --- | --- |
| [auth-service](auth-service/README.md) | Регистрация, вход, обновление токенов и управление аккаунтами |
| [article-service](article-service/README.md) | Статьи, фильтры, редакционный цикл и очередь модерации |
| [user-service](user-service/README.md) | Свой профиль и управление профилями |
| [gateway-service](gateway-service/README.md) | Внешние маршруты и проверка доступа в целевых сервисах |
| [authorization-server](authorization-server/README.md) | Отдельный OAuth2-модуль для сервисных интеграций |

Для запросов с телом JSON передавайте `Content-Type: application/json`. Для защищённых методов передавайте `Authorization: Bearer <accessToken>`. Даты в JSON передаются строками ISO 8601 в UTC.

## Основные сценарии

- Регистрация создаёт аккаунт `USER` в статусе `PENDING`. После подтверждения создания профиля через Kafka аккаунт переходит в `ACTIVE`. Пока профиль создаётся, вход возвращает `401 ACCOUNT_STILL_PENDING`. [Регистрация и события Kafka](auth-service/README.md#создание-профиля-через-kafka)
- Любой вошедший пользователь может читать и менять свой профиль. Ответ чтения содержит `accountId`, `username`, `displayName`, `createdAt` и `profileDescription`. [Профили](user-service/README.md)
- Статья начинается в `DRAFT`. Владелец отправляет её на проверку, после чего решение модерации переводит статью в `PUBLISHED` или `REJECTED`. У списка и карточки разные правила видимости. [Доступ и редакционный цикл](article-service/README.md)
- Маршруты управления аккаунтами и профилями требуют `ADMIN`. После смены роли новые права попадают в следующий выданный JWT. [Аккаунты](auth-service/README.md#управление-аккаунтами) · [Профили](user-service/README.md#управление-профилями)
