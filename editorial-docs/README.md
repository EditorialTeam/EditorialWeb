# API EditorialWeb

В Docker Compose внешние запросы к API идут через `http://localhost:8080`. Gateway сохраняет исходный путь и направляет запрос нужному сервису.

| Сервис | Назначение |
| --- | --- |
| [auth-service](auth-service/README.md) | Регистрация, вход, обновление токенов и управление аккаунтами |
| [article-service](article-service/README.md) | Статьи, комментарии, фильтры, редакционный цикл и очередь модерации |
| [user-service](user-service/README.md) | Свой профиль и управление профилями |
| [gateway-service](gateway-service/README.md) | Внешние маршруты и проверка доступа в целевых сервисах |

Для запросов с телом JSON передавайте `Content-Type: application/json`. Для защищённых методов передавайте `Authorization: Bearer <accessToken>`. Даты в JSON передаются строками ISO 8601 в UTC.

JWT выдаёт auth-service. Article-service и user-service проверяют их по публичным ключам auth-service, доступным внутри сети Compose по `http://auth-service:8080/.well-known/jwks.json`.

## Основные сценарии

- Регистрация создаёт аккаунт `USER` в статусе `PENDING`. После подтверждения создания профиля через Kafka аккаунт переходит в `ACTIVE`. Пока профиль создаётся, вход возвращает `401 ACCOUNT_STILL_PENDING`. [Регистрация и события Kafka](auth-service/README.md#создание-профиля-через-kafka)
- Любой вошедший пользователь может читать и менять свой профиль. Ответ чтения содержит `accountId`, `username`, `displayName`, `createdAt` и `profileDescription`. [Профили](user-service/README.md)
- Статья начинается в `DRAFT`. Владелец отправляет её на проверку, после чего решение модерации переводит статью в `PUBLISHED` или `REJECTED`. Ответы `GET /api/articles` и `GET /api/articles/{id}` содержат имя автора, которое article-service получает пакетным запросом к user-service. Для списка статей и получения статьи по ID применяются разные правила доступа. [Доступ и редакционный цикл](article-service/README.md)
- Маршруты управления аккаунтами и профилями требуют `ADMIN`. После смены роли новые права попадают в следующий выданный JWT. [Аккаунты](auth-service/README.md#управление-аккаунтами) · [Профили](user-service/README.md#управление-профилями)

- Для статей доступны корневые комментарии и ответы, списки с пагинацией, редактирование и логическое удаление. Создание требует опубликованной статьи, права записи определяются ролью и проверками сервиса. [Комментарии](article-service/README.md#комментарии)
