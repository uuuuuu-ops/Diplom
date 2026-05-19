import axios from 'axios';

export const API_BASE = (import.meta as any).env?.VITE_API_BASE_URL ?? 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

export default api;
export interface Lesson {
  id: string;
  courseId: string;
  title: string;
  description?: string;
  orderIndex: number;
  duration?: number;
  videoUrl?: string;
  videoFileName?: string;
  lectureText?: string;
  lecturePdfUrl?: string;
  published: boolean;
  quizRequired?: boolean;
}

export const getLessonById = (lessonId: string) =>
  api.get<Lesson>(`/lessons/${lessonId}`);
export const getLessonsByCourse = (courseId: string) =>
  api.get<Lesson[]>(`/lessons/course/${courseId}`);
export const deleteLesson = (lessonId: string) =>
  api.delete<string>(`/lessons/${lessonId}`);
export const createLesson = (courseId: string, data: FormData) =>
  api.post<Lesson>(`/lessons/course/${courseId}`, data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
export const updateLesson = (lessonId: string, data: FormData) =>
  api.put<Lesson>(`/lessons/${lessonId}`, data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export interface LessonComment {
  id: string;
  lessonId: string;
  courseId?: string;
  authorId: string;
  authorName?: string;
  authorAvatarUrl?: string;
  content: string;
  parentId?: string | null;
  markedAsAnswer?: boolean;
  createdAt: string;
  updatedAt?: string;
}

export const getLessonComments = (lessonId: string) =>
  api.get<LessonComment[]>(`/lessons/${lessonId}/comments`);
export const getLessonCommentReplies = (lessonId: string, commentId: string) =>
  api.get<LessonComment[]>(`/lessons/${lessonId}/comments/${commentId}/replies`);
export const addLessonComment = (
  lessonId: string,
  content: string,
  parentId?: string | null
) =>
  api.post<LessonComment>(`/lessons/${lessonId}/comments`, {
    content,
    parentId: parentId ?? null,
  });
export const markCommentAsAnswer = (lessonId: string, commentId: string) =>
  api.patch<LessonComment>(`/lessons/${lessonId}/comments/${commentId}/mark-answer`);
export const deleteLessonComment = (lessonId: string, commentId: string) =>
  api.delete<string>(`/lessons/${lessonId}/comments/${commentId}`);
export interface CourseProgress {
  id: string;
  userId: string;
  courseId: string;
  completedLessonIds: string[];
  passedQuizIds: string[];
  progressPercent: number;
  completed: boolean;
  certificateId?: string;
  updatedAt?: string;
}

export const completeLesson = (courseId: string, lessonId: string) =>
  api.post<CourseProgress>('/progress/complete', { courseId, lessonId });
export const getCourseProgress = (courseId: string) =>
  api.get<CourseProgress>(`/progress?courseId=${encodeURIComponent(courseId)}`);
export const isLessonUnlocked = (courseId: string, lessonId: string) =>
  api.get<{ unlocked: boolean }>(
    `/progress/lesson-unlocked?courseId=${encodeURIComponent(courseId)}&lessonId=${encodeURIComponent(lessonId)}`
  );

export interface QuizQuestion {
  question: string;
  options: string[];
  correctAnswerIndex?: number;
}
export interface Quiz {
  id: string;
  lessonId: string;
  title: string;
  description?: string;
  passingScore: number;
  timeLimitSeconds?: number | null;
  questions: QuizQuestion[];
  published: boolean;
}

export interface CreateQuizPayload {
  title: string;
  description: string;
  passingScore: number;
  timeLimitSeconds?: number | null;
  published: boolean;
  questions: { question: string; options: string[]; correctAnswerIndex: number }[];
}

export const createQuiz = (lessonId: string, payload: CreateQuizPayload) =>
  api.post<Quiz>(`/quizzes/lesson/${lessonId}`, payload);
export const updateQuiz = (quizId: string, payload: CreateQuizPayload) =>
  api.put<Quiz>(`/quizzes/${quizId}`, payload);
export const deleteQuiz = (quizId: string) =>
  api.delete<string>(`/quizzes/${quizId}`);

export interface QuizAttempt {
  id: string;
  userId: string;
  quizId: string;
  answers: number[];
  score: number;
  passed: boolean;
  completedAt: string;
  durationSeconds?: number;
}

export const getQuiz = (quizId: string) => api.get<Quiz>(`/quizzes/${quizId}`);
export const getQuizByLessonId = (lessonId: string) =>
  api.get<Quiz>(`/quizzes/lesson/${lessonId}`);
export const startQuiz = (quizId: string) =>
  api.post<{ sessionKey: string }>(`/quiz-attempts/start?quizId=${encodeURIComponent(quizId)}`);
export const submitQuiz = (quizId: string, answers: number[]) =>
  api.post<QuizAttempt>('/quiz-attempts/submit', { quizId, answers });
export const getMyQuizAttempts = (quizId: string) =>
  api.get<QuizAttempt[]>(`/quiz-attempts/my?quizId=${encodeURIComponent(quizId)}`);

export interface Enrollment {
  id: string;
  userId: string;
  courseId: string;
  status: 'ACTIVE' | 'CANCELED' | 'EXPIRED';
  enrolledAt: string;
}
export const getMyEnrollments = () => api.get<Enrollment[]>('/enrollments/my');
export const enrollFree = (courseId: string) =>
  api.post<Enrollment>(`/enrollments/free/${courseId}`);
export const checkAccess = (courseId: string) =>
  api.get<{ hasAccess: boolean }>(`/enrollments/check/${courseId}`);

export interface BookmarkCourse {
  id: string;
  title: string;
  description?: string;
  teacherName?: string;
  category?: string;
  level?: string;
  thumbnail?: string;
  price?: number;
  rating?: number;
}
export const toggleBookmark = (courseId: string) =>
  api.post<{ bookmarked: boolean }>(`/courses/${courseId}/bookmark`);
export const getMyBookmarks = () => api.get<BookmarkCourse[]>('/courses/bookmarks');

export const toggleLike = (courseId: string) =>
  api.post<{ liked: boolean; totalLikes: number }>(`/courses/${courseId}/like`);
export const getLikeStatus = (courseId: string) =>
  api.get<{ liked: boolean; totalLikes: number }>(`/courses/${courseId}/like/status`);

export interface CourseRating {
  id: string;
  userId: string;
  userName?: string;
  userAvatarUrl?: string;
  courseId: string;
  rating: number;
  review?: string;
  createdAt: string;
}
export const rateCourse = (courseId: string, rating: number, review?: string) =>
  api.post<CourseRating>(`/courses/${courseId}/ratings`, { rating, review });
export const getCourseRatings = (courseId: string) =>
  api.get<CourseRating[]>(`/courses/${courseId}/ratings`);
export const deleteMyRating = (courseId: string) =>
  api.delete<string>(`/courses/${courseId}/ratings`);

export interface Payment {
  id: string;
  userId: string;
  courseId?: string;
  paypalOrderId?: string;
  amount: number;
  currency: string;
  status: 'CREATED' | 'CAPTURED' | 'FAILED';
  createdAt: string;
}
export interface CreatePaypalOrderResponse {
  orderId: string;
  approveUrl: string;
}
export const createPaypalOrder = (courseId: string) =>
  api.post<CreatePaypalOrderResponse>(`/payments/paypal/orders/course/${courseId}`);
export const capturePaypalOrder = (orderId: string) =>
  api.post<Payment>('/payments/paypal/orders/capture', { orderId });
export const getMyPayments = () => api.get<Payment[]>('/payments/paypal/my');

export interface Subscription {
  id: string;
  userId: string;
  paypalSubscriptionId?: string;
  planId: string;
  status: 'PENDING' | 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  startDate?: string;
  nextBillingDate?: string;
}
export const getSubscriptionPlan = () =>
  api.get<{ planId: string }>('/subscriptions/paypal/plan');
export const createSubscription = () =>
  api.post<{ approvalUrl: string }>('/subscriptions/paypal/create');
export const confirmSubscription = (subscriptionId: string) =>
  api.post<Subscription>('/subscriptions/paypal/confirm', { subscriptionId });
export const getMySubscriptions = () => api.get<Subscription[]>('/subscriptions/paypal/my');

export interface Certificate {
  id: string;
  userId: string;
  studentName?: string;
  courseId: string;
  courseTitle?: string;
  instructorName?: string;
  issuedAt: string;
  verificationCode: string;
  pdfUrl?: string;
}
export const getCertificate = (id: string) =>
  api.get<Certificate>(`/api/certificates/${id}`);
export const getMyCertificates = () =>
  api.get<Certificate[]>(`/api/certificates/my`);
export const verifyCertificate = (verificationCode: string) =>
  api.get<Certificate>(`/api/certificates/verify/${verificationCode}`);
export const issueCertificate = (userId: string, courseId: string) =>
  api.post<Certificate>(
    `/api/certificates/issue?userId=${encodeURIComponent(userId)}&courseId=${encodeURIComponent(courseId)}`
  );
export const regenerateCertificate = (id: string) =>
  api.post<Certificate>(`/api/certificates/${id}/regenerate`);

export interface AdminStats {
  totalUsers: number;
  totalStudents: number;
  totalTeachers: number;
  totalCourses: number;
  totalEnrollments: number;
  totalRevenue: number;
}
export interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  enabled: boolean;
  teacherApproved: boolean;
  createdAt: string;
}
export const getAdminStats = () => api.get<AdminStats>('/admin/stats');
export const getAdminUsers = () => api.get<AdminUser[]>('/admin/users');
export const deleteAdminUser = (id: string) =>
  api.delete<{ message: string }>(`/admin/users/${id}`);

export interface TeacherApplicationDetail {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  specialization: string;
  yearsOfExperience: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  score?: number;
  aiSummary?: string;
  aiStrengths?: string;
  aiWeaknesses?: string;
  aiRecommendation?: string;
  reviewComment?: string;
  resumeUrl?: string;
  createdAt: string;
}
export const getAllApplications = () =>
  api.get<TeacherApplicationDetail[]>('/teacher-applications');
export const getPendingApplications = () =>
  api.get<TeacherApplicationDetail[]>('/teacher-applications/pending');
export const approveApplication = (applicationId: string, comment?: string) =>
  api.post<TeacherApplicationDetail>(
    `/teacher-applications/${applicationId}/approve`,
    null,
    { params: comment ? { reviewComment: comment } : undefined }
  );
export const rejectApplication = (applicationId: string, comment?: string) =>
  api.post<TeacherApplicationDetail>(
    `/teacher-applications/${applicationId}/reject`,
    null,
    { params: comment ? { reviewComment: comment } : undefined }
  );

export interface TeacherQuizQuestion {
  id?: string;
  questionText: string;
  options: string[];
}
export interface TeacherQuizAttempt {
  id: string;
  userId: string;
  applicationId: string;
  score: number;
  passed: boolean;
  submittedAt?: string;
}
export const getTeacherQuizQuestions = (applicationId: string) =>
  api.get<TeacherQuizQuestion[]>(`/teacher-applications/${applicationId}/questions`);
export const submitTeacherQuiz = (applicationId: string, answers: Record<string, number>) =>
  api.post<TeacherQuizAttempt>(`/teacher-applications/${applicationId}/submit`, answers);
export const getTeacherQuizResult = (applicationId: string) =>
  api.get<TeacherQuizAttempt>(`/teacher-applications/${applicationId}/result`);

export interface ActivityFeed {
  id: string;
  userId: string;
  type: string;
  message: string;
  metadata?: Record<string, unknown>;
  createdAt: string;
}
export const getMyActivity = () => api.get<ActivityFeed[]>('/activity');

export const deleteCourse = (courseId: string) =>
  api.delete<string>(`/courses/${courseId}`);
export const getMyCourses = () => api.get<BookmarkCourse[]>('/courses/my');

export const getPublicCourses = (params?: {
  category?: string;
  level?: string;
  page?: number;
  size?: number;
}) =>
  api.get<{ content: BookmarkCourse[]; totalElements: number; totalPages: number }>(
    '/courses/public',
    { params }
  );
