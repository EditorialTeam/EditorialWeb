# EditorialWeb

Бэкенд редакционной системы с регистрацией, профилями, статьями и очередью модерации. [Документация API](editorial-docs/README.md)

## Локальный запуск

Из корня проекта создайте файл `.env` рядом с `docker-compose.yaml` и задайте непустые пароли для трёх локальных баз.

```dotenv
ARTICLE_DB_PASSWORD=
USER_DB_PASSWORD=
AUTH_DB_PASSWORD=
```

Соберите и запустите сервисы через Docker Compose.

```sh
docker compose up -d --build
docker compose ps
```

Gateway принимает запросы на `http://localhost:8080`. Сервис `secrets-gen` создаёт ключи для JWT в томах Compose, а сервисы с PostgreSQL применяют миграции Flyway при запуске. Kafka используется для создания профиля и активации аккаунта.

Для первого запроса зарегистрируйте аккаунт через `POST /api/auth/register`, дождитесь активации и получите JWT через `POST /api/auth/login`. Новые аккаунты получают роль `USER`. Назначение ролей описано в [разделе управления аккаунтами](editorial-docs/auth-service/README.md#управление-аккаунтами).

В случае ошибки проверьте логи соответствующего сервиса.

```sh
docker compose logs --tail 100 gateway-service auth-service user-service article-service
```
