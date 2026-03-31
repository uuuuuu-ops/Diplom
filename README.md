#  Diplom LMS Backend

Backend часть дипломного проекта LMS (Learning Management System).  
Полноценная серверная архитектура для образовательной платформы с ролями, курсами, уроками и AI-анализом резюме.

---

##  Overview

Система предоставляет:

- аутентификацию и авторизацию (JWT)
- управление ролями пользователей
- процесс подачи и одобрения преподавателей
- управление курсами, уроками и тестами
- загрузку и хранение файлов
- автоматический анализ резюме
- оплату курсов через PayPal
- подписочную модель доступа
- систему enrollments и проверки доступа к курсам

---

##  Features

-  JWT Authentication & Authorization
-  Role-based Access (`STUDENT`, `TEACHER`, `ADMIN`)
-  Teacher Application System
-  AI Resume Analysis (local, no external API)
-  Course Management
-  Lesson Management (video + PDF)
-  Quiz System
-  File Upload & Serving
-  Resource Ownership Validation
- PayPal Course Payments
- PayPal Subscriptions
- Enrollment & Access Control

---

##  Project Structure


src/main/java/com/diploma/Diplom
│
├── controller # REST endpoints
├── service # business logic
├── repository # MongoDB access
├── model # entities
├── dto # DTO objects
├── security # JWT, filters, config
├── auth # authentication
├── ai # resume analysis
└── files # file handling


---

##  Tech Stack

| Category        | Technology |
|----------------|------------|
| Backend        | Java, Spring Boot |
| Security       | Spring Security, JWT |
| Database       | MongoDB |
| File Handling  | Apache PDFBox |
| Build Tool     | Maven |
| Utilities      | Lombok |

---

##  Roles & Permissions

# STUDENT
- Browse courses
- Enroll in free courses
- Buy paid courses
- Access lessons if enrolled or subscribed
# TEACHER
- Submit application
- Create courses, lessons and quizzes after approval
# ADMIN
- Review teacher applications
- Approve or reject teachers

---

##  Teacher Approval Workflow


- Register as TEACHER
- ↓
- Submit application
- ↓
- Upload resume (PDF)
- ↓
- AI analysis
- ↓
- Admin review
- ↓
- teacherApproved = true
- ↓
- Access to course creation


---

##  AI Resume Analysis

Local rule-based module:

### Extracts:
- education
- teaching experience
- skills
- projects
- technical stack

### Generates:
- `aiScore`
- `aiSummary`
- `aiStrengths`
- `aiWeaknesses`
- `aiRecommendation`

---

##  Data Models

### User
```
id
email
password
role
teacherApproved
```

### TeacherApplication
```
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
```

### Course
```
id
title
description
teacherId
category
level
thumbnail
published
free
price
currency
createdAt
updatedAt
```

### Lesson
```
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
```

### Quiz
```
id
lessonId
title
questions
published
createdAt
updatedAt
```
```
Enrollment
id
userId
courseId
createdAt
```

```
Payment
id
userId
courseId
orderId
amount
status
createdAt
```

```
Subscription
id
userId
subscriptionId
planType
status
createdAt
```
---

##  Getting Started

### 1. Clone repository


git clone https://github.com/uuuuuu-ops/Diplom

cd Diplom


### 2. Configure MongoDB


spring.data.mongodb.uri=mongodb://localhost:27017/diplom


### 3. Run application


mvn spring-boot:run


##  API Endpoints

###  AUTH

POST /auth/register  
Регистрация нового пользователя  

POST /auth/login  
Авторизация пользователя, возвращает JWT токен  

POST /auth/verify  
Подтверждение email пользователя  

---

###  TEACHER APPLICATIONS

POST /teacher-applications  
Создание заявки преподавателя с загрузкой резюме  

GET /teacher-applications  
Получение всех заявок (ADMIN)  

GET /teacher-applications/pending  
Получение всех заявок со статусом PENDING  

POST /teacher-applications/{applicationId}/approve  
Подтверждение заявки преподавателя (ADMIN)  

