# gateway-service API

Единая точка входа: `http://localhost:8080`. В Docker Compose только gateway публикует порт приложения на хост.

## Маршруты

| Внешний путь | Куда отправляется | Документация |
| --- | --- | --- |
| `/api/auth/**` | `http://auth-service:8080` | [auth-service](../auth-service/README.md) |
| `/api/articles/**` | `http://article-service:8080` | [article-service](../article-service/README.md) |

Префиксы путей сохраняются. Собственных REST-методов нет; тела запросов, ответы и статусы определяются целевым сервисом.

Gateway пропускает запросы без проверки JWT и ролей. Для защищённых методов передавайте `Authorization: Bearer <accessToken>` — проверку выполняет article-service.

Маршрутов к `/internal/users`, `/oauth2/token`, `/oauth2/jwks` и `/.well-known/jwks.json` нет. Эти адреса используются напрямую внутри сети Compose.
