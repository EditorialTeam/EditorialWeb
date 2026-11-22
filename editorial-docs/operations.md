# Запуск и эксплуатация

## Docker Compose

В корне находятся [docker-compose.yaml](../docker-compose.yaml) и [.env.example](../.env.example). Скопируйте пример в `.env` и задайте собственные значения. Compose читает этот файл при запуске; локальный запуск Java из IDE автоматически его не загружает.

| Переменная `.env` | Назначение |
| --- | --- |
| `ARTICLE_DB_PASSWORD` | Пароль `editorial` в БД `editorial_articles` |
| `USER_DB_PASSWORD` | Пароль `editorial` в БД `editorial_users` |
| `AUTH_DB_PASSWORD` | Пароль `editorial` в БД `editorial_auth` |
| `MINIO_ROOT_USER` | Пользователь MinIO, одновременно access key file-service |
| `MINIO_ROOT_PASSWORD` | Пароль MinIO, одновременно secret key file-service |
| `CORS_ALLOWED_ORIGINS` | Необязательный список разрешённых origin через запятую |

```sh
docker compose up -d --build
docker compose ps
docker compose logs --tail 100 auth-service user-service article-service file-service
```

После запуска откройте `http://127.0.0.1:9001`, войдите в MinIO и создайте bucket `editorial-media`. Код создаёт MinIO-клиент, но не bucket. Пока bucket отсутствует, загрузка файлов завершается ошибкой.

`secrets-gen` создаёт RSA-пары в `auth_keys` и `oauth_keys`, а также OAuth client secret в `oauth_secrets`. Auth-service использует только `auth_keys`, смонтированный в `/app/keys`. OAuth-артефакты создаются, но текущие сервисы их не используют. Существующие пары сохраняются между запусками; неполная пара завершает `secrets-gen` с ошибкой.

Базы запускаются с healthcheck, сервисы с JPA применяют Flyway. `depends_on` для Kafka и MinIO не обеспечивает их полной готовности: при проблемах регистрации или файлов проверьте логи соответствующих контейнеров.

## Адреса и хранение

| Компонент | Адрес в Compose | Доступ с хоста |
| --- | --- | --- |
| Gateway | `gateway-service:8080` | `http://localhost:8080` |
| Auth / User / Article / File | `<имя-сервиса>:8080` | HTTP-порты не опубликованы |
| База статей | `postgres-articles:5432` | `localhost:5434`, БД `editorial_articles` |
| База профилей | `postgres-users:5432` | `localhost:5435`, БД `editorial_users` |
| База аккаунтов | `postgres-auth:5432` | `localhost:5436`, БД `editorial_auth` |
| Kafka | `kafka:9092` | Порт не опубликован |
| MinIO S3 | `minio:9000` | Порт не опубликован |
| MinIO console | `minio:9001` | `http://127.0.0.1:9001` |

Пользователь всех PostgreSQL — `editorial`. Данные сохраняются в `articles_pgdata`, `users_pgdata`, `auth_pgdata`, `kafka_data`, `minio_data`. Остановка с сохранением томов:

```sh
docker compose down
```

Резервная копия должна учитывать и базы с object key, и MinIO с самими файлами, а также ключи auth-service. Изменение пароля в `.env` само по себе не меняет пароль пользователя уже инициализированной PostgreSQL.

## Конфигурация приложений

| Сервис | Параметры |
| --- | --- |
| auth-service | `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `APP_JWT_KEYS_DIRECTORY` |
| user-service | Параметры datasource, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `JWT_JWK_SET_URI` |
| article-service | Параметры datasource, `JWT_JWK_SET_URI` |
| file-service | `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`; bucket задан как `minio.bucket=editorial-media` |
| gateway-service | `CORS_ALLOWED_ORIGINS`, маршруты в `application.properties` |
| Все приложения | `SERVER_PORT`, в Compose равен `8080` |

`JWT_JWK_SET_URI` в Compose указывает на `http://auth-service:8080/.well-known/jwks.json`. Issuer — `editorial-auth-service`, audience — `editorial-api`.

