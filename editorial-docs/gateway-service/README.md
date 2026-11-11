# gateway-service API

Единая точка входа: `http://localhost:8080`. В Docker Compose только gateway публикует порт приложения на хост.

## Маршруты

| Внешний путь | Куда отправляется | Документация |
| --- | --- | --- |
| `/api/auth/**` | `http://auth-service:8080` | [auth-service](../auth-service/README.md) |
| `/api/admin/accounts/**` | `http://auth-service:8080` | [Управление аккаунтами](../auth-service/README.md#управление-аккаунтами) |
| `/api/articles/**` | `http://article-service:8080` | [Статьи](../article-service/README.md) |
| `/api/editor/articles/**` | `http://article-service:8080` | [Очередь и модерация](../article-service/README.md#редакционный-цикл) |
| `/api/users/**` | `http://user-service:8080` | [user-service](../user-service/README.md) |
| `/api/admin/users/**` | `http://user-service:8080` | [Управление профилями](../user-service/README.md#управление-профилями) |

Префиксы путей сохраняются. Тела запросов, ответы и статусы определяет целевой сервис.

Для защищённых методов передавайте `Authorization: Bearer <accessToken>`. Подпись, срок действия и параметры JWT проверяет целевой сервис. Article-service проверяет доступ к операциям со статьями, а user-service выбирает профиль текущего пользователя по `sub` токена. Оба административных префикса требуют роль `ADMIN` в целевых сервисах.

User-service и article-service получают публичные ключи напрямую у auth-service по адресу `http://auth-service:8080/.well-known/jwks.json` внутри сети Compose.

Имена авторов article-service получает напрямую у user-service через `POST /api/internal/users/batch` внутри сети Compose. [Формат внутреннего запроса](../user-service/README.md#внутренний-api-профилей)
