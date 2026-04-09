# 📚 LMS Diploma — Learning Management System

> Дипломный проект: полнофункциональная платформа онлайн-обучения с поддержкой монетизации, ИИ-скрининга преподавателей и системой сертификатов.

---

## 🗂 Содержание

- [Обзор проекта](#обзор-проекта)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Модули системы](#модули-системы)
- [API — краткий справочник](#api--краткий-справочник)
- [Запуск проекта](#запуск-проекта)
- [ИИ-модуль: Resume Screener](#ии-модуль-resume-screener)
- [Переменные окружения](#переменные-окружения)
- [TODO — Будущая работа](#todo--будущая-работа)

---

## Обзор проекта

LMS Diploma — REST-бэкенд системы управления обучением (Learning Management System), разработанный на Spring Boot с MongoDB. Платформа поддерживает три роли пользователей: **STUDENT**, **TEACHER** и **ADMIN**. Монетизация реализована через PayPal (разовые платежи и подписки). Для отбора кандидатов в преподаватели используется отдельный Python-сервис на FastAPI с моделью машинного обучения.

### Ключевые возможности

- Регистрация с верификацией по email и JWT-аутентификация
- Управление курсами, уроками, квизами и прогрессом студента
- Монетизация: разовая покупка курса и периодические подписки (PayPal)
- Скрининг резюме преподавателей через ML-модель (Random Forest)
- Генерация PDF-сертификатов по завершении курса с QR-кодом
- Загрузка медиафайлов через Cloudinary
- Полная документация Swagger / OpenAPI 3

---

## Архитектура

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (React)                  │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP / REST
┌───────────────────────▼─────────────────────────────┐
│           Spring Boot Backend  :8080                │
│                                                     │
│  controller/   service/   repository/               │
│  model/        dto/        security/                │
│  config/       exception/  util/                    │
└──────┬──────────────────────────┬───────────────────┘
       │ MongoDB                  │ HTTP
┌──────▼──────┐          ┌────────▼────────┐
│  MongoDB    │          │  FastAPI AI     │
│  (NoSQL DB) │          │  Service :8000  │
└─────────────┘          └─────────────────┘
                                  │ joblib
                         ┌────────▼────────┐
                         │ resume_model.pkl│
                         │ (Random Forest) │
                         └─────────────────┘
```

---

## Технологический стек

| Слой | Технология |
|------|------------|
| Backend | Java 17+, Spring Boot 3, Spring Security, Spring Data MongoDB |
| Аутентификация | JWT (io.jsonwebtoken), BCrypt |
| База данных | MongoDB |
| Платежи | PayPal REST API (Orders + Subscriptions) |
| Медиафайлы | Cloudinary |
| Документация | SpringDoc OpenAPI 3 / Swagger UI |
| Сертификаты | iText / PDF, QR-код (ZXing) |
| ИИ-сервис | Python 3.10+, FastAPI, scikit-learn, joblib, pandas |
| Email | Spring Mail (SMTP) |

---

## Модули системы

### 🔐 Auth — Аутентификация

- `POST /auth/register` — регистрация (роль всегда `STUDENT`, email-верификация обязательна)
- `POST /auth/verify` — подтверждение email по 6-значному коду
- `POST /auth/login` — получение JWT-токена

Верификационные коды генерируются через `SecureRandom` и живут 10 минут. При сбое отправки email пользователь и код автоматически откатываются.

---

### 👨‍🏫 TeacherApplication — Заявки преподавателей

Система двухэтапного отбора:
1. Студент подаёт заявку с резюме
2. Резюме анализируется AI-сервисом (score, strengths, weaknesses)
3. Квалификационный квиз от преподавателя
4. Администратор принимает или отклоняет заявку
5. При одобрении роль меняется на `TEACHER`, флаг `teacherApproved = true`

---

### 📖 Course — Управление курсами

- CRUD для курсов (только одобренные преподаватели)
- Поддержка бесплатных и платных курсов (USD)
- Загрузка превью-изображения через Cloudinary
- Публикация/снятие курса с публикации
- Средний рейтинг (`avgRating`, `ratingCount`) пересчитывается при каждой новой оценке

---

### 📝 Lesson — Уроки

- CRUD уроков внутри курса (только владелец курса)
- Видео-контент, вложения, порядок уроков (`orderIndex`)
- Прогресс по уроку: отметка завершения
- Комментарии к урокам

---

### 🧪 Quiz — Квизы

Два типа квизов:
- **CourseQuiz** — квиз для проверки знаний студентов по курсу
- **TeacherQuiz** — квалификационный квиз при подаче заявки в преподаватели

Поддерживается несколько попыток, хранение истории попыток.

---

### 💳 Payment & Subscription — Платежи

- **PayPal Orders** — разовая покупка курса
- **PayPal Subscriptions** — периодические подписки на контент
- `PaypalTokenCache` — кеш OAuth2-токена PayPal для минимизации запросов
- Mock-платёж (для тестирования без реального PayPal)
- Модели: `Payment`, `Subscription`, `PaymentStatus`, `SubscriptionStatus`, `SubscriptionType`

---

### 🎓 Certificate — Сертификаты

- Генерация PDF-сертификата по завершении курса
- Встроенный QR-код для верификации
- Хранение метаданных (дата, курс, студент, уникальный UUID)

---

### 📊 Progress — Прогресс студента

- `CourseProgress` — агрегированный прогресс по курсу
- `LessonProgress` — прогресс по отдельным урокам
- Автоматическое обновление при завершении урока

---

### 🤖 AI-модуль — Resume Screener

Отдельный FastAPI-сервис для скрининга резюме кандидатов в преподаватели. Подробнее — в разделе [ИИ-модуль](#ии-модуль-resume-screener).

---

## API — краткий справочник

После запуска полная документация доступна по адресу:

```
http://localhost:8080/swagger-ui/index.html
```

Для авторизации:
1. `POST /auth/login` — получить токен
2. Нажать **Authorize** в Swagger UI
3. Ввести `Bearer <ваш_токен>`

### Основные группы эндпоинтов

| Префикс | Описание |
|---------|----------|
| `/auth/**` | Публичные: регистрация, верификация, логин |
| `/api/courses/**` | Управление курсами |
| `/api/lessons/**` | Управление уроками |
| `/api/quiz/**` | Квизы студентов |
| `/api/teacher-quiz/**` | Квалификационные квизы |
| `/api/teacher-applications/**` | Заявки в преподаватели |
| `/api/payments/**` | PayPal: разовые платежи |
| `/api/subscriptions/**` | PayPal: подписки |
| `/api/certificates/**` | Сертификаты |
| `/api/progress/**` | Прогресс студента |
| `/api/ratings/**` | Рейтинги курсов |
| `/api/comments/**` | Комментарии к урокам |
| `/api/files/**` | Загрузка файлов |

---
## Эндпоинты 
---

## Модули
- **auth** — registration, login, verification
- **security** — JWT service, JWT filter, security configuration
- **controller** — public and protected REST endpoints
- **service** — business logic layer
- **repository** — Mongo repositories
- **model** — domain entities and enums
- **dto** — request/response objects
- **config** — Mongo, Cloudinary, file storage and other configuration
- **exception** — global exception handling
- **util** — security helpers
- **Ai** — separate FastAPI microservice for resume screening

---

## Основные сущности

- `User`
- `Course`
- `Lesson`
- `Enrollment`
- `Subscription`
- `Payment`
- `Quiz`
- `QuizQuestion`
- `QuizAttempt`
- `TeacherApplication`
- `TeacherQuizQuestion`
- `TeacherQuizAttempt`
- `CourseProgress`
- `LessonComment`
- `CourseRating`
- `Certificate`
- `VerificationCode`

---

## Группы АПИ

### Authentication
- `POST /auth/register`
- `POST /auth/verify`
- `POST /auth/login`

### Courses
- `POST /courses`
- `GET /courses/my`
- `GET /courses/{courseId}`
- `PUT /courses/{courseId}`
- `DELETE /courses/{courseId}`
- `GET /courses/public`

### Lessons
- `POST /lessons/course/{courseId}`
- `GET /lessons/course/{courseId}`
- `GET /lessons/{lessonId}`
- `PUT /lessons/{lessonId}`
- `DELETE /lessons/{lessonId}`

### Enrollments
- `POST /enrollments/free/{courseId}`
- `GET /enrollments/check/{courseId}`
- `GET /enrollments/my`

### PayPal Payments
- `POST /payments/paypal/orders/course/{courseId}`
- `POST /payments/paypal/orders/capture`
- `GET /payments/paypal/my`

### PayPal Subscriptions
- `GET /subscriptions/paypal/plan`
- `POST /subscriptions/paypal/confirm`
- `POST /subscriptions/paypal/save-pending`
- `GET /subscriptions/paypal/my`

### Quizzes
- `POST /quizzes/lesson/{lessonId}`
- `GET /quizzes/{quizId}`
- `GET /quizzes/lesson/{lessonId}`
- `PUT /quizzes/{quizId}`
- `DELETE /quizzes/{quizId}`

### Quiz Attempts
- `POST /quiz-attempts/submit`
- `GET /quiz-attempts/my`

### Progress
- `POST /progress/complete`
- `GET /progress`
- `GET /progress/lesson-unlocked`

### Lesson Comments
- `POST /lessons/{lessonId}/comments`
- `GET /lessons/{lessonId}/comments`
- `GET /lessons/{lessonId}/comments/{commentId}/replies`
- `PATCH /lessons/{lessonId}/comments/{commentId}/mark-answer`
- `DELETE /lessons/{lessonId}/comments/{commentId}`

### Course Ratings
- `POST /courses/{courseId}/ratings`
- `GET /courses/{courseId}/ratings`
- `DELETE /courses/{courseId}/ratings`

### Teacher Applications
- `POST /teacher-applications`
- `GET /teacher-applications`
- `GET /teacher-applications/pending`
- `POST /teacher-applications/{applicationId}/approve`
- `POST /teacher-applications/{applicationId}/reject`
- `GET /teacher-applications/{applicationId}/resume`

### Teacher Quiz
- `GET /api/teacher/quiz/{applicationId}/questions`
- `POST /api/teacher/quiz/{applicationId}/submit`
- `GET /api/teacher/quiz/{applicationId}/result`

### Certificates
- `POST /api/certificates/issue`
- `POST /api/certificates/{id}/regenerate`
- `GET /api/certificates/{id}`
- `GET /api/certificates/verify/{verificationCode}`


---
---

## Запуск проекта

### Требования

- Java 17+
- Maven 3.8+
- MongoDB (локально или Atlas)
- Python 3.10+ (для AI-сервиса)

### Backend (Spring Boot)

```bash
# Клонировать репозиторий
git clone <repo-url>
cd project

# Настроить application.properties (см. раздел "Переменные окружения")

# Собрать и запустить
mvn spring-boot:run
```

### AI-сервис (FastAPI)

```bash
cd Ai/

# Установить зависимости
pip install fastapi uvicorn joblib scikit-learn pandas

# Обучить модель (один раз)
python train_model.py

# Запустить API
uvicorn api:app --host 0.0.0.0 --port 8000 --reload
```

> **Важно:** перед запуском API необходимо сгенерировать `resume_model.pkl` через `train_model.py`.

---

## ИИ-модуль: Resume Screener

Сервис принимает данные резюме и возвращает оценку пригодности кандидата в преподаватели.

### Входные данные (`POST /analyze`)

```json
{
  "resumeText": "Python, Machine Learning, NLP...",
  "specialization": "Data Science",
  "yearsOfExperience": 4,
  "education": "M.Tech",
  "certifications": "AWS Certified",
  "projectsCount": 5
}
```

### Выходные данные

```json
{
  "score": 88,
  "recommendation": "STRONG_FIT",
  "summary": "Кандидат отлично подходит...",
  "strengths": "Опыт 4 лет. Сертификация: AWS...",
  "weaknesses": "Явных слабостей не обнаружено."
}
```

### Категории рекомендации

| Категория | Описание |
|-----------|----------|
| `STRONG_FIT` | Отличный кандидат |
| `GOOD_FIT` | Хороший кандидат, небольшие зоны роста |
| `NEEDS_REVIEW` | Требует дополнительной проверки |
| `WEAK_FIT` | Слабое соответствие требованиям |

**Аутентификация:** заголовок `X-API-Key: <ключ>` (ключ задаётся через переменную `API_KEY`).

**Health-check:** `GET /health`

---

## Переменные окружения

### Spring Boot (`application.properties`)

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/lms_db

# JWT
jwt.secret=<минимум 256-бит ключ>
jwt.expiration=86400000

# Email (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your@email.com
spring.mail.password=your_app_password

# Cloudinary
cloudinary.cloud-name=...
cloudinary.api-key=...
cloudinary.api-secret=...

# PayPal
paypal.client-id=...
paypal.client-secret=...
paypal.base-url=https://api-m.sandbox.paypal.com

# AI Service
ai.service.url=http://localhost:8000
ai.service.api-key=...
```

### AI-сервис (`.env` или системные переменные)

```bash
API_KEY=your_secret_api_key_here
```

---

# Тесты для Diplom Backend

## Структура файлов

Поместите тестовые файлы в:
```
src/test/java/com/diploma/Diplom/
├── auth/
│   └── AuthServiceTest.java
└── service/
    ├── CourseServiceTest.java
    └── EnrollmentAndQuizServiceTest.java  
```

## Зависимости в pom.xml

Убедитесь, что в вашем `pom.xml` есть:

```xml
<dependencies>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

</dependencies>
```

`spring-boot-starter-test` уже включает в себя:
- **JUnit 5** — фреймворк тестирования
- **Mockito** — мокирование зависимостей
- **AssertJ** — удобные assertion'ы (`assertThat(...)`)

## Запуск тестов

```bash
mvn test

mvn test -Dtest=CourseServiceTest

mvn test -Dtest=CourseServiceTest#createCourse_paidWithoutPrice_throws
```

## Что тестируется

### AuthServiceTest (11 тестов)
| Метод       | Тест-кейс                                             |
|-------------|-------------------------------------------------------|
| `register`  | Успешная регистрация                                  |
| `register`  | Email уже занят → ConflictException                   |
| `register`  | Ошибка отправки email → откат + InternalServerException |
| `verify`    | Верный код → аккаунт включается                       |
| `verify`    | Неверный код → UnauthorizedException                  |
| `verify`    | Просроченный код → UnauthorizedException              |
| `login`     | Успешный вход → токен + данные пользователя           |
| `login`     | Аккаунт не подтверждён → ForbiddenException           |
| `login`     | Неверный пароль → BadRequestException                 |
| `login`     | Пользователь не найден → ResourceNotFoundException    |

### CourseServiceTest (12 тестов)
| Метод             | Тест-кейс                                         |
|-------------------|---------------------------------------------------|
| `createCourse`    | Платный курс с ценой                              |
| `createCourse`    | Бесплатный курс → цена = 0                        |
| `createCourse`    | Платный без цены → BadRequestException            |
| `createCourse`    | Студент создаёт курс → ForbiddenException         |
| `createCourse`    | Неподтверждённый учитель → ForbiddenException     |
| `createCourse`    | С файлом превью → загрузка в Cloudinary           |
| `updateCourse`    | Успешное обновление полей                         |
| `updateCourse`    | Чужой курс → ForbiddenException                  |
| `updateCourse`    | Курс не найден → ResourceNotFoundException        |
| `deleteCourse`    | Удаляет курс + уроки                             |
| `deleteCourse`    | С превью → удаляет из Cloudinary                 |
| `getPublicCourses`| Возвращает только опубликованные                 |

### EnrollmentServiceTest (6 тестов)
| Метод               | Тест-кейс                                       |
|---------------------|-------------------------------------------------|
| `hasAccess`         | Бесплатный курс → всегда true                   |
| `hasAccess`         | Есть активная запись → true                     |
| `hasAccess`         | Есть подписка → true                            |
| `hasAccess`         | Нет ни записи, ни подписки → false              |
| `enrollFreeCourse`  | Успешная запись                                 |
| `enrollFreeCourse`  | Уже записан → возвращает существующую запись    |
| `enrollFreeCourse`  | Платный курс → ForbiddenException               |

### QuizServiceTest (8 тестов)
| Метод         | Тест-кейс                                            |
|---------------|------------------------------------------------------|
| `createQuiz`  | Успешное создание                                    |
| `createQuiz`  | Квиз уже существует → BadRequestException            |
| `createQuiz`  | Пустой список вопросов → BadRequestException         |
| `createQuiz`  | Неверный индекс ответа → BadRequestException         |
| `createQuiz`  | Один вариант ответа → BadRequestException            |
| `createQuiz`  | Чужой курс → ForbiddenException                     |
| `createQuiz`  | passingScore по умолчанию = 60                      |
| `deleteQuiz`  | Успешное удаление                                    |
| `deleteQuiz`  | Квиз не найден → ResourceNotFoundException           |

## TODO — Будущая работа

# Frontend (React / Next.js)
Бэкенд полностью готов, но фронтенда нет вообще. Нужно:

Страницы авторизации (регистрация → верификация email → логин)
Каталог курсов с фильтрацией по категории, уровню, цене
Личный кабинет студента: мои курсы, прогресс, сертификаты
Кабинет преподавателя: создание/редактирование курсов, уроков, квизов
Панель администратора: заявки преподавателей, одобрение/отклонение
Интеграция PayPal Buttons SDK (для оформления платежей и подписок)
Просмотрщик уроков с видеоплеером


# Docker / Docker Compose
Сейчас проект запускается вручную тремя отдельными командами (Spring Boot, FastAPI, MongoDB). Нужно:

Dockerfile для Spring Boot
Dockerfile для FastAPI AI-сервиса
docker-compose.yml объединяющий: backend + AI + MongoDB
.env.example с описанием всех переменных


# Роль ADMIN и панель управления
В коде упоминается роль ADMIN и логика одобрения преподавателей, но контроллера и сервиса для администратора нет. Нужно:

AdminController с эндпоинтами для управления пользователями
Просмотр всех заявок (TeacherApplication) в статусе PENDING
Одобрение / отклонение с сохранением комментария
Управление публикацией курсов (возможность снятия с публикации)


# Тесты
Тестов в проекте нет совсем. Критически важно:

Unit-тесты для AuthService, EnrollmentService, CourseProgressService
Integration-тесты для PayPal-флоу (с mock PayPal API)
Тест на data leakage для AI-модели (уже исправлен recruiter_hire, но нужна регрессия)
API-тесты через MockMvc для основных контроллеров


# Конфигурация продакшена

Заменить allowedOriginPatterns(List.of("*")) в SecurityConfig на конкретные домены
Настроить реальные PayPal credentials (сейчас sandbox)
Добавить rate limiting на эндпоинты аутентификации (защита от brute-force)
Настроить HTTPS / SSL


# AI-сервис — улучшения

Добавить Dockerfile и переменные окружения для продакшена
Расширить набор признаков модели (сейчас только 6 фич — слабо для реального скрининга)
Добавить логирование запросов к /analyze для мониторинга
Написать тесты для API с mock-моделью


# Обработка ошибок и логирование

GlobalExceptionHandler уже есть, но не все исключения покрыты единообразно
Добавить структурированное логирование (JSON logs для продакшена)
Настроить алерты на критические ошибки (email или Slack)


# Документация API

Добавить @Operation и @ApiResponse аннотации ко всем контроллерам (сейчас только у части)
Описать все DTO через @Schema
Добавить примеры запросов/ответов в Swagge

---

## 📄 Лицензия

Дипломный проект. Все права защищены.

---

