-- =============================================================
-- schema.sql — Реестр записей (rw-test)
-- Единый скрипт для чистого сервера PostgreSQL 12+ (ТЗ п. 8.1)
-- Запуск: psql -U appuser -d appdb -f schema.sql
-- =============================================================

-- 1. Создание БД (выполняется ОТДЕЛЬНО, от суперпользователя):
-- CREATE DATABASE appdb WITH ENCODING 'UTF8' TEMPLATE template0;
-- CREATE USER appuser WITH PASSWORD 'secret';
-- GRANT ALL PRIVILEGES ON DATABASE appdb TO appuser;

-- 2. DDL таблиц (ТЗ п. 6.2) ----------------------------------

CREATE TABLE app_user (
    id            BIGSERIAL    PRIMARY KEY,
    login         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100)
);

CREATE TABLE record (
    id          BIGSERIAL    PRIMARY KEY,
    num_value   BIGINT       NOT NULL,
    text_value  VARCHAR(500) NOT NULL,
    date_value  DATE         NOT NULL,
    image       BYTEA,
    image_name  VARCHAR(255),
    image_mime  VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- 3. Начальные данные (ТЗ п. 8.1) ----------------------------

-- Пользователь admin / admin123 (пароль — только BCrypt-хэш, FR-AUTH-9):
INSERT INTO app_user (login, password_hash, display_name) VALUES
 ('admin', '$2a$10$thLIhDwCMW/3GCZMyeyOE.BeIfOnodzwlBvzfILNNHldNikqhMaEO', 'Администратор');

-- Тестовые записи с кириллицей (18 шт. — проверка пагинации и кодировки):
INSERT INTO record (num_value, text_value, date_value, image, image_name, image_mime) VALUES
 (42, 'Привет, БЧ! Ёжик', DATE '2026-01-15', decode('89504e470d0a1a0a0000000d4948445200000040000000400802000000250be689000000554944415478daedcf410d00200c04b073b12f1210388d68420659685203cda9355a04040404040404040404040404040404040404040404040404040404040404047e0aa4f76c020202020202020202020202028f5c88fdaa133856ecd40000000049454e44ae426082', 'hex'), 'флаг.png', 'image/png'),
 (1001, 'Первая тестовая запись — проверка кириллицы', DATE '2026-01-20', NULL, NULL, NULL),
 (-7, 'Белорусская железная дорога: тест отрицательного числа', DATE '2026-02-02', NULL, NULL, NULL),
 (2026, 'Электропоезд ЭР9 прибыл на станцию Минск-Пассажирский', DATE '2026-02-14', decode('89504e470d0a1a0a0000000d4948445200000040000000400802000000250be689000000564944415478daedd6411100400803319ce0039b67f424a0a3433a6b20bf56cf8bae00000000000000000000000000000000000022013f7c0000000000000000a701ee340000000000000000000000000000000040620b7e95e62d94f8bcda0000000049454e44ae426082', 'hex'), 'поезд.png', 'image/png'),
 (555, 'Грузовой состав № 45 отправлен в Барановичи', DATE '2026-03-01', NULL, NULL, NULL),
 (88, 'Плановый ремонт пути на перегоне Жлобин — Гомель', DATE '2026-03-11', NULL, NULL, NULL),
 (123456789, 'Ёлка у вокзала установлена, проверка буквы Ё', DATE '2026-03-25', NULL, NULL, NULL),
 (3, 'Закупка шпал: дуб, сосна; партия 12000 штук', DATE '2026-04-03', NULL, NULL, NULL),
 (777, 'Модернизация системы СЦБ на участке Брест — Минск', DATE '2026-04-18', NULL, NULL, NULL),
 (21, 'Обходной лист: проверка стрелочных переводов', DATE '2026-05-06', NULL, NULL, NULL),
 (900, 'Отчёт о погрузке зерна за апрель', DATE '2026-05-21', NULL, NULL, NULL),
 (64, 'Вагон-ресторан: обновление меню', DATE '2026-06-09', NULL, NULL, NULL),
 (13, 'Инвентаризация складского имущества депо Лида', DATE '2026-06-30', NULL, NULL, NULL),
 (450, 'Летний график движения пассажирских поездов', DATE '2026-07-12', NULL, NULL, NULL),
 (8, 'Проверка тормозных систем локомотива БКГ-1', DATE '2026-08-01', decode('89504e470d0a1a0a0000000d4948445200000040000000400802000000250be689000000534944415478daedcf410d00400804b11586c4f30c16c8fd483a19034dbf9c3e00000000000000000000000000000000000000000000000000000000000000000000006b401d0f00000000000000000000e0bb01fcc0553c8304518f0000000049454e44ae426082', 'hex'), 'локомотив.png', 'image/png'),
 (314, 'Письмо о согласовании графика подвоза материалов', DATE '2026-09-15', NULL, NULL, NULL),
 (2500, 'Годовой план капитального ремонта путей', DATE '2026-10-04', NULL, NULL, NULL),
 (999999999999, 'Контрольная запись: большое число в пределах BIGINT', DATE '2026-11-20', NULL, NULL, NULL);
