import api from './index';

export interface Course {
  id: string;
  title: string;
  description: string;
  teacherId: string;
  teacherName?: string;
  category: string;
  level: string;
  thumbnail?: string;
  published: boolean;
  createdAt: string;
  updatedAt: string;
  price?: number;
  rating?: number;
  avgRating?: number;
  free?: boolean;
  lessonCount?: number;
}

export interface Lesson {
  id: string;
  courseId: string;
  title: string;
  description: string;
  orderIndex: number;
  duration?: number;
  videoUrl?: string;
  videoFileName?: string;
  lectureText?: string;
  lecturePdfUrl?: string;
  published: boolean;
}

export const getCourses = (params?: {
  category?: string;
  level?: string;
  page?: number;
  size?: number;
}) =>
  api.get<{ content: Course[]; totalElements: number; totalPages: number }>(
    '/courses/public',
    { params }
  );

export const getCourseById = (id: string) => api.get<Course>(`/courses/${id}`);
export const getLessonsByCourse = (courseId: string) =>
  api.get<Lesson[]>(`/lessons/course/${courseId}`);

export default api;
