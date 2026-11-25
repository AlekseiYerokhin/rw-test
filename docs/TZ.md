# Техническое задание

## на разработку Web-приложения «Реестр записей»

| | |
|---|---|
| **Заказчик** | Белорусская железная дорога (БЧ) |
| **Основание** | Тестовое задание кандидату |
| **Тип разработки** | Новая разработка (с нуля) |
| **Версия документа** | 2.1 (добавлено Приложение В — детальная структура backend) |
| **Дата** | 23.11.2026 |

---

## 1. Общие сведения

### 1.1. Наименование системы

Web-приложение «Реестр записей» (далее — Система, Приложение).

### 1.2. Назначение документа

Настоящее техническое задание (ТЗ) определяет цели, функциональные и нефункциональные требования, архитектуру, состав результатов и критерии приёмки разрабатываемого Web-приложения. Документ составлен на основании тестового задания Заказчика и служит основой для разработки и проверки результата.

### 1.3. Основание для разработки

Тестовое задание Заказчика, включающее:

- обязательный стек: **Java 8+**, **Apache Tomcat 8.5+**, **PostgreSQL**;
- функционал: аутентификация, просмотр списка записей, CRUD-операции над записями;
- состав записи: число, текст, дата, картинка;
- хранение данных в СУБД;
- поддержка кириллицы, формат даты **ДД.ММ.ГГГГ**;
- результаты: SQL-скрипт создания объектов БД, приложение для развёртывания в Tomcat, изменения конфигурации PostgreSQL/Tomcat (при необходимости).

### 1.4. Цели создания Системы

1. Продемонстрировать умение разрабатывать полноценное Web-приложение: SPA-фронтенд на **Angular** + Java-бэкенд с **REST API**, от проектирования схемы БД до развёртывания в контейнере сервлетов.
2. Обеспечить ведение реестра записей произвольного содержания (число, текст, дата, изображение) с разграничением доступа через аутентификацию.

---

## 2. Термины и сокращения

| Термин | Определение |
|---|---|
| Запись | Единица данных реестра, состоящая из числа, текста, даты и изображения |
| Пользователь | Лицо, прошедшее аутентификацию в Системе |
| SPA | Single Page Application — одностраничное приложение (Angular) |
| REST API | HTTP-интерфейс обмена данными в формате JSON |
| CRUD | Create, Read, Update, Delete — операции создания, чтения, обновления, удаления |
| СУБД | PostgreSQL |
| WAR | Web Application Archive — формат упаковки приложения для Tomcat |
| ORM | Object-Relational Mapping (Hibernate/JPA) |
| DTO | Data Transfer Object — объект передачи данных между фронтендом и бэкендом |

---

## 3. Требования к программным средствам (стек)

### 3.1. Обязательные средства (по заданию)

| Компонент | Требование задания | Принятое решение |
|---|---|---|
| Язык / платформа | Java 8+ | Java 11 (обратно совместимо с 8) |
| Контейнер сервлетов | Apache Tomcat 8.5+ | Apache Tomcat 9 (допустимо 8.5) |
| СУБД | PostgreSQL | PostgreSQL 12+ (допустимо 10+) |

### 3.2. Принятый стек реализации

Архитектура: **SPA на Angular + Java-бэкенд с REST API**, упакованные в **единый WAR** для развёртывания в Tomcat.

**Backend:**

| Слой | Технология | Назначение |
|---|---|---|
| Сборка | Maven 3.6+ (`packaging: war`) | Сборка единого `app.war` (backend + собранный frontend) |
| Фреймворк | **Spring Boot 2.7.x** (WAR-режим, внешний Tomcat) | Автоконфигурация, REST-контроллеры |
| REST | Spring Web (`@RestController`) | JSON API под `/api/**` |
| Доступ к данным | Spring Data JPA + Hibernate | ORM, работа с PostgreSQL |
| Валидация | Bean Validation (Hibernate Validator) | Серверная валидация DTO |
| Аутентификация | HttpSession + `AuthController` + `HandlerInterceptor` (защита `/api/**`) | Сессионная аутентификация |
| Хэширование паролей | BCrypt | Безопасное хранение паролей |
| JSON | Jackson (в составе Spring Boot) | Сериализация DTO, UTF-8 |
| Драйвер БД | PostgreSQL JDBC Driver 42.x | Подключение к СУБД |

> **Почему Spring Boot 2.7, а не 3.x:** Boot 3 / Spring 6 требуют Jakarta EE 9+ (Tomcat 10+, пакеты `jakarta.*`). Задание требует Tomcat 8.5+, поэтому используется линейка Boot 2.7 (`javax.*`), совместимая с Tomcat 8.5/9 и Java 8/11.

**Frontend:**

| Слой | Технология | Назначение |
|---|---|---|
| Фреймворк | **Angular 17+** (standalone-компоненты) | SPA: все страницы, роутинг, формы |
| Язык | TypeScript | — |
| UI-кит | Angular Material (допустим Bootstrap/свои стили) | Таблицы, диалоги, datepicker |
| HTTP | HttpClient + Interceptor | Вызовы REST API, обработка 401 |
| Защита роутов | `CanActivateFn` (AuthGuard) | Запрет доступа к страницам без входа |
| Сборка | Angular CLI (`ng build`) → статика в WAR | Продакшн-бандл |

> **Почему единый WAR:** задание требует «приложение в виде, пригодном для размещения в Apache Tomcat». SPA-бандл (`dist/`) копируется в статические ресурсы WAR; Tomcat отдаёт и фронтенд, и API с одного origin — без CORS-проблем в продакшне.

### 3.3. Допустимые альтернативы

В соответствии с п. 3 задания допускается замена отдельных компонентов при условии сохранения всего функционала и состава результатов:

- вместо Spring Boot — классический Spring MVC (XML/Java-config) или «чистые» сервлеты с Jackson;
- вместо JPA/Hibernate — JDBC (`PreparedStatement`);
- вместо Angular Material — Bootstrap, Taiga UI, собственные стили;
- вместо Maven — Gradle (артефакт всё равно WAR).

> **Важно:** выбранный стек фиксируется в README, сборка WAR — воспроизводимой одной командой.

---

## 4. Функциональные требования

Идентификаторы требований: `FR-<блок>-<номер>`.

### 4.1. Аутентификация (блок AUTH)

