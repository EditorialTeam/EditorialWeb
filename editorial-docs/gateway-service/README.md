# gateway-service API

Единая точка входа: `http://localhost:8080`. В Docker Compose только gateway публикует порт приложения на хост.

## Маршруты

| Внешний путь | Куда отправляется | Документация |
| --- | --- | --- |
| `/api/auth/**` | `http://auth-service:8080` | [auth-service](../auth-service/README.md) |
| `/api/admin/accounts/**` | `http://auth-service:8080` | [Управление аккаунтами](../auth-service/README.md#управление-аккаунтами) |
| `/api/articles/**` | `http://article-service:8080` | [Статьи, комментарии и реакции](../article-service/README.md) |
| `/api/editor/articles/**` | `http://article-service:8080` | [Очередь и модерация](../article-service/README.md#редакционный-цикл) |
| `/api/users/**` | `http://user-service:8080` | [user-service](../user-service/README.md) |
| `/api/admin/users/**` | `http://user-service:8080` | [Управление профилями](../user-service/README.md#управление-профилями) |
| `/api/files/**` | `http://file-service:8080` | [Скачивание файлов](../file-service/README.md) |

Префиксы путей сохраняются. Тела запросов, ответы и статусы определяет целевой сервис.

Для защищённых методов передавайте `Authorization: Bearer <accessToken>`. Подпись, срок действия и параметры JWT проверяет целевой сервис. Article-service проверяет доступ к операциям со статьями, а user-service выбирает профиль текущего пользователя по `sub` токена. Оба административных префикса требуют роль `ADMIN` в целевых сервисах.

User-service и article-service получают публичные ключи напрямую у auth-service по адресу `http://auth-service:8080/.well-known/jwks.json` внутри сети Compose.

Имена авторов article-service получает напрямую у user-service через `POST /api/internal/users/batch` внутри сети Compose. [Формат внутреннего запроса](../user-service/README.md#внутренний-api-профилей)

Маршруты комментариев входят в `/api/articles/{articleId}/comments` и обслуживаются существующим маршрутом `/api/articles/**`. Сюда входят создание, корневой список, ответы, редактирование и удаление. [Форматы запросов и доступ](../article-service/README.md#комментарии)

Маршруты реакций `PUT`, `GET` и `DELETE /api/articles/{articleId}/reactions` также обслуживаются маршрутом `/api/articles/**`. [Реакции на статьи](../article-service/README.md#реакции-на-статьи)

Обложки и изображения статей используют `/api/articles/file/**`, аватары — `/api/users/me/avatar`. Загрузка и замена идут через multipart, скачивание по object key — через `/api/files?key=...`. Публичное скачивание не требует JWT.

## CORS и конфигурация

Маршруты определены в [application.properties](../../gateway-service/src/main/resources/application.properties). Spring Security gateway разрешает запросы; аутентификацию и роли проверяют целевые сервисы. JWKS `/.well-known/jwks.json` и `/api/internal/**` через gateway не маршрутизируются.

`CORS_ALLOWED_ORIGINS` задаёт разрешённые origin через запятую. В Compose по умолчанию указаны `http://localhost`, `http://localhost:80`, `http://localhost:5173` и `http://127.0.0.1`. Для другого адреса фронтенда передайте его явно. Значения по умолчанию в `application.properties` при прямом запуске отличаются от Compose.

Разрешены методы `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`, `HEAD`; заголовки `Authorization`, `Content-Type`, `Accept`, `X-Requested-With`, `Origin`. Включён `allowCredentials`, preflight кешируется до 3600 секунд. В ответах доступны клиенту заголовки `Authorization`, `Link`, `X-Total-Count`.

Gateway запускается на 8080; TLS и внешний reverse proxy в текущем Compose не настроены. [Порты и развёртывание](../operations.md).
