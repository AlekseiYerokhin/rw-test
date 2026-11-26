# Реестр записей (rw-test)

Web-приложение «Реестр записей» — тестовое задание для Белорусской железной дороги (БЧ).
SPA на Angular + Java-бэкенд с REST API, упакованные в единый WAR для развёртывания в Apache Tomcat.

[![CI](https://github.com/AlekseiYerokhin/rw-test/actions/workflows/ci.yml/badge.svg)](https://github.com/AlekseiYerokhin/rw-test/actions/workflows/ci.yml)

Полный текст технического задания: [docs/TZ.md](docs/TZ.md).

## Стек

| Слой | Технология |
|---|---|
| Backend | Java 11, Spring Boot 2.7.x (`javax.*`), Spring Data JPA, Bean Validation, BCrypt |
| Frontend | Angular 22 (standalone, zoneless), Angular Material 22, TypeScript strict |
| БД | PostgreSQL 12+ |
| Сборка | Maven (`packaging: war`) → единый `app.war` (frontend внутри), внешний Tomcat 8.5+/9 |

## Структура репозитория

```
├── backend/    Maven-проект (Spring Boot, WAR), пакет by.bch.records
├── frontend/   Angular SPA (Angular 22)
├── db/         schema.sql — создание объектов БД и начальные данные
└── docs/       ТЗ
```

## Требования

| Компонент | Версия | Примечание |
|---|---|---|
| JDK | 11 | |
| Maven | 3.6+ | |
| PostgreSQL | 12+ | |
| Apache Tomcat | 8.5+/9 | внешний контейнер |
| Node.js | **не требуется для сборки** | `frontend-maven-plugin` сам скачает Node 24.22.3 |
| Node.js (для dev-режима) | 24 LTS (≥ 24.15.0) | требование Angular 22 |

## 1. Подготовка БД

Выполнить от суперпользователя PostgreSQL:

```bash
psql -c "CREATE USER appuser WITH PASSWORD 'ваш_пароль';"
psql -c "CREATE DATABASE appdb WITH ENCODING 'UTF8' TEMPLATE template0 OWNER appuser;"
```

Применить схему и начальные данные (таблицы `app_user`/`record`, пользователь `admin`, 18 тестовых записей):

```bash
psql -U appuser -d appdb -f db/schema.sql
```

## 2. Настройка подключения

```bash
cp backend/src/main/resources/application.properties.example \
   backend/src/main/resources/application.properties
```

Пароль БД задаётся **только** переменной окружения (файл с секретами не коммитится):

```bash
export APP_DB_PASSWORD='ваш_пароль'
```

При необходимости поправьте URL/пользователя в `application.properties`.

## 3. Сборка (одной командой)

```bash
cd backend && mvn clean package
```

Что происходит: `frontend-maven-plugin` скачивает Node 24.22.3 → `npm ci` → `ng build --configuration production` → бандл попадает в `backend/target/classes/static` → Maven упаковывает `backend/target/app.war` (backend + SPA).

Результат: **`backend/target/app.war`**.

## 4. Развёртывание в Tomcat

```bash
cp backend/target/app.war $CATALINA_BASE/webapps/
$CATALINA_BASE/bin/startup.sh
```

Приложение будет доступно по адресу **http://localhost:8080/app/** (контекст `/app`).
Для развёртывания в корне скопируйте WAR как `ROOT.war` → http://localhost:8080/.

Перед запуском Tomcat задайте переменную окружения в его среде (например, в `bin/setenv.sh`):

```bash
export APP_DB_PASSWORD='ваш_пароль'
```

## 5. Изменения конфигурации Tomcat / PostgreSQL

| Файл | Изменение | Обязательность |
|---|---|---|
| Tomcat `conf/server.xml` | у коннектора 8080: `URIEncoding="UTF-8"` | **рекомендуется** — корректная кириллица в URL/параметрах |
| Tomcat `conf/web.xml` | лимиты multipart | не требуется: лимит 5 МБ задан в приложении (`spring.servlet.multipart.*`) |
| PostgreSQL `postgresql.conf` | — | не требуется: UTF-8 задаётся при создании БД |
| PostgreSQL `pg_hba.conf` | `host appdb appuser 127.0.0.1/32 md5` | только при сетевом доступе |

Пример коннектора:

```xml
<Connector port="8080" protocol="HTTP/1.1" URIEncoding="UTF-8"
           connectionTimeout="20000" redirectPort="8443" />
```

## 6. Dev-режим

Backend (embedded Tomcat, корень, порт 8080):

```bash
cd backend && mvn spring-boot:run
```

Frontend (порт 4200, proxy `/api` → 8080):

```bash
cd frontend && npm ci && npm start
```

> Нюанс: если backend для dev-режима развёрнут на **внешнем** Tomcat в контексте `/app`, кука `JSESSIONID` получит `Path=/app` и не будет отправляться dev-сервером с корня. Для dev используйте embedded-режим (`mvn spring-boot:run`) или разверните WAR как `ROOT.war`.

## Учётные данные

`admin / admin123` (создаётся `db/schema.sql`, пароль хранится как BCrypt-хэш).

## Принятые решения

- **Картинки** — в БД (`BYTEA`), байты отдаются отдельным эндпоинтом `GET /api/records/{id}/image`; список грузит только метаданные
- **Форма записи** — отдельные роуты `/records/new` и `/records/:id/edit`
- **Аутентификация** — HttpSession (`HttpOnly`, `SameSite=Lax`, таймаут 30 мин), BCrypt; без JWT
- **CSRF-lite** — мутирующие запросы требуют `X-Requested-With: XMLHttpRequest` (frontend ставит автоматически)
- **SPA-fallback** — не-API пути отдают `index.html` (F5 и прямые ссылки работают)
- **`RecordRow`** — interface-проекция с алиасами JPQL вместо конструкторного выражения: Hibernate 5.6 не разбирает FQN, начинающиеся с ключевого слова `by` (`by.bch…`), в `SELECT new` — падает с `ClassCastException`
- **Angular 22** — актуальная стабильная версия (ТЗ требует 17+); требует Node ^22.22.3 || ^24.15.0 || >=26 — в сборке фиксирован Node 24.22.3

## Проверка

- `cd backend && mvn test` — юнит-тесты сервисов (18 тестов)
- `cd frontend && npm test` — тесты Angular (vitest)
- CI (GitHub Actions): сборка WAR + прогон `schema.sql` на чистом PostgreSQL 16 с проверками данных
