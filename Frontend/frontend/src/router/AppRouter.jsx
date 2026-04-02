import { BrowserRouter, Route, Routes } from "react-router-dom";
import MainLayout from "../layouts/MainLayout";

import HomePage from "../pages/public/HomePage";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";
import VerifyEmailPage from "../pages/auth/VerifyEmailPage";

import StudentCoursesPage from "../pages/student/StudentCoursesPage";
import StudentCourseDetailsPage from "../pages/student/StudentCourseDetailsPage";

import TeacherApplyPage from "../pages/teacher/TeacherApplyPage";
import TeacherApplicationStatusPage from "../pages/teacher/TeacherApplicationStatusPage";
import TeacherCoursesPage from "../pages/teacher/TeacherCoursesPage";
import CreateCoursePage from "../pages/teacher/CreateCoursePage";
import EditLessonPage from "../pages/teacher/EditLessonPage";
import EditCoursePage from "../pages/teacher/EditCoursePage";

import UnauthorizedPage from "../pages/system/UnauthorizedPage";
import ProtectedRoute from "../components/auth/ProtectedRoute";
import CreateLessonPage from "../pages/teacher/CreateLessonPage";

function Placeholder({ title }) {
  return (
    <div className="px-6 py-10">
      <div className="mx-auto max-w-7xl text-3xl font-bold text-white">
        {title}
      </div>
    </div>
  );
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />

        <Route element={<MainLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/verify-email" element={<VerifyEmailPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          <Route path="/student/courses" element={<StudentCoursesPage />} />
          <Route path="/student/courses/:courseId" element={<StudentCourseDetailsPage />} />

          <Route
            path="/student/courses/:courseId/lessons/:lessonId"
            element={
              <ProtectedRoute allowedRoles={["STUDENT", "TEACHER", "ADMIN"]}>
                <Placeholder title="Lesson Page" />
              </ProtectedRoute>
            }
          />

          <Route
          path="/teacher/courses/:courseId/edit"
          element={
            <ProtectedRoute allowedRoles={["TEACHER"]}>
              <EditCoursePage />
            </ProtectedRoute>
          }
          />

          <Route
            path="/teacher/apply"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherApplyPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/application-status"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherApplicationStatusPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/courses"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherCoursesPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/courses/new"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <CreateCoursePage />
              </ProtectedRoute>
            }
          />

          <Route
          path="/teacher/courses/:courseId/lessons/:lessonId/edit"
          element={
            <ProtectedRoute allowedRoles={["TEACHER"]}>
              <EditLessonPage />
            </ProtectedRoute>
          }
        />

          <Route
          path="/teacher/courses/:courseId/lessons/new"
          element={
          <ProtectedRoute allowedRoles={["TEACHER"]}>
          <CreateLessonPage />
          </ProtectedRoute>
          }
          />

          <Route
            path="/admin/applications"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <Placeholder title="Admin Applications Page" />
              </ProtectedRoute>
            }
          />

          <Route
            path="*"
            element={
              <div className="px-6 py-10">
                <div className="mx-auto max-w-7xl text-3xl font-bold text-white">
                  404 - Page Not Found
                </div>
              </div>
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}