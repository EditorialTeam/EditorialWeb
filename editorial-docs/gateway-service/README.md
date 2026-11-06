# gateway-service API

Единая точка входа: `http://localhost:8080`. В Docker Compose только gateway публикует порт приложения на хост.

## Маршруты

| Внешний путь | Куда отправляется | Документация |
| --- | --- | --- |
| `/api/auth/**` | `http://auth-service:8080` | [auth-service](../auth-service/README.md) |
| `/api/admin/accounts/**` | `http://auth-service:8080` | [Управление аккаунтами](../auth-service/README.md#управление-аккаунтами) |
| `/api/articles/**` | `http://article-service:8080` | [article-service](../article-service/README.md) |
| `/api/users/**` | `http://user-service:8080` | [user-service](../user-service/README.md) |
| `/api/admin/users/**` | `http://user-service:8080` | [Управление профилями](../user-service/README.md#управление-профилями) |

Префиксы путей сохраняются. Собственных REST-методов нет; тела запросов, ответы и статусы определяются целевым сервисом.

Для защищённых методов передавайте `Authorization: Bearer <accessToken>`. Подпись, срок действия и параметры JWT проверяет целевой сервис. Права на запись статей проверяет article-service, а user-service выбирает профиль по `sub` токена.

User-service и article-service получают публичные ключи напрямую у auth-service по адресу `http://auth-service:8080/.well-known/jwks.json` внутри сети Compose.