| ID | Требование |
|---|---|
| FR-AUTH-1 | Приложение содержит **страницу входа** (Angular-роут `/login`): форма с полями «Логин», «Пароль» и кнопкой «Войти» (Reactive Forms, обязательная валидация заполнения). |
| FR-AUTH-2 | Форма отправляет `POST /api/auth/login` (JSON: `login`, `password`). Проверка — по таблице пользователей в БД; пароли хранятся **только в виде BCrypt-хэша**. |
| FR-AUTH-3 | При успехе сервер создаёт HttpSession (кука `JSESSIONID`), возвращает `200 OK` с DTO пользователя (`login`, `displayName`); фронтенд перенаправляет на `/records`. |
| FR-AUTH-4 | При неверных данных сервер возвращает `401 Unauthorized`; форма показывает «Неверный логин или пароль» (без уточнения, какое поле неверно). |
| FR-AUTH-5 | Все страницы, кроме `/login`, защищены **на двух уровнях**: (а) AuthGuard на фронтенде; (б) серверный интерсептор/фильтр — любой запрос `/api/**` без сессии → `401`. HttpInterceptor фронтенда при ответе 401 перенаправляет на `/login`. |
| FR-AUTH-6 | Кнопка **«Выход»** вызывает `POST /api/auth/logout` (инвалидация сессии) и перенаправляет на `/login`. |
| FR-AUTH-7 | Эндпоинт `GET /api/auth/me` возвращает текущего пользователя (`200`) или `401` — используется для восстановления состояния входа при перезагрузке страницы. |
| FR-AUTH-8 | Время жизни сессии без активности — **30 минут** (настройка в `application.properties`: `server.servlet.session.timeout=30m`). |
| FR-AUTH-9 | Регистрация через UI **не требуется**: пользователи создаются SQL-скриптом (см. п. 8.1). Как минимум: `admin / admin123`. |

### 4.2. Отображение списка записей (блок LIST)

| ID | Требование |
|---|---|
| FR-LIST-1 | После входа пользователь попадает на страницу **списка записей** (роут `/records`), данные запрашиваются с сервера. |
| FR-LIST-2 | Таблица с колонками: **№ (ID), Число, Текст, Дата, Картинка (миниатюра), Действия**. |
| FR-LIST-3 | Даты отображаются строго в формате **ДД.ММ.ГГГГ** — Angular pipe `date:'dd.MM.yyyy'` с `LOCALE_ID: 'ru'`. |
| FR-LIST-4 | Картинка отображается миниатюрой (~64×64 px, пропорции сохраняются): `<img src="/api/records/{id}/image">`. Клик открывает полноразмерный просмотр (диалог). |
| FR-LIST-5 | **Серверная пагинация**: по умолчанию 10 записей на страницу; параметры запроса `page`, `size`; ответ содержит `content`, `totalElements`, `totalPages`, `number` (формат Spring `Page`). |
| FR-LIST-6 | **Серверная сортировка** по колонкам «Число», «Текст», «Дата»: параметры `sort` (`numValue`/`textValue`/`dateValue`) и `dir` (`asc`/`desc`); клик по заголовку меняет направление. |
| FR-LIST-7 | При пустом списке отображается «Записей пока нет» и кнопка «Добавить запись». |
| FR-LIST-8 | На странице — кнопка **«Добавить запись»**; в каждой строке — кнопки **«Редактировать»** и **«Удалить»**. |

### 4.3. Добавление записи (блок CREATE)

| ID | Требование |
|---|---|
| FR-CREATE-1 | Форма добавления открывается по кнопке «Добавить запись» — отдельный роут `/records/new` либо модальный диалог (решение фиксируется в README). |
| FR-CREATE-2 | Поля формы: **Число** (`input type="number"`), **Текст** (`textarea`), **Дата** (datepicker с форматом отображения ДД.ММ.ГГГГ), **Картинка** (выбор файла с превью). |
| FR-CREATE-3 | Все поля **обязательны**. |
| FR-CREATE-4 | Отправка: `POST /api/records` с `multipart/form-data` (FormData: поля + файл). |
| FR-CREATE-5 | При успехе (`201 Created`) — возврат к списку + snackbar «Запись успешно добавлена». |
| FR-CREATE-6 | При ошибке валидации сервер возвращает `400 Bad Request` с объектом ошибок `{поле: сообщение}`; форма подсвечивает ошибочные поля и показывает тексты ошибок **на русском**, введённые значения не теряются. |

### 4.4. Редактирование записи (блок UPDATE)

| ID | Требование |
|---|---|
| FR-UPDATE-1 | Форма редактирования открывается по кнопке «Редактировать» (роут `/records/{id}/edit` или диалог), данные загружаются `GET /api/records/{id}` и **предзаполняют форму**. |
| FR-UPDATE-2 | Дата в форме отображается в формате ДД.ММ.ГГГГ. |
| FR-UPDATE-3 | Текущая картинка показывается миниатюрой. |
| FR-UPDATE-4 | Картинку можно **не менять** (файл не выбран — остаётся прежняя) либо загрузить новую — тогда прежняя заменяется. |
| FR-UPDATE-5 | Сохранение: `PUT /api/records/{id}` с `multipart/form-data`. При успехе — возврат к списку + snackbar «Запись обновлена». |

### 4.5. Удаление записи (блок DELETE)

| ID | Требование |
|---|---|
| FR-DELETE-1 | Удаление инициируется кнопкой «Удалить» в строке списка. |
| FR-DELETE-2 | Перед удалением — **диалог подтверждения**: «Удалить запись №N? Действие необратимо». |
| FR-DELETE-3 | Удаление: `DELETE /api/records/{id}` → `204 No Content`. |
| FR-DELETE-4 | После удаления — обновление списка + snackbar «Запись удалена». |

### 4.6. Состав записи (модель данных)

| Поле | JSON (DTO) | Тип (БД) | Обязательность | Ограничения |
|---|---|---|---|---|
| ID | `id: number` | `BIGSERIAL` PK | авто | Генерируется СУБД |
| Число | `numValue: number` | `BIGINT` | да | Целое число, диапазон BIGINT |
| Текст | `textValue: string` | `VARCHAR(500)` | да | 1–500 символов; **кириллица, UTF-8** |
| Дата | `dateValue: "yyyy-MM-dd"` (ISO 8601) | `DATE` | да | В JSON — ISO; **на UI — ДД.ММ.ГГГГ** |
| Картинка | отдельно: `GET /api/records/{id}/image` | `BYTEA` + метаданные | да (при создании) | JPEG, PNG, GIF; ≤ 5 МБ |
| Мета картинки | `imageName: string`, `hasImage: boolean` | `VARCHAR` / вычисляемое | — | Для отображения без загрузки байтов |

> **Формат даты в API:** транспорт — ISO `yyyy-MM-dd` (стандарт, удобен для `LocalDate` и `input type="date"`); **отображение всегда ДД.ММ.ГГГГ** — требование п. 2.6 задания относится к UI.

### 4.7. Валидация (блок VALID)

| ID | Требование |
|---|---|
| FR-VALID-1 | Валидация — **двухуровневая**: серверная (Bean Validation на DTO, обязательна) + клиентская (Reactive Forms, для UX). |
| FR-VALID-2 | «Число»: отклоняются нечисловые значения, дробные и вне диапазона BIGINT. |
| FR-VALID-3 | «Текст»: отклоняются пустое значение и длина > 500 символов. |
| FR-VALID-4 | «Дата»: отклоняются несуществующие даты (30.02.2026) и значения вне разумного диапазона (год 1900–2100). |
| FR-VALID-5 | «Картинка»: отклоняются файлы недопустимого типа (проверка MIME + расширения **на сервере**) и размер > 5 МБ. |
| FR-VALID-6 | Все сообщения об ошибках — на русском языке, кириллица отображается корректно. |

