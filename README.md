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

- 🔐 JWT Authentication & Authorization
- 👥 Role-based Access (`STUDENT`, `TEACHER`, `ADMIN`)
- 📄 Teacher Application System
- 🤖 AI Resume Analysis (local, no external API)
- 📚 Course Management
- 🎥 Lesson Management (video + PDF)
- 📝 Quiz System
- 📁 File Upload & Serving
- 🛡️ Resource Ownership Validation

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

---

### FILES

GET /files?path=...  
Получение файла по пути (видео, PDF, изображения)
---

##  Status

Active development.

---

##  Authors

Rinat  
Miierzhan  
Rassul  