Внутренние HTTP-адреса клиентов article-service и user-service заданы в Java-конфигурации как `http://user-service:8080` и `http://file-service:8080`. Маршруты gateway также используют DNS-имена Compose. Для запуска всех Java-процессов на хосте потребуются разные порты, переопределение маршрутов и адаптация этих клиентов. Kafka рекламирует `kafka:9092` и не публикует порт, поэтому текущий Compose рассчитан на запуск приложений в контейнерах.

В file-service явно не настроены multipart-лимиты Spring. Проверка размера в Java допускает до 10 MiB, но multipart-ограничения веб-сервера могут отклонить запрос раньше. Для согласования лимитов задайте `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` и `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE` в окружении нужных сервисов, включая gateway и сервис, принимающий загрузку.

## Сборка и проверки

Модули задают Java 21; Dockerfile и CI используют JDK 21. Корневой `pom.xml` отдельно содержит `maven.compiler.source/target=26`, но модули наследуются от Spring Boot, а не от корневого POM. Корень служит агрегатором пяти модулей.

С установленным Maven из корня:

```sh
mvn -B clean test
mvn -B -DskipTests package
```

В каждом сервисе есть Maven Wrapper. Например, в PowerShell из `article-service`:

```powershell
.\mvnw.cmd -DskipTests package
```

В Linux/macOS: `./mvnw -DskipTests package`. Для сборки нужен доступ к Maven-репозиториям или заполненный локальный кеш. Интеграционные проверки с Testcontainers требуют Docker; настройки конкретных тестов находятся в `src/test` модулей. Изменение документации не требует запуска инфраструктуры.

В `scripts` есть smoke-, security- и k6-сценарии. Они пока не полностью соответствуют Compose: smoke и k6 обращаются к JWKS через gateway, где такого маршрута нет; smoke также проверяет внешний фронтенд на порту 80, которого здесь нет. k6 не передаёт JWT, поэтому не измеряет успешное авторизованное чтение статей. Security-скрипт показывает открытые порты, а Compose действительно публикует PostgreSQL на 5434–5436. Результаты этих скриптов нужно интерпретировать с учётом конфигурации.

## CI/CD

[Workflow](../.github/workflows/deploy.yml) запускает `mvn -B clean test` на JDK 21 при push в любую ветку и при ручном запуске. Автодеплой выполняется только при push в `main` после успешных тестов.

Деплой подключается по SSH, синхронизирует `~/editorial` с `origin/main`, создаёт `.env`, выполняет `docker compose build` и `docker compose up -d --remove-orphans`. Затем выводит состояния контейнеров и очищает dangling images. Это не проверка готовности API и не автоматический откат.

Используемые GitHub Secrets: `SERVER_HOST`, `SERVER_USER`, `SERVER_SSH_KEY`, необязательные `SERVER_SSH_PASSPHRASE` и `SERVER_PORT` (по умолчанию 22), три пароля БД, `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD` и необязательный `CORS_ALLOWED_ORIGINS`. Код репозитория использует предоставленный GitHub `GITHUB_TOKEN` для синхронизации.

## Диагностика

- Аккаунт остаётся `PENDING`: проверьте Kafka, логи auth/user и их таблицы `outbox_events`. [События регистрации](auth-service/README.md#создание-профиля-через-kafka).
- `401` после входа: проверьте `exp`, issuer/audience JWT и доступность JWKS внутри сети Compose.
- `403` при работе со статьёй: проверьте роль в JWT, владельца и статус. Изменение роли не меняет уже выданный токен.
- Ошибка загрузки: проверьте bucket, реквизиты MinIO, MIME и размер файла, а также multipart-лимиты.
- Нет имён авторов: проверьте внутренний batch API user-service; article-service допускает ответ без данных профиля.

Не все сервисные ошибки имеют общий JSON-формат. Точные обработанные статусы приведены в документации каждого сервиса.