---

## 5. Нефункциональные требования

### 5.1. Кодировки и локализация (критично для приёмки)

| ID | Требование |
|---|---|
| NFR-LOC-1 | **UTF-8 на всех уровнях**: исходный код, JSON API (Jackson + `spring.servlet.encoding.force=true`), соединение с БД, сама БД (`ENCODING 'UTF8'`). |
| NFR-LOC-2 | Текстовые данные с **кириллицей** корректно сохраняются в БД и отображаются на всех страницах без искажений (`????`, кракозябры недопустимы). Проверочная строка: «Привет, БЧ! Ёжик». |
| NFR-LOC-3 | Все даты на UI — формат **ДД.ММ.ГГГГ** (`LOCALE_ID ru`, pipe `dd.MM.yyyy`, datepicker с русской локалью). |
| NFR-LOC-4 | Язык интерфейса — русский. |

### 5.2. Безопасность

| ID | Требование |
|---|---|
| NFR-SEC-1 | Защита от SQL-инъекций: только ORM/параметризованные запросы; конкатенация SQL запрещена. |
| NFR-SEC-2 | Защита от XSS: вывод только через биндинги Angular (интерполяция `{{ }}` экранирует по умолчанию); `[innerHTML]` с пользовательскими данными запрещён. |
| NFR-SEC-3 | Пароли — только BCrypt-хэш в БД; пароль не возвращается ни в одном DTO, не логируется. |
| NFR-SEC-4 | Сессионная кука — `HttpOnly`, `SameSite=Lax`. |
| NFR-SEC-5 | CSRF: API принимает мутирующие запросы (POST/PUT/DELETE) только с заголовком `X-Requested-With: XMLHttpRequest` (проверяется интерсептором) — базовая защита при куки-сессии; либо полноценный CSRF-токен. |
| NFR-SEC-6 | Учётные данные БД — в `application.properties` / переменных окружения, **не в коде**; файл с реальным паролем не коммитится (есть `application.properties.example`). |
| NFR-SEC-7 | Эндпоинт `/api/records/{id}/image` доступен только аутентифицированным пользователям. |

### 5.3. Производительность и объёмы

| ID | Требование |
|---|---|
| NFR-PERF-1 | Корректная работа при **10 000 записей**: пагинация и сортировка — на уровне SQL (`LIMIT/OFFSET`, `ORDER BY`), запрещено «выбрать всё в память». |
| NFR-PERF-2 | Эндпоинт списка (страница 10 записей, без байтов картинок) — отклик до 1 с на машине разработчика. |
| NFR-PERF-3 | Список **не загружает байты изображений** — только метаданные; байты отдаются отдельным эндпоинтом по требованию (`<img>`). |

### 5.4. Совместимость

| ID | Требование |
|---|---|
| NFR-COMP-1 | Браузеры: актуальные Google Chrome, Mozilla Firefox, Microsoft Edge. |
| NFR-COMP-2 | ОС сервера: Windows / Linux. |
| NFR-COMP-3 | Сборка фронтенда: Node.js 18+ и npm на машине сборки (версии фиксируются в README). |

### 5.5. Требования к коду

| ID | Требование |
|---|---|
| NFR-CODE-1 | Backend — слои **controller → service → repository**; бизнес-логика не в контроллерах. |
| NFR-CODE-2 | Frontend — feature-структура (components/services/models), типизированные модели DTO, без `any`. |
| NFR-CODE-3 | Кодировка исходников UTF-8; отсутствие «мёртвого» кода и `console.log` в продакшн-сборке. |
| NFR-CODE-4 | Весь проект собирается одной командой (`mvn clean package`), включая сборку Angular (frontend-maven-plugin) — см. 8.2. |
| NFR-CODE-5 | (Рекомендуется) Юнит-тесты: JUnit 5 для сервисного слоя, Jasmine/Karma — опционально. |

---

## 6. Архитектура

### 6.1. Общая схема

```
┌──────────────────────────┐                ┌────────────────────────────────────────┐
│        Браузер            │   same origin  │           Apache Tomcat 8.5+           │
│ ┌──────────────────────┐ │  /        ───► │  статика Angular (index.html, *.js)    │
│ │  Angular SPA          │ │  /api/**  ───► │  ┌─────────┐ ┌───────────┐ ┌────────┐ │
│ │  login / records /    │ │   (JSON,       │  │AuthFilter│→│@RestController│→│Service│ │
│ │  record-form          │ │   multipart)   │  └─────────┘ └───────────┘ └───┬────┘ │
│ │  HttpClient+Interceptor│ │                │                                ▼      │
│ └──────────────────────┘ │                │                    Spring Data JPA      │
└──────────────────────────┘                └───────────────────────┬────────────────┘
                                                                    │ JDBC
                                                              ┌─────▼──────┐
                                                              │ PostgreSQL │
                                                              │ app_user   │
                                                              │ record     │
                                                              └────────────┘
```

- **Frontend**: SPA на Angular; в продакшне отдаётся Tomcat'ом как статика из WAR.
- **Backend**: REST API под префиксом `/api/**`; сессионная аутентификация (HttpSession).
- **SPA-роутинг**: запросы к не-API путям (`/records`, `/login` и т.д.) Tomcat перенаправляет на `index.html` (rewrite-фильтр / `ErrorPage 404 → index.html`), чтобы работали прямые ссылки и F5.

### 6.2. Структура базы данных

**Таблица `app_user`** — пользователи:

| Колонка | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | BIGSERIAL | PRIMARY KEY | Идентификатор |
| `login` | VARCHAR(50) | NOT NULL, UNIQUE | Логин |
| `password_hash` | VARCHAR(100) | NOT NULL | BCrypt-хэш пароля |
| `display_name` | VARCHAR(100) | | Отображаемое имя |

**Таблица `record`** — записи реестра:

| Колонка | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | BIGSERIAL | PRIMARY KEY | Идентификатор |
| `num_value` | BIGINT | NOT NULL | Число |
| `text_value` | VARCHAR(500) | NOT NULL | Текст (кириллица, UTF-8) |
| `date_value` | DATE | NOT NULL | Дата |
| `image` | BYTEA | | Бинарное содержимое картинки |
| `image_name` | VARCHAR(255) | | Исходное имя файла |
| `image_mime` | VARCHAR(100) | | MIME-тип (`image/jpeg`, `image/png`, `image/gif`) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT now() | Дата создания записи |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT now() | Дата последнего изменения |

> **Хранение изображений:** основной вариант — в БД (`BYTEA`): приложение самодостаточно, переносится одним WAR + одним скриптом. Допустимая альтернатива — файловое хранилище (`uploads/` вне WAR) с путём в БД; выбранный вариант фиксируется в README.

