# API EditorialWeb

В Docker Compose внешние запросы идут через `http://localhost:8080`. Остальные сервисы доступны внутри сети Compose.

| Сервис | API |
| --- | --- |
| [auth-service](auth-service/README.md) | Регистрация, вход, обновление токенов, выход |
| [article-service](article-service/README.md) | Чтение, создание и редактирование статей |
| [user-service](user-service/README.md) | Внутреннее создание профилей (WIP) |
| [authorization-server](authorization-server/README.md) | OAuth2-токены для межсервисных запросов |
| [gateway-service](gateway-service/README.md) | Маршрутизация внешних запросов |

Тела запросов и ответов — JSON, кроме запроса OAuth2-токена. Для JSON-запросов нужен `Content-Type: application/json`. Даты — строки ISO 8601 в UTC.

Документация составлена по контроллерам, DTO и конфигурации. User-service в разработке, его контракт может меняться. Работа API на запущенном стенде не проверялась.
