[Документация API и обзор изменений](editorial-docs/README.md)

## Локальный запуск

Укажите пароли локальных баз PostgreSQL в файле `.env` рядом с `docker-compose.yaml`.

```dotenv
ARTICLE_DB_PASSWORD=
USER_DB_PASSWORD=
AUTH_DB_PASSWORD=
```

Из корня проекта запустите docker compose команду: 

```sh
docker compose up -d --build 
```