### 6.3. REST API (карта эндпоинтов)

Базовый префикс: `/api`. Формат: JSON, UTF-8. Ошибки: `{ "message": "...", "fieldErrors": { "поле": "текст" } }`.

| Метод | URL | Запрос | Ответ | Доступ |
|---|---|---|---|---|
| POST | `/api/auth/login` | `{login, password}` | `200 {login, displayName}` / `401` | все |
| POST | `/api/auth/logout` | — | `204` | авторизован |
| GET | `/api/auth/me` | — | `200 {login, displayName}` / `401` | авторизован |
| GET | `/api/records?page=0&size=10&sort=dateValue&dir=desc` | — | `200 Page<RecordDto>` | авторизован |
| GET | `/api/records/{id}` | — | `200 RecordDto` / `404` | авторизован |
| POST | `/api/records` | `multipart/form-data`: `numValue`, `textValue`, `dateValue`, `image` | `201 RecordDto` / `400` | авторизован |
| PUT | `/api/records/{id}` | `multipart/form-data` (поле `image` опционально) | `200 RecordDto` / `400` / `404` | авторизован |
| DELETE | `/api/records/{id}` | — | `204` / `404` | авторизован |
| GET | `/api/records/{id}/image` | — | `200` байты, `Content-Type: image_mime` / `404` | авторизован |

**RecordDto** (без байтов картинки):

```json
{
  "id": 1,
  "numValue": 42,
  "textValue": "Привет, БЧ! Ёжик",
  "dateValue": "2026-03-05",
  "imageName": "photo.png",
  "hasImage": true
}
```

### 6.4. Потоки данных

**Аутентификация:** форма → `POST /api/auth/login` → Service сверяет BCrypt-хэш → HttpSession (`userId`) → `200` → роутер на `/records`. AuthGuard при старте приложения вызывает `GET /api/auth/me`; 401 → `/login`.

**Создание записи:** Reactive Form → FormData → `POST /api/records` → Bean Validation → парсинг даты ISO→`LocalDate` → байты файла → сохранение → `201` → snackbar, редирект к списку.

**Отдача картинки:** `<img src="/api/records/{id}/image">` → чтение `BYTEA` → `Content-Type` из `image_mime`, `Cache-Control: private, max-age=3600`.

**Dev-режим фронтенда:** `ng serve` (порт 4200) + `proxy.conf.json` → `http://localhost:8080` — кука и API работают без CORS.

---

## 7. Требования к пользовательскому интерфейсу

### 7.1. Страница входа (роут `/login`)

- По центру: карточка «Вход в систему», поля «Логин», «Пароль», кнопка «Войти».
- Ошибка 401: красное сообщение «Неверный логин или пароль».
- Кнопка неактивна, пока поля пусты.

### 7.2. Страница списка (роут `/records`)

- Шапка (toolbar): название приложения, имя пользователя, кнопка «Выход».
- Кнопка «Добавить запись».
- Таблица (Material Table или аналог): № | Число | Текст | Дата | Картинка | Действия («✎», «🗑»).
- Сортируемые заголовки «Число», «Текст», «Дата» (стрелка направления).
- Пагинатор под таблицей (выбор 10/25/50 на страницу).
- Snackbar-уведомления об успехе/ошибке операций.

### 7.3. Форма записи (добавление/редактирование)

- Поля с подписями: «Число», «Текст», «Дата (ДД.ММ.ГГГГ)», «Картинка».
- Ошибки валидации — под полями, на русском.
- Превью выбранной картинки; при редактировании — миниатюра текущей.
- Кнопки: «Сохранить» (неактивна при невалидной форме), «Отмена».

### 7.4. Диалоги

- Подтверждение удаления: «Удалить запись №N? Действие необратимо» — кнопки «Удалить» / «Отмена».
- Просмотр картинки в полный размер — диалог по клику на миниатюру.

### 7.5. Общие требования к UI

- Аккуратный внешний вид (Angular Material / Bootstrap); «сырой» HTML недопустим.
- Все подписи, кнопки, сообщения — на русском языке.
- Индикатор загрузки (spinner/progress bar) при запросах к API.

---

## 8. Состав результатов (требования к поставке)

В соответствии с п. 4 задания результатом являются:

### 8.1. SQL-скрипт создания объектов БД — `schema.sql`

Единый скрипт, выполняемый на «чистом» сервере PostgreSQL:

1. Создание БД `appdb` с кодировкой UTF8 (или отдельный `init.sql` + команда в README).
2. DDL таблиц `app_user` и `record` (см. 6.2).
3. Начальные данные:
   - пользователь `admin` / `admin123` (BCrypt-хэш);
   - 15–20 тестовых записей, **включая тексты на кириллице** (проверка пагинации и кодировки); картинки тестовым записям добавляются через UI либо вставляются скриптом (маленький PNG в hex).

### 8.2. Приложение для развёртывания в Apache Tomcat

1. Готовый артефакт **`app.war`** (backend + собранный frontend внутри).
2. Полный **исходный код**: `backend/` (Maven) + `frontend/` (Angular) в одном репозитории.
3. Сборка **одной командой** `mvn clean package`:
   - `frontend-maven-plugin` выполняет `npm ci` и `ng build --configuration production`;
   - содержимое `frontend/dist/**` копируется в `src/main/resources/static` (или веб-ресурсы WAR) до упаковки;
   - допускается ручной двухшаговый вариант (`ng build` + копирование + `mvn package`), описанный в README.
4. **`README.md`**:
   - требования: JDK 11, Maven 3.6+, Node.js 18+, Tomcat 8.5+, PostgreSQL 12+;
   - выполнение `schema.sql`;
   - настройка подключения к БД (`application.properties` / env);
   - сборка и развёртывание WAR (копирование в `$CATALINA_BASE/webapps/`);
   - dev-режим: `ng serve` + proxy на 8080;
   - учётные данные: `admin / admin123`.

### 8.3. Изменения конфигурации PostgreSQL и Apache Tomcat (при необходимости)

Перечисляются в README (и прикладываются, если отличаются от дефолтных):

| Файл | Изменение | Необходимость |
|---|---|---|
| Tomcat `conf/server.xml` | `URIEncoding="UTF-8"` у `<Connector port="8080">` | Корректная кириллица в URL/параметрах |
| Tomcat `conf/web.xml` | лимиты multipart ≥ 5 МБ | Только если дефолтных недостаточно |
| PostgreSQL `postgresql.conf` | без изменений (UTF-8 задаётся при создании БД) | — |
| PostgreSQL `pg_hba.conf` | `host appdb appuser 127.0.0.1/32 md5` | Только если нужен сетевой доступ |

> Принцип: минимум ручных правок конфигурации; все нестандартные изменения документируются.

---

## 9. Критерии приёмки

