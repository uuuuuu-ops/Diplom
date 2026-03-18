# 🚀 Diplom LMS Backend

Backend часть дипломного проекта LMS (Learning Management System).  
Полноценная серверная архитектура для образовательной платформы с ролями, курсами, уроками и AI-анализом резюме.

---

## 📌 Overview

Система предоставляет:

- аутентификацию и авторизацию (JWT)
- управление ролями пользователей
- процесс подачи и одобрения преподавателей
- управление курсами, уроками и тестами
- загрузку и хранение файлов
- автоматический анализ резюме

---

## 🧩 Features

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

## 🏗️ Project Structure


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

## ⚙️ Tech Stack

| Category        | Technology |
|----------------|------------|
| Backend        | Java, Spring Boot |
| Security       | Spring Security, JWT |
| Database       | MongoDB |
| File Handling  | Apache PDFBox |
| Build Tool     | Maven |
| Utilities      | Lombok |

---

## 👥 Roles & Permissions

### STUDENT
- View courses, lessons and quizzes *(planned)*

### TEACHER
- Submit application  
- Create courses, lessons and quizzes *(after approval)*

### ADMIN
- Review teacher applications  
- Approve or reject teachers

---

## 🔄 Teacher Approval Workflow


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

## 🤖 AI Resume Analysis

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

## 🧱 Data Models

### User

id
email
password
role
teacherApproved


### TeacherApplication

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


### Course

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

### Lesson

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


### Quiz

id
lessonId
title
questions
published
createdAt
updatedAt


---

## 🚀 Getting Started

### 1. Clone repository


git clone https://github.com/uuuuuu-ops/Diplom

cd Diplom


### 2. Configure MongoDB


spring.data.mongodb.uri=mongodb://localhost:27017/diplom


### 3. Run application


mvn spring-boot:run


---
## 🔌 API Endpoints

### 🔐 AUTH


POST /auth/register
POST /auth/login
POST /auth/verify


---

### 👨‍🏫 TEACHER APPLICATIONS


POST /teacher-applications
GET /teacher-applications
GET /teacher-applications/pending
POST /teacher-applications/{applicationId}/approve
POST /teacher-applications/{applicationId}/reject


---

### 📚 COURSES


POST /courses
GET /courses/my
GET /courses/{courseId}
PUT /courses/{courseId}
DELETE /courses/{courseId}


---

### 🎥 LESSONS


POST /lessons/course/{courseId}
GET /lessons/course/{courseId}
GET /lessons/{lessonId}
PUT /lessons/{lessonId}
DELETE /lessons/{lessonId}


---

### 📝 QUIZZES


POST /quizzes/lesson/{lessonId}
GET /quizzes/lesson/{lessonId}
GET /quizzes/{quizId}
PUT /quizzes/{quizId}
DELETE /quizzes/{quizId}


---

### 📁 FILES


GET /files?path=...

---

## 📊 Status

Active development.

---

## 👨‍💻 Authors

Rinat  
Miierzhan  
Rassul  
