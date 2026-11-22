# EditorialWeb

Бэкенд редакционной системы с регистрацией, профилями и аватарами, статьями и изображениями, комментариями, реакциями и очередью модерации.

- [Документация всех сервисов и API](editorial-docs/README.md)
- [Запуск, конфигурация, сборка и деплой](editorial-docs/operations.md)

## Сервисы

| Модуль | Назначение |
| --- | --- |
| `gateway-service` | Единая HTTP-точка входа и CORS |
| `auth-service` | Аккаунты, JWT, refresh tokens и управление ролями |
| `user-service` | Профили, аватары и внутренний пакетный API профилей |
| `article-service` | Статьи, модерация, комментарии, реакции, обложки и изображения |
| `file-service` | Загрузка, скачивание и удаление изображений в MinIO |

Сервисы написаны на Java / Spring Boot. Аккаунты, профили и статьи используют отдельные PostgreSQL и миграции Flyway. Kafka передаёт события регистрации, MinIO хранит изображения. Dockerfile сервисов используют JDK/JRE 21.

## Локальный запуск

Нужны Docker Engine или Docker Desktop и Docker Compose v2. Из корня проекта скопируйте `.env.example` в `.env` и замените примеры паролей и реквизитов MinIO.

```dotenv
ARTICLE_DB_PASSWORD=<пароль базы статей>
USER_DB_PASSWORD=<пароль базы профилей>
AUTH_DB_PASSWORD=<пароль базы аккаунтов>
MINIO_ROOT_USER=<логин MinIO>
MINIO_ROOT_PASSWORD=<пароль MinIO>
```

Соберите и запустите сервисы через Docker Compose.

```sh
docker compose up -d --build
docker compose ps
```

Gateway принимает запросы на `http://localhost:8080`. Консоль MinIO доступна на `http://127.0.0.1:9001`: войдите с реквизитами из `.env` и создайте bucket `editorial-media`. Автоматическое создание bucket в текущем коде отсутствует.

JWT выдаёт auth-service, а user-service и article-service проверяют их по публичным ключам auth-service. Другие HTTP-порты приложений на хост не опубликованы.

Сервис `secrets-gen` создаёт ключи auth-service в томе `auth_keys`, а сервисы с PostgreSQL применяют миграции Flyway при запуске. Kafka используется для создания профиля и активации аккаунта. Имена авторов статей и комментариев article-service получает по внутреннему HTTP API user-service.

Для первого запроса зарегистрируйте аккаунт через `POST /api/auth/register`, дождитесь активации и получите JWT через `POST /api/auth/login`. Новые аккаунты получают роль `USER`. Назначение ролей описано в [разделе управления аккаунтами](editorial-docs/auth-service/README.md#управление-аккаунтами).

В случае ошибки проверьте логи соответствующего сервиса.

```sh
docker compose logs --tail 100 gateway-service auth-service user-service article-service file-service
```

Полные переменные окружения, адреса баз, команды сборки и текущие ограничения описаны в [инструкции эксплуатации](editorial-docs/operations.md). Фронтенд в этот репозиторий и Compose не входит.
