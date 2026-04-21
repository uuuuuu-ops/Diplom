# LMS Diploma — Learning Management System

> Дипломный проект: полнофункциональная платформа онлайн-обучения на Spring Boot с монетизацией через PayPal, асинхронной обработкой событий через RabbitMQ, кешированием в Redis и ИИ-скринингом кандидатов в преподаватели.

---

## 🗂 Содержание

- [Обзор проекта](#обзор-проекта)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Модули системы](#модули-системы)
- [Асинхронные события (RabbitMQ)](#асинхронные-события-rabbitmq)
- [Кеширование (Redis)](#кеширование-redis)
- [API — краткий справочник](#api--краткий-справочник)
- [Запуск проекта](#запуск-проекта)
- [Docker Compose](#docker-compose)
- [ИИ-модуль: Resume Screener](#ии-модуль-resume-screener)
- [Переменные окружения](#переменные-окружения)
- [Тестирование](#тестирование)

---

## Обзор проекта

LMS Diploma — REST-бэкенд системы управления обучением (Learning Management System), разработанный на **Spring Boot 4 / Java 21** с **MongoDB Atlas**. Платформа поддерживает три роли пользователей: **STUDENT**, **TEACHER** и **ADMIN**. Монетизация реализована через PayPal (разовые платежи и периодические подписки). Для отбора кандидатов в преподаватели используется отдельный Python-сервис на FastAPI с моделью машинного обучения.

### Ключевые возможности

- Регистрация с email-верификацией (6-значный код, Redis TTL 10 мин) и JWT-аутентификация
- Управление курсами, уроками, квизами, прогрессом и сертификатами
- Монетизация: разовая покупка курса и периодические подписки (PayPal REST API)
- Асинхронная обработка событий через **RabbitMQ** (email, сертификаты, активность, платежи, подписки, уведомления)
- Кеширование через **Redis** (курсы, рейтинги, прогресс, подписки, лайки, лента активности)
- Скрининг резюме преподавателей через ML-модель (Random Forest, FastAPI)
- Генерация PDF-сертификатов с QR-кодом по завершении курса
- Загрузка медиафайлов через **Cloudinary**
- Лента активности, лайки, закладки и комментарии к урокам и курсам
- Полная документация **Swagger / OpenAPI 3**

---

## Архитектура

```
┌──────────────────────────────────────────────────────────┐
│                    Frontend (React)                      │
└────────────────────────┬─────────────────────────────────┘
                         │ HTTP / REST
┌────────────────────────▼─────────────────────────────────┐
│             Spring Boot Backend  :8080                   │
│                                                          │
│  controller/   service/    repository/   messaging/      │
│  model/        dto/        security/     config/         │
│  exception/    mapper/     util/                         │
└───────┬──────────────────────┬───────────────────────────┘
        │                      │
   ┌────▼────┐           ┌─────▼──────┐
   │ MongoDB │           │  RabbitMQ  │
   │  Atlas  │           │  :5672     │
   └─────────┘           └────────────┘
        │
   ┌────▼────┐           ┌────────────┐
   │  Redis  │           │  FastAPI   │
   │  :6379  │           │  AI :8000  │
   └─────────┘           └─────┬──────┘
                               │ joblib
                        ┌──────▼──────┐
                        │ resume_model│
                        │ .pkl (RF)   │
                        └─────────────┘
```

---

## Технологический стек

| Слой | Технология |
|------|------------|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data MongoDB |
| Аутентификация | JWT (jjwt 0.11.5), BCrypt |
| База данных | MongoDB Atlas |
| Кеш | Redis 7, Spring Cache |
| Очередь сообщений | RabbitMQ 3, Spring AMQP |
| Платежи | PayPal REST API (Orders + Subscriptions) |
| Медиафайлы | Cloudinary |
| Документация | SpringDoc OpenAPI 3 / Swagger UI |
| Сертификаты | Apache PDFBox, OpenHTMLtoPDF, ZXing (QR-коды) |
| Email | Spring Mail (Gmail SMTP) |
| ИИ-сервис | Python 3.10+, FastAPI, scikit-learn, joblib, pandas |
| Шаблоны | Thymeleaf (HTML-шаблоны сертификатов) |
| Сборка | Maven 3.8+, Lombok 1.18 |
| Контейнеры | Docker, Docker Compose |

---

## Модули системы

### 🔐 Auth — Аутентификация

Регистрация с обязательной email-верификацией. Коды генерируются через `SecureRandom`, хранятся в Redis с TTL 10 минут и удаляются после успешной верификации.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/auth/register` | POST | Публичный | Регистрация, отправка кода на email |
| `/auth/verify` | POST | Публичный | Подтверждение email по 6-значному коду |
| `/auth/login` | POST | Публичный | Получение JWT-токена |

---

### 👤 Profile — Профиль пользователя

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/profile` | GET | Аутентификация | Получить свой профиль + лента активности |
| `/profile` | PATCH | Аутентификация | Обновить имя, возраст, аватар |

---

### 📖 Course — Управление курсами

CRUD для курсов с поддержкой бесплатных и платных (USD) вариантов. Загрузка превью через Cloudinary. Средний рейтинг `avgRating` пересчитывается автоматически.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/courses/public` | GET | Публичный | Каталог опубликованных курсов (пагинация, фильтр по категории/уровню) |
| `/courses/{id}` | GET | Аутентификация | Детали курса |
| `/courses` | POST | TEACHER | Создать курс (multipart: данные + превью) |
| `/courses/my` | GET | TEACHER | Мои курсы |
| `/courses/{id}` | PUT | TEACHER | Обновить курс |
| `/courses/{id}` | DELETE | TEACHER | Удалить курс |

---

### 📝 Lesson — Уроки

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/lessons/course/{courseId}` | POST | TEACHER | Добавить урок |
| `/lessons/course/{courseId}` | GET | Аутентификация | Список уроков курса |
| `/lessons/{id}` | GET | Аутентификация | Детали урока |
| `/lessons/{id}` | PUT | TEACHER | Обновить урок |
| `/lessons/{id}` | DELETE | TEACHER | Удалить урок |

---

### 🧪 Quiz — Квизы студентов

Каждый урок может иметь один квиз. Поддерживается проходной балл (`passingScore`, по умолчанию 60), таймер и хранение истории попыток.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/quizzes/lesson/{lessonId}` | POST | TEACHER | Создать квиз |
| `/quizzes/lesson/{lessonId}` | GET | Аутентификация | Квиз урока |
| `/quizzes/{id}` | GET | Аутентификация | Детали квиза |
| `/quizzes/{id}` | PUT | TEACHER | Обновить квиз |
| `/quizzes/{id}` | DELETE | TEACHER | Удалить квиз |
| `/quiz-attempts/submit` | POST | STUDENT | Отправить ответы |
| `/quiz-attempts/my` | GET | STUDENT | Мои попытки |

---

### 📊 Progress — Прогресс студента

Автоматически вычисляет `progressPercent`. При 100% прогрессе инициирует генерацию сертификата через RabbitMQ.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/progress/complete` | POST | STUDENT | Отметить урок выполненным |
| `/progress` | GET | STUDENT | Прогресс по курсу |
| `/progress/lesson-unlocked` | GET | STUDENT | Проверить доступность урока |

---

### 🎓 Certificate — Сертификаты

PDF генерируется асинхронно через RabbitMQ. Содержит QR-код для верификации по уникальному UUID.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/api/certificates/{id}` | GET | Аутентификация | Получить сертификат |
| `/api/certificates/verify/{code}` | GET | Публичный | Верифицировать сертификат по QR |

---

### 👨‍🏫 TeacherApplication — Заявки преподавателей

Двухэтапный отбор: AI-скрининг резюме → квалификационный квиз → решение администратора.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/teacher-applications` | POST | TEACHER | Подать заявку с резюме (PDF) |
| `/teacher-applications` | GET | TEACHER | Свои заявки |
| `/teacher-applications/pending` | GET | ADMIN | Все заявки на рассмотрении |
| `/teacher-applications/{id}/approve` | POST | ADMIN | Одобрить заявку |
| `/teacher-applications/{id}/reject` | POST | ADMIN | Отклонить заявку |
| `/teacher-applications/{id}/resume` | GET | ADMIN | Скачать резюме |

---

### 💳 Payment — Платежи (PayPal Orders)

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/payments/paypal/orders/course/{id}` | POST | STUDENT | Создать заказ на курс |
| `/payments/paypal/orders/capture` | POST | STUDENT | Подтвердить платёж |
| `/payments/paypal/my` | GET | STUDENT | История платежей |

---

### 🔄 Subscription — Подписки (PayPal Subscriptions)

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/subscriptions/paypal/plan` | GET | STUDENT | Информация о тарифе |
| `/subscriptions/paypal/save-pending` | POST | STUDENT | Сохранить pending-подписку |
| `/subscriptions/paypal/confirm` | POST | STUDENT | Активировать подписку |
| `/subscriptions/paypal/my` | GET | STUDENT | Мои подписки |

---

### 💬 Comments — Комментарии

Поддержка вложенных ответов (дерево). Целевой тип — `COURSE` или `LESSON`. При добавлении комментария отправляется уведомление через RabbitMQ.

| Эндпоинт | Метод | Доступ | Описание |
|----------|-------|--------|----------|
| `/comments/{type}/{targetId}` | POST | Аутентификация | Добавить комментарий |
| `/comments/{type}/{targetId}` | GET | Аутентификация | Список комментариев с ответами |

---

### ⭐ Ratings, ❤️ Likes, 🔖 Bookmarks, 📰 Activity

| Эндпоинт | Описание |
|----------|----------|
| `POST/GET/DELETE /courses/{id}/ratings` | Оценки курса (1–5, только записанные студенты) |
| `POST /likes/course/{id}` | Переключить лайк курса |
| `GET /likes/course/{id}/status` | Статус лайка + счётчик (кеш Redis) |
| `POST /bookmarks/{courseId}` | Переключить закладку |
| `GET /bookmarks` | Мои закладки |
| `GET /activity` | Лента активности (кеш Redis 2 мин) |

---

## Асинхронные события (RabbitMQ)

Все длительные или побочные операции выполняются асинхронно через очереди. Каждая очередь имеет **dead-letter queue** (DLQ) для обработки ошибок доставки.

| Очередь | Exchange | Продюсер | Консьюмер | Назначение |
|---------|----------|----------|-----------|------------|
| `email.queue` | `email.exchange` | `EmailProducer` | `EmailConsumer` | Отправка писем (верификация, уведомления) |
| `certificate.queue` | `certificate.exchange` | `CertificateProducer` | `CertificateConsumer` | Генерация PDF-сертификата |
| `notification.queue` | `notification.exchange` | `CommentNotificationProducer` | `CommentNotificationConsumer` | Уведомления о новых комментариях |
| `activity.queue` | `activity.exchange` | `ActivityProducer` | `ActivityConsumer` | Запись событий в ленту активности |
| `payment.queue` | `payment.exchange` | `PaymentProducer` | `PaymentConsumer` | Активация enrollment после платежа |
| `enrollment.queue` | `enrollment.exchange` | `EnrollmentProducer` | — | Welcome-событие при записи на курс |
| `subscription.queue` | `subscription.exchange` | `SubscriptionProducer` | — | Уведомление об изменении подписки |

---

## Кеширование (Redis)

Spring Cache с `RedisCacheManager`. Ключи инвалидируются при каждом изменении соответствующих данных с помощью `@CacheEvict`.

| Кеш | TTL | Что хранит |
|-----|-----|-----------|
| `course` | 10 мин | Отдельный курс по ID |
| `courses` | 2 мин | Публичный каталог с фильтрами |
| `courseRating` | 10 мин | Рейтинги курса |
| `access` | 5 мин | Факт доступа пользователя к курсу |
| `progress` | 5 мин | Прогресс пользователя по курсу |
| `subscription` | 3 мин | Наличие активной подписки |
| `activityFeed` | 2 мин | Лента активности пользователя |
| `likes:course:{id}` | 30 мин | Счётчик лайков курса (Redis напрямую) |
| `verification:{email}` | 10 мин | Код верификации email (Redis напрямую) |

Сериализация значений — JSON через `GenericJackson2JsonRedisSerializer` с поддержкой `LocalDateTime` (`JavaTimeModule`).

---

## Запуск проекта

### Требования

- Java 21+
- Maven 3.8+
- MongoDB Atlas (или локальный MongoDB 6+)
- Redis 7+
- RabbitMQ 3+
- Python 3.10+ (для AI-сервиса, опционально)

### Локальный запуск

```bash
# 1. Клонировать репозиторий
git clone <repo-url>
cd Diplom

# 2. Создать файл .env в корне проекта (см. раздел "Переменные окружения")

# 3. Поднять Redis и RabbitMQ через Docker
docker run -d -p 6379:6379 redis:7-alpine
docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 4. Запустить Spring Boot
mvn spring-boot:run
```

### AI-сервис (FastAPI)

```bash
cd src/main/java/com/diploma/Diplom/Ai/

pip install fastapi uvicorn joblib scikit-learn pandas

# Обучить модель (один раз, создаёт resume_model.pkl)
python train_model.py

# Запустить сервис
uvicorn api:app --host 0.0.0.0 --port 8000 --reload
```

> **Важно:** перед запуском необходимо сгенерировать `resume_model.pkl` через `train_model.py`.

---

## Docker Compose

Проект содержит готовый `docker-compose.yml`. MongoDB подключается через Atlas (переменная окружения), AI-сервис запускается отдельно.

```bash
# Создать .env с переменными (см. ниже)

# Поднять всё
docker compose up -d

# Остановить
docker compose down
```

**Сервисы:**

| Сервис | Образ | Порты |
|--------|-------|-------|
| `app` | Собирается из `Dockerfile` | `8080:8080` |
| `redis` | `redis:7-alpine` | `6379:6379` |
| `rabbitmq` | `rabbitmq:3-management` | `5672`, `15672` (UI) |

RabbitMQ Management UI доступен по адресу `http://localhost:15672` (guest / guest).

---

## ИИ-модуль: Resume Screener

Отдельный FastAPI-сервис для скрининга резюме кандидатов в преподаватели на базе модели Random Forest.

### `POST /analyze`

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

**Ответ:**

```json
{
  "score": 88,
  "recommendation": "STRONG_FIT",
  "summary": "Кандидат отлично подходит...",
  "strengths": "Опыт 4 лет. Сертификация: AWS...",
  "weaknesses": "Явных слабостей не обнаружено."
}
```

| Рекомендация | Описание |
|--------------|----------|
| `STRONG_FIT` | Отличный кандидат |
| `GOOD_FIT` | Хороший кандидат, небольшие зоны роста |
| `NEEDS_REVIEW` | Требует дополнительной проверки |
| `WEAK_FIT` | Слабое соответствие |

**Аутентификация:** заголовок `X-API-Key: <ключ>`. **Health-check:** `GET /health`.

---

## Swagger UI

```
http://localhost:8080/swagger-ui.html
```

**Группы API:** Authentication · Courses · Lessons · Quizzes · Progress & Certificates · Enrollments & Payments · Teacher Applications · Comments · Profiles.

**Авторизация:** `POST /auth/login` → скопировать токен → нажать **Authorize** → ввести `Bearer <токен>`.

---

## Переменные окружения

Создайте файл `.env` в корне проекта:

```env
# MongoDB Atlas
SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/lms_db

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# JWT
JWT_SECRET=минимум-256-бит-случайная-строка
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Email (Gmail — нужен App Password)
SPRING_MAIL_USERNAME=your@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# PayPal (Sandbox)
PAYPAL_CLIENT_ID=your_client_id
PAYPAL_CLIENT_SECRET=your_client_secret
PAYPAL_BASE_URL=https://api-m.sandbox.paypal.com
PAYPAL_SUBSCRIPTION_PLAN_ID=your_plan_id

# AI Service
AI_RESUME_API_URL=http://localhost:8000

# Пути (опционально)
APP_BASE_URL=http://localhost:8080
APP_UPLOAD_DIR=uploads
CERTIFICATE_STORAGE_PATH=uploads/certificates
```

Для AI-сервиса (рядом с `api.py`):

```env
API_KEY=your_secret_api_key_here
```

---

## Тестирование

### Структура тестов

```
src/test/java/com/diploma/Diplom/
├── DiplomApplicationTests.java              — контекст загружается
├── auth/
│   └── AuthServiceTest.java                 — 10 unit-тестов
├── controller/
│   ├── AuthControllerTest.java              — 6 интеграционных (MockMvc)
│   ├── CourseControllerTest.java            — 13 интеграционных (MockMvc)
│   └── TestSecurityConfig.java             — тестовая конфигурация Security
├── repository/
│   └── RepositoryTest.java                  — 26 тестов (Embedded MongoDB)
├── security/
│   └── SecurityRoutesTest.java              — 15 тестов маршрутов Security
└── service/
    ├── ActivityFeedServiceTest.java         — 4 unit-теста
    ├── BookmarkServiceTest.java             — 6 unit-тестов
    ├── CommentServiceTest.java              — 8 unit-тестов
    ├── CourseProgressServiceTest.java       — 9 unit-тестов
    ├── CourseRatingServiceTest.java         — 10 unit-тестов
    ├── CourseServiceTest.java               — 23 unit-теста
    ├── EmailProducerTest.java               — 3 unit-теста
    ├── EnrollmentServiceTest.java           — 8 unit-тестов
    ├── LikeServiceTest.java                 — 6 unit-тестов
    ├── MessagingProducersTest.java          — 8 unit-тестов (4 продюсера)
    ├── ProfileServiceTest.java              — 6 unit-тестов
    ├── QuizAttemptServiceTest.java          — 10 unit-тестов
    ├── QuizServiceTest.java                 — 9 unit-тестов
    ├── SubscriptionServiceTest.java         — 8 unit-тестов
    └── VerificationCodeRedisServiceTest.java — 9 unit-тестов
```

**Итого: 183+ теста.**

### Запуск

```bash
# Все тесты
mvn test

# Один класс
mvn test -Dtest=CourseServiceTest

# Один метод
mvn test -Dtest=EnrollmentServiceTest#enrollFreeCourse_paidCourse_throws

# Только unit-тесты сервисов
mvn test -Dtest="*ServiceTest,*ProducerTest"
```

### Инструменты

| Инструмент | Назначение |
|------------|------------|
| JUnit 5 | Фреймворк тестирования |
| Mockito | Мокирование зависимостей |
| AssertJ | Выразительные проверки (`assertThat`) |
| MockMvc | HTTP-тесты контроллеров |
| Embedded MongoDB | Repository-тесты без внешней БД |
| Spring Security Test | `@WithMockUser`, `.with(user(...))` |

---

## 📄 Лицензия

Дипломный проект. Все права защищены.
