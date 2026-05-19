import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import VerificationPage from './pages/VerificationPage';
import CoursesPage from './pages/CoursesPage';
import CourseDetailPage from './pages/CourseDetailPage';
import CreateCoursePage from './pages/CreateCoursePage';
import EditCoursePage from './pages/EditCoursePage';
import EditLessonPage from './pages/EditLessonPage';
import ProfilePage from './pages/ProfilePage';
import TeacherApplicationPage from './pages/TeacherApplicationPage';
import TeacherQuizPage from './pages/TeacherQuizPage';
import CourseLearningPage from './pages/CourseLearningPage';
import LessonPage from './pages/LessonPage';
import QuizPage from './pages/QuizPage';
import MyEnrollmentsPage from './pages/MyEnrollmentsPage';
import MyBookmarksPage from './pages/MyBookmarksPage';
import CheckoutPage from './pages/CheckoutPage';
import PaymentHistoryPage from './pages/PaymentHistoryPage';
import CertificateVerifyPage from './pages/CertificateVerifyPage';
import MyCertificatesPage from './pages/MyCertificatesPage';
import ActivityFeedPage from './pages/ActivityFeedPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import NotFoundPage from './pages/NotFoundPage';
import PrivateRoute from './components/PrivateRoute';
import './App.css';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/verify" element={<VerificationPage />} />
        <Route path="/courses" element={<CoursesPage />} />
        <Route path="/courses/:id" element={<CourseDetailPage />} />
        <Route path="/profile/:userId" element={<ProfilePage />} />
        <Route path="/certificates/verify" element={<CertificateVerifyPage />} />
        <Route path="/certificates/verify/:code" element={<CertificateVerifyPage />} />

        {/* Authenticated */}
        <Route
          path="/courses/create"
          element={
            <PrivateRoute allowedRoles={['TEACHER', 'ADMIN']}>
              <CreateCoursePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/courses/:id/edit"
          element={
            <PrivateRoute allowedRoles={['TEACHER', 'ADMIN']}>
              <EditCoursePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/courses/:courseId/lessons/:lessonId/edit"
          element={
            <PrivateRoute allowedRoles={['TEACHER', 'ADMIN']}>
              <EditLessonPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/courses/:courseId/lessons/new"
          element={
            <PrivateRoute allowedRoles={['TEACHER', 'ADMIN']}>
              <EditLessonPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/courses/:id/checkout"
          element={
            <PrivateRoute>
              <CheckoutPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/courses/:courseId/learn"
          element={
            <PrivateRoute>
              <CourseLearningPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/courses/:courseId/lessons/:lessonId"
          element={
            <PrivateRoute>
              <LessonPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/quizzes/:quizId"
          element={
            <PrivateRoute>
              <QuizPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/my-enrollments"
          element={
            <PrivateRoute>
              <MyEnrollmentsPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/bookmarks"
          element={
            <PrivateRoute>
              <MyBookmarksPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/payments"
          element={
            <PrivateRoute>
              <PaymentHistoryPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/certificates"
          element={
            <PrivateRoute>
              <MyCertificatesPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/activity"
          element={
            <PrivateRoute>
              <ActivityFeedPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <ProfilePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/teacher-apply"
          element={
            <PrivateRoute>
              <TeacherApplicationPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/teacher-apply/:applicationId/quiz"
          element={
            <PrivateRoute>
              <TeacherQuizPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <PrivateRoute>
              <AdminDashboardPage />
            </PrivateRoute>
          }
        />

        {/* 404 */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
