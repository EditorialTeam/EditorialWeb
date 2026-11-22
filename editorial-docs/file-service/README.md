# file-service API

File-service хранит изображения в MinIO. В сети Compose он доступен по `http://file-service:8080`, публичное скачивание через gateway — по `http://localhost:8080/api/files`. PostgreSQL и Kafka сервис не использует.

## Публичное скачивание

| Метод | Путь | Авторизация | Ответ |
| --- | --- | --- | --- |
| GET | `/api/files?key=<objectKey>` | JWT не требуется | `200`, бинарное содержимое |

```sh
curl -G "http://localhost:8080/api/files" --data-urlencode "key=articles/1/example.png" -o image.png
```

В PowerShell используйте `curl.exe`. Ответ передаётся потоком с MIME и длиной объекта из MinIO, а также `Content-Disposition: inline`.

Ключ — идентификатор объекта, а не готовый URL. URL-кодируйте значение query-параметра `key`. Публичный маршрут не проверяет владельца файла, наличие JWT, статус статьи или её видимость. Поэтому известный ключ позволяет скачать в том числе изображение черновика. Проверка доступа к списку изображений в article-service не защищает сам файловый URL.

## Внутренний API

Эти маршруты вызываются напрямую из user-service и article-service, gateway их не публикует. Собственная авторизация внутреннего API в текущем file-service не настроена; доступ ограничивается сетевой доступностью сервиса.

| Метод | Путь | Запрос | Успешный ответ |
| --- | --- | --- | --- |
| POST | `/api/internal/files` | Multipart: `file`, `fileCategory`, `entityId` | `201`, `{"objectKey":"..."}` |
| GET | `/api/internal/files?key=...` | Ключ объекта | `200`, бинарное содержимое |
| DELETE | `/api/internal/files?key=...` | Ключ объекта | `204`, без тела |

Допустимые категории:

| `fileCategory` | `entityId` у вызывающего сервиса | Формат ключа |
| --- | --- | --- |
| `AVATAR` | ID аккаунта | `avatars/<entityId>/<uuid>.<ext>` |
| `COVER` | ID статьи | `covers/<entityId>/<uuid>.<ext>` |
| `ARTICLE_IMAGE` | ID статьи | `articles/<entityId>/<uuid>.<ext>` |

File-service не проверяет наличие аккаунта/статьи или права пользователя: это ответственность вызывающего сервиса. `entityId` используется при формировании ключа. Новая загрузка всегда получает новый UUID.

## Проверка файлов

Пустой файл и файл больше `10 * 1024 * 1024` байт отклоняются. Apache Tika определяет MIME по содержимому; переданные имя и `Content-Type` не определяют расширение итогового объекта.

| MIME | Расширение |
| --- | --- |
| `image/jpeg` | `.jpg` |
| `image/png` | `.png` |
| `image/gif` | `.gif` |
| `image/webp` | `.webp` |

Остальные MIME отклоняются. До этой проверки запрос может быть отклонён multipart-лимитами Spring; явно согласованные лимиты в текущих `application.properties` отсутствуют. [Конфигурация загрузки](../operations.md#конфигурация-приложений).

## MinIO

Нужны `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`. Имя bucket задано в `application.properties`: `minio.bucket=editorial-media` (может быть переопределено через `MINIO_BUCKET`). В Compose реквизиты берутся из `MINIO_ROOT_USER` и `MINIO_ROOT_PASSWORD` файла `.env`.

Bucket нужно создать отдельно через консоль `http://127.0.0.1:9001`; автоматической инициализации нет. S3 API `minio:9000` доступен только внутри сети Compose. Данные сохраняются в `minio_data`.

## Ошибки и ограничения

| HTTP | `code` | Причина |
| --- | --- | --- |
| 400 | `FAULTY_FILE` | Пустой, слишком большой или неподдерживаемый файл; также ошибки операций MinIO |
| 404 | `FILE_NOT_FOUND` | При скачивании MinIO вернул `NoSuchKey` |

Формат обработанной ошибки — `{"code":"...","message":"..."}`. Поля `errors` здесь нет. Неверная категория enum или отсутствующие обязательные параметры отклоняются Spring; отдельный общий JSON-обработчик для них не задан. Пустой ключ вызывает `IllegalArgumentException`, для него отдельного обработчика нет.

Удаление делегируется MinIO и не проверяет наличие записи в базах других сервисов. Нет транзакции между MinIO и PostgreSQL, автоматической очистки неиспользуемых объектов или очереди повторных удалений. Правила замены и удаления связей описаны в [article-service](../article-service/README.md#обложка-и-изображения-статьи) и [user-service](../user-service/README.md#аватар).
