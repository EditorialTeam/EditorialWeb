# authorization-server API

Выдаёт OAuth2-токены для межсервисных вызовов. Адрес в сети Compose и issuer: `http://authorization-server:8080`. Через gateway не опубликован.

## Получение токена

`POST /oauth2/token`

Аутентификация клиента — HTTP Basic (`client_secret_basic`). Зарегистрирован клиент `editorial-auth-service`, grant — `client_credentials`, доступный scope — `profiles:create`.

```http
POST /oauth2/token
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&scope=profiles%3Acreate
```

Секрет читается из файла `client_secret` в каталоге `OAUTH_CLIENT_SECRETS`. В Compose файл создаёт `secrets-gen`; реальный секрет в документацию не включается.

Успешный ответ — `200`, стандартный OAuth2 JSON с полями `access_token`, `token_type`, `expires_in`, `scope`. Срок действия отдельно в проекте не настроен — используйте `expires_in` из ответа.

JWT предназначен для user-service: `aud=editorial-user-service`. Auth-service получает его автоматически перед созданием профиля.

## Публичные ключи

`GET /oauth2/jwks` — ключи проверки сервисных JWT, ответ `200` в формате JWKS (`keys`). ID ключа: `editorial-oauth-key-1`. User-service использует этот адрес для проверки подписи.

## Ошибки

Ошибки выдачи токена обрабатывает Spring Authorization Server: OAuth2 JSON с полем `error` и, при наличии, `error_description`/`error_uri`. Типичные причины: неверные данные клиента (`invalid_client`), недоступный scope (`invalid_scope`), неподдерживаемый grant (`unsupported_grant_type`). Собственного формата `ApiError` нет.