POST /teacher-applications/{applicationId}/reject  
Отклонение заявки преподавателя (ADMIN)  

---

###  COURSES

POST /courses  
Создание курса (только для подтверждённых преподавателей)  

GET /courses/my  
Получение курсов текущего преподавателя  

GET /courses/{courseId}  
Получение курса по ID  

PUT /courses/{courseId}  
Обновление курса (только владелец)  

DELETE /courses/{courseId}  
Удаление курса (только владелец)  

---

###  LESSONS

POST /lessons/course/{courseId}  
Создание урока в курсе  

GET /lessons/course/{courseId}  
Получение всех уроков курса  

GET /lessons/{lessonId}  
Получение урока по ID  

PUT /lessons/{lessonId}  
Обновление урока (только владелец курса)  

DELETE /lessons/{lessonId}  
Удаление урока (только владелец курса)  

---

###  QUIZZES

POST /quizzes/lesson/{lessonId}  
Создание квиза для урока  

GET /quizzes/lesson/{lessonId}  
Получение квиза по уроку  

GET /quizzes/{quizId}  
Получение квиза по ID  

PUT /quizzes/{quizId}  
Обновление квиза (только владелец)  

DELETE /quizzes/{quizId}  
Удаление квиза (только владелец)  

### Certificate

Выпустить сертификат
POST /api/certificates/issue?userId=USER_ID&courseId=COURSE_ID

Регенерировать
POST /api/certificates/{certificateId}/regenerate

Получить сертификат
GET /api/certificates/{certificateId}

Проверить сертификат
GET /api/certificates/verify/{verificationCode}


### PAYMENTS (PayPal)

POST /payments/paypal/orders/course/{courseId} — создать PayPal order для покупки курса

POST /payments/paypal/orders/capture — подтвердить оплату после approve

GET /payments/paypal/my — получить мои платежи

### SUBSCRIPTIONS (PayPal)

GET /subscriptions/paypal/plan — получить PayPal planId

POST /subscriptions/paypal/confirm — подтвердить подписку

POST /subscriptions/paypal/save-pending — сохранить pending subscription

GET /subscriptions/paypal/my — получить мои подписки

### ENROLLMENTS

POST /enrollments/free/{courseId} — записаться на бесплатный курс

GET /enrollments/check/{courseId} — проверить доступ к курсу

GET /enrollments/my — получить мои enrollments





## Frontend Integration Flow

The frontend should interact with the backend in the following way:


# AUTH

## Register

POST /auth/register
json{ "email": "user@mail.com", "password": "pass123", "role": "STUDENT" }
roles: STUDENT or TEACHER

## Login

POST /auth/login
json{ "email": "user@mail.com", "password": "pass123" }
→ returns JWT token. Store it and attach to every request.
Verify email
POST /auth/verify
json{ "email": "user@mail.com", "code": "123456" }


# Teacher Application Flow

After registering as `TEACHER`, the user must submit an application and wait for admin approval before they can create courses. `teacherApproved` in the JWT will flip to `true` once approved.

**Submit application + upload resume**
`POST /teacher-applications`
`Content-Type: multipart/form-data`

fullName: "Rassul Bekov"
specialization: "Backend Development"
yearsOfExperience: 3
resume: <PDF file>

→ AI automatically scores the resume and fills aiScore, aiSummary, aiStrengths, aiWeaknesses
Admin: get all applications
GET /teacher-applications 🔒 ADMIN
Admin: get pending only
GET /teacher-applications/pending 🔒 ADMIN
Admin: approve
POST /teacher-applications/{applicationId}/approve 🔒 ADMIN
Admin: reject
POST /teacher-applications/{applicationId}/reject 🔒 ADMIN
json{ "reviewComment": "Not enough experience" }

# COURSES  TEACHER (approved only)
Create course
POST /courses
json{
  "title": "Spring Boot Basics",
  "description": "Learn Spring Boot from scratch",
  "category": "Backend",
  "level": "BEGINNER",
  "thumbnail": "optional-url"
}
My courses
GET /courses/my
Get course by ID
GET /courses/{courseId}
Update course
PUT /courses/{courseId}
json{ "title": "Updated title", "description": "...", "published": true }


