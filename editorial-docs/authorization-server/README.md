# authorization-server

Отдельный модуль Spring Authorization Server сохранён для будущих OAuth2-интеграций. Его настроенный issuer и адрес в сети Compose — `http://authorization-server:8080`.

Регистрация и активация аккаунтов используют [события Kafka](../auth-service/README.md#создание-профиля-через-kafka). Пользовательские JWT выдаёт auth-service. Для проверки подписи user-service и article-service читают ключи из `http://auth-service:8080/.well-known/jwks.json`.

Регистрация клиента `editorial-auth-service` со scope `profiles:create` удалена вместе со старым REST-вызовом создания профиля. Чтобы выдавать сервисные токены через `/oauth2/token`, сначала нужно настроить OAuth2-клиента в authorization-server.

В Compose для этого модуля предусмотрен отдельный комплект RSA-ключей. Настроенный ID ключа — `editorial-oauth-key-1`. Прямой доступ к модулю предусмотрен внутри сети Compose.
