# Diplom LMS Backend

Backend часть дипломного проекта для образовательной платформы с ролями пользователей, заявками преподавателей, автоматическим анализом резюме, курсами, уроками, квизами и загрузкой файлов.

## Возможности проекта

Система поддерживает:

- регистрацию и авторизацию пользователей через JWT
- роли:
  - `STUDENT`
  - `TEACHER`
  - `ADMIN`
- подачу заявки преподавателя
- загрузку PDF-резюме
- автоматический анализ резюме
- подтверждение преподавателя администратором
- создание и управление курсами
- создание и управление уроками
- загрузку thumbnail, видео и PDF-лекций
- создание и управление квизами
- просмотр загруженных файлов через браузер

## Технологии

- Java
- Spring Boot
- Spring Security
- JWT
- MongoDB
- Apache PDFBox
- Lombok
- Maven

## Архитектура проекта

Основные модули:

- `auth` — регистрация вход и верификация через email
- `security` — JWT-фильтр и конфигурация безопасности
- `teacher applications` — заявки преподавателей и анализ резюме
- `courses` — управление курсами
- `lessons` — управление уроками
- `quizzes` — управление тестами
- `files` — хранение и отдача файлов

## Роли и логика доступа

### STUDENT
Может просматривать курсы, уроки и квизы после подключения student-side логики.

### TEACHER
Может:
- подать заявку преподавателя
- после подтверждения администратором создавать курсы, уроки и квизы

### ADMIN
Может:
- просматривать заявки преподавателей
- подтверждать или отклонять заявки

## Teacher approval logic

Пользователь с ролью `TEACHER` не может сразу создавать курсы.

Для этого:

1. пользователь регистрируется как `TEACHER`
2. подаёт заявку преподавателя
3. загружает PDF-резюме
4. система анализирует резюме
5. администратор подтверждает заявку
6. после этого `teacherApproved = true`
7. только после этого преподаватель может создавать курсы и уроки

## Автоматический анализ резюме

Проект использует локальный rule-based AI module для анализа резюме.

### Что делает анализатор

- извлекает текст из PDF
- поддерживает английские и русские ключевые слова
- определяет:
  - образование
  - преподавательский опыт
  - навыки
  - проекты
  - технические компетенции
- вычисляет:
  - `aiScore`
  - `aiSummary`
  - `aiStrengths`
  - `aiWeaknesses`
  - `aiRecommendation`

### Почему используется локальный AI agent

В проекте используется локальный интеллектуальный модуль, чтобы:

- не зависеть от внешнего платного API
- упростить разработку и тестирование
- обеспечить работу системы без внешних сервисов

## Структура данных

### User
Содержит:
- id
- email
- password
- role
- teacherApproved

### TeacherApplication
Содержит:
- userId
- fullName
- email
- resumeText
- resumeFileName
- resumeFileUrl
- specialization
- yearsOfExperience
- aiScore
- aiSummary
- aiStrengths
- aiWeaknesses
- aiRecommendation
- status
- reviewComment
- createdAt

### Course
Содержит:
- id
- title
- description
- teacherId
- category
- level
- thumbnail
- published
- createdAt
- updatedAt

### Lesson
Содержит:
- id
- courseId
- title
- description
- orderIndex
- duration
- videoUrl
- videoFileName
- lectureText
- lecturePdfUrl
- lecturePdfFileName
- published
- createdAt
- updatedAt

### Quiz
Содержит:
- id
- lessonId
- title
- questions
- published
- createdAt
- updatedAt

### QuizQuestion
Содержит:
- question
- options
- correctAnswerIndex

## Настройка проекта

### 1. Клонировать проект

```bash
git clone https://github.com/uuuuuu-ops/Diplom
cd Diplom

### 2. Настроить Монгодб
Запустить дб локально и добавитьв application-properties
spring.application.name=Diplom
server.port=8080

spring.data.mongodb.uri=mongodb://localhost:8080/diplom

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=550MB

app.upload.dir=uploads

jwt.secret=MySuperSecretKeyForJwtToken123456789
jwt.expiration=86400000

logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=INFO
logging.level.com.diploma.Diplom=DEBUG


##Endpoints

AUTH

POST /auth/register

POST /auth/login

POST /auth/verify

TEACHER APPLICATIONS

POST /teacher-applications

GET /teacher-applications

GET /teacher-applications/pending

POST /teacher-applications/{applicationId}/approve

POST /teacher-applications/{applicationId}/reject

COURSES

POST /courses

GET /courses/my

GET /courses/{courseId}

PUT /courses/{courseId}

DELETE /courses/{courseId}

LESSONS

POST /lessons/course/{courseId}

GET /lessons/course/{courseId}

GET /lessons/{lessonId}

PUT /lessons/{lessonId}

DELETE /lessons/{lessonId}

 QUIZZES

POST /quizzes/lesson/{lessonId}

GET /quizzes/lesson/{lessonId}

GET /quizzes/{quizId}

PUT /quizzes/{quizId}

DELETE /quizzes/{quizId}

 FILES

GET /files?path=...

##Тестирование через Postman

###Авторизация

Сначала выполнить:

POST /auth/login

Получить JWT token.

Во все защищённые запросы добавить заголовок:

Authorization: Bearer YOUR_TOKEN

2. Создание курса

Использовать form-data в Postman.

3. Создание урока

Использовать form-data в Postman.

4. Создание квиза

Использовать raw JSON.

###Возможные ошибки

403 Forbidden

Причины:

отсутствует JWT токен

неверная роль

teacherApproved = false

400 Bad Request

Причины:

отсутствуют обязательные поля

неверный form-data

ошибки в JSON

Lesson not found / Course not found

Проверь правильность lessonId и courseId

Quiz already exists for this lesson

Урок уже содержит квиз

###Безопасность

В проекте используется:

JWT authentication

role-based authorization

ownership validation

teacher approval check

###Проверки гарантируют, что:

преподаватель может изменять только свои курсы

преподаватель может изменять только свои уроки

преподаватель может изменять только свои квизы

неподтверждённый teacher не может создавать курсы и уроки

Дальнейшее развитие проекта

###Следующие улучшения могут включать:

student enrollment

прохождение уроков студентами

сдача квизов

хранение результатов квизов

progress tracking

рейтинг преподавателей

comments/reviews

облачное хранение видео и PDF

streaming для видео

Статус проекта

Проект находится в активной разработке и уже включает основной backend функционал для LMS платформы.

Автор

Diploma project backend by Rinat , Miierzhan and Rassul

