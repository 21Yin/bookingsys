# Local Run

## Start infrastructure

```bash
docker compose up -d
```

This starts:
- PostgreSQL on `127.0.0.1:5433`
- Redis on `127.0.0.1:6379`

## Environment values

The app defaults already match the Docker setup in `application.properties` and `.env`.

## Run the application

```bash
mvn spring-boot:run
```

## Swagger UI

Open:

```text
http://localhost:8080/swagger-ui/index.html
```

## Admin basic auth

Default credentials:

```text
admin / admin123
```

Use these for `/api/admin/**` endpoints.

## Mobile API auth

1. Register at `/api/auth/register`
2. Verify email with the returned mock token at `/api/auth/verify-email`
3. Login at `/api/auth/login`
4. Use the returned Bearer token for user endpoints