**Delete course**
`DELETE /courses/{courseId}`



# LESSONS TEACHER (course owner)

**Create lesson**
`POST /lessons/course/{courseId}`
`Content-Type: multipart/form-data`

title: "Intro to Controllers"
description: "..."
orderIndex: 1
duration: 30
lectureText: "optional inline text"
video: <video file>
lecturePdf: <PDF file>
Get all lessons in course
GET /lessons/course/{courseId}
Get lesson by ID
GET /lessons/{lessonId}
Update lesson
PUT /lessons/{lessonId}
Content-Type: multipart/form-data — same fields as create
Delete lesson
DELETE /lessons/{lessonId}

# QUIZZES 🔒 TEACHER (course owner)
Create quiz
POST /quizzes/lesson/{lessonId}
json{
  "title": "Controllers Quiz",
  "questions": [
    {
      "question": "What annotation maps HTTP requests?",
      "options": ["@Controller", "@Service", "@Repository", "@Component"],
      "correctAnswer": "@Controller"
    }
  ]
}


**Get quiz by lesson**
`GET /quizzes/lesson/{lessonId}`

**Get quiz by ID**
`GET /quizzes/{quizId}`

**Update quiz**
`PUT /quizzes/{quizId}` — same body as create

**Delete quiz**
`DELETE /quizzes/{quizId}`

---


# Paid Course Flow

User clicks Buy Course

Frontend calls:
POST /payments/paypal/orders/course/{courseId}
Backend returns PayPal order data

User approves payment in PayPal
Frontend sends:
POST /payments/paypal/orders/capture
Payment is saved in backend

# Subscription Flow
Frontend requests plan:
GET /subscriptions/paypal/plan

Frontend renders PayPal subscription button
After PayPal approval frontend receives subscriptionId
Frontend sends it to:
POST /subscriptions/paypal/confirm

# Optional pending flow:

POST /subscriptions/paypal/save-pending
Free Course Enrollment Flow

User clicks Enroll for Free
Frontend calls:
POST /enrollments/free/{courseId}
Enrollment is created

Access can be checked via:
GET /enrollments/check/{courseId}
Access Check

GET /enrollments/check/{courseId}

# Используется для:

показа кнопки Buy / Subscribe / Open
скрытия закрытого контента
проверки доступа к урокам курса

# FILES

**Serve any file (video, PDF, image)**
`GET /files?path=uploads/filename.pdf`

Pass the JWT header. Use the `videoUrl`, `lecturePdfUrl`, `resumeFileUrl`, and `thumbnail` values returned from other endpoints directly as the `path` param.

---

## Role → Page Map

| Role | Accessible pages |
|---|---|
| STUDENT | Browse courses, view lessons, take quizzes |
| TEACHER (pending) | Submit application, wait screen |
| TEACHER (approved) | Course CRUD, lesson CRUD, quiz CRUD |
| ADMIN | All applications, approve/reject panel |

---

## Frontend Routing + API Mapping

### AUTH

#### `/login`
Использует:
- `POST /auth/login`

Назначение:
- авторизация пользователя
- получение JWT токена

---

#### `/register`
Использует:
- `POST /auth/register`

Назначение:
- регистрация нового пользователя (`STUDENT` или `TEACHER`)

---

#### `/verify-email`
Использует:
- `POST /auth/verify`

Назначение:
- подтверждение email пользователя

---

### STUDENT

#### `/student/courses`
Использует:
- `GET /courses/{courseId}` *(для отдельной карточки курса или списка через будущий публичный список курсов)*
- `GET /enrollments/my`
- `GET /subscriptions/paypal/my`
- `GET /payments/paypal/my`

Назначение:
- просмотр доступных курсов
- отображение статуса доступа
- отображение купленных / доступных курсов

---

#### `/student/courses/:courseId`
Использует:
- `GET /courses/{courseId}`
- `GET /enrollments/check/{courseId}`