Приёмка — развёртывание «с нуля» по README на чистом окружении и проверка по чек-листу:

| № | Проверка | Пункт задания |
|---|---|---|
| 1 | `schema.sql` выполняется без ошибок, создаёт таблицы и тестовые данные | 4.1 |
| 2 | `app.war` разворачивается в Tomcat без ошибок; SPA открывается, F5 на `/records` не даёт 404 | 4.2 |
| 3 | Без входа: страница списка недоступна (редирект на логин), API возвращает 401 | 2.1 |
| 4 | Вход `admin/admin123` успешен; неверный пароль — сообщение об ошибке; вход переживает перезагрузку страницы | 2.1 |
| 5 | Список отображается таблицей с серверной пагинацией и сортировкой | 2.2 |
| 6 | Запись добавляется через форму (с картинкой) и появляется в списке | 2.3 |
| 7 | Запись редактируется (в т.ч. без замены картинки и с заменой) | 2.3 |
| 8 | Запись удаляется после диалога подтверждения | 2.3 |
| 9 | В записи присутствуют и отображаются: число, текст, дата, картинка (миниатюра + полный размер) | 2.4 |
| 10 | Данные хранятся в PostgreSQL и переживают перезапуск Tomcat | 2.5 |
| 11 | Текст «Привет, БЧ! Ёжик» сохраняется и отображается без искажений во всех операциях | 2.6 |
| 12 | Все даты на UI — ДД.ММ.ГГГГ (список, форма); несуществующая дата отклоняется | 2.6 |
| 13 | Изменения конфигурации (если есть) описаны в README и достаточны для запуска | 4.3 |

**Все 13 пунктов должны быть выполнены.**

---

## 10. Этапы выполнения (рекомендуемый план)

| Этап | Содержание | Результат |
|---|---|---|
| 1 | Проектирование: схема БД, API-контракты, структура репозитория | `schema.sql` (черновик), скелеты backend/frontend |
| 2 | Backend: подключение к БД, entity, repository, CRUD API | Работающие эндпоинты `/api/records` (проверка curl/Postman) |
| 3 | Backend: аутентификация, сессии, защита API | `/api/auth/*`, 401 для неавторизованных |
| 4 | Frontend: каркас Angular, роутинг, AuthGuard, Interceptor, страница входа | Вход/выход работают из браузера |
| 5 | Frontend: список (таблица, пагинация, сортировка), формы создания/редактирования, удаление | Полный CRUD в UI |
| 6 | Картинки: загрузка с превью, отдача, миниатюры; формат дат ДД.ММ.ГГГГ; проверка кириллицы | Соответствие п. 2.4–2.6 |
| 7 | Сборка единого WAR, SPA-fallback, чистое развёртывание с нуля, README, чек-лист (раздел 9) | Поставочный комплект |

---

## 11. Ограничения и допущения

1. Одна роль пользователя (без разграничения прав); регистрация через UI не требуется.
2. Максимальный размер картинки — 5 МБ; форматы — JPEG, PNG, GIF.
3. Развёртывание — единый WAR на внешнем Tomcat (не embedded, не Docker, если иное не согласовано).
4. Frontend и backend в продакшне — один origin (CORS не требуется; в dev — через proxy `ng serve`).
5. Локализация — только русская; UTF-8 обязателен везде.
6. JWT не используется — сессионная аутентификация (проще и достаточно для одного приложения).
7. Любые отклонения от стека (п. 3.2) допустимы в рамках п. 3 задания и фиксируются в README.

---

## 12. Трассировка требований задания → ТЗ

| Пункт задания | Раздел ТЗ |
|---|---|
| 1.1 Java 8+ | 3.1 |
| 1.2 Apache Tomcat 8.5+ | 3.1, 8.2, 8.3 |
| 1.3 PostgreSQL | 3.1, 6.2, 8.1 |
| 2.1 Форма аутентификации | 4.1 |
| 2.2 Список записей | 4.2 |
| 2.3 Добавление/удаление/редактирование | 4.3–4.5 |
| 2.4 Число, текст, дата, картинка | 4.6 |
| 2.5 Хранение в СУБД | 6.2, 8.1 |
| 2.6 Кириллица, ДД.ММ.ГГГГ | 5.1, 4.7 |
| 3 Свобода выбора фреймворков | 3.2, 3.3 |
| 4.1 SQL-скрипт | 8.1 |
| 4.2 Приложение для Tomcat | 8.2 |
| 4.3 Изменения конфигурации | 8.3 |

---

## Приложение А. Пример `schema.sql` (черновик)

```sql
-- Создание БД (выполняется отдельно, от суперпользователя):
-- CREATE DATABASE appdb WITH ENCODING 'UTF8' TEMPLATE template0;

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

-- Пользователь admin / admin123 (BCrypt):
INSERT INTO app_user (login, password_hash, display_name)
VALUES ('admin', '$2a$10$<bcrypt-hash-of-admin123>', 'Администратор');

-- Тестовые данные с кириллицей:
INSERT INTO record (num_value, text_value, date_value) VALUES
 (1, 'Первая тестовая запись — проверка кириллицы', DATE '2026.01.15'),
 (2, 'Белорусская железная дорога: тест', DATE '2026.02.20');
 -- ... ещё записи для пагинации (15–20 шт.)
```

## Приложение Б. Примеры API-контрактов

**POST /api/auth/login**

```json
// Запрос
{ "login": "admin", "password": "admin123" }

// Ответ 200
{ "login": "admin", "displayName": "Администратор" }

// Ответ 401
{ "message": "Неверный логин или пароль" }
```

**GET /api/records?page=0&size=2&sort=dateValue&dir=desc**

```json
{
  "content": [
    { "id": 2, "numValue": 2, "textValue": "Белорусская железная дорога: тест",
      "dateValue": "2026-02-20", "imageName": "logo.png", "hasImage": true },
    { "id": 1, "numValue": 1, "textValue": "Первая тестовая запись — проверка кириллицы",
      "dateValue": "2026-01-15", "imageName": null, "hasImage": false }
  ],
  "totalElements": 17,
  "totalPages": 9,
  "number": 0,
  "size": 2
}
```

**Ответ 400 при ошибках валидации (POST /api/records)**

```json
{
  "message": "Ошибка валидации",
  "fieldErrors": {
    "textValue": "Текст обязателен и не должен превышать 500 символов",
    "dateValue": "Некорректная дата",
    "image": "Допустимы только JPEG, PNG, GIF до 5 МБ"
  }
}
```

---

## Приложение В. Детальная структура backend

### В.1. Дерево пакетов

