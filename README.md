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

### STUDENT
- View courses, lessons and quizzes *(planned)*

### TEACHER
- Submit application  
- Create courses, lessons and quizzes *(after approval)*

### ADMIN
- Review teacher applications  
- Approve or reject teachers

---

##  Teacher Approval Workflow


Register as TEACHER
↓
Submit application
↓
Upload resume (PDF)
↓
AI analysis
↓
Admin review
↓
teacherApproved = true
↓
Access to course creation


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



## Frontend Integration Flow

The frontend should interact with the backend in the following way:


# AUTH
Register
POST /auth/register
json{ "email": "user@mail.com", "password": "pass123", "role": "STUDENT" }
roles: STUDENT or TEACHER
Login
POST /auth/login
json{ "email": "user@mail.com", "password": "pass123" }
→ returns JWT token. Store it and attach to every request.
Verify email
POST /auth/verify
json{ "email": "user@mail.com", "code": "123456" }
```

---

# TEACHER APPLICATION FLOW

After registering as `TEACHER`, the user must submit an application and wait for admin approval before they can create courses. `teacherApproved` in the JWT will flip to `true` once approved.

**Submit application + upload resume**
`POST /teacher-applications`
`Content-Type: multipart/form-data`
```
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
```

**Delete course**
`DELETE /courses/{courseId}`

---

# LESSONS TEACHER (course owner)

**Create lesson**
`POST /lessons/course/{courseId}`
`Content-Type: multipart/form-data`
```
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
```

**Get quiz by lesson**
`GET /quizzes/lesson/{lessonId}`

**Get quiz by ID**
`GET /quizzes/{quizId}`

**Update quiz**
`PUT /quizzes/{quizId}` — same body as create

**Delete quiz**
`DELETE /quizzes/{quizId}`

---

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

# Frontend Routing Suggestion
```
/login
/register
/verify-email

/student/courses
/student/courses/:courseId
/student/courses/:courseId/lessons/:lessonId

/teacher/apply
/teacher/application-status
/teacher/courses
/teacher/courses/new
/teacher/courses/:courseId/edit
/teacher/courses/:courseId/lessons/new
/teacher/courses/:courseId/lessons/:lessonId/edit
/teacher/courses/:courseId/lessons/:lessonId/quiz

/admin/applications
/admin/applications/:applicationId

# Certificate

1. When a student completes a lesson, call:
   `POST /progress/complete`

2. When a student submits a quiz, call:
   `POST /quiz-attempts/submit`

3. After each lesson completion or quiz submission, refresh the course progress using:
   `GET /progress`

4. If the course reaches 100% completion and all required quizzes are passed, the certificate is automatically generated by the backend.

5. To display certificate details on the certificate page, use:
   `GET /certificates/{id}`

6. To verify certificate authenticity publicly, use:
   `GET /certificates/verify/{verificationCode}`

---




##  Status

Active development.

---

##  Authors

Rinat  
Miierzhan  
Rassul  
