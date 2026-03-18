# 🚀 Diplom LMS Backend

> Backend часть дипломного проекта LMS (Learning Management System)  
> Полноценная серверная архитектура для образовательной платформы с ролями, курсами, уроками и AI-анализом резюме.

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
- 🛡️ Secure Resource Ownership Validation

---

## 🏗️ Architecture


src/main/java/com/diploma/Diplom
│
├── controller # REST endpoints
├── service # business logic
├── repository # MongoDB access layer
├── model # entities
├── dto # request/response objects
├── security # JWT, filters, config
├── auth # authentication logic
├── ai # resume analysis module
└── files # file handling


---

## ⚙️ Tech Stack

| Category        | Technology |
|----------------|----------|
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
- Create content after approval:
  - courses
  - lessons
  - quizzes

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

### 🔍 Extracts
- education
- teaching experience
- skills
- projects
- technical stack

### 📊 Generates
- aiScore
- aiSummary
- aiStrengths
- aiWeaknesses
- aiRecommendation

### ✅ Why Local AI
- no external API dependency
- faster development
- works offline
- no cost

---

## 🧱 Data Models

### User
```text
id
email
password
role
teacherApproved
TeacherApplication
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
###Course
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
###Lesson
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
###Quiz
id
lessonId
title
questions
published
createdAt
updatedAt
🚀 Getting Started
1. Clone repository
git clone https://github.com/uuuuuu-ops/Diplom
cd Diplom
2. Configure MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/diplom
3. Configure application.properties
spring.application.name=Diplom
server.port=8080

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=550MB

app.upload.dir=uploads

jwt.secret=MySuperSecretKeyForJwtToken123456789
jwt.expiration=86400000
4. Run application
mvn spring-boot:run
🔌 API Reference
Auth
POST /auth/register
POST /auth/login
POST /auth/verify
Teacher Applications
POST /teacher-applications
GET /teacher-applications
GET /teacher-applications/pending
POST /teacher-applications/{id}/approve
POST /teacher-applications/{id}/reject
Courses
POST /courses
GET /courses/my
GET /courses/{id}
PUT /courses/{id}
DELETE /courses/{id}
Lessons
POST /lessons/course/{courseId}
GET /lessons/course/{courseId}
GET /lessons/{id}
PUT /lessons/{id}
DELETE /lessons/{id}
Quizzes
POST /quizzes/lesson/{lessonId}
GET /quizzes/lesson/{lessonId}
GET /quizzes/{id}
PUT /quizzes/{id}
DELETE /quizzes/{id}
Files
GET /files?path=...
🧪 Testing
Login
POST /auth/login
Authorization Header
Authorization: Bearer YOUR_TOKEN
Request Types
Feature	Type
Courses	form-data
Lessons	form-data
Quizzes	raw JSON
⚠️ Error Handling
403 Forbidden

missing JWT token

wrong role

teacher not approved

400 Bad Request

invalid input

missing fields

wrong request format

Business Errors

Course not found

Lesson not found

Quiz already exists

🔐 Security

JWT Authentication

Role-based Authorization

Resource Ownership Validation

Teacher Approval Check

🚀 Roadmap

student enrollment

quiz submissions

progress tracking

ratings & reviews

cloud storage (AWS S3 / Firebase)

video streaming

📊 Status

Active development. Core backend functionality is implemented.

👨‍💻 Authors

Rinat

Miierzhan

Rassul
