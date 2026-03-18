Diplom LMS Backend

Backend часть дипломного проекта образовательной платформы. Система реализует управление пользователями, курсами, уроками, тестами и заявками преподавателей с автоматическим анализом резюме.

Возможности проекта

Платформа поддерживает:

регистрацию и авторизацию пользователей через JWT

роли пользователей: STUDENT, TEACHER, ADMIN

подачу заявки преподавателя

загрузку PDF-резюме

автоматический анализ резюме

подтверждение преподавателя администратором

создание и управление курсами

создание и управление уроками

загрузку thumbnail, видео и PDF-лекций

создание и управление квизами

просмотр загруженных файлов через браузер

Технологии

Java

Spring Boot

Spring Security

JWT

MongoDB

Apache PDFBox

Lombok

Maven

Архитектура проекта

Основные модули:

auth — регистрация, вход и верификация

security — JWT-фильтр и конфигурация безопасности

teacher-applications — заявки преподавателей и анализ резюме

courses — управление курсами

lessons — управление уроками

quizzes — управление тестами

files — хранение и отдача файлов

Роли и логика доступа

STUDENT
Может просматривать курсы, уроки и квизы после реализации student-side логики.

TEACHER
Может подать заявку преподавателя и после подтверждения администратором создавать курсы, уроки и квизы.

ADMIN
Может просматривать заявки преподавателей и подтверждать или отклонять их.

Teacher Approval Logic

Пользователь с ролью TEACHER не может сразу создавать курсы.

Процесс:

пользователь регистрируется как TEACHER

подаёт заявку преподавателя

загружает PDF-резюме

система анализирует резюме

администратор подтверждает заявку

устанавливается teacherApproved = true

после этого преподаватель получает доступ к созданию курсов и уроков

Автоматический анализ резюме

Используется локальный rule-based модуль.

Анализатор:

извлекает текст из PDF

поддерживает английские и русские ключевые слова

определяет образование, опыт, навыки, проекты и технические компетенции

вычисляет aiScore, aiSummary, aiStrengths, aiWeaknesses, aiRecommendation

Причины использования локального модуля:

отсутствие зависимости от внешних API

простота тестирования

автономная работа системы

Структура данных

User:

id

email

password

role

teacherApproved

TeacherApplication:

userId

fullName

email

resumeText

resumeFileName

resumeFileUrl

specialization

yearsOfExperience

aiScore

aiSummary

aiStrengths

aiWeaknesses

aiRecommendation

status

reviewComment

createdAt

Course:

id

title

description

teacherId

category

level

thumbnail

published

createdAt

updatedAt

Lesson:

id

courseId

title

description

orderIndex

duration

videoUrl

videoFileName

lectureText

lecturePdfUrl

lecturePdfFileName

published

createdAt

updatedAt

Quiz:

id

lessonId

title

questions

published

createdAt

updatedAt

QuizQuestion:

question

options

correctAnswerIndex

Настройка проекта

Клонирование:

git clone https://github.com/uuuuuu-ops/Diplom

cd Diplom

Настройка application.properties:

spring.application.name=Diplom
server.port=8080

spring.data.mongodb.uri=mongodb://localhost:27017/diplom

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=550MB

app.upload.dir=uploads

jwt.secret=MySuperSecretKeyForJwtToken123456789
jwt.expiration=86400000

logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=INFO
logging.level.com.diploma.Diplom=DEBUG

Endpoints

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

Тестирование через Postman

Авторизация:

POST /auth/login

После получения JWT необходимо добавлять заголовок:

Authorization: Bearer YOUR_TOKEN

Создание курса и урока выполняется через form-data.
Создание квиза — через raw JSON.

Возможные ошибки

403 Forbidden:

отсутствует JWT токен

неверная роль

teacherApproved = false

400 Bad Request:

отсутствуют обязательные поля

неверный формат form-data

ошибки в JSON

Lesson not found / Course not found:

проверь правильность ID

Quiz already exists for this lesson:

у урока уже есть квиз

Безопасность

Реализовано:

JWT authentication

role-based authorization

проверка владения ресурсами

проверка teacherApproved

Гарантии:

преподаватель может изменять только свои курсы

преподаватель может изменять только свои уроки

преподаватель может изменять только свои квизы

неподтверждённый преподаватель не может создавать контент

Дальнейшее развитие

регистрация студентов на курсы

прохождение уроков

сдача квизов

хранение результатов

отслеживание прогресса

рейтинги преподавателей

комментарии и отзывы

облачное хранение файлов

видео-стриминг

Статус проекта

Проект находится в активной разработке и уже реализует основной backend функционал LMS системы.

Автор

Diploma project backend by Rinat, Miierzhan and Rassul