Корневой пакет: **`by.bch.records`** (18 классов).

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/by/bch/records/
    │   │   ├── RecordsApplication.java            ← точка входа
    │   │   │
    │   │   ├── config/                            ← конфигурация
    │   │   │   └── WebConfig.java
    │   │   │
    │   │   ├── controller/                        ← HTTP-слой (REST)
    │   │   │   ├── AuthController.java
    │   │   │   └── RecordController.java
    │   │   │
    │   │   ├── service/                           ← бизнес-логика
    │   │   │   ├── AuthService.java
    │   │   │   └── RecordService.java
    │   │   │
    │   │   ├── repository/                        ← доступ к данным (Spring Data JPA)
    │   │   │   ├── AppUserRepository.java
    │   │   │   ├── RecordRepository.java
    │   │   │   └── ImageView.java
    │   │   │
    │   │   ├── entity/                            ← JPA-сущности (= таблицы БД)
    │   │   │   ├── AppUser.java
    │   │   │   └── RecordEntity.java
    │   │   │
    │   │   ├── dto/                               ← объекты для API (вход/выход)
    │   │   │   ├── LoginRequest.java
    │   │   │   ├── UserDto.java
    │   │   │   ├── RecordDto.java
    │   │   │   ├── RecordRow.java
    │   │   │   ├── RecordForm.java
    │   │   │   └── ApiError.java
    │   │   │
    │   │   ├── security/                          ← аутентификация/защита
    │   │   │   ├── AuthInterceptor.java
    │   │   │   └── SessionKeys.java
    │   │   │
    │   │   └── exception/                         ← исключения + глобальный обработчик
    │   │       ├── UnauthorizedException.java
    │   │       ├── NotFoundException.java
    │   │       ├── BadRequestException.java
    │   │       └── GlobalExceptionHandler.java
    │   │
    │   └── resources/
    │       ├── application.properties             ← реальные пароли не коммитятся!
    │       ├── application.properties.example
    │       └── static/                            ← сюда собирается Angular (ng build)
    │
    └── test/
        └── java/by/bch/records/                   ← юнит-тесты сервисов (JUnit 5)
```

### В.2. Назначение пакетов и правила зависимостей

| Пакет | Назначение | Может зависеть от |
|---|---|---|
| `controller` | Только HTTP: маппинг URL, приём/отдача DTO, коды ответов. **Без бизнес-логики** | `service`, `dto` |
| `service` | Бизнес-логика, транзакции (`@Transactional`), валидация файлов, маппинг entity ↔ DTO | `repository`, `entity`, `dto` |
| `repository` | Интерфейсы Spring Data JPA, запросы | `entity` |
| `entity` | JPA-сущности. **Никогда не выходят наружу service-слоя** (в API только DTO) | — |
| `dto` | Объекты передачи данных + Bean Validation-аннотации | — |
| `security` | Интерсептор сессии, константы | — |
| `config` | Регистрация интерсептора, SPA-fallback | `security` |
| `exception` | Исключения + `@RestControllerAdvice` (единый формат ошибок `ApiError`) | `dto` |

Направление зависимостей — строго сверху вниз: `controller → service → repository → entity`.

### В.3. Точка входа

```java
package by.bch.records;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Точка входа. Наследование от SpringBootServletInitializer ОБЯЗАТЕЛЬНО:
 * именно оно позволяет разворачивать приложение как WAR во внешнем Tomcat.
 */
@SpringBootApplication
public class RecordsApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(RecordsApplication.class);   // для WAR-развёртывания
    }

    public static void main(String[] args) {                // для локальной разработки (embedded Tomcat)
        SpringApplication.run(RecordsApplication.class, args);
    }
}
```

### В.4. Слой `entity` — JPA-сущности

**`AppUser.java`** — таблица `app_user`:

```java
package by.bch.records.entity;

import javax.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;        // только BCrypt-хэш, никогда не отдаётся в API

    @Column(name = "display_name", length = 100)
    private String displayName;

    // геттеры/сеттеры
}
```

**`RecordEntity.java`** — таблица `record`:

```java
package by.bch.records.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "record")
public class RecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num_value", nullable = false)
    private Long numValue;              // «число» из задания

    @Column(name = "text_value", nullable = false, length = 500)
    private String textValue;           // «текст» (кириллица, UTF-8 на уровне БД)

    @Column(name = "date_value", nullable = false)
    private LocalDate dateValue;        // «дата»

    @Column(name = "image")
    private byte[] image;               // «картинка» — BYTEA; в списке НЕ выбирается (см. RecordRow)

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "image_mime", length = 100)
    private String imageMime;           // image/jpeg | image/png | image/gif

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    // геттеры/сеттеры
}
```

### В.5. Слой `repository` — Spring Data JPA

**`AppUserRepository.java`**:

```java
package by.bch.records.repository;

import by.bch.records.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByLogin(String login);   // для аутентификации
}
```

**`ImageView.java`** — проекция только для картинки (байты + MIME), чтобы не грузить всю сущность:

```java
package by.bch.records.repository;

/** Проекция Spring Data: только картинка записи (для GET /api/records/{id}/image). */
public interface ImageView {
    byte[] getImage();
    String getImageMime();
}
```

**`RecordRepository.java`**:

```java
package by.bch.records.repository;

import by.bch.records.dto.RecordRow;
import by.bch.records.entity.RecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    /**
     * Постраничная выборка списка БЕЗ байтов картинки (требование NFR-PERF-3).
     * Возвращает лёгкую проекцию RecordRow.
     */
    @Query("SELECT new by.bch.records.dto.RecordRow(" +
           "  r.id, r.numValue, r.textValue, r.dateValue, r.imageName, r.imageMime) " +
           "FROM RecordEntity r")
    Page<RecordRow> findAllRows(Pageable pageable);

    /** Только картинка конкретной записи (байты + MIME). */
    Optional<ImageView> findProjectedById(@Param("id") Long id);
}
```

### В.6. Слой `dto` — объекты API

```java
/** Вход: логин. POST /api/auth/login */
public class LoginRequest {
    @NotBlank(message = "Логин обязателен")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    private String password;
    // геттеры/сеттеры
}
```

```java
/** Выход: текущий пользователь. */
public class UserDto {
    private String login;
    private String displayName;
    // конструктор (login, displayName), геттеры
}
```

```java
/** Выход: запись реестра. Байтов картинки НЕТ — только метаданные. */
public class RecordDto {
    private Long id;
    private Long numValue;
    private String textValue;
    private LocalDate dateValue;   // Jackson сериализует как "yyyy-MM-dd" (ISO)
    private String imageName;
    private boolean hasImage;
    // конструкторы, геттеры/сеттеры
}
```

```java
/** Лёгкая строка списка (проекция JPQL, см. RecordRepository.findAllRows). */
public class RecordRow {
    private final Long id;
    private final Long numValue;
    private final String textValue;
    private final LocalDate dateValue;
    private final String imageName;
    private final String imageMime;

    public RecordRow(Long id, Long numValue, String textValue,
                     LocalDate dateValue, String imageName, String imageMime) { ... }
    // геттеры
}
```

```java
/** Вход: форма создания/редактирования (multipart/form-data, биндинг через @ModelAttribute). */
public class RecordForm {

    @NotNull(message = "Число обязательно")
    private Long numValue;