Дополнительно может вызывать:
- `POST /enrollments/free/{courseId}` — если курс бесплатный
- `POST /payments/paypal/orders/course/{courseId}` — если курс платный
- `GET /subscriptions/paypal/plan` — если доступ по подписке

Назначение:
- просмотр страницы курса
- показ кнопки `Enroll`, `Buy`, `Subscribe` или `Open`

---

#### `/student/courses/:courseId/lessons/:lessonId`
Использует:
- `GET /enrollments/check/{courseId}`
- `GET /lessons/{lessonId}`
- `GET /quizzes/lesson/{lessonId}`

Назначение:
- просмотр урока
- загрузка lesson content
- получение quiz для урока
- защита страницы через access check

---

#### `/student/payments`
Использует:
- `GET /payments/paypal/my`

Назначение:
- отображение истории платежей пользователя

---

#### `/student/subscriptions`
Использует:
- `GET /subscriptions/paypal/my`
- `GET /subscriptions/paypal/plan`

Назначение:
- просмотр текущей подписки
- отображение доступного PayPal plan

---

#### `/student/enrollments`
Использует:
- `GET /enrollments/my`

Назначение:
- отображение всех enrollment записей пользователя

---

#### `/student/courses/:courseId/buy`
Использует:
- `POST /payments/paypal/orders/course/{courseId}`

После approve:
- `POST /payments/paypal/orders/capture`

Назначение:
- запуск покупки платного курса через PayPal

---

#### `/student/courses/:courseId/subscribe`
Использует:
- `GET /subscriptions/paypal/plan`

После approve:
- `POST /subscriptions/paypal/confirm`

Опционально:
- `POST /subscriptions/paypal/save-pending`

Назначение:
- запуск подписочного PayPal flow

---

#### `/student/courses/:courseId/access`
Использует:
- `GET /enrollments/check/{courseId}`

Назначение:
- проверка, есть ли у пользователя доступ к курсу

---

### TEACHER

#### `/teacher/apply`
Использует:
- `POST /teacher-applications`

Назначение:
- подача заявки преподавателя
- загрузка resume PDF
- запуск AI resume analysis

---

#### `/teacher/application-status`
Использует:
- `GET /teacher-applications` *(если есть отдельный endpoint статуса для текущего пользователя — лучше использовать его, но из текущего README явно не показан)*

Назначение:
- просмотр статуса заявки преподавателя

---

#### `/teacher/courses`
Использует:
- `GET /courses/my`

Назначение:
- отображение всех курсов преподавателя

---

#### `/teacher/courses/new`
Использует:
- `POST /courses`

`Content-Type: multipart/form-data`

Поля:
- `title`
- `description`
- `category`
- `level`
- `thumbnailFile`
- `free`
- `price`

Назначение:
- создание нового курса

---

#### `/teacher/courses/:courseId/edit`
Использует:
- `GET /courses/{courseId}`
- `PUT /courses/{courseId}`

`Content-Type: multipart/form-data`

Поля:
- `title`
- `description`
- `category`
- `level`
- `published`
- `thumbnailFile`

Дополнительно:
- `DELETE /courses/{courseId}`

Назначение:
- редактирование курса
- публикация курса
- удаление курса

---

#### `/teacher/courses/:courseId/settings`
Использует:
- `GET /courses/{courseId}`
- `PUT /courses/{courseId}`

Назначение:
- настройка публикации курса
- обновление thumbnail
- настройка метаданных курса

---

#### `/teacher/courses/:courseId/lessons/new`
Использует:
- `POST /lessons/course/{courseId}`

Назначение:
- создание нового урока внутри курса

---

#### `/teacher/courses/:courseId/lessons/:lessonId/edit`
Использует:
- `GET /lessons/{lessonId}`
- `PUT /lessons/{lessonId}`
- `DELETE /lessons/{lessonId}`

Назначение:
- редактирование или удаление урока

---

#### `/teacher/courses/:courseId/lessons/:lessonId/quiz`
Использует:
- `POST /quizzes/lesson/{lessonId}`
- `GET /quizzes/lesson/{lessonId}`
- `GET /quizzes/{quizId}`
- `PUT /quizzes/{quizId}`
- `DELETE /quizzes/{quizId}`

