# Реестр записей (rw-test)

Web-приложение «Реестр записей» — тестовое задание для Белорусской железной дороги (БЧ).
SPA на Angular + Java-бэкенд с REST API, единый WAR для развёртывания в Apache Tomcat.

[![CI](https://github.com/AlekseiYerokhin/rw-test/actions/workflows/ci.yml/badge.svg)](https://github.com/AlekseiYerokhin/rw-test/actions/workflows/ci.yml)

Полный текст технического задания: [docs/TZ.md](docs/TZ.md).

## Стек

| Слой | Технология |
|---|---|
| Backend | Java 11, Spring Boot 2.7.x (`javax.*`), Spring Data JPA, Bean Validation, BCrypt |
| Frontend | Angular 22 (standalone), Angular Material, TypeScript |
| БД | PostgreSQL 12+ |
| Сборка | Maven (`packaging: war`) → единый `app.war`, развёртывание во внешнем Tomcat 8.5+/9 |

## Структура репозитория

```
├── backend/    Maven-проект (Spring Boot, WAR), пакет by.bch.records
├── frontend/   Angular SPA (появится на этапе 4)
├── db/         schema.sql — создание объектов БД и начальные данные
└── docs/       ТЗ и сопроводительная документация
```

## Требования

- JDK 11, Maven 3.6+, Node 24 LTS (≥ 24.15.0 — для Angular 22), Tomcat 8.5+/9, PostgreSQL 12+

## Подготовка БД

```bash
# от суперпользователя PostgreSQL:
psql -c "CREATE DATABASE appdb WITH ENCODING 'UTF8' TEMPLATE template0;"
psql -d appdb -f db/schema.sql   # таблицы + admin/admin123 + 18 тестовых записей
```

## Настройка подключения к БД

Скопировать `backend/src/main/resources/application.properties.example` → `application.properties`,
пароль задаётся переменной окружения `APP_DB_PASSWORD` (реальный файл не коммитится).

## Сборка и развёртывание

```bash
cd backend && mvn clean package   # → target/app.war
cp target/app.war $CATALINA_BASE/webapps/
```

## Dev-режим

Backend: `mvn spring-boot:run` в `backend/` (порт 8080). Frontend: `ng serve` + proxy на 8080 (появится на этапе 4).

## Учётные данные

`admin / admin123`

## Принятые решения

- Хранение картинок — в БД (`BYTEA`), основной вариант ТЗ п. 6.2
- Форма записи — отдельные роуты `/records/new` и `/records/:id/edit` (не диалог)
- SPA-fallback на `index.html` для прямых ссылок и F5
- Аутентификация — HttpSession + BCrypt (без JWT)

> **TODO (#28):** полная инструкция по развёртыванию «с нуля», таблица изменений конфигурации Tomcat/PostgreSQL, версии окружения сборки.