    @NotBlank(message = "Текст обязателен")
    @Size(max = 500, message = "Текст не должен превышать 500 символов")
    private String textValue;

    @NotNull(message = "Дата обязательна")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)   // ожидает yyyy-MM-dd от фронтенда
    private LocalDate dateValue;

    /** Картинка: тип/размер/обязательность проверяются в RecordService.validateImage(). */
    private MultipartFile image;
    // геттеры/сеттеры
}
```

```java
/** Единый формат ошибки API. */
public class ApiError {
    private final int status;
    private final String message;
    private final Map<String, String> fieldErrors;   // null, если не ошибка валидации
    private final Instant timestamp = Instant.now();
    // статические фабрики: of(status, message), of(status, message, fieldErrors)
}
```

### В.7. Слой `service` — бизнес-логика

**`AuthService.java`**:

```java
package by.bch.records.service;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository userRepository;

    /** Только крипто-модуль Spring Security, БЕЗ всего Spring Security. */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository userRepository) { this.userRepository = userRepository; }

    /**
     * Проверка логина/пароля.
     * @return пользователь при успехе
     * @throws UnauthorizedException при неверном логине ИЛИ пароле (одинаковое сообщение!)
     */
    public AppUser authenticate(String login, String rawPassword) {
        AppUser user = userRepository.findByLogin(login)
                .orElseThrow(UnauthorizedException::new);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException();
        }
        return user;
    }
}
```

**`RecordService.java`** — ядро CRUD:

```java
package by.bch.records.service;

@Service
@Transactional
public class RecordService {

    private static final Set<String> ALLOWED_MIME =
            Set.of("image/jpeg", "image/png", "image/gif");
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;   // 5 МБ

    /** Белый список полей сортировки — защита от подстановки произвольных полей. */
    private static final Set<String> SORTABLE =
            Set.of("id", "numValue", "textValue", "dateValue");

    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) { this.recordRepository = recordRepository; }

    /** Список: серверная пагинация + сортировка. */
    @Transactional(readOnly = true)
    public Page<RecordDto> getPage(int page, int size, String sort, String dir) {
        String field = SORTABLE.contains(sort) ? sort : "id";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));
        return recordRepository.findAllRows(pageable).map(this::toDto);
    }

    /** Одна запись или 404. */
    @Transactional(readOnly = true)
    public RecordDto getById(Long id) {
        return toDto(recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена")));
    }

    /** Создание: картинка обязательна. */
    public RecordDto create(RecordForm form) {
        validateImage(form.getImage(), true);
        RecordEntity entity = new RecordEntity();
        fill(entity, form);
        return toDto(recordRepository.save(entity));
    }

    /** Обновление: картинку можно не менять. */
    public RecordDto update(Long id, RecordForm form) {
        RecordEntity entity = recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        boolean replaceImage = form.getImage() != null && !form.getImage().isEmpty();
        if (replaceImage) {
            validateImage(form.getImage(), true);
        }
        fill(entity, form);   // внутри: image меняется только если replaceImage
        return toDto(recordRepository.save(entity));
    }

    /** Удаление: несуществующий id → 404. */
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new NotFoundException("Запись не найдена");
        }
        recordRepository.deleteById(id);
    }

    /** Картинка записи или 404. */
    @Transactional(readOnly = true)
    public ImageView getImage(Long id) {
        ImageView view = recordRepository.findProjectedById(id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
        if (view.getImage() == null || view.getImage().length == 0) {
            throw new NotFoundException("У записи нет картинки");
        }
        return view;
    }

    /** Серверная проверка файла: наличие, тип (MIME + расширение), размер. */
    private void validateImage(MultipartFile file, boolean required) { ... }

    /** Перенос полей формы в сущность + чтение байтов файла. */
    private void fill(RecordEntity entity, RecordForm form) { ... }

    private RecordDto toDto(RecordEntity e) { ... }
    private RecordDto toDto(RecordRow r) { ... }
}
```

### В.8. Слой `controller` — REST-эндпоинты

**`AuthController.java`**:

```java
package by.bch.records.controller;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    /** Вход. 200 + UserDto | 401 */
    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        AppUser user = authService.authenticate(req.getLogin(), req.getPassword());
        request.changeSessionId();                                  // защита от session fixation
        request.getSession(true).setAttribute(SessionKeys.USER_ID, user.getId());
        return new UserDto(user.getLogin(), user.getDisplayName());
    }

    /** Выход. 204 */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    /** Текущий пользователь (восстановление входа после F5). 200 | 401 от интерсептора */
    @GetMapping("/me")
    public UserDto me(HttpSession session) { ... }
}
```

**`RecordController.java`**:

```java
package by.bch.records.controller;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) { this.recordService = recordService; }

    /** GET /api/records?page=0&size=10&sort=dateValue&dir=desc */
    @GetMapping
    public Page<RecordDto> list(@RequestParam(defaultValue = "0")  int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "id") String sort,
                                @RequestParam(defaultValue = "asc") String dir) {
        return recordService.getPage(page, size, sort, dir);
    }

    @GetMapping("/{id}")
    public RecordDto get(@PathVariable Long id) {
        return recordService.getById(id);
    }

    /** Создание: multipart/form-data. 201 | 400 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RecordDto create(@Valid @ModelAttribute RecordForm form) {
        return recordService.create(form);
    }

    /** Редактирование: multipart/form-data, поле image опционально. 200 | 400 | 404 */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecordDto update(@PathVariable Long id, @Valid @ModelAttribute RecordForm form) {
        return recordService.update(id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recordService.delete(id);
    }

    /** Отдача байтов картинки с корректным Content-Type и кэшированием. */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        ImageView view = recordService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(view.getImageMime()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(view.getImage());
    }
}
```

### В.9. Пакет `security` — защита API

**`SessionKeys.java`**:

```java
package by.bch.records.security;

/** Ключи атрибутов HttpSession — в одном месте, без «магических строк». */
public final class SessionKeys {
    public static final String USER_ID = "userId";
    private SessionKeys() {}
}
```

**`AuthInterceptor.java`** — единая точка проверки сессии и CSRF-lite:

```java
package by.bch.records.security;