Назначение:
- создание, просмотр, обновление и удаление квиза

---

### ADMIN

#### `/admin/applications`
Использует:
- `GET /teacher-applications`
- `GET /teacher-applications/pending`

Назначение:
- просмотр всех teacher applications
- фильтрация pending заявок

---

#### `/admin/applications/:applicationId`
Использует:
- `POST /teacher-applications/{applicationId}/approve`
- `POST /teacher-applications/{applicationId}/reject`

Назначение:
- подтверждение или отклонение teacher application

---

### PAYMENT FLOW PAGES

#### `/payment/success`
Использует:
- `POST /payments/paypal/orders/capture`

Назначение:
- финализация PayPal course payment после approve

---

#### `/payment/cancel`
Использует:
- backend endpoint не требуется

Назначение:
- страница отменённой оплаты

---

### SUBSCRIPTION FLOW PAGES

#### `/subscription/success`
Использует:
- `POST /subscriptions/paypal/confirm`

Опционально:
- `POST /subscriptions/paypal/save-pending`

Назначение:
- финализация PayPal subscription после approve

---

#### `/subscription/cancel`
Использует:
- backend endpoint не требуется

Назначение:
- страница отменённой подписки

---

### CERTIFICATES

#### `/student/certificates`
Использует:
- `GET /api/certificates/{certificateId}` *(или будущий endpoint списка сертификатов)*

Назначение:
- просмотр сертификатов студента

---

#### `/student/certificates/:certificateId`
Использует:
- `GET /api/certificates/{certificateId}`
- `GET /api/certificates/verify/{verificationCode}`

Назначение:
- просмотр сертификата
- проверка подлинности сертификата

---

### FILE ACCESS

#### Любая страница с видео, PDF, thumbnail, resume preview
Использует:
- `GET /files?path=...`

Назначение:
- получение загруженных файлов
- отображение thumbnail
- открытие lecture PDF
- загрузка video content

---

## Full Backend Endpoint List

### AUTH
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/verify`

### TEACHER APPLICATIONS
- `POST /teacher-applications`
- `GET /teacher-applications`
- `GET /teacher-applications/pending`
- `POST /teacher-applications/{applicationId}/approve`
- `POST /teacher-applications/{applicationId}/reject`

### COURSES
- `POST /courses`
- `GET /courses/my`
- `GET /courses/{courseId}`
- `PUT /courses/{courseId}`
- `DELETE /courses/{courseId}`

### LESSONS
- `POST /lessons/course/{courseId}`
- `GET /lessons/course/{courseId}`
- `GET /lessons/{lessonId}`
- `PUT /lessons/{lessonId}`
- `DELETE /lessons/{lessonId}`

### QUIZZES
- `POST /quizzes/lesson/{lessonId}`
- `GET /quizzes/lesson/{lessonId}`
- `GET /quizzes/{quizId}`
- `PUT /quizzes/{quizId}`
- `DELETE /quizzes/{quizId}`

### PAYMENTS
- `POST /payments/paypal/orders/course/{courseId}`
- `POST /payments/paypal/orders/capture`
- `GET /payments/paypal/my`

### SUBSCRIPTIONS
- `GET /subscriptions/paypal/plan`
- `POST /subscriptions/paypal/confirm`
- `POST /subscriptions/paypal/save-pending`
- `GET /subscriptions/paypal/my`

### ENROLLMENTS
- `POST /enrollments/free/{courseId}`
- `GET /enrollments/check/{courseId}`
- `GET /enrollments/my`

### CERTIFICATES
- `POST /api/certificates/issue?userId=USER_ID&courseId=COURSE_ID`
- `POST /api/certificates/{certificateId}/regenerate`
- `GET /api/certificates/{certificateId}`
- `GET /api/certificates/verify/{verificationCode}`

### FILES
- `GET /files?path=...`


##  Status

Active development.

---

##  Authors

Rinat  
Miierzhan  
Rassul  
