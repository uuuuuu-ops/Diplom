# 📚 LMS Diploma — Learning Management System

> Дипломный проект: полнофункциональная платформа онлайн-обучения с поддержкой монетизации, ИИ-скрининга преподавателей, асинхронной обработкой событий и системой сертификатов.

---

## 🗂 Содержание

- [Обзор проекта](#обзор-проекта)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Модули системы](#модули-системы)
- [Кеширование (Redis)](#кеширование-redis)
- [Очереди сообщений (RabbitMQ)](#очереди-сообщений-rabbitmq)
- [API — краткий справочник](#api--краткий-справочник)
- [Запуск проекта](#запуск-проекта)
- [ИИ-модуль: Resume Screener](#ии-модуль-resume-screener)
- [Переменные окружения](#переменные-окружения)
- [Тесты](#тесты)
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
- **Redis-кеширование** горячих данных (курсы, доступ, прогресс, рейтинги)
- **RabbitMQ** для асинхронной обработки событий (email, сертификаты, запись на курс, платежи)
- Полная документация Swagger / OpenAPI 3

---

## Архитектура

```
┌─────────────────────────────────────────────────────────┐
│                   Frontend (React)                      │
└───────────────────────┬─────────────────────────────────┘
                        │ HTTP / REST
┌───────────────────────▼─────────────────────────────────┐
│             Spring Boot Backend  :8080                  │
│                                                         │
│  controller/   service/    repository/                  │
│  messaging/    config/     security/                    │
│  model/        dto/        exception/   util/           │
└──────┬─────────────┬──────────────┬──────────┬──────────┘
       │ MongoDB     │ Redis        │ RabbitMQ │ HTTP
┌──────▼──────┐ ┌────▼────┐ ┌──────▼──────┐ ┌─▼──────────────┐
│  MongoDB    │ │  Redis  │ │  RabbitMQ   │ │ FastAPI AI     │
│  (NoSQL DB) │ │  Cache  │ │  (queues)   │ │ Service :8000  │
└─────────────┘ └─────────┘ └─────────────┘ └────────────────┘
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
| Кеширование | Redis 7, Spring Cache (`@Cacheable`, `@CacheEvict`) |
| Очереди | RabbitMQ 3, Spring AMQP (Direct Exchange + DLQ) |
| Платежи | PayPal REST API (Orders + Subscriptions) |
| Медиафайлы | Cloudinary |
| Документация | SpringDoc OpenAPI 3 / Swagger UI |
| Сертификаты | iText / PDF, QR-код (ZXing) |
| ИИ-сервис | Python 3.10+, FastAPI, scikit-learn, joblib, pandas |
| Email | Spring Mail (SMTP) |
| Контейнеризация | Docker, Docker Compose |

---

## Модули системы

### 🔐 Auth — Аутентификация

- `POST /auth/register` — регистрация (роль всегда `STUDENT`, email-верификация обязательна)
- `POST /auth/verify` — подтверждение email по 6-значному коду
- `POST /auth/login` — получение JWT-токена

Верификационные коды генерируются через `SecureRandom`, хранятся в **Redis** с TTL 10 минут. При сбое отправки email пользователь и код автоматически откатываются. Отправка письма происходит асинхронно через **RabbitMQ** (`email.queue`).

---

### 👨‍🏫 TeacherApplication — Заявки преподавателей

Система двухэтапного отбора:
1. Студент подаёт заявку с резюме (PDF)
2. Резюме анализируется AI-сервисом (score, strengths, weaknesses)
3. Квалификационный квиз от преподавателя
4. Администратор принимает или отклоняет заявку
5. При одобрении роль меняется на `TEACHER`, флаг `teacherApproved = true`
6. Решение отправляется кандидату async через `teacher.notification.queue`

---

### 📖 Course — Управление курсами

- CRUD для курсов (только одобренные преподаватели)
- Поддержка бесплатных и платных курсов (USD)
- Загрузка превью-изображения через Cloudinary
- Публикация/снятие курса с публикации
- Средний рейтинг (`avgRating`, `ratingCount`) пересчитывается при каждой новой оценке
- Кеширование: отдельный курс — TTL 10 мин, каталог — TTL 2 мин, инвалидация при обновлении/удалении

---

### 📝 Lesson — Уроки

- CRUD уроков внутри курса (только владелец курса)
- Видео-контент, вложения, порядок уроков (`orderIndex`)
- Прогресс по уроку: отметка завершения
- Комментарии к урокам с уведомлением через `notification.queue`

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
- `PaypalTokenRedisCache` — кеш OAuth2-токена PayPal в Redis для минимизации запросов
- После успешного платежа событие уходит в `payment.queue` → async активация enrollment + invoice email
- При активации/отмене подписки уведомление уходит в `subscription.queue`
- Модели: `Payment`, `Subscription`, `PaymentStatus`, `SubscriptionStatus`

---

### 🎓 Certificate — Сертификаты

- Автоматическая генерация PDF-сертификата по завершении курса
- Запрос на генерацию уходит в `certificate.queue` (async)
- Встроенный QR-код для верификации
- Хранение метаданных (дата, курс, студент, уникальный UUID)

---

### 📊 Progress — Прогресс студента

- `CourseProgress` — агрегированный прогресс по курсу (кеш TTL 5 мин)
- `LessonProgress` — прогресс по отдельным урокам
- Автоматическое обновление при завершении урока
- Запись в `ActivityFeed` происходит async через `activity.queue`

---

### 📰 ActivityFeed — Лента активности

- Хранит события: завершение урока, закладки, полученные комментарии
- Последние 20 записей кешируются в Redis (TTL 2 мин)
- Запись событий — асинхронно через RabbitMQ, не блокирует HTTP-ответ

---

### 🤖 AI-модуль — Resume Screener

Отдельный FastAPI-сервис для скрининга резюме кандидатов в преподаватели. Подробнее — в разделе [ИИ-модуль](#ии-модуль-resume-screener).

---

## Кеширование (Redis)

| Кеш | Ключ | TTL | Инвалидация |
|-----|------|-----|-------------|
| `course` | `courseId` | 10 мин | `updateCourse`, `deleteCourse` |
| `courses` | `pub:{category}:{level}:{page}:{size}` | 2 мин | `updateCourse`, `deleteCourse` |
| `courseRating` | `courseId` | 10 мин | `rateOrUpdate`, `deleteRating` |
| `access` | `userId:courseId` | 5 мин | `enrollFreeCourse`, `activatePurchasedEnrollment` |
| `progress` | `userId:courseId` | 5 мин | `markLessonCompleted`, `markQuizPassed` |
| `subscription` | `userId` | 3 мин | `activateSubscription`, `cancelSubscription` |
| `activityFeed` | `userId` | 2 мин | `addActivity` |
| `paypal:access_token` | фиксированный | TTL токена − 60 с | автоматически по истечении |
| `verification:{email}` | email | 10 мин | после успешной верификации |
| `likes:course:{courseId}` | courseId | 30 мин | при like/unlike |

---

## Очереди сообщений (RabbitMQ)

Все очереди — **durable**, с **Dead Letter Queue (DLQ)** для неудачных сообщений. Тип exchange: `Direct`.

| Очередь | Событие | Producer | Consumer |
|---------|---------|----------|----------|
| `email.queue` | Отправка email | `EmailProducer` | `EmailConsumer` |
| `certificate.queue` | Генерация сертификата | `CertificateProducer` | `CertificateConsumer` |
| `notification.queue` | Новый комментарий | `CommentNotificationProducer` | `CommentNotificationConsumer` |
| `enrollment.queue` | Запись на курс | `EnrollmentProducer` | `EnrollmentConsumer` |
| `payment.queue` | Платёж захвачен | `PaymentProducer` | `PaymentConsumer` |
| `teacher.notification.queue` | Решение по заявке | `TeacherNotificationProducer` | `TeacherNotificationConsumer` |
| `subscription.queue` | Активация/отмена подписки | `SubscriptionProducer` | `SubscriptionConsumer` |
| `activity.queue` | Событие в ленту | `ActivityProducer` | `ActivityConsumer` |

Каждая очередь имеет соответствующий DLQ: `email.dlq`, `certificate.dlq`, и т.д.

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
| `/courses/**` | Управление курсами |
| `/lessons/**` | Управление уроками |
| `/quizzes/**` | Квизы студентов |
| `/api/teacher-quiz/**` | Квалификационные квизы |
| `/teacher-applications/**` | Заявки в преподаватели |
| `/payments/paypal/**` | PayPal: разовые платежи |
| `/subscriptions/paypal/**` | PayPal: подписки |
| `/api/certificates/**` | Сертификаты |
| `/progress/**` | Прогресс студента |
| `/courses/{id}/ratings/**` | Рейтинги курсов |
| `/lessons/{id}/comments/**` | Комментарии к урокам |
| `/enrollments/**` | Запись на курсы |
| `/activity/**` | Лента активности |
| `/bookmarks/**` | Закладки |
| `/profile/**` | Профиль пользователя |

### Полный список эндпоинтов

#### Authentication
- `POST /auth/register`
- `POST /auth/verify`
- `POST /auth/login`

#### Courses
- `POST /courses`
- `GET /courses/my`
- `GET /courses/{courseId}`
- `PUT /courses/{courseId}`
- `DELETE /courses/{courseId}`
- `GET /courses/public`

#### Lessons
- `POST /lessons/course/{courseId}`
- `GET /lessons/course/{courseId}`
- `GET /lessons/{lessonId}`
- `PUT /lessons/{lessonId}`
- `DELETE /lessons/{lessonId}`

#### Enrollments
- `POST /enrollments/free/{courseId}`
- `GET /enrollments/check/{courseId}`
- `GET /enrollments/my`

#### PayPal Payments
- `POST /payments/paypal/orders/course/{courseId}`
- `POST /payments/paypal/orders/capture`
- `GET /payments/paypal/my`

#### PayPal Subscriptions
- `GET /subscriptions/paypal/plan`
- `POST /subscriptions/paypal/create`
- `POST /subscriptions/paypal/confirm`
- `GET /subscriptions/paypal/my`

#### Quizzes
- `POST /quizzes/lesson/{lessonId}`
- `GET /quizzes/{quizId}`
- `GET /quizzes/lesson/{lessonId}`
- `PUT /quizzes/{quizId}`
- `DELETE /quizzes/{quizId}`

#### Quiz Attempts
- `POST /quiz-attempts/submit`
- `GET /quiz-attempts/my`

#### Progress
- `POST /progress/complete`
- `GET /progress`
- `GET /progress/lesson-unlocked`

#### Lesson Comments
- `POST /lessons/{lessonId}/comments`
- `GET /lessons/{lessonId}/comments`
- `PATCH /lessons/{lessonId}/comments/{commentId}/mark-answer`
- `DELETE /lessons/{lessonId}/comments/{commentId}`

#### Course Ratings
- `POST /courses/{courseId}/ratings`
- `GET /courses/{courseId}/ratings`
- `DELETE /courses/{courseId}/ratings`

#### Teacher Applications
- `POST /teacher-applications`
- `GET /teacher-applications`
- `GET /teacher-applications/pending`
- `POST /teacher-applications/{applicationId}/approve`
- `POST /teacher-applications/{applicationId}/reject`
- `GET /teacher-applications/{applicationId}/resume`

#### Teacher Quiz
- `GET /api/teacher/quiz/{applicationId}/questions`
- `POST /api/teacher/quiz/{applicationId}/submit`
- `GET /api/teacher/quiz/{applicationId}/result`

#### Certificates
- `POST /api/certificates/issue`
- `POST /api/certificates/{id}/regenerate`
- `GET /api/certificates/{id}`
- `GET /api/certificates/verify/{verificationCode}`

#### Activity Feed
- `GET /activity`

---

## Запуск проекта

### Требования

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (рекомендуется)
- Python 3.10+ (для AI-сервиса без Docker)

### 🐳 Через Docker Compose (рекомендуется)

```bash
# Клонировать репозиторий
git clone <repo-url>
cd project

# Создать .env на основе примера
cp .env.example .env
# Заполнить переменные (MongoDB URI, PayPal, Cloudinary и т.д.)

# Запустить всё одной командой
docker-compose up --build
```

Docker Compose поднимает три контейнера:
- **diplom-app** — Spring Boot бэкенд на порту `8080`
- **redis** — Redis 7 на порту `6379`
- **rabbitmq** — RabbitMQ 3 с management UI на портах `5672` и `15672`

> RabbitMQ Management UI: `http://localhost:15672` (login: `guest` / `guest`)

### Вручную (без Docker)

```bash
# 1. Запустить Redis
redis-server

# 2. Запустить RabbitMQ
rabbitmq-server

# 3. Собрать и запустить Spring Boot
mvn spring-boot:run
```

### AI-сервис (FastAPI)

```bash
cd src/main/java/com/diploma/Diplom/Ai/

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

### Spring Boot (`.env`)

```properties
# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/lms_db

# JWT
JWT_SECRET=<минимум 256-бит ключ>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Email (SMTP)
SPRING_MAIL_USERNAME=your@email.com
SPRING_MAIL_PASSWORD=your_app_password

# Cloudinary
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

# PayPal
PAYPAL_CLIENT_ID=...
PAYPAL_CLIENT_SECRET=...
PAYPAL_BASE_URL=https://api-m.sandbox.paypal.com
PAYPAL_SUBSCRIPTION_PLAN_ID=...

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# AI Service
AI_RESUME_API_URL=http://localhost:8000
```

### AI-сервис

```bash
API_KEY=your_secret_api_key_here
```

---

## Тесты

### Запуск

```bash
# Все тесты
mvn test

# Один класс
mvn test -Dtest=CourseServiceTest

# Один метод
mvn test -Dtest=CourseServiceTest#createCourse_paidWithoutPrice_throws
```

### Покрытие

| Тест-класс | Кол-во тестов | Что проверяется |
|------------|---------------|-----------------|
| `AuthServiceTest` | 11 | Регистрация, верификация, логин |
| `CourseServiceTest` | 12 | CRUD курсов, доступ, Cloudinary |
| `EnrollmentServiceTest` | 6 | Доступ к курсу, запись |
| `QuizServiceTest` | 8 | Создание/удаление квизов, валидация |
| `CourseProgressServiceTest` | — | Прогресс, async activity, сертификаты |
| `CourseRatingServiceTest` | — | Рейтинги, пересчёт среднего |
| `BookmarkServiceTest` | — | Закладки, activity |
| `LikeServiceTest` | — | Лайки, Redis-счётчик |
| `VerificationCodeRedisServiceTest` | — | OTP в Redis, TTL, верификация |
| `EmailProducerTest` | 3 | Отправка в RabbitMQ |
| `ProfileServiceTest` | — | Профиль пользователя |
| `QuizAttemptServiceTest` | — | Попытки квиза |
| `RepositoryTest` | — | MongoDB-запросы |
| `SecurityRoutesTest` | — | Защищённые/публичные маршруты |
| `AuthControllerTest` | — | HTTP-уровень аутентификации |
| `CourseControllerTest` | — | HTTP-уровень курсов |

---

## Основные сущности

- `User`
- `Course`
- `Lesson`
- `Enrollment`
- `Subscription`
- `Payment`
- `Quiz` / `QuizQuestion` / `QuizAttempt`
- `TeacherApplication`
- `TeacherQuizQuestion` / `TeacherQuizAttempt`
- `CourseProgress` / `LessonProgress`
- `Comment`
- `CourseRating`
- `Certificate`
- `ActivityFeed`
- `Bookmark`
- `Like`

---

## TODO — Будущая работа

### Frontend (React / Next.js)
Бэкенд полностью готов, фронтенд отсутствует. Необходимо:
- Страницы авторизации (регистрация → верификация email → логин)
- Каталог курсов с фильтрацией по категории, уровню, цене
- Личный кабинет студента: мои курсы, прогресс, сертификаты
- Кабинет преподавателя: создание/редактирование курсов, уроков, квизов
- Панель администратора: заявки преподавателей, одобрение/отклонение
- Интеграция PayPal Buttons SDK
- Просмотрщик уроков с видеоплеером

### Роль ADMIN
- `AdminController` с эндпоинтами для управления пользователями
- Просмотр и обработка заявок `TeacherApplication` в статусе `PENDING`
- Управление публикацией курсов

### Продакшен-конфигурация
- Заменить `allowedOriginPatterns(*)` в `SecurityConfig` на конкретные домены
- Настроить реальные PayPal credentials (сейчас sandbox)
- Добавить rate limiting на эндпоинты аутентификации (защита от brute-force)
- Настроить HTTPS / SSL
- Redis Cluster / Sentinel для отказоустойчивости

### AI-сервис — улучшения
- Добавить `Dockerfile` и переменные окружения в `docker-compose`
- Расширить набор признаков модели (сейчас 6 фич — мало для продакшена)
- Добавить логирование запросов к `/analyze` для мониторинга
- Написать тесты для API с mock-моделью

### Мониторинг и логирование
- Добавить структурированное логирование (JSON-формат для продакшена)
- Настроить алерты на DLQ (Dead Letter Queue) в RabbitMQ
- Подключить метрики (Spring Actuator + Prometheus/Grafana)
- Настроить алерты на критические ошибки (email или Slack)

### Документация API
- Добавить `@Operation` и `@ApiResponse` ко всем контроллерам
- Описать все DTO через `@Schema`
- Добавить примеры запросов/ответов в Swagger

---

## 📄 Лицензия

Дипломный проект. Все права защищены.