/**
 * Проверяет каждый запрос к /api/** (кроме /api/auth/login):
 *  1) мутирующие методы (POST/PUT/DELETE) требуют заголовок X-Requested-With: XMLHttpRequest
 *     — базовая защита от CSRF при куки-сессии (NFR-SEC-5); Angular HttpClient ставит его автоматически;
 *  2) наличие авторизованной сессии, иначе 401 в формате ApiError.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws IOException {

        String method = req.getMethod();
        boolean mutating = !"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method);
        if (mutating && !"XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            writeJsonError(res, 403, "Отсутствует заголовок X-Requested-With");
            return false;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.USER_ID) == null) {
            writeJsonError(res, 401, "Требуется аутентификация");
            return false;
        }
        return true;
    }

    private void writeJsonError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(
            "{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
```

### В.10. Пакет `config`

**`WebConfig.java`** — регистрация интерсептора + SPA-fallback:

```java
package by.bch.records.config;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** Всё /api/** под защитой, кроме эндпоинта входа. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    /**
     * SPA-fallback: неизвестные не-API пути (/records, /login, ...) отдают index.html.
     * Благодаря этому работают F5 и прямые ссылки (критерий приёмки №2).
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        return requested.exists() && requested.isReadable()
                                ? requested
                                : new ClassPathResource("/static/index.html");
                    }
                });
    }
}
```

### В.11. Пакет `exception` — единые ошибки

```java
/** 401 — неверный логин/пароль или отсутствие сессии. Сообщение всегда нейтральное. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() { super("Неверный логин или пароль"); }
}
```

```java
/** 404 — запись/картинка не найдена. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
```

```java
/** 400 — ошибка бизнес-валидации (например, недопустимый файл). */
public class BadRequestException extends RuntimeException {
    private final Map<String, String> fieldErrors;   // может быть null
    public BadRequestException(String message) { super(message); this.fieldErrors = null; }
    public BadRequestException(String message, Map<String, String> fieldErrors) { ... }
}
```

**`GlobalExceptionHandler.java`** — превращает все исключения в `ApiError` (JSON):

```java
package by.bch.records.exception;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> unauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ApiError.of(401, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(ApiError.of(404, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, ex.getMessage(), ex.getFieldErrors()));
    }

    /**
     * Ошибки Bean Validation.
     * BindException — для @ModelAttribute (multipart-формы),
     * MethodArgumentNotValidException — для @RequestBody (JSON).
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> validation(BindException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Ошибка валидации", fieldErrors));
    }

    /** Превышен multipart-лимит (5 МБ). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Файл превышает максимальный размер 5 МБ"));
    }

    /** Всё остальное — 500, детали только в лог (не наружу!). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internal(Exception ex) {
        log.error("Необработанная ошибка", ex);
        return ResponseEntity.status(500)
                .body(ApiError.of(500, "Внутренняя ошибка сервера"));
    }
}
```

### В.12. `application.properties`

```properties
# --- База данных (реальный пароль — через переменную окружения) ---
spring.datasource.url=jdbc:postgresql://localhost:5432/appdb
spring.datasource.username=appuser
spring.datasource.password=${APP_DB_PASSWORD:}

# --- JPA: схему создаёт ТОЛЬКО schema.sql, Hibernate лишь проверяет соответствие ---
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

# --- JSON/кодировка: UTF-8 везде (кириллица!) ---
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

# --- Сессия ---
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax

# --- Загрузка файлов (картинки до 5 МБ) ---
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=6MB
```

> Примечание: Spring Boot по умолчанию сериализует `LocalDate` как `"yyyy-MM-dd"` (ISO) — настраивать Jackson дополнительно не требуется.

### В.13. `pom.xml` — ключевые элементы

```xml
<packaging>war</packaging>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>          <!-- последняя 2.7.x: javax.*, Tomcat 8.5/9, Java 8+ -->
</parent>

<properties>
    <java.version>11</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <dependency>                              <!-- REST-контроллеры, Jackson -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>                              <!-- JPA + Hibernate -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>                              <!-- Bean Validation (@NotBlank и т.д.) -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>                              <!-- ТОЛЬКО BCrypt, без всего Spring Security -->
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-crypto</artifactId>
    </dependency>
    <dependency>                              <!-- драйвер PostgreSQL -->
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>                              <!-- ОБЯЗАТЕЛЬНО provided: сервер — внешний Tomcat -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>                              <!-- юнит-тесты -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Сборка Angular внутрь WAR** (`frontend-maven-plugin` в `pom.xml`):

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.15.0</version>
    <configuration>
        <workingDirectory>${project.basedir}/../frontend</workingDirectory>
        <nodeVersion>v20.11.0</nodeVersion>
    </configuration>
    <executions>
        <execution>
            <id>install-node</id>
            <phase>generate-resources</phase>
            <goals><goal>install-node-and-npm</goal></goals>
        </execution>
        <execution>
            <id>npm-ci</id>
            <phase>generate-resources</phase>
            <goals><goal>npm</goal></goals>
            <configuration><arguments>ci</arguments></configuration>
        </execution>
        <execution>
            <id>ng-build</id>
            <phase>generate-resources</phase>
            <goals><goal>npm</goal></goals>
            <configuration><arguments>run build -- --configuration production</arguments></configuration>
        </execution>
    </executions>
</plugin>
```

> Самый простой способ попадания бандла в WAR: в `angular.json` фронтенда указать
> `"outputPath": "../backend/target/classes/static"` — тогда `ng build` кладёт файлы
> сразу в сборочный каталог backend'а (до фазы `package`), и копирование отдельным
> плагином не нужно.

### В.14. Сводная таблица классов

| # | Класс | Пакет | Аннотации | Назначение |
|---|---|---|---|---|
| 1 | `RecordsApplication` | (root) | `@SpringBootApplication` | Точка входа, WAR-инициализация |
| 2 | `AppUser` | entity | `@Entity` | Сущность пользователя |
| 3 | `RecordEntity` | entity | `@Entity` | Сущность записи |
| 4 | `AppUserRepository` | repository | interface | Поиск пользователя по логину |
| 5 | `RecordRepository` | repository | interface | CRUD + проекции списка/картинки |
| 6 | `ImageView` | repository | interface | Проекция «только картинка» |
| 7 | `AuthService` | service | `@Service`, `@Transactional` | Проверка логина/пароля (BCrypt) |
| 8 | `RecordService` | service | `@Service`, `@Transactional` | CRUD, валидация файлов, маппинг |
| 9 | `AuthController` | controller | `@RestController` | `/api/auth/**` |
| 10 | `RecordController` | controller | `@RestController` | `/api/records/**` |
| 11 | `LoginRequest` | dto | Bean Validation | Входные данные логина |
| 12 | `UserDto` | dto | — | Текущий пользователь |
| 13 | `RecordDto` | dto | — | Запись для API |
| 14 | `RecordRow` | dto | — | Строка списка без байтов |
| 15 | `RecordForm` | dto | Bean Validation | Форма multipart |
| 16 | `ApiError` | dto | — | Единый формат ошибок |
| 17 | `AuthInterceptor` | security | `HandlerInterceptor` | Сессия + CSRF-lite |
| 18 | `SessionKeys` | security | — | Константы ключей сессии |
| 19 | `WebConfig` | config | `@Configuration` | Интерсепторы + SPA-fallback |
| 20 | `UnauthorizedException` | exception | — | 401 |
| 21 | `NotFoundException` | exception | — | 404 |
| 22 | `BadRequestException` | exception | — | 400 |
| 23 | `GlobalExceptionHandler` | exception | `@RestControllerAdvice` | Единые JSON-ошибки |
